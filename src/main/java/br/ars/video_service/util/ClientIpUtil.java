// src/main/java/br/ars/video_service/util/ClientIpUtil.java
package br.ars.video_service.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtil {
  private ClientIpUtil() {}

  /** Resolve o IP real do cliente respeitando proxies comuns. */
  public static String resolveClientIp(HttpServletRequest req) {
    if (req == null) return null;
    String[] headers = {
        "CF-Connecting-IP",    // Cloudflare
        "X-Real-IP",
        "X-Client-IP",
        "X-Forwarded-For",     // pode vir "ip1, ip2, ip3"
        "Forwarded"            // "for=ip"
    };
    for (String h : headers) {
      String v = req.getHeader(h);
      if (v != null && !v.isBlank()) {
        String ip = extractFirstIp(v.trim());
        if (ip != null && !ip.isBlank()) return ip;
      }
    }
    String remote = req.getRemoteAddr();
    return (remote != null && !remote.isBlank()) ? remote : null;
  }

  private static String extractFirstIp(String raw) {
    if (raw == null || raw.isBlank()) return null;
    if (raw.contains(",")) return raw.split(",")[0].trim();
    if (raw.startsWith("for=")) {
      String s = raw.substring(4).trim();
      if (s.startsWith("\"")) s = s.replace("\"", "");
      if (s.contains(";")) s = s.split(";", 2)[0].trim();
      return s;
    }
    return raw;
  }
}
