package br.ars.video_service.dto;

import java.util.UUID;

public class VideoRequestDTO {
    private UUID userId;
    private String descricao;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
