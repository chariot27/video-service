package br.ars.video_service.util;

import java.text.Normalizer;

public final class SlugUtil {
    private SlugUtil() {}

    public static String toSlug(String text) {
        String base = text == null ? "video" : text.toLowerCase();
        base = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9]+","-")
                .replaceAll("(^-|-$)","");
        return base.isBlank() ? "video" : base;
    }

    public static String ext(String filename) {
        if (filename == null) return ".mp4";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i) : ".mp4";
    }
}
