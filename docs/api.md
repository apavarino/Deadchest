## DeadChest - API

DeadChest exposes a public API for integrations.

### API class

Use static methods from `DeadChestAPI`.

### Methods

| Method                                                                | Description                                         | Available since |
|-----------------------------------------------------------------------|-----------------------------------------------------|-----------------|
| `public static List<ChestData> getChests(Player player)`              | Return all known DeadChests for a player.           | `4.6.0`         |
| `public static boolean giveBackChest(Player player, ChestData chest)` | Give back one specific chest inventory to a player. | `4.6.0`         |
| `public static boolean removeChest(ChestData chest)`                  | Remove a specific chest from world/storage.         | `4.7.0`         |

### Events

| Event                  | Description                                      | Available since |
|------------------------|--------------------------------------------------|-----------------|
| `DeadchestPickUpEvent` | Fired when a player picks up/claims a DeadChest. | `4.7.0`         |

### Notes

- Use null checks and online/offline checks for player-dependent operations.
- Review behavior on full inventories depending on configured recovery mode.

