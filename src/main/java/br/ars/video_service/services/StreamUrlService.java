// src/main/java/br/ars/video_service/services/StreamUrlService.java
package br.ars.video_service.services;

import br.ars.video_service.util.BunnyStreamSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StreamUrlService {

  @Value("${bunny.stream.cdnHost}")            private String cdnHost;
  @Value("${bunny.stream.signingKey}")         private String signingKey;
  @Value("${bunny.stream.tokenTtlSeconds:3600}") private long ttlSeconds;

  /** Retorna URL HLS assinada para um GUID de vídeo da Video Library */
  public String signedHls(String videoGuid) {
    String path = "/" + videoGuid + "/playlist.m3u8";
    return BunnyStreamSigner.buildSignedUrl(cdnHost, signingKey, path, ttlSeconds);
  }

  /** Se já existir uma URL (mesmo sem domínio), tenta assinar extraindo o path */
  public String ensureSignedFromUrl(String raw) {
    if (raw == null || raw.isBlank()) return raw;
    // normaliza: extrai o path começando em "/<guid>/playlist.m3u8"
    int idx = raw.indexOf('/', raw.indexOf("://") > 0 ? raw.indexOf("://") + 3 : 0);
    String path = idx >= 0 ? raw.substring(idx) : raw;
    if (!path.startsWith("/")) path = "/" + path;
    return BunnyStreamSigner.buildSignedUrl(cdnHost, signingKey, path, ttlSeconds);
  }
}
