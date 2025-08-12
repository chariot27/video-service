package br.ars.video_service.services;

import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.models.Video;
import br.ars.video_service.repositories.VideoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoQueryService {

    private final VideoRepository repository;
    private final VideoMapper mapper;

    public VideoQueryService(VideoRepository repository, VideoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<VideoResponseDTO> listReady(int limit) {
        int size = Math.max(1, Math.min(50, limit)); // clamp 1..50
        Pageable pg = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "dataUpload"));
        List<Video> rows = repository.findReady(VideoStatus.READY, true, pg);
        // por segurança: só mapeia os que têm HLS setado
        return rows.stream()
                .filter(v -> v.getHlsMasterUrl() != null && !v.getHlsMasterUrl().isBlank())
                .map(mapper::toDto)
                .toList();
    }
}
