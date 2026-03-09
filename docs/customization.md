## DeadChest - Customization

This section explains how to customize DeadChest with the current configuration system.

### What can be customized

You can customize:

- Language and messages (localization JSON files)
- Visual effects around chests
- Pickup animation particles
- Pickup sound

Most visual/audio settings are in `config.yml` under `visuals.*`.

### Localization files

DeadChest uses JSON localization files located in:

`plugins/DeadChest/localization/`

Examples of bundled languages:

- `en.json`
- `fr.json`
- `es.json`
- `de.json`
- `pt-br.json`
- `pl.json`
- `it.json`
- `zh-cn.json`

Select active language in `config.yml`:

```yaml
localization:
  language: en
```

### Placeholders in messages

Current placeholders use indexed format:

- `{0}`
- `{1}`
- `{2}`

Example:

```json
"commands.list.title.player": "DeadChests of {0}"
```

### Colors and formatting

Minecraft formatting codes are supported in localization strings.

- Use `§` codes in text if needed.
- Keep valid JSON syntax (quotes, commas, escaping).

### Legacy migration note

If you used old `locale.yml`, DeadChest migrates it automatically to `localization/en.json` on startup and archives the
legacy file.

### Visual customization (config.yml)

You can tune visuals in these sections:

- `visuals.effect-animation.*`
- `visuals.pickup-animation.*`
- `visuals.sound.pickup.*`

Quick reference:

| Key                                 | Description                             |
|-------------------------------------|-----------------------------------------|
| `visuals.effect-animation.style`    | Orbit style (`soul`, `flame`, `ender`). |
| `visuals.effect-animation.radius`   | Orbit radius around chest.              |
| `visuals.effect-animation.speed`    | Orbit speed multiplier.                 |
| `visuals.pickup-animation.particle` | Particle used on pickup.                |
| `visuals.pickup-animation.count`    | Number of particles.                    |
| `visuals.sound.pickup.name`         | Bukkit sound played on pickup.          |
| `visuals.sound.pickup.volume`       | Pickup sound volume.                    |
| `visuals.sound.pickup.pitch`        | Pickup sound pitch.                     |

For full option list, see [configuration](configuration.md).

### Apply changes

- Run `/dc reload` after editing `config.yml` or localization files.
- Validate server logs if a JSON formatting error occurs.

