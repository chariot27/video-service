// br/ars/video_service/config/BunnyStreamProps.java
package br.ars.video_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bunny.stream")
public class BunnyStreamProps {
    /**
     * Ex.: 479707
     */
    private Long libraryId;

    /**
     * Ex.: chave em BUNNY_STREAM_API_KEY
     */
    private String apiKey;

    /**
     * Host CDN do seu library, ex.: vz-xxxxxx.b-cdn.net
     */
    private String cdnHost;

    /**
     * Endpoint público da API de vídeo do Bunny (fixo).
     */
    private String baseUrl = "https://video.bunnycdn.com";

    // getters/setters
    public Long getLibraryId() { return libraryId; }
    public void setLibraryId(Long libraryId) { this.libraryId = libraryId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getCdnHost() { return cdnHost; }
    public void setCdnHost(String cdnHost) { this.cdnHost = cdnHost; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
