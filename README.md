# TheEndProtector

TheEndProtector is a Minecraft server plugin that protects and automatically resets the main island in The End dimension after boss encounters. It supports both the vanilla Ender Dragon and custom MythicMobs bosses. This README provides full documentation: features, installation, configuration, examples, commands, permissions, troubleshooting and development notes.

Table of contents
- Features
- Requirements
- Installation
- Configuration (full)
- Commands
- Permissions
- Troubleshooting
- Development & notes

Features
- Island protection: prevents block placement and breaking on the main End island when the configured boss is dead (configurable radius).
- Automatic rollback: uses CoreProtect to rollback changes to the timestamp saved when the boss spawned.
- Countdown notifications: configurable intervals and custom messages with color codes and placeholders (`{seconds}`, `{mob}`).
- Auto-cleanup (auto-rollback): optionally remove boss and rollback if no players are found on the island for a configured timeout.
- Auto-respawn (vanilla): optionally respawn the Ender Dragon after rollback completes.
- MythicMobs compatibility: supports detection and rollback for MythicMobs active mobs.
- Customizable deny messages and an option to allow block edits while the boss is dead.

Requirements
- Minecraft Server: Paper/Spigot 1.21+ recommended
- CoreProtect: Required for rollback functionality (plugin must be installed and enabled)
- MythicMobs: Optional — required only if you want to use MythicMobs mode

Installation
1. Place `TheEndProtector.jar` into your server's `plugins/` folder.
2. Install CoreProtect (and MythicMobs if using MythicMobs mode).
3. Start the server so the plugin can create `plugins/TheEndProtector/config.yml`.
4. Edit `config.yml` to customize behavior (see Configuration below).
5. Restart the server or use `/theendprotector reload` to apply configuration changes.

Configuration (full)
Open `plugins/TheEndProtector/config.yml`. Below is a representative example and a detailed reference for each option the plugin uses.

Example configuration

```yaml
vanilla:
  enabled: true
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60, 30, 10, 5]
  rollback-notification-message: "&cThe End will refresh in {seconds} seconds!"
  auto-rollback-enabled: true
  auto-rollback-minutes: 5
  allow-blocks-when-mob-dead: false
  block-deny-message: "&cCannot adjust blocks on the main island as the {mob} is not alive."
  auto-respawn-dragon: true

mythicmobs:
  enabled: false
  type: "CUSTOM_BOSS_NAME"
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60, 30, 10, 5]
  rollback-notification-message: "&cThe End will refresh in {seconds} seconds!"
  auto-rollback-enabled: true
  auto-rollback-minutes: 5
  allow-blocks-when-mob-dead: false
  block-deny-message: "&cCannot adjust blocks on the main island as the {mob} is not alive."
```

Option reference
- `vanilla.enabled` (boolean) — enable vanilla Ender Dragon support (default true).
- `mythicmobs.enabled` (boolean) — use MythicMobs active mob as the boss (default false).
- `mythicmobs.type` (string) — MythicMobs internal mob name to detect (when MythicMobs mode is used).
- `protection-radius` (int) — radius (blocks) from x=0,z=0 treated as the main island protection zone.
- `rollback-delay` (int, seconds) — how many seconds to wait after mob death before running rollback.
- `rollback-notifications` (list[int]) — countdown times (in seconds) that trigger notifications before rollback. Empty list disables notifications.
- `rollback-notification-message` (string) — message template sent to players in The End when a notification triggers. Use `{seconds}` placeholder and `&` color codes (Adventure legacy serializer is used).
- `auto-rollback-enabled` (boolean) — whether the plugin will automatically remove the boss and start rollback when no players are found on the main island for the configured timeout.
- `auto-rollback-minutes` (int) — number of minutes without players before auto-rollback triggers (checked every minute).
- `allow-blocks-when-mob-dead` (boolean) — if true, players may place/break blocks on the main island even when the boss is dead.
- `block-deny-message` (string) — message shown to a player when their block action is denied; supports `{mob}` placeholder and `&` legacy color codes.
- `auto-respawn-dragon` (boolean) — if true (vanilla only), the Ender Dragon will be respawned automatically after rollback completes.

Notes
- If you use MythicMobs, ensure the `mythicmobs.type` value matches the internal mob name in MythicMobs config.
- To disable notifications completely set `rollback-notifications: []` and/or `rollback-notification-message: ""`.

Commands
All commands require OP by default. Future versions may add granular permissions.

- `/theendprotector help` — Show the help menu with subcommands and aliases.
- `/theendprotector reload` — Reload plugin configuration from `config.yml`.
- `/theendprotector status` — Show current runtime status: mode, mob alive/dead, protection state, players on island, and auto-rollback timers.
- `/theendprotector test` — Trigger a rollback test (only useful when a boss is currently present; intended for admins).
- `/theendprotector info` — Show plugin version, author, and dependency status (CoreProtect/MythicMobs).

Deprecated legacy commands (still supported with warning as subcommands of `/theendprotector`):
- `/theendprotector rollbacktest` — alias for `/theendprotector test`
- `/theendprotector reloadconfig` — alias for `/theendprotector reload`

Permissions
- `theendprotector.op` — default OP access for administrative commands.
You can gate specific commands by adding permission checks in the plugin if you prefer fine-grained control.

