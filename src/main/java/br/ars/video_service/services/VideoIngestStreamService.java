package br.ars.video_service.services;

import br.ars.video_service.bunny.BunnyStreamClient;
import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.models.Video;
import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.repositories.VideoRepository;
import br.ars.video_service.util.SlugUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VideoIngestStreamService {

    private final VideoRepository repository;
    private final VideoMapper mapper;
    private final BunnyStreamClient bunny;

    @Value("${video.upload.path:./uploads/videos}")
    private String uploadPath;

    public VideoIngestStreamService(VideoRepository repository, VideoMapper mapper, BunnyStreamClient bunny) {
        this.repository = repository;
        this.mapper = mapper;
        this.bunny = bunny;
    }

    @Transactional
    public VideoResponseDTO uploadToStream(VideoRequestDTO dto, MultipartFile arquivo) throws IOException {
        // 1) Staging local (só pra termos um arquivo temp limpo)
        File pasta = new File(uploadPath);
        if (!pasta.exists()) pasta.mkdirs();

        UUID videoId = UUID.randomUUID();
        String slug = SlugUtil.toSlug(dto.getDescricao());
        String ext = SlugUtil.ext(arquivo.getOriginalFilename());
        String uniqueName = String.format("%s-%s-%s%s", slug, dto.getUserId(), videoId, ext);
        File destino = new File(pasta, uniqueName);
        arquivo.transferTo(destino);

        // 2) Cria vídeo no Bunny Stream
        String streamVideoId = bunny.createVideo(slug);

        // 3) Upload do arquivo bruto p/ Bunny
        bunny.uploadVideo(streamVideoId, destino);

        // 4) Monta entidade e persiste
        Video video = mapper.toEntity(dto);
        video.setId(videoId);
        video.setUserId(dto.getUserId());
        video.setUniqueSlug(uniqueName.replace(ext,""));
        video.setOriginalFilename(arquivo.getOriginalFilename());
        video.setStreamVideoId(streamVideoId);
        video.setHlsMasterUrl(bunny.buildPlaybackUrl(streamVideoId));
        video.setStatus(VideoStatus.UPLOADED); // Bunny ainda vai processar
        video.setDataUpload(LocalDateTime.now());
        video.setAtivo(true);
        video.setFoiOtimizado(false);
        video.setCaminhoCDN(video.getHlsMasterUrl());
        video.setThumbnailUrl(bunny.buildThumbnailUrl(streamVideoId));

        Video saved = repository.save(video);

        // 5) Limpeza do staging
        destino.delete();

        return mapper.toDto(saved);
    }

    @Transactional
    public VideoResponseDTO refreshStatus(UUID id) {
        Video v = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado"));

        if (v.getStreamVideoId() == null) return mapper.toDto(v);

        var data = bunny.getVideo(v.getStreamVideoId());
        // bunny retorna campos como "status", "encodeProgress", "transcodingStatus" etc.
        // Consideraremos que se "status" == 4 (published) ou "encodeProgress" == 100 => READY
        Object encodeProgress = data.get("encodeProgress");
        Object status = data.get("status"); // int: 1=uploaded, 2=processing, 4=published...
        boolean ready = (encodeProgress != null && String.valueOf(encodeProgress).equals("100"))
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
