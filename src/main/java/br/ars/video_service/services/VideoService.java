package br.ars.video_service.services;

import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.repositories.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private final VideoRepository repository;
    private final VideoMapper mapper;

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
    public void deletar(UUID id) {
        repository.findById(id).ifPresent(v -> {
            v.setAtivo(false);
            repository.save(v);
        });
    }
}
