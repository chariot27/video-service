package br.ars.video_service.repositories;

import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.models.Video;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findAllByAtivoTrue();

    List<Video> findByStatusAndAtivoIsTrueOrderByDataUploadDesc(
            VideoStatus status, Pageable pageable
    );

    @Query("""
        select v from Video v
        where v.status = :status and v.ativo = :ativo
        order by v.dataUpload desc
    """)
    List<Video> findReady(@Param("status") VideoStatus status,
                          @Param("ativo") boolean ativo,
                          Pageable pageable);
}
