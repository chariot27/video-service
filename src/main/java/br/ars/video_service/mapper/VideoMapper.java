package br.ars.video_service.mapper;

import org.mapstruct.*;
import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.models.Video;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VideoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "uniqueSlug", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "streamVideoId", ignore = true)
    @Mapping(target = "hlsMasterUrl", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataUpload", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "foiOtimizado", ignore = true)
    @Mapping(target = "caminhoCDN", ignore = true)
    Video toEntity(VideoRequestDTO dto);

    @Mapping(target = "descricao", expression = "java(entity.getUniqueSlug())")
    @Mapping(target = "hlsMasterUrl", source = "hlsMasterUrl")
    @Mapping(target = "streamVideoId", source = "streamVideoId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "dataUpload", source = "dataUpload")
    VideoResponseDTO toDto(Video entity);
}
