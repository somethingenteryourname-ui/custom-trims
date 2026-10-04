package dev.customtrims.trim;

import dev.customtrims.CustomTrimsPlugin;
import dev.customtrims.effect.AuraShape;
import dev.customtrims.effect.ParticleStyle;
import dev.customtrims.effect.TrailShape;
import dev.customtrims.util.ColorUtil;
import dev.customtrims.util.Enums;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads/writes custom trims on armor items and holds the presets. */
public final class TrimManager {

    public static final Map<String, TrimPattern> PATTERNS = new LinkedHashMap<>();
    public static final Map<String, TrimMaterial> MATERIALS = new LinkedHashMap<>();

    static {
        PATTERNS.put("bolt", TrimPattern.BOLT);
        PATTERNS.put("coast", TrimPattern.COAST);
        PATTERNS.put("dune", TrimPattern.DUNE);
        PATTERNS.put("eye", TrimPattern.EYE);
        PATTERNS.put("flow", TrimPattern.FLOW);
        PATTERNS.put("host", TrimPattern.HOST);
        PATTERNS.put("raiser", TrimPattern.RAISER);
        PATTERNS.put("rib", TrimPattern.RIB);
        PATTERNS.put("sentry", TrimPattern.SENTRY);
        PATTERNS.put("shaper", TrimPattern.SHAPER);
        PATTERNS.put("silence", TrimPattern.SILENCE);
        PATTERNS.put("snout", TrimPattern.SNOUT);
        PATTERNS.put("spire", TrimPattern.SPIRE);
        PATTERNS.put("tide", TrimPattern.TIDE);
        PATTERNS.put("vex", TrimPattern.VEX);
        PATTERNS.put("ward", TrimPattern.WARD);
        PATTERNS.put("wayfinder", TrimPattern.WAYFINDER);
        PATTERNS.put("wild", TrimPattern.WILD);

        MATERIALS.put("amethyst", TrimMaterial.AMETHYST);
        MATERIALS.put("copper", TrimMaterial.COPPER);
        MATERIALS.put("diamond", TrimMaterial.DIAMOND);
        MATERIALS.put("emerald", TrimMaterial.EMERALD);
        MATERIALS.put("gold", TrimMaterial.GOLD);
        MATERIALS.put("iron", TrimMaterial.IRON);
        MATERIALS.put("lapis", TrimMaterial.LAPIS);
        MATERIALS.put("netherite", TrimMaterial.NETHERITE);
        MATERIALS.put("quartz", TrimMaterial.QUARTZ);
        MATERIALS.put("redstone", TrimMaterial.REDSTONE);
        MATERIALS.put("resin", TrimMaterial.RESIN);
    }

    private final CustomTrimsPlugin plugin;
    private final NamespacedKey key;
    private final Map<String, TrimSettings> presets = new LinkedHashMap<>();
    private int minPieces = 1;
    private double maxSize = 3.0;
    private double maxDensity = 3.0;

