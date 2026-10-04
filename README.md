# CustomTrims (Minecraft 1.21.11)

Custom armor trims with a colored particle **trail** that follows you and an **aura** around you.
Works on Paper (and Paper forks like Purpur).

## Build with GitHub (no setup on your PC)
1. Make a new GitHub repo (for example `custom-trims`).
2. Upload everything in this folder, including the hidden `.github` folder.
3. Open the **Actions** tab, wait for the green check, open the run, and download **CustomTrims** under *Artifacts*.
4. Unzip it, drop `CustomTrims-1.0.0.jar` in your server's `plugins` folder, and restart.

## Quick start (in game, wearing armor)
```
/ctrim presets              list all ready-made trims
/ctrim preset inferno       put a preset on the armor you're wearing
/ctrim color #FF00AA aqua   any two colors (names or hex)
/ctrim trail flame comet    trail particle + trail shape
/ctrim aura wings gradient  aura shape + aura particle
/ctrim pattern silence gold vanilla trim look on the armor
/ctrim size 1.5             bigger aura
/ctrim density 2            more particles
/ctrim glint off            turn off the enchant shine
/ctrim name Starfall        name your trim (shows in the lore)
/ctrim info                 see your current trim
/ctrim options              list every particle, shape, pattern, material, color
/ctrim toggle               hide/show your own effects
/ctrim remove               take the custom trim off
/ctrim give <player> angel diamond   give a full trimmed set (ops)
/ctrim reload                        reload config.yml (ops)
```

## Options
- **Colored particle styles** (use your colors): dust, dual, gradient, blend, rainbow, sparkle
- **Vanilla particles**: flame, soul_flame, end_rod, heart, enchant, witch, electric, snow, totem, portal,
  reverse_portal, note, sculk, soul, glow, firework, cloud, crit, magic_crit, happy, ash, white_ash,
  crimson, warped, nautilus, dolphin, smoke, wax, scrape, cherry, obsidian_tear, lava
- **Trail shapes**: stream, comet, double, spiral, sparks, cloud, footsteps
- **Aura shapes**: ring, double_ring, halo, helix, double_helix, orbit, pulse, crown, tornado, wings, fireflies, none
- **Patterns**: all 18 vanilla patterns (or none) • **Materials**: all 11 vanilla materials
- **Colors**: any hex color. Leather armor gets dyed to your exact color too.

## Permissions
- `customtrims.use` – edit your own trim (everyone by default)
- `customtrims.give` – `/ctrim give` (ops)
- `customtrims.admin` – reload, and apply/remove trims on other players (ops)

Add your own presets in `plugins/CustomTrims/config.yml`, then `/ctrim reload`.
