# EssentialsY

A unified, optimized Minecraft essentials plugin by **SushiMC** — all modules in a single JAR with split configuration and easy module/command toggling.

**Documentation:** [docs.sushii.dev](https://docs.sushii.dev/)

## What's Different

EssentialsY transforms the legacy multi-plugin essentials suite into one cohesive plugin:

- **Single JAR** — Chat, Spawn, Protect, AntiBuild, and GeoIP are built into one plugin. No more juggling 5+ separate JARs.
- **Split configuration** — The monolithic 1300-line `config.yml` is split into focused files under `plugins/EssentialsY/config/`:
  - `core.yml` — Global settings, teleport, AFK, nick, locale
  - `homes.yml` — Home system settings
  - `economy.yml` — Economy and command costs
  - `help.yml` — Help system settings
  - `chat.yml` — Chat formatting and radius
  - `protect.yml` — World protection flags
  - `antibuild.yml` — Build/use restrictions
  - `spawn.yml` — Spawn points and new player handling
- **Module toggles** — `modules.yml` lets you enable/disable entire feature modules without removing JARs
- **Command categories** — `commands.yml` lets you disable individual commands or entire categories (teleport, economy, moderation, fun, utility, signs)
- **Unified package** — New infrastructure lives under `net.essentialsy.*` with an internal `ModuleManager`

## Requirements

- CraftBukkit, Spigot, or Paper
- Java 8 or higher
- Vault (optional, for economy/chat prefix integration)
- LuckPerms recommended for permissions

## Building

```bash
./gradlew build
```

Output JAR: `jars/EssentialsY-1.0.0-SNAPSHOT.jar`

### Test server

```bash
./gradlew build :EssentialsY:runServer
```

## Configuration

On first run, EssentialsY creates split config files in `plugins/EssentialsY/config/`. If upgrading from a legacy essentials setup with an existing `config.yml`, settings are automatically migrated to the new split format (original backed up as `config.yml.migrated`).

### Enable a module

All modules are disabled by default. Edit `plugins/EssentialsY/modules.yml`:

```yaml
modules:
  chat:
    enabled: true
  spawn:
    enabled: true
```

Then run `/essentials reload`.

### Disable command categories

Edit `plugins/EssentialsY/commands.yml`:

```yaml
categories:
  fun:
    enabled: false
```

This disables all fun commands (burn, fireball, lightning, etc.) at once.

### Disable individual commands

```yaml
disabled:
  - god
  - fly
```

## Module Overview

| Module | Default | Config File | Description |
|--------|---------|-------------|-------------|
| chat | disabled | `config/chat.yml` | Chat formatting, local radius |
| spawn | disabled | `config/spawn.yml` | Spawn points, respawn, newbies |
| protect | disabled | `config/protect.yml` | World protection |
| antibuild | disabled | `config/antibuild.yml` | Build restrictions |
| geoip | disabled | `geoip.yml` | Country lookup on join |
| discord | disabled | `discord.yml` | Discord bridge |
| discordlink | disabled | `discord-link.yml` | Account linking |

## Documentation

Full docs are at **[docs.sushii.dev](https://docs.sushii.dev/)**, including the [EssentialsY guide](https://docs.sushii.dev/essentialsy/).

## License

GPL-3.0

## Credits

Maintained by [SushiMC](https://github.com/codingsushi79).  
Repository: [github.com/codingsushi79/EssentialsY](https://github.com/codingsushi79/EssentialsY)