Troubleshooting
- Rollback does nothing: confirm CoreProtect is installed and enabled; check CoreProtect console/messages.
- Config options not applying: ensure you ran `/theendprotector reload` or restarted the server after editing `config.yml`.
- MythicMobs detection failing: verify the internal mob name and that MythicMobs plugin is loaded before TheEndProtector.
- Plugin cannot find The End world: ensure your server has an End world (commonly `world_the_end`) and that world environment is THE_END.

Development notes
- Uses Adventure API for modern text handling and color codes.
- CoreProtect API is required to perform the rollback; ensure API compatibility.
- The plugin runs rollback in a background thread and performs in-game notifications on the main thread via the scheduler.

License & support
- Provided as-is. No warranty. Use on your servers at your own risk.
- For issues: check server console logs, enable debug in source if needed, and verify dependencies are present.

# TheEndProtector

A comprehensive Minecraft plugin that protects and automatically resets the main island in The End dimension. Supports both vanilla EnderDragon and MythicMobs custom bosses with advanced rollback features.

## Features

### 🛡️ Island Protection
- **Automatic Protection**: Prevents block placement and breaking on the main End island when the boss is not alive
- **Configurable Radius**: Set custom protection radius from the center (0,0 coordinates)
- **End Crystal Prevention**: Blocks placing End Crystals on obsidian (only allows on bedrock)

### 🔄 Automatic Rollback
- **CoreProtect Integration**: Uses CoreProtect to rollback changes to the pre-boss state
- **Smart Timing**: Automatically saves timestamp when boss spawns for precise rollback
- **Delayed Execution**: Configurable delay before rollback begins

### ⏰ Smart Auto-Rollback
- **Player Detection**: Monitors for players on the main island
- **Timeout System**: Automatically removes boss and rolls back if no players present for configured time
- **Boss Respawn**: Option to respawn Ender Dragon after rollback (vanilla mode only)

### 📢 Customizable Notifications
- **Countdown Alerts**: Notify players before rollback with customizable timing
- **Rich Messages**: Support for Minecraft color codes and custom messages
- **Placeholder System**: Use `{seconds}` to display remaining time
- **Targeted Broadcasting**: Only shows to players currently in The End

### 🎯 Multi-Mode Support
- **Vanilla Mode**: Full support for vanilla EnderDragon
- **MythicMobs Mode**: Integration with MythicMobs for custom bosses
- **Flexible Configuration**: Easy switching between modes

## Installation

### Requirements
- **Minecraft Server**: 1.21+ (Paper/Spigot recommended)
- **CoreProtect**: Required for rollback functionality
- **MythicMobs**: Optional, only needed for MythicMobs mode

### Steps
1. Download TheEndProtector.jar
2. Place in your `plugins/` folder
3. Install CoreProtect plugin
4. Start your server to generate config files
5. Configure settings in `config.yml`
6. Restart server or use `/theendprotector reload` command

## Configuration

The plugin supports two modes: **Vanilla** (default) and **MythicMobs**. Both have similar configuration options.

### Basic Configuration

```yaml
# Choose your mode
vanilla:
  enabled: true
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60, 30, 10, 5]
  rollback-notification-message: "&c⚠️ The End will refresh in {seconds} seconds! ⚠️"
  auto-rollback-minutes: 5
  auto-respawn-dragon: true

mythicmobs:
  enabled: false
  type: "ENDER_DRAGON"
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60, 30, 10, 5]
  rollback-notification-message: "&c⚠️ The End will refresh in {seconds} seconds! ⚠️"
  auto-rollback-minutes: 5
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `enabled` | Enable/disable this mode | `true` for vanilla, `false` for mythicmobs |
| `protection-radius` | Blocks protected from center (0,0) | `150` |
| `rollback-delay` | Seconds to wait before rollback after boss death | `5` |
| `rollback-notifications` | Seconds before rollback to show notifications | `[60, 30, 10, 5]` |
| `rollback-notification-message` | Message template (use `{seconds}` placeholder) | `"&cThe End will refresh in {seconds} seconds!"` |
| `auto-rollback-minutes` | Minutes without players before auto-rollback | `5` |
| `auto-respawn-dragon` | Respawn Ender Dragon after rollback (vanilla only) | `true` |
| `type` | MythicMobs mob type (mythicmobs mode only) | `"ENDER_DRAGON"` |

### Notification Examples

```yaml
# Simple red warning
rollback-notification-message: "&cThe End will refresh in {seconds} seconds!"

# Fancy warning with symbols
rollback-notification-message: "&c⚠️ &lWARNING: &cThe End will refresh in {seconds} seconds! ⚠️"

# Multi-color message
rollback-notification-message: "&e[&cWARNING&e] &fThe End will refresh in &c{seconds} &fseconds!"

# Disable notifications
rollback-notifications: []
rollback-notification-message: ""
```

## Commands

Main command: `/theendprotector` (aliases: `/tep`, `/endprotector`, `/theend`)

Subcommands:
- `help` (`h`) — show help
- `reload` (`r`, `reloadconfig`, `reload-config`, `reload_config`) — reload configuration
- `status` (`s`) — show current plugin status
- `test` (`t`, `rollbacktest`, `rollback-test`, `rollback_test`, `rollback`) — trigger rollback test
- `info` (`i`) — show plugin info

Usage examples:

```bash
# Show help
/theendprotector help

# Reload configuration
/theendprotector reload

# Check plugin status
/theendprotector status

# Test rollback
/theendprotector test
```


