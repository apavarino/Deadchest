## DeadChest - Configuration

Make sure you have [installed](installation.md) the plugin before editing the configuration.

You can edit configuration by making change on `config.yml` file on the plugin folder

DeadChest now uses a structured `config.yml` (schema version `2`) since `v5.0.0`.
After any change, run `/dc reload`.

### Global

| Key              | Type    | Default | Description                   |
|------------------|---------|---------|-------------------------------|
| `config-version` | integer | `2`     | Configuration schema version. |

### Localization

| Key                     | Type   | Default | Description                                                                                                                            |
|-------------------------|--------|---------|----------------------------------------------------------------------------------------------------------------------------------------|
| `localization.language` | string | `en`    | Language file loaded from `plugins/DeadChest/localization/<language>.json` (ex: `en`, `fr`, `es`, `de`, `pt-br`, `pl`, `it`, `zh-cn`). |

### Updates

| Key                  | Type    | Default | Description                                |
|----------------------|---------|---------|--------------------------------------------|
| `updates.auto-check` | boolean | `true`  | Enable automatic update checks at startup. |

### Chest

| Key                           | Type    | Default                 | Description                                                     |
|-------------------------------|---------|-------------------------|-----------------------------------------------------------------|
| `chest.owner-only-open`       | boolean | `true`                  | Only owner can open chest (except `deadchest.chestPass`).       |
| `chest.duration-seconds`      | integer | `300`                   | Chest lifetime in seconds. `0` = infinite.                      |
| `chest.indestructible`        | boolean | `true`                  | Protect chest block from destruction/explosions.                |
| `chest.max-per-player`        | integer | `15`                    | Max active chests per player. `0` = unlimited.                  |
| `chest.recovery-mode`         | string  | `inventory-then-ground` | `inventory-then-ground` or `ground-drop`.                       |
| `chest.block-type`            | string  | `chest`                 | `chest`, `player-head`, `barrel`, `shulker-box`, `ender-chest`. |
| `chest.drop-items-on-timeout` | boolean | `false`                 | On timeout: drop items (`true`) or remove contents (`false`).   |

### Permissions

| Key                            | Type    | Default | Description                                            |
|--------------------------------|---------|---------|--------------------------------------------------------|
| `permissions.require-generate` | boolean | `false` | Require `deadchest.generate` to create chest on death. |
| `permissions.require-claim`    | boolean | `false` | Require `deadchest.get` to claim/retrieve chest.       |
| `permissions.require-list-own` | boolean | `false` | Require `deadchest.list.own` for `/dc list`.           |

### Maintenance

| Key                              | Type    | Default | Description                              |
|----------------------------------|---------|---------|------------------------------------------|
| `maintenance.cleanup-on-startup` | boolean | `false` | Remove all DeadChests on server startup. |

### Generation Rules

| Key                              | Type    | Default | Description                                       |
|----------------------------------|---------|---------|---------------------------------------------------|
| `generation.allow-in-creative`   | boolean | `true`  | Allow chest generation on death in Creative mode. |
| `generation.allow-on-lava`       | boolean | `true`  | Allow generation when death occurs in lava.       |
| `generation.allow-on-water`      | boolean | `true`  | Allow generation when death occurs in water.      |
| `generation.allow-on-rails`      | boolean | `true`  | Allow generation when death occurs on rails.      |
| `generation.allow-in-minecart`   | boolean | `true`  | Allow generation when death occurs in minecart.   |
| `generation.allow-in-end-worlds` | boolean | `true`  | Allow generation in The End worlds.               |

### Messages

| Key                                  | Type    | Default | Description                                |
|--------------------------------------|---------|---------|--------------------------------------------|
| `messages.display-position-on-death` | boolean | `true`  | Send chest coordinates to player on death. |

### XP

| Key                   | Type    | Default | Description                                                              |
|-----------------------|---------|---------|--------------------------------------------------------------------------|
| `xp.store-on-death`   | boolean | `false` | Store XP in chest instead of normal orb drop.                            |
| `xp.store-percentage` | integer | `100`   | XP percent stored when enabled (`0` to `100`, values >100 duplicate XP). |

### PvP

