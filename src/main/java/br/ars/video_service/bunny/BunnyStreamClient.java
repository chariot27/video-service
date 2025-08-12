// br/ars/video_service/bunny/BunnyStreamClient.java
package br.ars.video_service.bunny;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class BunnyStreamClient {

    private final WebClient webClient;

    @Value("${bunny.stream.library-id}") private String libraryId;
    @Value("${bunny.stream.api-key}")   private String apiKey;     // AccessKey
    @Value("${bunny.stream.cdn-host}")  private String cdnHost;    // vz-....b-cdn.net

    public BunnyStreamClient(@Qualifier("bunnyWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /** 1) Cria vídeo (retorna GUID) */
    @SuppressWarnings("unchecked")
    public String createVideo(String title) {
        String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos";

        Map<String, Object> resp = webClient.post()
                .uri(url)
                .headers(this::addAccessKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("title", title))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || resp.get("guid") == null) {
            throw new RuntimeException("Falha ao criar vídeo no Bunny Stream (resposta sem GUID).");
        }
        return String.valueOf(resp.get("guid"));
    }

    /** 2a) Upload por FILE, mas **streaming** (sem Files.readAllBytes) */
    public void uploadVideo(String videoId, File file) {
        Path path = file.toPath();
        try (InputStream in = Files.newInputStream(path)) {
            long len = Files.size(path);
            String ct = probeContentTypeSafe(path);
            uploadVideoStream(videoId, file.getName(), in, len, ct);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir arquivo para upload.", e);
        }
    }

    /** 2b) Upload por STREAM puro (sem staging em disco) */
    public void uploadVideoStream(String videoId,
                                  String filename,
                                  InputStream input,
                                  long contentLength,
                                  String contentType) {

        String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos/" + videoId;

        // Resource que informa o contentLength (evita buffering)
        InputStreamResource resource = new KnownLengthInputStreamResource(input, filename, contentLength);

        webClient.put()
                .uri(url)
                .headers(this::addAccessKey)
                .contentType(contentType != null ? MediaType.parseMediaType(contentType)
                                                 : MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(resource)
                .retrieve()
                .toBodilessEntity()
                .block(); // bloquear aqui é ok na camada de serviço
    }

    /** 3) Status do vídeo */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVideo(String videoId) {
        String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos/" + videoId;

        Map<String, Object> resp = webClient.get()
                .uri(url)
                .headers(this::addAccessKeyJson)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null) throw new RuntimeException("Falha ao consultar vídeo no Bunny Stream.");
        return resp;
    }

    /** URL de reprodução HLS */
    public String buildPlaybackUrl(String videoId) {
        return "https://" + cdnHost + "/" + videoId + "/playlist.m3u8";
    }

    /** Thumb padrão */
    public String buildThumbnailUrl(String videoId) {
        return "https://" + cdnHost + "/" + videoId + "/thumbnail.jpg";
    }

    /* ===== helpers ===== */

    private void addAccessKey(HttpHeaders h) {
        h.add("AccessKey", apiKey);
    }

    private void addAccessKeyJson(HttpHeaders h) {
        h.add("AccessKey", apiKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    }

    private String probeContentTypeSafe(Path path) {
        try {
            String ct = Files.probeContentType(path);
            return (ct != null) ? ct : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /** InputStreamResource que conhece o tamanho e nome (evita buffering do body) */
    private static class KnownLengthInputStreamResource extends InputStreamResource {
        private final String filename;
        private final long length;

        public KnownLengthInputStreamResource(InputStream inputStream, String filename, long length) {
            super(inputStream);
            this.filename = filename;
            this.length = length;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long contentLength() {
            return length;
        }
    }
}
