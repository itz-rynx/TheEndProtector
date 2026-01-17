# TheEndProtector

TheEndProtector protects and automatically resets the main island in The End dimension. It supports vanilla Ender Dragon encounters and optional MythicMobs custom bosses, with configurable protection and rollback behavior.

## Features

- Island protection: prevents block placement and breaking on the main End island while the configured boss is not alive.
- Configurable protection radius centered on (0,0).
- Automatic rollback using CoreProtect to restore the island to the state before the boss spawn.
- Configurable rollback delay and optional countdown notifications using color codes and placeholders (e.g. `{seconds}`).
- Auto-cleanup when the island is abandoned (configurable timeout and can be enabled/disabled).
- Optional automatic respawn of the Ender Dragon after rollback (vanilla mode).
- MythicMobs integration: support spawning and rollback for custom bosses.
- Customizable deny messages and an option to allow block edits while the boss is dead.

## Installation

### Requirements

- Minecraft server 1.21+ (Paper/Spigot recommended)
- CoreProtect (required for rollback functionality)
- MythicMobs (optional, only needed for MythicMobs mode)

### Installation Steps

1. Copy `TheEndProtector.jar` to your server's `plugins/` folder.
2. Start the server to generate the default `config.yml`.
3. Edit `plugins/TheEndProtector/config.yml` to configure modes, protection radius, rollback behavior, notifications, and other options.
4. Restart the server or reload the plugin configuration.

For detailed configuration examples, open the generated `config.yml` after the first run.