    public TrimManager(CustomTrimsPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "trim");
        reload();
    }

    // ------------------------------------------------------------------ config

    public void reload() {
        FileConfiguration c = plugin.getConfig();
        minPieces = Math.max(1, Math.min(4, c.getInt("min-armor-pieces", 1)));
        maxSize = Math.max(0.5, c.getDouble("max-size", 3.0));
        maxDensity = Math.max(0.5, c.getDouble("max-density", 3.0));

        presets.clear();
        ConfigurationSection sec = c.getConfigurationSection("presets");
        if (sec == null) return;
        for (String id : sec.getKeys(false)) {
            ConfigurationSection ps = sec.getConfigurationSection(id);
            if (ps != null) presets.put(id.toLowerCase(Locale.ROOT), fromConfig(id, ps));
        }
    }

    private TrimSettings fromConfig(String id, ConfigurationSection c) {
        TrimSettings s = new TrimSettings();
        s.name = TrimSettings.cleanName(c.getString("name", id));
        s.pattern = c.getString("pattern", s.pattern).toLowerCase(Locale.ROOT);
        s.material = c.getString("material", s.material).toLowerCase(Locale.ROOT);
        List<String> colors = c.getStringList("colors");
        if (!colors.isEmpty()) {
            Color a = ColorUtil.parse(colors.get(0));
            if (a != null) {
                s.primary = a;
                s.secondary = a;
            }
        }
        if (colors.size() > 1) {
            Color b = ColorUtil.parse(colors.get(1));
            if (b != null) s.secondary = b;
        }
        s.trailParticle = Enums.parse(ParticleStyle.class, c.getString("trail"), s.trailParticle);
        s.trailShape = Enums.parse(TrailShape.class, c.getString("trail-shape"), s.trailShape);
        s.auraShape = Enums.parse(AuraShape.class, c.getString("aura"), s.auraShape);
        s.auraParticle = Enums.parse(ParticleStyle.class, c.getString("aura-particle"), s.auraParticle);
        s.size = clampSize(c.getDouble("size", 1.0));
        s.density = clampDensity(c.getDouble("density", 1.0));
        s.glint = c.getBoolean("glint", true);
        return s;
    }

    public Map<String, TrimSettings> getPresets() {
        return Collections.unmodifiableMap(presets);
    }

    public TrimSettings getPreset(String id) {
        TrimSettings s = presets.get(id.toLowerCase(Locale.ROOT));
        return s == null ? null : s.copy();
    }

    public double clampSize(double v) {
        return Math.max(0.3, Math.min(maxSize, v));
    }

    public double clampDensity(double v) {
        return Math.max(0.25, Math.min(maxDensity, v));
    }

    public double getMaxSize() {
        return maxSize;
    }

    public double getMaxDensity() {
        return maxDensity;
    }

    // ------------------------------------------------------------------ items

    public TrimSettings read(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String raw = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return raw == null ? null : TrimSettings.deserialize(raw);
    }

    public void apply(ItemStack item, TrimSettings s) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, s.serialize());

        // The real vanilla trim look on the armor model
        if (meta instanceof ArmorMeta armorMeta) {
            TrimPattern pattern = PATTERNS.get(s.pattern);
            TrimMaterial material = MATERIALS.get(s.material);
            armorMeta.setTrim(pattern != null && material != null ? new ArmorTrim(material, pattern) : null);
        }
        // Leather armor can be dyed to the exact trim color
        if (meta instanceof LeatherArmorMeta leather) {
            leather.setColor(s.primary);
        }
        meta.setEnchantmentGlintOverride(s.glint ? Boolean.TRUE : null);

        List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(TrimManager::isOurLore);
        lore.add("\u00A78\u2726 " + ColorUtil.gradient(s.name, s.primary, s.secondary) + " \u00A78Trim");
        lore.add("\u00A78\u25C7 \u00A77Trail: \u00A7f" + Enums.pretty(s.trailParticle) + " \u00A78(" + Enums.pretty(s.trailShape) + ")");
        lore.add("\u00A78\u25C7 \u00A77Aura: \u00A7f" + Enums.pretty(s.auraShape) + " \u00A78(" + Enums.pretty(s.auraParticle) + ")");
        lore.add("\u00A78\u25C7 \u00A77Colors: " + ColorUtil.chat(s.primary) + "\u25A0 " + ColorUtil.hex(s.primary)
                + " " + ColorUtil.chat(s.secondary) + "\u25A0 " + ColorUtil.hex(s.secondary));
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    public void clear(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(key);
        if (meta instanceof ArmorMeta armorMeta) armorMeta.setTrim(null);
        meta.setEnchantmentGlintOverride(null);
        if (meta.hasLore() && meta.getLore() != null) {
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.removeIf(TrimManager::isOurLore);
            meta.setLore(lore.isEmpty() ? null : lore);
        }
        item.setItemMeta(meta);
    }

    private static boolean isOurLore(String line) {
        String plain = ColorUtil.strip(line);
        return (plain.startsWith("\u2726 ") && plain.endsWith(" Trim")) || plain.startsWith("\u25C7 ");
    }

    // ------------------------------------------------------------------ players

    /** The trim that should currently show effects, or null. */
    public TrimSettings getActive(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack[] order = {inv.getChestplate(), inv.getLeggings(), inv.getHelmet(), inv.getBoots()};
        TrimSettings first = null;
        int count = 0;
        for (ItemStack it : order) {
            TrimSettings s = read(it);
            if (s != null) {
                count++;
                if (first == null) first = s;
            }
        }
        return count >= minPieces ? first : null;
    }

    /** The trim on any worn piece (or the held item), ignoring min-armor-pieces. */
    public TrimSettings current(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack[] order = {inv.getChestplate(), inv.getLeggings(), inv.getHelmet(), inv.getBoots(), inv.getItemInMainHand()};
        for (ItemStack it : order) {
            TrimSettings s = read(it);
            if (s != null) return s;
        }
        return null;
    }

    public TrimSettings currentOrDefault(Player p) {
        TrimSettings s = current(p);
        if (s != null) return s;
        TrimSettings def = getPreset("default");
        return def != null ? def : new TrimSettings();
    }

    /** Applies settings to every worn armor piece (or the held armor piece if none are worn). */
    public int applyToWorn(Player p, TrimSettings s) {
        PlayerInventory inv = p.getInventory();
        int n = 0;
        ItemStack it = inv.getHelmet();
        if (notEmpty(it)) { apply(it, s); inv.setHelmet(it); n++; }
        it = inv.getChestplate();
        if (notEmpty(it)) { apply(it, s); inv.setChestplate(it); n++; }
        it = inv.getLeggings();
        if (notEmpty(it)) { apply(it, s); inv.setLeggings(it); n++; }
        it = inv.getBoots();
        if (notEmpty(it)) { apply(it, s); inv.setBoots(it); n++; }
        if (n == 0) {
            it = inv.getItemInMainHand();
            if (isArmorPiece(it)) { apply(it, s); inv.setItemInMainHand(it); n++; }
        }
        return n;
    }

    public int clearWorn(Player p) {
        PlayerInventory inv = p.getInventory();
        int n = 0;
        ItemStack it = inv.getHelmet();
        if (read(it) != null) { clear(it); inv.setHelmet(it); n++; }
        it = inv.getChestplate();
        if (read(it) != null) { clear(it); inv.setChestplate(it); n++; }
        it = inv.getLeggings();
        if (read(it) != null) { clear(it); inv.setLeggings(it); n++; }
        it = inv.getBoots();
        if (read(it) != null) { clear(it); inv.setBoots(it); n++; }
        it = inv.getItemInMainHand();
        if (read(it) != null) { clear(it); inv.setItemInMainHand(it); n++; }
        return n;
    }

    private static boolean notEmpty(ItemStack it) {
        return it != null && !it.getType().isAir();
    }

    public static boolean isArmorPiece(ItemStack it) {
        if (it == null) return false;
        Material m = it.getType();
        String n = m.name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS")
                || n.endsWith("_BOOTS") || m == Material.ELYTRA;
    }
}
