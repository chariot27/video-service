package br.ars.video_service.repositories;

import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.models.Video;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findAllByAtivoTrue();

    List<Video> findByStatusAndAtivoIsTrueOrderByDataUploadDesc(
            VideoStatus status, Pageable pageable
    );
}
