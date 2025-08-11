package br.ars.video_service.dto;

import br.ars.video_service.enums.VideoStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class VideoResponseDTO {
    private UUID id;
    private UUID userId;
    private String descricao;
    private String hlsMasterUrl;
    private String streamVideoId;
    private VideoStatus status;
    private LocalDateTime dataUpload;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getHlsMasterUrl() { return hlsMasterUrl; }
    public void setHlsMasterUrl(String hlsMasterUrl) { this.hlsMasterUrl = hlsMasterUrl; }
    public String getStreamVideoId() { return streamVideoId; }
    public void setStreamVideoId(String streamVideoId) { this.streamVideoId = streamVideoId; }
    public VideoStatus getStatus() { return status; }
    public void setStatus(VideoStatus status) { this.status = status; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}
