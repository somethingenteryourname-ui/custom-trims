package dev.customtrims.util;

import org.bukkit.Color;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Color parsing, blending and chat-color helpers. */
public final class ColorUtil {

    public static final Map<String, Color> NAMED = new LinkedHashMap<>();

    static {
        put("red", 0xFF2D2D);
        put("crimson", 0xB0102A);
        put("orange", 0xFF8C1A);
        put("gold", 0xFFC72C);
        put("yellow", 0xFFF23D);
        put("lime", 0x7CFF3A);
        put("green", 0x1FAA3A);
        put("emerald", 0x00D27A);
        put("mint", 0x7DFFC8);
        put("teal", 0x00A3A3);
        put("aqua", 0x3DF5FF);
        put("cyan", 0x00C8E0);
        put("sky", 0x7EC8FF);
        put("blue", 0x2D5BFF);
        put("navy", 0x14215E);
        put("purple", 0x8B2DFF);
        put("violet", 0xB57CFF);
        put("lavender", 0xD7B8FF);
        put("magenta", 0xFF2DD9);
        put("pink", 0xFF8FCB);
        put("rose", 0xFF4F7B);
        put("white", 0xFFFFFF);
        put("silver", 0xC8CED6);
        put("gray", 0x7A7A7A);
        put("black", 0x111111);
        put("brown", 0x7A4A21);
    }

    private ColorUtil() {
    }

    private static void put(String name, int rgb) {
        NAMED.put(name, Color.fromRGB(rgb));
    }

    /** Parses a color name, #RRGGBB, RRGGBB or #RGB. Returns null if invalid. */
    public static Color parse(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        Color named = NAMED.get(s);
        if (named != null) return named;
        if (s.startsWith("#")) s = s.substring(1);
        if (s.matches("[0-9a-f]{3}")) {
            s = "" + s.charAt(0) + s.charAt(0) + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2);
        }
        if (s.matches("[0-9a-f]{6}")) return Color.fromRGB(Integer.parseInt(s, 16));
        return null;
    }

    public static String hex(Color c) {
        return String.format("#%06X", c.asRGB());
    }

    /** Legacy hex chat color code (works in chat and item lore). */
    public static String chat(Color c) {
        String h = String.format("%06X", c.asRGB());
        StringBuilder sb = new StringBuilder("\u00A7x");
        for (char ch : h.toCharArray()) sb.append('\u00A7').append(ch);
        return sb.toString();
    }

    public static Color lerp(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return Color.fromRGB(r, g, bl);
    }

    /** Full-brightness rainbow color, h wraps around 0..1. */
    public static Color hue(double h) {
        h = h - Math.floor(h);
        double r = Math.abs(h * 6 - 3) - 1;
        double g = 2 - Math.abs(h * 6 - 2);
        double b = 2 - Math.abs(h * 6 - 4);
        return Color.fromRGB(to255(r), to255(g), to255(b));
    }

    private static int to255(double v) {
        return (int) Math.round(Math.max(0, Math.min(1, v)) * 255);
    }

    /** Colors each letter of the text along a gradient from a to b. */
    public static String gradient(String text, Color a, Color b) {
        StringBuilder sb = new StringBuilder();
        int n = text.length();
        for (int i = 0; i < n; i++) {
            double t = n <= 1 ? 0 : (double) i / (n - 1);
            sb.append(chat(lerp(a, b, t))).append(text.charAt(i));
        }
        return sb.toString();
    }

    public static String strip(String s) {
        return s == null ? "" : s.replaceAll("\u00A7[0-9a-fA-Fk-oK-OrRxX]", "");
    }
}
