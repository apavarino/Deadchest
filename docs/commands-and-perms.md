## DeadChest - Commands & Permissions

Make sure you have [installed](installation.md) the plugin before reading this section.

This page documents the current `/dc` commands and permission nodes.

### Commands

| Command                 | Permission               | Description                                                                  |
|-------------------------|--------------------------|------------------------------------------------------------------------------|
| `/dc reload`            | `deadchest.admin`        | Reload configuration and localization.                                       |
| `/dc repair`            | `deadchest.admin`        | Remove nearby broken legacy/DeadChest holograms around the executing player. |
| `/dc repair force`      | `deadchest.admin`        | Force-remove nearby armor stand holograms around the executing player.       |
| `/dc remove`            | `deadchest.remove.own`   | Remove all of your own DeadChests.                                           |
| `/dc remove <player>`   | `deadchest.remove.other` | Remove all DeadChests belonging to a specific player.                        |
| `/dc removeinfinite`    | `deadchest.admin`        | Remove all infinite DeadChests.                                              |
| `/dc removeall`         | `deadchest.admin`        | Remove all DeadChests globally.                                              |
| `/dc list`              | `deadchest.list.own`*    | List your own DeadChests.                                                    |
| `/dc list <player>`     | `deadchest.list.other`   | List DeadChests for a specific player.                                       |
| `/dc list all`          | `deadchest.list.other`   | List all DeadChests on the server.                                           |
| `/dc giveback <player>` | `deadchest.giveback`     | Give back a player's oldest DeadChest inventory.                             |
| `/dc ignore`            | `deadchest.admin`        | Open the ignore-items GUI/list.                                              |

\* `deadchest.list.own` is only required when `permissions.require-list-own: true` in `config.yml`.

### Permission Nodes

| Permission                | Description                                                                          |
|---------------------------|--------------------------------------------------------------------------------------|
| `deadchest.admin`         | Access admin commands (`reload`, `repair`, `removeall`, `removeinfinite`, `ignore`). |
| `deadchest.generate`      | Generate a DeadChest on death (if required by config).                               |
| `deadchest.get`           | Claim/retrieve a DeadChest (if required by config).                                  |
| `deadchest.chestPass`     | Bypass owner-only chest access.                                                      |
| `deadchest.infinityChest` | Create infinite DeadChests.                                                          |
| `deadchest.remove.own`    | Remove own DeadChests with `/dc remove`.                                             |
| `deadchest.remove.other`  | Remove another player's DeadChests with `/dc remove <player>`.                       |
| `deadchest.list.own`      | List own DeadChests with `/dc list` (if required by config).                         |
| `deadchest.list.other`    | List another player's/all DeadChests with `/dc list <player>` or `/dc list all`.     |
| `deadchest.giveback`      | Give back another player's items with `/dc giveback <player>`.                       |

### Config-Dependent Permission Requirements

The following config flags control whether some actions require permissions:

- `permissions.require-generate`: require `deadchest.generate` for chest generation.
- `permissions.require-claim`: require `deadchest.get` for claiming chests.
- `permissions.require-list-own`: require `deadchest.list.own` for `/dc list`.

