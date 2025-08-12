// src/main/java/br/ars/video_service/util/BunnyStreamSigner.java
package br.ars.video_service.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

public final class BunnyStreamSigner {
  private BunnyStreamSigner() {}

  private static String md5Hex(String s) {
    try {
      var md = MessageDigest.getInstance("MD5");
      var b = md.digest(s.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder();
      for (byte x : b) sb.append(String.format("%02x", x));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Falha ao gerar MD5", e);
    }
  }

  /** path deve começar com "/", ex: "/{GUID}/playlist.m3u8" */
  public static String buildSignedUrl(String cdnHost, String signingKey, String path, long ttlSeconds) {
    long expires = Instant.now().getEpochSecond() + ttlSeconds;
    String token = md5Hex(signingKey + path + expires);
    return "https://" + cdnHost + path + "?token=" + token + "&expires=" + expires;
  }
}
