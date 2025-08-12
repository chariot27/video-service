// br/ars/video_service/services/VideoQueryService.java
package br.ars.video_service.services;

import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.mapper.VideoMapper;
import br.ars.video_service.repositories.VideoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoQueryService {

    private final VideoRepository repo;
    private final VideoMapper mapper;

    public VideoQueryService(VideoRepository repo, VideoMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<VideoResponseDTO> listReady(int limit) {
        var page = PageRequest.of(0, Math.max(1, limit));
        return repo.findByStatusAndAtivoIsTrueOrderByDataUploadDesc(VideoStatus.READY, page)
                   .stream()
                   .map(mapper::toDto)
                   .toList();
    }
}
