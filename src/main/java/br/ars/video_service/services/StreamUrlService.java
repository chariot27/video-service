// src/main/java/br/ars/video_service/services/StreamUrlService.java
package br.ars.video_service.services;

import br.ars.video_service.util.BunnyStreamSigner;
import br.ars.video_service.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StreamUrlService {

  private static final Logger log = LoggerFactory.getLogger(StreamUrlService.class);

  @Value("${bunny.stream.cdnHost:${bunny.stream.cdn-host:}}")
  private String cdnHost;

  @Value("${bunny.stream.signingKey:${bunny.stream.signing-key:}}")
  private String signingKey;

  @Value("${bunny.stream.tokenTtlSeconds:${bunny.stream.token-ttl-seconds:3600}}")
  private long ttlSeconds;

  @Value("${bunny.stream.signingAlgo:${bunny.stream.signing-algo:SHA256}}")
  private String signingAlgo;

  /** Se true, tentamos travar o token ao IP do cliente (se a Pull Zone exigir). */
  @Value("${bunny.stream.ipLockClient:false}")
  private boolean ipLockClient;

  /** IP fixo (RARAMENTE útil). Melhor deixar vazio. */
  @Value("${bunny.stream.ipLock:${bunny.stream.ip-lock:}}")
  private String ipLockStatic;

  /** URL HLS assinada a partir do GUID (sem request/IP). */
  public String hlsUrl(String videoGuid) {
    return hlsUrlForRequest(videoGuid, null);
  }

  /** URL HLS assinada a partir do GUID considerando o IP do cliente (se habilitado). */
  public String hlsUrlForRequest(String videoGuid, HttpServletRequest req) {
    if (!StringUtils.hasText(videoGuid)) return null;
    ensureHost();
    String path = "/" + videoGuid + "/playlist.m3u8";

    if (!StringUtils.hasText(signingKey)) {
      return "https://" + cdnHost + path;
    }

    String ip = null;
    if (ipLockClient) {
      ip = ClientIpUtil.resolveClientIp(req);
    } else if (StringUtils.hasText(ipLockStatic)) {
      ip = ipLockStatic;
    }

    String url = BunnyStreamSigner.buildSignedUrl(
        cdnHost, signingKey, path, ttlSeconds, signingAlgo, ip);

    if (log.isDebugEnabled()) {
      log.debug("[HLS SIGN] host={} algo={} ttl={} ipLockClient={} ipStatic={} clientIp={} path={} url={}",
          cdnHost, signingAlgo, ttlSeconds, ipLockClient, (ipLockStatic != null && !ipLockStatic.isBlank()), ip, path, url);
    }
    return url;
  }

  /** Normaliza e assina uma URL/Path/GUID arbitrário (considera o IP do cliente se habilitado). */
  public String ensureSignedFromUrlForRequest(String raw, HttpServletRequest req) {
    if (!StringUtils.hasText(raw)) return raw;
    ensureHost();
    String path = extractPathOrGuidToPath(raw);

    if (!StringUtils.hasText(signingKey)) {
      return "https://" + cdnHost + path;
    }

    String ip = null;
    if (ipLockClient) {
      ip = ClientIpUtil.resolveClientIp(req);
    } else if (StringUtils.hasText(ipLockStatic)) {
      ip = ipLockStatic;
    }

    String url = BunnyStreamSigner.buildSignedUrl(
        cdnHost, signingKey, path, ttlSeconds, signingAlgo, ip);

    if (log.isDebugEnabled()) {
      log.debug("[HLS SIGN RAW] host={} algo={} ttl={} ipLockClient={} ipStatic={} clientIp={} raw={} path={} url={}",
          cdnHost, signingAlgo, ttlSeconds, ipLockClient, (ipLockStatic != null && !ipLockStatic.isBlank()), ip, raw, path, url);
    }
    return url;
  }

  private void ensureHost() {
    if (!StringUtils.hasText(cdnHost)) {
      cdnHost = "vz-c05b38f9-149.b-cdn.net"; // seu fallback
    }
  }

  private static String extractPathOrGuidToPath(String raw) {
    String r = raw.trim();
    if (!r.contains("/") && r.length() >= 8 && r.contains("-")) {
      return "/" + r + "/playlist.m3u8";
    }
    int schemeIdx = r.indexOf("://");
    if (schemeIdx > 0) {
      int firstSlash = r.indexOf('/', schemeIdx + 3);
      String path = (firstSlash >= 0) ? r.substring(firstSlash) : "/";
      return normalizeToPlaylist(path);
    }
    if (!r.startsWith("/")) r = "/" + r;
    return normalizeToPlaylist(r);
  }

  private static String normalizeToPlaylist(String path) {
    String p = path.split("\\?")[0];
    if (p.endsWith("/playlist.m3u8")) return p;
    if (!p.endsWith(".m3u8")) {
      if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
      return p + "/playlist.m3u8";
    }
    return p;
  }
}
