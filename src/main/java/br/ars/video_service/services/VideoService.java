package br.ars.video_service.services;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.models.Video;
import br.ars.video_service.repositories.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private final VideoRepository repository;
    private final VideoMapper mapper;

    @Value("${video.upload.path:./uploads}")
    private String uploadPath;

    public VideoService(VideoRepository repository, VideoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<VideoResponseDTO> listarVideos() {
        return repository.findAllByAtivoTrue().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public VideoResponseDTO buscarPorId(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado."));
    }

    @Transactional
    public VideoResponseDTO uploadVideo(VideoRequestDTO dto, MultipartFile arquivo) throws IOException {
        // Criar diretório se não existir
        File pasta = new File(uploadPath);
        if (!pasta.exists()) pasta.mkdirs();

        // Salvar arquivo localmente
        String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
        File destino = new File(pasta, nomeArquivo);
        arquivo.transferTo(destino);

        // Criar entidade
        Video video = mapper.toEntity(dto);
        video.setCaminhoLocal(destino.getAbsolutePath());
        video.setDataUpload(LocalDateTime.now());
        video.setAtivo(true);
        video.setFoiOtimizado(false);
        video.setCaminhoCDN(null); // será preenchido após otimização/CDN futura

        return mapper.toDto(repository.save(video));
    }

    @Transactional
    public void deletar(UUID id) {
        repository.findById(id).ifPresent(video -> {
            video.setAtivo(false);
            repository.save(video);
        });
    }

    @Transactional
    public void marcarComoOtimizado(UUID id, String cdnUrl) {
        Video video = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vídeo não encontrado"));

        video.setFoiOtimizado(true);
        video.setCaminhoCDN(cdnUrl);
        repository.save(video);
    }
}
