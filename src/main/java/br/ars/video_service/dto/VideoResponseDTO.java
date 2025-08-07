package br.ars.video_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class VideoResponseDTO {

    private UUID id;
    private String titulo;
    private String descricao;
    private Integer duracaoSegundos;
    private String caminhoLocal;
    private String caminhoCDN;
    private LocalDateTime dataUpload;
    private Long likes;
    private boolean foiOtimizado;
    private boolean ativo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void setDuracaoSegundos(Integer duracaoSegundos) {
        this.duracaoSegundos = duracaoSegundos;
    }

    public String getCaminhoLocal() {
        return caminhoLocal;
    }

    public void setCaminhoLocal(String caminhoLocal) {
        this.caminhoLocal = caminhoLocal;
    }

    public String getCaminhoCDN() {
        return caminhoCDN;
    }

    public void setCaminhoCDN(String caminhoCDN) {
        this.caminhoCDN = caminhoCDN;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public void setDataUpload(LocalDateTime dataUpload) {
        this.dataUpload = dataUpload;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public boolean isFoiOtimizado() {
        return foiOtimizado;
    }

    public void setFoiOtimizado(boolean foiOtimizado) {
        this.foiOtimizado = foiOtimizado;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
