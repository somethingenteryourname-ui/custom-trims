package dev.customtrims.trim;

import dev.customtrims.effect.AuraShape;
import dev.customtrims.effect.ParticleStyle;
import dev.customtrims.effect.TrailShape;
import dev.customtrims.util.ColorUtil;
import dev.customtrims.util.Enums;
import org.bukkit.Color;

import java.util.Locale;

/** Everything about one custom trim. Stored on the armor item as a string. */
public final class TrimSettings {

    public String name = "Custom";
    public String pattern = "silence";
    public String material = "diamond";
    public Color primary = Color.fromRGB(0x00E5FF);
    public Color secondary = Color.fromRGB(0xB000FF);
    public ParticleStyle trailParticle = ParticleStyle.GRADIENT;
    public TrailShape trailShape = TrailShape.COMET;
    public AuraShape auraShape = AuraShape.RING;
    public ParticleStyle auraParticle = ParticleStyle.BLEND;
    public double size = 1.0;
    public double density = 1.0;
    public boolean glint = true;

    public TrimSettings copy() {
        TrimSettings s = new TrimSettings();
        s.name = name;
        s.pattern = pattern;
        s.material = material;
        s.primary = primary;
        s.secondary = secondary;
        s.trailParticle = trailParticle;
        s.trailShape = trailShape;
        s.auraShape = auraShape;
        s.auraParticle = auraParticle;
        s.size = size;
        s.density = density;
        s.glint = glint;
        return s;
    }

    public static String cleanName(String raw) {
        String s = ColorUtil.strip(raw == null ? "" : raw).replace(";", "").replace("=", "").trim();
        if (s.isEmpty()) s = "Custom";
        if (s.length() > 24) s = s.substring(0, 24);
        return s;
    }

    public String serialize() {
        return "v=1"
                + ";name=" + name
                + ";pattern=" + pattern
                + ";material=" + material
                + ";c1=" + Integer.toHexString(primary.asRGB())
                + ";c2=" + Integer.toHexString(secondary.asRGB())
                + ";trail=" + trailParticle.name()
                + ";trailShape=" + trailShape.name()
                + ";aura=" + auraShape.name()
                + ";auraParticle=" + auraParticle.name()
                + ";size=" + size
                + ";density=" + density
                + ";glint=" + glint;
    }

    public static TrimSettings deserialize(String raw) {
        TrimSettings s = new TrimSettings();
        if (raw == null) return s;
        for (String part : raw.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String v = kv[1];
            try {
                switch (kv[0]) {
                    case "name" -> s.name = cleanName(v);
                    case "pattern" -> s.pattern = v.toLowerCase(Locale.ROOT);
                    case "material" -> s.material = v.toLowerCase(Locale.ROOT);
                    case "c1" -> s.primary = Color.fromRGB(Integer.parseInt(v, 16) & 0xFFFFFF);
                    case "c2" -> s.secondary = Color.fromRGB(Integer.parseInt(v, 16) & 0xFFFFFF);
                    case "trail" -> s.trailParticle = Enums.parse(ParticleStyle.class, v, s.trailParticle);
                    case "trailShape" -> s.trailShape = Enums.parse(TrailShape.class, v, s.trailShape);
                    case "aura" -> s.auraShape = Enums.parse(AuraShape.class, v, s.auraShape);
                    case "auraParticle" -> s.auraParticle = Enums.parse(ParticleStyle.class, v, s.auraParticle);
                    case "size" -> s.size = Double.parseDouble(v);
                    case "density" -> s.density = Double.parseDouble(v);
                    case "glint" -> s.glint = Boolean.parseBoolean(v);
                    default -> {
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // bad value in the stored data: keep the default for that field
            }
        }
        return s;
    }
}
