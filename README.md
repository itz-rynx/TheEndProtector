# TheEndProtector
[![Version](https://img.shields.io/badge/version-0.5-blue.svg)](https://github.com/rynx/RegionMusic)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Author](https://img.shields.io/badge/author-rynx-purple.svg)](https://github.com/rynx)

An elegant and focused plugin to protect and automatically reset the main island in The End. Built for Paper/Spigot 1.21+, integrates with CoreProtect and optionally MythicMobs.

--- 
💝 Love this plugin? Consider supporting the development: [Donate via PayPal](https://paypal.me/qbao1702)
---  

Features
- Island protection within a configurable radius centered on (0,0).
- CoreProtect-powered rollback to the boss-spawn timestamp.
- Countdown notifications with `{seconds}` and color codes (supports `&`).
- Auto-cleanup (auto-rollback) when the island is empty (configurable timeout and enable switch).
- Optional automatic respawn of the Ender Dragon after rollback (vanilla mode).
- MythicMobs compatibility for custom bosses.
- Custom deny messages and configuration to allow building when the boss is dead.

Quick install
1. Drop `TheEndProtector.jar` into your server `plugins/` folder.  
2. Install `CoreProtect` (required). Install `MythicMobs` if using MythicMobs mode.  
3. Start the server to generate `plugins/TheEndProtector/config.yml`.  
4. Edit the config and run `/theendprotector reload` (or restart).

Configuration (summary)
Open `plugins/TheEndProtector/config.yml` after first run. Example snippet:

```yaml
vanilla:
  enabled: true
  protection-radius: 150
  rollback-delay: 5
  rollback-notifications: [60,30,10,5]
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
  rollback-notifications: [60,30,10,5]
  rollback-notification-message: "&cThe End will refresh in {seconds} seconds!"
  auto-rollback-enabled: true
  auto-rollback-minutes: 5
  allow-blocks-when-mob-dead: false
  block-deny-message: "&cCannot adjust blocks on the main island as the {mob} is not alive."
```

Recommended options to tune first: `protection-radius`, `rollback-delay`, `rollback-notifications`, `auto-rollback-enabled`, `auto-rollback-minutes`.

Commands & examples
Main command: `/theendprotector` (aliases: `/tep`, `/endprotector`, `/theend`)

Subcommands (aliases included):

| Command | Aliases | Purpose |
|---|---:|:---|
| `help` | `h` | Display help |
| `reload` | `r`, `reloadconfig` | Reload configuration |
| `status` | `s` | Show current status |
| `test` | `t`, `rollbacktest` | Trigger rollback test |
| `info` | `i` | Show plugin/version info |

Short usage:
```bash
/theendprotector help
/theendprotector reload
/theendprotector status
/theendprotector test
```

Permissions
- Default administrative access is OP. The plugin checks `theendprotector.op` if present.  
If you want granular control add specific permission checks (`theendprotector.reload`, `theendprotector.test`) and gate subcommands in code.

Troubleshooting
- Rollback doesn't occur — ensure CoreProtect is installed and enabled and the API is accessible.  
- Config changes not applied — use `/theendprotector reload` or restart.  
- MythicMobs not detected — verify `mythicmobs.type` and that MythicMobs is loaded.  
- No End world — verify an End world exists with environment THE_END.

Development notes
- Uses Adventure API / LegacyComponentSerializer for `&` color codes.  
- Rollback runs in a background thread; notifications are scheduled on the main thread.

Author & license
- Author: Rynx  
- Version: 0.5

---  



