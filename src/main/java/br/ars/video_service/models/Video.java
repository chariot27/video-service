package br.ars.video_service.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "duracao_segundos")
    private Integer duracaoSegundos;

    @Column(name = "caminho_local", nullable = false)
    private String caminhoLocal;

    @Column(name = "caminho_cdn")
    private String caminhoCDN;

    @Column(name = "data_upload")
    private LocalDateTime dataUpload;

    @Column(name = "quantidade_likes")
    private Long likes = 0L;

    @Column(name = "foi_otimizado")
    private boolean foiOtimizado = false;

    @Column(name = "ativo")
    private boolean ativo = true;

    // Getters e Setters

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
