// src/main/java/br/ars/video_service/models/Video.java
package br.ars.video_service.models;

import br.ars.video_service.enums.VideoStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 300)
    private String uniqueSlug;

    private String originalFilename;

    // ---- Bunny Stream ----
    private String streamVideoId;  // <- precisa pros métodos get/setStreamVideoId
    private String hlsMasterUrl;   // <- URL playlist.m3u8
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;    // <- precisa pro setStatus/getStatus

    private LocalDateTime dataUpload;
    private boolean ativo = true;

    // legado / compat
    private boolean foiOtimizado;
    private String caminhoCDN;

    // ===== Getters/Setters =====
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUniqueSlug() { return uniqueSlug; }
    public void setUniqueSlug(String uniqueSlug) { this.uniqueSlug = uniqueSlug; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStreamVideoId() { return streamVideoId; }
    public void setStreamVideoId(String streamVideoId) { this.streamVideoId = streamVideoId; }

    public String getHlsMasterUrl() { return hlsMasterUrl; }
    public void setHlsMasterUrl(String hlsMasterUrl) { this.hlsMasterUrl = hlsMasterUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public VideoStatus getStatus() { return status; }
    public void setStatus(VideoStatus status) { this.status = status; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public boolean isFoiOtimizado() { return foiOtimizado; }
    public void setFoiOtimizado(boolean foiOtimizado) { this.foiOtimizado = foiOtimizado; }

    public String getCaminhoCDN() { return caminhoCDN; }
    public void setCaminhoCDN(String caminhoCDN) { this.caminhoCDN = caminhoCDN; }
}
