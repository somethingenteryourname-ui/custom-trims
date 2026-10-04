package dev.customtrims.command;

import dev.customtrims.CustomTrimsPlugin;
import dev.customtrims.effect.AuraShape;
import dev.customtrims.effect.ParticleStyle;
import dev.customtrims.effect.TrailShape;
import dev.customtrims.trim.TrimManager;
import dev.customtrims.trim.TrimSettings;
import dev.customtrims.util.ColorUtil;
import dev.customtrims.util.Enums;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public final class TrimCommand implements TabExecutor {

    private static final String PREFIX = "\u00A7b\u00A7lTrims \u00A78\u00BB \u00A77";
    private static final List<String> NONE = List.of();
    private static final List<String> SUBS = List.of(
            "help", "presets", "preset", "give", "color", "trail", "aura", "pattern", "material",
            "size", "density", "glint", "name", "info", "remove", "toggle", "options", "reload");
    private static final List<String> ARMOR_TYPES = List.of(
            "netherite", "diamond", "iron", "golden", "chainmail", "leather", "copper");

    private final CustomTrimsPlugin plugin;

    public TrimCommand(CustomTrimsPlugin plugin) {
        this.plugin = plugin;
    }

    private TrimManager tm() {
        return plugin.getTrimManager();
    }

    private static void msg(CommandSender s, String text) {
        s.sendMessage(PREFIX + text);
    }

    private static boolean perm(CommandSender s, String node) {
        if (s.hasPermission(node)) return true;
        msg(s, "\u00A7cYou don't have permission to do that.");
        return false;
    }

    // ================================================================= execute

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(sender, label);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "presets", "list" -> listPresets(sender, label);
            case "options" -> options(sender);
            case "preset" -> preset(sender, args, label);
            case "give" -> give(sender, args, label);
            case "remove" -> remove(sender, args);
            case "toggle" -> toggle(sender);
            case "info" -> info(sender);
            case "reload" -> {
                if (perm(sender, "customtrims.admin")) {
                    plugin.reloadAll();
                    msg(sender, "Reloaded! \u00A7f" + tm().getPresets().size() + "\u00A77 presets loaded.");
                }
            }
            case "color", "colour", "trail", "aura", "pattern", "material", "size", "density", "glint", "name" ->
                    edit(sender, sub, args, label);
            default -> msg(sender, "Unknown subcommand. Try \u00A7f/" + label + " help");
        }
        return true;
    }

    private void help(CommandSender s, String l) {
        s.sendMessage("\u00A78\u00A7m                                                  ");
        s.sendMessage(ColorUtil.gradient("  Custom Trims", Color.fromRGB(0x00E5FF), Color.fromRGB(0xB000FF)) + " \u00A78- \u00A77make your armor glow");
        s.sendMessage("\u00A7f/" + l + " presets \u00A78- \u00A77list ready-made trims");
        s.sendMessage("\u00A7f/" + l + " preset <name> \u00A78- \u00A77put a preset on your worn armor");
        s.sendMessage("\u00A7f/" + l + " color <color> [color2] \u00A78- \u00A77name or #hex, any color");
        s.sendMessage("\u00A7f/" + l + " trail <particle> [shape] \u00A78- \u00A77trail behind you");
        s.sendMessage("\u00A7f/" + l + " aura <shape> [particle] \u00A78- \u00A77aura around you");
        s.sendMessage("\u00A7f/" + l + " pattern <pattern> [material] \u00A78- \u00A77trim look on the armor");
        s.sendMessage("\u00A7f/" + l + " material <material> \u00A78- \u00A77trim material look");
        s.sendMessage("\u00A7f/" + l + " size <" + tm().getMaxSize() + " max> \u00A78| \u00A7fdensity <n> \u00A78| \u00A7fglint on/off");
        s.sendMessage("\u00A7f/" + l + " name <text> \u00A78- \u00A77name your trim");
        s.sendMessage("\u00A7f/" + l + " info \u00A78| \u00A7fremove \u00A78| \u00A7ftoggle \u00A78| \u00A7foptions");
        if (s.hasPermission("customtrims.give")) {
            s.sendMessage("\u00A7f/" + l + " give <player> <preset> [armor type] \u00A78- \u00A77full trimmed set");
        }
        if (s.hasPermission("customtrims.admin")) {
            s.sendMessage("\u00A7f/" + l + " reload");
        }
        s.sendMessage("\u00A78\u00A7m                                                  ");
    }

    private void listPresets(CommandSender s, String label) {
        Map<String, TrimSettings> presets = tm().getPresets();
        if (presets.isEmpty()) {
            msg(s, "No presets in the config.");
            return;
        }
        msg(s, "Presets \u00A78(\u00A7f/" + label + " preset <name>\u00A78):");
        for (Entry<String, TrimSettings> e : presets.entrySet()) {
            TrimSettings p = e.getValue();
            s.sendMessage(" \u00A78\u2022 \u00A7f" + e.getKey() + " \u00A78- " + ColorUtil.gradient(p.name, p.primary, p.secondary)
                    + " \u00A78(\u00A77" + Enums.pretty(p.trailParticle) + " trail, " + Enums.pretty(p.auraShape) + " aura\u00A78)");
        }
    }

    private void options(CommandSender s) {
        msg(s, "\u00A7bTrail/aura particles: \u00A7f" + String.join(", ", Enums.names(ParticleStyle.values())));
        msg(s, "\u00A7bTrail shapes: \u00A7f" + String.join(", ", Enums.names(TrailShape.values())));
        msg(s, "\u00A7bAura shapes: \u00A7f" + String.join(", ", Enums.names(AuraShape.values())));
        msg(s, "\u00A7bPatterns: \u00A7f" + String.join(", ", TrimManager.PATTERNS.keySet()) + ", none");
        msg(s, "\u00A7bMaterials: \u00A7f" + String.join(", ", TrimManager.MATERIALS.keySet()));
        StringBuilder colors = new StringBuilder();
        for (Entry<String, Color> e : ColorUtil.NAMED.entrySet()) {
            colors.append(ColorUtil.chat(e.getValue())).append(e.getKey()).append(' ');
        }
        msg(s, "\u00A7bColors: " + colors + "\u00A77or any hex like \u00A7f#FF00AA");
        msg(s, "\u00A77Colored particles: \u00A7fdust, dual, gradient, blend, rainbow, sparkle\u00A77 (they use your colors)");
    }

    // ================================================================== presets

    private void preset(CommandSender sender, String[] args, String label) {
        if (args.length < 2) {
            msg(sender, "Usage: \u00A7f/" + label + " preset <name> [player]");
            return;
        }
        TrimSettings preset = tm().getPreset(args[1]);
        if (preset == null) {
            msg(sender, "\u00A7cUnknown preset. See \u00A7f/" + label + " presets");
            return;
        }
        Player target = resolveTarget(sender, args, 2);
        if (target == null) return;

        int changed = tm().applyToWorn(target, preset);
        if (changed == 0) {
            msg(sender, "\u00A7c" + (target == sender ? "You need" : target.getName() + " needs") + " to wear armor (or hold a piece).");
            return;
        }
        plugin.invalidate(target);
        msg(sender, "Applied " + ColorUtil.gradient(preset.name, preset.primary, preset.secondary)
                + "\u00A77 to \u00A7f" + changed + "\u00A77 piece" + (changed == 1 ? "" : "s")
                + (target == sender ? "" : " on \u00A7f" + target.getName()) + "\u00A77.");
    }

    private void give(CommandSender sender, String[] args, String label) {
        if (!perm(sender, "customtrims.give")) return;
        if (args.length < 3) {
            msg(sender, "Usage: \u00A7f/" + label + " give <player> <preset> [" + String.join("|", ARMOR_TYPES) + "]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            msg(sender, "\u00A7cPlayer not found.");
            return;
        }
        TrimSettings preset = tm().getPreset(args[2]);
        if (preset == null) {
            msg(sender, "\u00A7cUnknown preset. See \u00A7f/" + label + " presets");
            return;
        }
        String type = args.length >= 4 ? args[3].toUpperCase(Locale.ROOT) : "NETHERITE";
        if (type.equals("GOLD")) type = "GOLDEN";

        List<ItemStack> items = new ArrayList<>();
        for (String piece : new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"}) {
            Material m = Material.matchMaterial(type + "_" + piece);
            if (m == null) {
                msg(sender, "\u00A7cUnknown armor type. Use: " + String.join(", ", ARMOR_TYPES));
                return;
            }
            ItemStack item = new ItemStack(m);
            tm().apply(item, preset);
            items.add(item);
        }
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(items.toArray(new ItemStack[0]));
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }
        msg(sender, "Gave \u00A7f" + target.getName() + "\u00A77 a " + ColorUtil.gradient(preset.name, preset.primary, preset.secondary)
                + "\u00A77 " + type.toLowerCase(Locale.ROOT) + " set.");
        if (target != sender) {
            msg(target, "You received a " + ColorUtil.gradient(preset.name, preset.primary, preset.secondary) + "\u00A77 armor set!");
        }
    }

    private void remove(CommandSender sender, String[] args) {
        Player target = resolveTarget(sender, args, 1);
        if (target == null) return;
        int n = tm().clearWorn(target);
        plugin.invalidate(target);
        msg(sender, n == 0 ? "No custom trims found on worn armor."
                : "Removed custom trims from \u00A7f" + n + "\u00A77 piece" + (n == 1 ? "" : "s") + ".");
    }

    private void toggle(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            msg(sender, "Only players can do that.");
            return;
        }
        boolean hidden = plugin.toggleHidden(p.getUniqueId());
        msg(sender, hidden ? "Your trim effects are now \u00A7chidden\u00A77." : "Your trim effects are now \u00A7ashowing\u00A77.");
    }

    private void info(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            msg(sender, "Only players can do that.");
            return;
        }
        TrimSettings s = tm().current(p);
        if (s == null) {
            msg(sender, "You aren't wearing a custom trim. Try \u00A7f/ctrim presets");
            return;
        }
        msg(sender, ColorUtil.gradient(s.name, s.primary, s.secondary) + "\u00A77 trim:");
        sender.sendMessage(" \u00A77Look: \u00A7f" + s.pattern + "\u00A77 pattern, \u00A7f" + s.material + "\u00A77 material, glint \u00A7f" + (s.glint ? "on" : "off"));
        sender.sendMessage(" \u00A77Colors: " + ColorUtil.chat(s.primary) + "\u25A0 " + ColorUtil.hex(s.primary)
                + " " + ColorUtil.chat(s.secondary) + "\u25A0 " + ColorUtil.hex(s.secondary));
        sender.sendMessage(" \u00A77Trail: \u00A7f" + Enums.pretty(s.trailParticle) + "\u00A77 (" + Enums.pretty(s.trailShape) + ")");
        sender.sendMessage(" \u00A77Aura: \u00A7f" + Enums.pretty(s.auraShape) + "\u00A77 (" + Enums.pretty(s.auraParticle) + ")");
        sender.sendMessage(" \u00A77Size: \u00A7f" + s.size + "\u00A77  Density: \u00A7f" + s.density);
        if (plugin.isHidden(p.getUniqueId())) sender.sendMessage(" \u00A7cEffects are hidden (/ctrim toggle).");
    }

    /** Self if no name given; another player if a name is given and sender is admin. */
    private Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            if (!perm(sender, "customtrims.admin")) return null;
            Player t = Bukkit.getPlayerExact(args[index]);
            if (t == null) msg(sender, "\u00A7cPlayer not found.");
            return t;
        }
        if (!(sender instanceof Player p)) {
            msg(sender, "\u00A7cConsole must name a player.");
            return null;
        }
        return perm(sender, "customtrims.use") ? p : null;
    }

    // ===================================================================== edit

    private void edit(CommandSender sender, String sub, String[] args, String label) {
        if (!(sender instanceof Player p)) {
            msg(sender, "Only players can edit their armor.");
            return;
        }
        if (!perm(sender, "customtrims.use")) return;
        if (args.length < 2) {
            msg(sender, "Usage: \u00A7f" + usage(sub, label));
            return;
        }
        TrimSettings s = tm().currentOrDefault(p);
        String error = applyEdit(s, sub, args);
        if (error != null) {
            msg(sender, "\u00A7c" + error);
            return;
        }
        int changed = tm().applyToWorn(p, s);
        if (changed == 0) {
            msg(sender, "\u00A7cYou need to wear armor (or hold a piece) first.");
            return;
        }
        plugin.invalidate(p);
        msg(sender, "Updated \u00A7f" + changed + "\u00A77 piece" + (changed == 1 ? "" : "s") + ": " + describe(sub, s));
    }

    private String usage(String sub, String l) {
        return switch (sub) {
            case "color", "colour" -> "/" + l + " color <color> [color2]";
            case "trail" -> "/" + l + " trail <particle> [shape]";
            case "aura" -> "/" + l + " aura <shape> [particle]";
            case "pattern" -> "/" + l + " pattern <pattern> [material]";
            case "material" -> "/" + l + " material <material>";
            case "size" -> "/" + l + " size <0.3-" + tm().getMaxSize() + ">";
            case "density" -> "/" + l + " density <0.25-" + tm().getMaxDensity() + ">";
            case "glint" -> "/" + l + " glint <on|off>";
            case "name" -> "/" + l + " name <text>";
            default -> "/" + l + " help";
        };
    }

    /** Changes the settings. Returns an error message, or null on success. */
    private String applyEdit(TrimSettings s, String sub, String[] a) {
        switch (sub) {
            case "color", "colour" -> {
                Color c1 = ColorUtil.parse(a[1]);
                if (c1 == null) return "Unknown color '" + a[1] + "'. Use a name (see /ctrim options) or hex like #FF00AA.";
                Color c2 = c1;
                if (a.length >= 3) {
                    c2 = ColorUtil.parse(a[2]);
                    if (c2 == null) return "Unknown color '" + a[2] + "'.";
                }
                s.primary = c1;
                s.secondary = c2;
            }
            case "trail" -> {
                ParticleStyle st = Enums.parse(ParticleStyle.class, a[1], null);
                if (st == null) return "Unknown particle. See /ctrim options";
                s.trailParticle = st;
                if (a.length >= 3) {
                    TrailShape shape = Enums.parse(TrailShape.class, a[2], null);
                    if (shape == null) return "Unknown trail shape. See /ctrim options";
                    s.trailShape = shape;
                }
            }
            case "aura" -> {
                AuraShape shape = Enums.parse(AuraShape.class, a[1], null);
                if (shape == null) return "Unknown aura shape. See /ctrim options";
                s.auraShape = shape;
                if (a.length >= 3) {
                    ParticleStyle st = Enums.parse(ParticleStyle.class, a[2], null);
                    if (st == null) return "Unknown particle. See /ctrim options";
                    s.auraParticle = st;
                } else if (s.auraParticle == ParticleStyle.NONE && shape != AuraShape.NONE) {
                    s.auraParticle = ParticleStyle.BLEND;
                }
            }
            case "pattern" -> {
                String pat = a[1].toLowerCase(Locale.ROOT);
                if (!pat.equals("none") && !TrimManager.PATTERNS.containsKey(pat)) return "Unknown pattern. See /ctrim options";
                s.pattern = pat;
                if (a.length >= 3) {
                    String mat = a[2].toLowerCase(Locale.ROOT);
                    if (!TrimManager.MATERIALS.containsKey(mat)) return "Unknown material. See /ctrim options";
                    s.material = mat;
                }
            }
            case "material" -> {
                String mat = a[1].toLowerCase(Locale.ROOT);
                if (!TrimManager.MATERIALS.containsKey(mat)) return "Unknown material. See /ctrim options";
                s.material = mat;
            }
            case "size" -> {
                try {
                    s.size = tm().clampSize(Double.parseDouble(a[1]));
                } catch (NumberFormatException e) {
                    return "Size must be a number, like 1.5";
                }
            }
            case "density" -> {
                try {
                    s.density = tm().clampDensity(Double.parseDouble(a[1]));
                } catch (NumberFormatException e) {
                    return "Density must be a number, like 1.5";
                }
            }
            case "glint" -> {
                String v = a[1].toLowerCase(Locale.ROOT);
                if (v.equals("on") || v.equals("true") || v.equals("yes")) s.glint = true;
                else if (v.equals("off") || v.equals("false") || v.equals("no")) s.glint = false;
                else return "Use on or off.";
            }
            case "name" -> s.name = TrimSettings.cleanName(String.join(" ", Arrays.copyOfRange(a, 1, a.length)));
            default -> {
                return "Unknown option.";
            }
        }
        return null;
    }

    private String describe(String sub, TrimSettings s) {
        return switch (sub) {
            case "color", "colour" -> ColorUtil.chat(s.primary) + "\u25A0 " + ColorUtil.hex(s.primary) + " "
                    + ColorUtil.chat(s.secondary) + "\u25A0 " + ColorUtil.hex(s.secondary);
            case "trail" -> "\u00A7f" + Enums.pretty(s.trailParticle) + "\u00A77 trail (" + Enums.pretty(s.trailShape) + ")";
            case "aura" -> "\u00A7f" + Enums.pretty(s.auraShape) + "\u00A77 aura (" + Enums.pretty(s.auraParticle) + ")";
            case "pattern", "material" -> "\u00A7f" + s.pattern + "\u00A77 pattern, \u00A7f" + s.material + "\u00A77 material";
            case "size" -> "size \u00A7f" + s.size;
            case "density" -> "density \u00A7f" + s.density;
            case "glint" -> "glint \u00A7f" + (s.glint ? "on" : "off");
            case "name" -> ColorUtil.gradient(s.name, s.primary, s.secondary);
            default -> "";
        };
    }

    // ============================================================ tab complete

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] a) {
        if (a.length == 1) return filter(SUBS, a[0]);
        String sub = a[0].toLowerCase(Locale.ROOT);
        int n = a.length;
        List<String> options = switch (sub) {
            case "color", "colour" -> n <= 3 ? colorSuggestions() : NONE;
            case "trail" -> n == 2 ? Enums.names(ParticleStyle.values()) : n == 3 ? Enums.names(TrailShape.values()) : NONE;
            case "aura" -> n == 2 ? Enums.names(AuraShape.values()) : n == 3 ? Enums.names(ParticleStyle.values()) : NONE;
            case "pattern" -> n == 2 ? patternSuggestions() : n == 3 ? new ArrayList<>(TrimManager.MATERIALS.keySet()) : NONE;
            case "material" -> n == 2 ? new ArrayList<>(TrimManager.MATERIALS.keySet()) : NONE;
            case "size", "density" -> n == 2 ? List.of("0.5", "1", "1.5", "2", "3") : NONE;
            case "glint" -> n == 2 ? List.of("on", "off") : NONE;
            case "preset" -> n == 2 ? new ArrayList<>(tm().getPresets().keySet())
                    : (n == 3 && sender.hasPermission("customtrims.admin")) ? players() : NONE;
            case "give" -> n == 2 ? players() : n == 3 ? new ArrayList<>(tm().getPresets().keySet()) : n == 4 ? ARMOR_TYPES : NONE;
            case "remove" -> (n == 2 && sender.hasPermission("customtrims.admin")) ? players() : NONE;
            default -> NONE;
        };
        return filter(options, a[n - 1]);
    }

    private static List<String> colorSuggestions() {
        List<String> out = new ArrayList<>(ColorUtil.NAMED.keySet());
        out.add("#");
        return out;
    }

    private static List<String> patternSuggestions() {
        List<String> out = new ArrayList<>(TrimManager.PATTERNS.keySet());
        out.add("none");
        return out;
    }

    private static List<String> players() {
        List<String> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        return out;
    }

    private static List<String> filter(List<String> options, String typed) {
        String t = typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) if (o.toLowerCase(Locale.ROOT).startsWith(t)) out.add(o);
        return out;
    }
}
