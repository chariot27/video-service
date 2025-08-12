// src/main/java/br/ars/video_service/services/StreamUrlService.java
package br.ars.video_service.services;

import br.ars.video_service.util.BunnyStreamSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StreamUrlService {

  // Aceita camelCase OU kebab-case (a segunda é fallback)
  @Value("${bunny.stream.cdnHost:${bunny.stream.cdn-host:}}")
  private String cdnHost;

  @Value("${bunny.stream.signingKey:${bunny.stream.signing-key:}}")
  private String signingKey;

  @Value("${bunny.stream.tokenTtlSeconds:${bunny.stream.token-ttl-seconds:3600}}")
  private long ttlSeconds;

  @Value("${bunny.stream.signingAlgo:${bunny.stream.signing-algo:SHA256}}")
  private String signingAlgo;

  // opcional: travar por IP (se configurado na Pull Zone)
  @Value("${bunny.stream.ipLock:${bunny.stream.ip-lock:}}")
  private String ipLock;

  /** URL HLS assinada a partir do GUID da Video Library (playlist.m3u8) */
  public String hlsUrl(String videoGuid) {
    if (!StringUtils.hasText(videoGuid)) return null;
    String path = "/" + videoGuid + "/playlist.m3u8";
    ensureHost();

    // Sem chave -> retorna sem assinatura (útil para teste)
    if (!StringUtils.hasText(signingKey)) {
      return "https://" + cdnHost + path;
    }
    return BunnyStreamSigner.buildSignedUrl(
        cdnHost, signingKey, path, ttlSeconds, signingAlgo, blankToNull(ipLock));
  }

  /** Alias para compatibilidade retroativa com chamadas antigas */
  public String signedHls(String videoGuid) {
    return hlsUrl(videoGuid);
  }

  /** Normaliza e assina uma URL/Path/GUID arbitrário */
  public String ensureSignedFromUrl(String raw) {
    if (!StringUtils.hasText(raw)) return raw;
    ensureHost();

    String path = extractPathOrGuidToPath(raw);
    if (!StringUtils.hasText(signingKey)) {
      return "https://" + cdnHost + path;
    }
    return BunnyStreamSigner.buildSignedUrl(
        cdnHost, signingKey, path, ttlSeconds, signingAlgo, blankToNull(ipLock));
  }

  private void ensureHost() {
    if (!StringUtils.hasText(cdnHost)) {
      // fallback seguro para sua Pull Zone
      cdnHost = "vz-c05b38f9-149.b-cdn.net";
    }
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }

  /** Converte:
   *  - "f92875e6-...a23f" -> "/f92875e6-...a23f/playlist.m3u8"
   *  - "/f928.../playlist.m3u8" -> mantém
   *  - "https://vz-.../f928.../playlist.m3u8" -> extrai path
   */
  private static String extractPathOrGuidToPath(String raw) {
    String r = raw.trim();

    // apenas GUID?
    if (!r.contains("/") && r.length() >= 8 && r.contains("-")) {
      return "/" + r + "/playlist.m3u8";
    }

    // URL completa?
    int schemeIdx = r.indexOf("://");
    if (schemeIdx > 0) {
      int firstSlash = r.indexOf('/', schemeIdx + 3);
      String path = (firstSlash >= 0) ? r.substring(firstSlash) : "/";
      return normalizeToPlaylist(path);
    }

    // path relativo
    if (!r.startsWith("/")) r = "/" + r;
    return normalizeToPlaylist(r);
  }

  private static String normalizeToPlaylist(String path) {
    String p = path.split("\\?")[0]; // remove query, será refeita na assinatura
    if (p.endsWith("/playlist.m3u8")) return p;

    // se termina com GUID apenas
    if (!p.endsWith(".m3u8")) {
      if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
      return p + "/playlist.m3u8";
    }
    return p; // já é algum .m3u8 (ex.: manifest.m3u8), mantém
  }
}
