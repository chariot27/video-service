// src/main/java/br/ars/video_service/util/BunnyStreamSigner.java
package br.ars.video_service.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

public final class BunnyStreamSigner {

  private BunnyStreamSigner() {}

  /**
   * Gera URL assinada no formato:
   *   https://{host}{path}?token={hash}&expires={unix}[&ip={ip}]
   *
   * Hash:
   *   - SHA256 (recomendado)  token = hex( sha256( signingKey + path + expires [+ ip] ) )
   *   - MD5 (legado)          token = hex( md5(    signingKey + path + expires [+ ip] ) )
   *
   * Observação: A inclusão do IP no cálculo deve refletir a configuração da Pull Zone.
   */
  public static String buildSignedUrl(
      String host,
      String signingKey,
      String path,
      long ttlSeconds,
      String algo,
      String ip // pode ser null
  ) {
    long exp = Instant.now().getEpochSecond() + Math.max(60, ttlSeconds);
    String normalizedPath = (path.startsWith("/") ? path : "/" + path);

    String material = signingKey + normalizedPath + exp + (ip != null ? ip : "");
    String token = digestHex(material, algo);

    StringBuilder sb = new StringBuilder();
    sb.append("https://").append(host).append(normalizedPath)
      .append("?token=").append(token)
      .append("&expires=").append(exp);
    if (ip != null) {
      sb.append("&ip=").append(URLEncoder.encode(ip, StandardCharsets.UTF_8));
    }
    return sb.toString();
  }

  private static String digestHex(String s, String algo) {
    String upper = (algo == null ? "SHA256" : algo.trim().toUpperCase());
    String jAlgo = switch (upper) {
      case "MD5" -> "MD5";
      case "SHA256" -> "SHA-256";
      default -> "SHA-256";
    };
    try {
      MessageDigest md = MessageDigest.getInstance(jAlgo);
      byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(dig.length * 2);
      for (byte b : dig) {
        String h = Integer.toHexString(b & 0xff);
        if (h.length() == 1) hex.append('0');
        hex.append(h);
      }
      return hex.toString();
    } catch (Exception e) {
      throw new RuntimeException("Hash error (" + jAlgo + "): " + e.getMessage(), e);
    }
  }
}
