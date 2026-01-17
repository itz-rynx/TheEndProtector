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
  # Enable automatic rollback when no players are present (true/false)
  auto-rollback-enabled: true
  # Allow block changes when the mob is dead (true/false)
  allow-blocks-when-mob-dead: false
  # Message shown when a block action is blocked. Use {mob} placeholder and & color codes.
  block-deny-message: "&cCannot adjust blocks on the main island as the {mob} is not alive."

mythicmobs:
  enabled: false
  type: "ENDER_DRAGON"
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60, 30, 10, 5]
  rollback-notification-message: "&c⚠️ The End will refresh in {seconds} seconds! ⚠️"
  auto-rollback-minutes: 5
  auto-rollback-enabled: true
  allow-blocks-when-mob-dead: false
  block-deny-message: "&cCannot adjust blocks on the main island as the {mob} is not alive."
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
| `auto-rollback-enabled` | Enable automatic rollback when no players present | `true` |
| `allow-blocks-when-mob-dead` | Allow block place/break on main island when mob dead | `false` |
| `block-deny-message` | Message shown when a block action is denied, supports `{mob}` and `&` color codes | `"&cCannot adjust blocks on the main island as the {mob} is not alive."` |

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

### Main Command
| Command | Aliases | Permission | Description |
|---------|---------|------------|-------------|
| `/theendprotector` | `/tep`, `/endprotector`, `/theend` | `theendprotector.op` | Main plugin command with subcommands |

### Subcommands
| Subcommand | Alias | Description |
|------------|-------|-------------|
| `help` | `h` | Show help menu |
| `reload` | `r` | Reload configuration |
| `status` | `s` | Show current plugin status |
| `test` | `t` | Test rollback functionality |
| `info` | `i` | Show plugin information |

### Legacy Commands (Deprecated)
| Command | Description |
|---------|-------------|
| `/rollbacktest` | Test rollback (use `/theendprotector test` instead) |
| `/reloadconfig` | Reload config (use `/theendprotector reload` instead) |

### Usage Examples

```bash
# Show help menu
/theendprotector help
/theendprotector
/tep help

# Reload configuration
/theendprotector reload
/tep r

# Check plugin status
/theendprotector status
/tep s

# Test rollback functionality
/theendprotector test
/tep t

# Show plugin information
/theendprotector info
/tep i

# Using aliases
/endprotector status
/theend reload

<!-- Example command outputs removed to avoid long inline logs. Use the commands in the Usage Examples section to view output in-game. -->

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `theendprotector.op` | OP | Access to all TheEndProtector commands |

**Note**: All commands currently require OP status. Future versions may include more granular permissions.

## How It Works

### Protection System
1. When a boss spawns, the plugin records a timestamp
2. While the boss is alive, players can build freely on the main island
3. When the boss dies, the island becomes protected
4. Players cannot place or break blocks within the protection radius

### Rollback Process
1. Boss death triggers protection and starts rollback timer
2. Countdown notifications warn players in The End
3. After delay, CoreProtect rolls back all changes since boss spawn
4. Optional: Respawn boss for continuous gameplay

### Auto-Rollback Feature
1. Monitors player presence on main island every minute
2. If no players detected for configured time, automatically removes boss
3. Triggers immediate rollback to prevent abandoned islands

## Compatibility

### Plugin Compatibility
- ✅ **CoreProtect**: Required for rollback functionality
- ✅ **MythicMobs**: Optional, enables custom boss support
- ✅ **Paper/Spigot**: Full support for 1.21+
- ⚠️ **Other Protection Plugins**: May conflict with block protection

### Known Limitations
- Requires CoreProtect for rollback functionality
- Protection only applies to main End island (coordinates around 0,0)
- MythicMobs integration requires MythicMobs plugin to be loaded

## Troubleshooting

### Common Issues

**"Plugin not working"**
- Ensure CoreProtect is installed and enabled
- Check console for errors during startup
- Verify configuration syntax

**"Rollback not working"**
- Check CoreProtect database integrity
- Ensure boss spawned after plugin installation
- Verify rollback delay settings

**"MythicMobs mode not working"**
- Install MythicMobs plugin
- Ensure mob type matches exactly in MythicMobs config
- Check MythicMobs console for errors

### Debug Information
Enable debug mode by setting `debugMessages = true` in the source code for additional console logging.

## Support

For issues and feature requests:
- Check this README for configuration help
- Review server console for error messages
- Test with default configuration first
- Ensure all required plugins are properly installed

## License

This plugin is provided as-is for Minecraft server administrators. Use at your own risk.
