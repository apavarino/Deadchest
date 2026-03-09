## DeadChest - How It Works

This page explains how DeadChest behaves in common gameplay situations.

### Default behavior

When a player dies, DeadChest creates a protected chest at a suitable location near the death point.
By default:

- chest access is owner-only
- a hologram shows owner and remaining time
- the chest expires after configured timeout (unless infinite)

All behavior is configurable in [configuration](configuration.md).

### Chest placement rules

DeadChest never intentionally replaces solid blocks. If the exact death block is invalid, it searches for the nearest
valid location.

#### Overworld, Nether, End, custom worlds

The same placement logic is used in all worlds.

#### Death below the world

DeadChest clamps to the lowest valid height for that world at the same horizontal position.

#### Death above world max height

DeadChest clamps to the highest valid height for that world at the same horizontal position.

#### Death on non-placeable spots (doors, rails, torches, etc.)

If the exact block cannot host a chest, DeadChest searches for the next valid free space.

### Water and lava deaths

DeadChest can generate in these environments if enabled:

- `generation.allow-on-water`
- `generation.allow-on-lava`

### Item handling

- Items with Curse of Vanishing are not stored.
- Retrieval behavior depends on `chest.recovery-mode`.

### Protection and anti-grief behavior

Depending on config, DeadChests are protected against unauthorized access and destruction.
This includes owner checks and block protection logic.

### Performance model

DeadChest is designed for active servers and focuses work on loaded areas/chunks to limit overhead.

