package br.ars.video_service.repositories;

import br.ars.video_service.models.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {

    // Consulta para buscar vídeos ativos
    List<Video> findAllByAtivoTrue();

    // Consulta por otimizados
    List<Video> findAllByFoiOtimizadoTrue();
}
