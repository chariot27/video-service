package br.ars.video_service.mapper;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.models.Video;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "caminhoLocal", ignore = true)
    @Mapping(target = "caminhoCDN", ignore = true)
    @Mapping(target = "dataUpload", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "likes", constant = "0L")
    @Mapping(target = "foiOtimizado", constant = "false")
    @Mapping(target = "ativo", constant = "true")
    Video toEntity(VideoRequestDTO dto);

    VideoResponseDTO toDto(Video entity);
}
