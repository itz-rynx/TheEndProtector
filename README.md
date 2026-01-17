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
6. Restart server or use `/reloadconfig` command

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

| Command | Permission | Description |
|---------|------------|-------------|
| `/rollbacktest` | `theendprotector.op` | Test rollback functionality (OP only) |
| `/reloadconfig` | `theendprotector.op` | Reload configuration without restart (OP only) |

### Usage Examples

```bash
# Test rollback (only works when boss is alive)
# This simulates boss death and starts rollback process
/rollbacktest

# Reload configuration after making changes
/reloadconfig
```


