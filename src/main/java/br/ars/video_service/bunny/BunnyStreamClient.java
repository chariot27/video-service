package br.ars.video_service.bunny;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@Service
public class BunnyStreamClient {

    private final RestTemplate http = new RestTemplate();

    @Value("${bunny.stream.library-id}") private String libraryId;
    @Value("${bunny.stream.api-key}") private String apiKey;       // "AccessKey"
    @Value("${bunny.stream.cdn-host}") private String cdnHost;     // vz-....b-cdn.net

    // 1) Cria vídeo (retorna videoId GUID)
    public String createVideo(String title) {
        String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos";
        HttpHeaders h = headersJson();
        Map<String, Object> payload = Map.of("title", title);
        ResponseEntity<Map> resp = http.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, h), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Falha ao criar vídeo no Bunny Stream");
        }
        Object guid = resp.getBody().get("guid");
        if (guid == null) throw new RuntimeException("Resposta sem GUID");
        return guid.toString();
    }

    // 2) Upload do arquivo bruto
    public void uploadVideo(String videoId, File file) {
        try {
            String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos/" + videoId;
            HttpHeaders h = new HttpHeaders();
            h.add("AccessKey", apiKey);
            h.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            byte[] bytes = Files.readAllBytes(file.toPath());
            ResponseEntity<String> resp = http.exchange(url, HttpMethod.PUT, new HttpEntity<>(bytes, h), String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Falha no upload do vídeo: " + resp.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro no upload para Bunny Stream", e);
        }
    }

    // 3) Status do vídeo
    public Map getVideo(String videoId) {
        String url = "https://video.bunnycdn.com/library/" + libraryId + "/videos/" + videoId;
        HttpHeaders h = headersJson();
        ResponseEntity<Map> resp = http.exchange(url, HttpMethod.GET, new HttpEntity<>(h), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Falha ao consultar vídeo");
        }
        return resp.getBody();
    }

    // URL de reprodução HLS
    public String buildPlaybackUrl(String videoId) {
        return "https://" + cdnHost + "/" + videoId + "/playlist.m3u8";
    }

    // Thumb padrão (opcional)
    public String buildThumbnailUrl(String videoId) {
        return "https://" + cdnHost + "/" + videoId + "/thumbnail.jpg";
    }

    private HttpHeaders headersJson() {
        HttpHeaders h = new HttpHeaders();
        h.add("AccessKey", apiKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
