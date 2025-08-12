// br/ars/video_service/services/VideoIngestStreamService.java
package br.ars.video_service.services;

import br.ars.video_service.bunny.BunnyStreamClient;
import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.models.Video;
import br.ars.video_service.repositories.VideoRepository;
import br.ars.video_service.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VideoIngestStreamService {

    private final VideoRepository repository;
    private final VideoMapper mapper;
    private final BunnyStreamClient bunny;

    public VideoIngestStreamService(VideoRepository repository,
                                    VideoMapper mapper,
                                    BunnyStreamClient bunny) {
        this.repository = repository;
        this.mapper = mapper;
        this.bunny = bunny;
    }

    @Transactional
    public VideoResponseDTO uploadToStream(VideoRequestDTO dto, MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo do vídeo é obrigatório.");
        }

        // Gera identificadores e nomes
        UUID videoId = UUID.randomUUID();
        String slug = SlugUtil.toSlug(dto.getDescricao());
        String ext = SlugUtil.ext(arquivo.getOriginalFilename()); // exemplo: ".mp4"
        String uniqueName = String.format("%s-%s-%s%s", slug, dto.getUserId(), videoId, ext);

        // 1) Cria o vídeo no Bunny Stream
        String streamVideoId = bunny.createVideo(slug);

        // 2) Upload por stream (sem staging em disco)
        long contentLength = arquivo.getSize();
        String contentType = arquivo.getContentType() != null ? arquivo.getContentType() : "application/octet-stream";

        try (InputStream in = arquivo.getInputStream()) {
            // Método do client que envia via streaming (precisa existir no BunnyStreamClient)
            bunny.uploadVideoStream(streamVideoId, uniqueName, in, contentLength, contentType);
        }

        // 3) Persiste metadados no banco
        Video video = mapper.toEntity(dto);
        video.setId(videoId);
        video.setUserId(dto.getUserId());
        video.setUniqueSlug(uniqueName.replace(ext, ""));
        video.setOriginalFilename(arquivo.getOriginalFilename());
        video.setStreamVideoId(streamVideoId);
        video.setHlsMasterUrl(bunny.buildPlaybackUrl(streamVideoId));
        video.setThumbnailUrl(bunny.buildThumbnailUrl(streamVideoId));
        video.setStatus(VideoStatus.UPLOADED); // Bunny ainda vai processar
        video.setDataUpload(LocalDateTime.now());
        video.setAtivo(true);
        video.setFoiOtimizado(false);
        video.setCaminhoCDN(video.getHlsMasterUrl());

        Video saved = repository.save(video);
        return mapper.toDto(saved);
    }

    @Transactional
    public VideoResponseDTO refreshStatus(UUID id) {
        Video v = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado"));

        if (v.getStreamVideoId() == null) return mapper.toDto(v);

        var data = bunny.getVideo(v.getStreamVideoId());
        Object encodeProgress = data.get("encodeProgress");
        Object status = data.get("status"); // 1=uploaded, 2=processing, 4=published...

        boolean ready = (encodeProgress != null && "100".equals(String.valueOf(encodeProgress)))
                || (status != null && "4".equals(String.valueOf(status)));

        if (ready) {
            v.setStatus(VideoStatus.READY);
            v.setFoiOtimizado(true);
            v.setCaminhoCDN(v.getHlsMasterUrl());
        } else {
            v.setStatus(VideoStatus.PROCESSING);
        }

        repository.save(v);
        return mapper.toDto(v);
    }
}