| Key                                 | Type    | Default | Description                                                 |
|-------------------------------------|---------|---------|-------------------------------------------------------------|
| `pvp.keep-inventory-on-player-kill` | boolean | `false` | On PvP death: keep inventory and skip DeadChest generation. |

### Integrations

| Key                                     | Type    | Default | Description                                        |
|-----------------------------------------|---------|---------|----------------------------------------------------|
| `integrations.worldguard.enabled`       | boolean | `false` | Enable WorldGuard region checks.                   |
| `integrations.worldguard.default-allow` | boolean | `false` | Default region policy if no DeadChest flag is set. |

### Durability

| Key                                | Type    | Default | Description                                                            |
|------------------------------------|---------|---------|------------------------------------------------------------------------|
| `durability.loss-on-death-percent` | integer | `0`     | Durability loss on death (percentage of max durability). `0` disables. |

### Logging

| Key                                   | Type    | Default | Description                         |
|---------------------------------------|---------|---------|-------------------------------------|
| `logging.deadchest-create-to-console` | boolean | `false` | Log each chest creation in console. |

### Visuals - Effect Animation

| Key                                | Type    | Default | Description                                      |
|------------------------------------|---------|---------|--------------------------------------------------|
| `visuals.effect-animation.enabled` | boolean | `true`  | Enable orbit particles around active DeadChests. |
| `visuals.effect-animation.style`   | string  | `ender` | Effect style: `soul`, `flame`, `ender`.          |
| `visuals.effect-animation.radius`  | number  | `0.8`   | Orbit radius around chest center.                |
| `visuals.effect-animation.speed`   | number  | `1.1`   | Orbit speed multiplier.                          |

### Visuals - Pickup Animation

| Key                                 | Type    | Default    | Description                                     |
|-------------------------------------|---------|------------|-------------------------------------------------|
| `visuals.pickup-animation.enabled`  | boolean | `true`     | Enable animation when a player claims a chest.  |
| `visuals.pickup-animation.particle` | string  | `FIREWORK` | Bukkit particle name used for pickup animation. |
| `visuals.pickup-animation.count`    | integer | `22`       | Number of particles spawned.                    |
| `visuals.pickup-animation.offset-x` | number  | `0.45`     | Particle spread on X axis.                      |
| `visuals.pickup-animation.offset-y` | number  | `0.5`      | Particle spread on Y axis.                      |
| `visuals.pickup-animation.offset-z` | number  | `0.45`     | Particle spread on Z axis.                      |
| `visuals.pickup-animation.speed`    | number  | `0.08`     | Particle extra/speed value.                     |
| `visuals.pickup-animation.y-shift`  | number  | `0.55`     | Vertical center offset from block base.         |

### Visuals - Pickup Sound

| Key                            | Type    | Default                 | Description                   |
|--------------------------------|---------|-------------------------|-------------------------------|
| `visuals.sound.pickup.enabled` | boolean | `true`                  | Enable sound on chest pickup. |
| `visuals.sound.pickup.name`    | string  | `ENTITY_PLAYER_LEVELUP` | Bukkit sound name.            |
| `visuals.sound.pickup.volume`  | number  | `1.2`                   | Sound volume.                 |
| `visuals.sound.pickup.pitch`   | number  | `1.0`                   | Sound pitch.                  |

### Filters

| Key                       | Type         | Default        | Description                                                         |
|---------------------------|--------------|----------------|---------------------------------------------------------------------|
| `filters.excluded-worlds` | list<string> | Example values | Worlds where DeadChest generation is disabled.                      |
| `filters.excluded-items`  | list<string> | Example values | Items that are not stored in DeadChest.                             |
| `filters.ignored-items`   | list<string> | Example values | Items ignored by DeadChest processing (not stored and not removed). |

### Permission reference

- `deadchest.admin`: admin commands (`reload`, `repair`, `removeall`, `removeinfinite`, `ignore`)
- `deadchest.generate`: generate chest on death (if required)
- `deadchest.get`: claim/retrieve chest (if required)
- `deadchest.list.own`: list own chests (if required)
- `deadchest.list.other`: list another player's chests
- `deadchest.remove.own`: remove own chests
- `deadchest.remove.other`: remove another player's chests
- `deadchest.giveback`: give back another player's items
- `deadchest.chestPass`: bypass owner-only chest access
- `deadchest.infinityChest`: create infinite chests

