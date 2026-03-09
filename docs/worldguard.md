## DeadChest - WorldGuard Integration

DeadChest can use WorldGuard regions to control where a DeadChest may be generated.

### Prerequisites

- DeadChest installed and running
- WorldGuard installed
- Integration enabled in `config.yml`

### Enable integration

Set the following keys in `config.yml`:

```yaml
integrations:
  worldguard:
    enabled: true
    default-allow: false
```

- `enabled`: activates WorldGuard checks.
- `default-allow`: fallback when no explicit DeadChest flag result applies.

### DeadChest WorldGuard flags

DeadChest registers these custom region flags:

| Flag        | Meaning                                              |
|-------------|------------------------------------------------------|
| `dc-owner`  | Region owners can generate DeadChest in the region.  |
| `dc-member` | Region members can generate DeadChest in the region. |
| `dc-guest`  | Any player can generate DeadChest in the region.     |

### How flags are evaluated

At death location, DeadChest checks regions and applies logic in this order:

1. If `dc-owner=allow` and player is owner -> allow.
2. If `dc-member=allow` and player is member -> allow.
3. If owner/member flags explicitly deny -> deny.
4. If `dc-guest=allow` -> allow.
5. If `dc-guest=deny` -> deny.
6. Otherwise fallback to `integrations.worldguard.default-allow`.

### Example commands (WorldGuard)

Command syntax may vary slightly by WorldGuard version, but typically:

```text
/rg flag <region> dc-owner allow
/rg flag <region> dc-member allow
/rg flag <region> dc-guest deny
```

### Verify integration

- Restart/reload server.
- Check console logs for WorldGuard detection and flag behavior.
- Test player deaths in and out of flagged regions.

### Troubleshooting

- If no custom flags appear, ensure WorldGuard is loaded before DeadChest check happens.
- If behavior seems global, verify the exact region at death location.
- If unsure, temporarily set `default-allow: true` to compare behavior.

