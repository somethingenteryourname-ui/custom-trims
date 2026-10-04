package dev.customtrims;

import dev.customtrims.command.TrimCommand;
import dev.customtrims.effect.EffectTask;
import dev.customtrims.trim.TrimManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CustomTrimsPlugin extends JavaPlugin {

    private TrimManager trimManager;
    private EffectTask effectTask;
    private final Set<UUID> hidden = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        trimManager = new TrimManager(this);

        TrimCommand command = new TrimCommand(this);
        PluginCommand pc = getCommand("ctrim");
        if (pc != null) {
            pc.setExecutor(command);
            pc.setTabCompleter(command);
        }

        startEffects();
        getLogger().info("CustomTrims enabled with " + trimManager.getPresets().size() + " presets.");
    }

    @Override
    public void onDisable() {
        if (effectTask != null) {
            effectTask.cancel();
            effectTask = null;
        }
    }

    private void startEffects() {
        if (effectTask != null) effectTask.cancel();
        int interval = Math.max(1, Math.min(20, getConfig().getInt("tick-interval", 2)));
        effectTask = new EffectTask(this, interval);
        effectTask.runTaskTimer(this, 20L, interval);
    }

    public void reloadAll() {
        reloadConfig();
        trimManager.reload();
        startEffects();
    }

    public void invalidate(Player p) {
        if (effectTask != null) effectTask.invalidate(p.getUniqueId());
    }

    public boolean isHidden(UUID id) {
        return hidden.contains(id);
    }

    /** @return true if effects are now hidden */
    public boolean toggleHidden(UUID id) {
        if (hidden.remove(id)) return false;
        hidden.add(id);
        return true;
    }

    public TrimManager getTrimManager() {
        return trimManager;
    }
}
