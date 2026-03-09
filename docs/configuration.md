## Deadchest - Configuration

Make sure you have [installed](installation.md) the plugin before reading this step.

This section describes how to configure Deadchest through the`config.yml` file

| Option                            | Value          | Default | Description                                                                                                                      |
|-----------------------------------|----------------|---------|----------------------------------------------------------------------------------------------------------------------------------|
| `OnlyOwnerCanOpenDeadChest`       | boolean        | true    | If true only the owner can open the chest. If false, anyone can open it                                                          |
| `DeadChestDuration`               | number         | 360     | Duration (in sec) before the chest expires. 0 is for infinite duration (no expiration)                                           |
| `IndestuctibleChest`              | boolean        | true    | Enable protection of the chest against griefing                                                                                  |
| `maxDeadChestPerPlayer`           | number         | 15      | Maximum number of Deadchest than a player can have at the same time                                                              |
| `logDeadChestOnConsole`           | boolean        | false   | Log on the console new deadchests                                                                                                |
| `RequirePermissionToGenerate`     | boolean        | false   | Should players need permission `deadchest.generate` to generate a DeadChest                                                      |
| `RequirePermissionToGetChest`     | boolean        | false   | Should players need permission `deadchest.get` to retrieve their DeadChest                                                       |
| `RequirePermissionToListOwn`      | boolean        | false   | Should players need permission `deadchest.list.own` to view their DeadChests (using `/dc list`)                                  |
| `AutoCleanupOnStart`              | boolean        | false   | Remove existing DeadChests on server start                                                                                       |
| `GenerateDeadChestInCreative`     | boolean        | true    | Generate Deadchests in creative mode                                                                                             |
| `DisplayDeadChestPositionOnDeath` | boolean        | true    | Display a message to the player with the location of the Deadchest when they die                                                 |
| `DropMode`                        | 1,2            | 1       | 1 = Items are added directly to the player's inventory until full, excess is dropped. 2 = All items are dropped on the ground.   |
| `DropBlock`                       | 1,2,3,4,5      | 1       | 1 = Chest, 2 = Player Head, 3 = Barrel, 4 = Shulker Box, 5 = Ender Chest                                                         |
| `ItemsDroppedAfterTimeOut`        | boolean        | false   | false = DeadChest contents are removed when the timer expires. true = contents are dropped on the ground when the timer expires. |
| `GenerateOnLava`                  | boolean        | true    | Enable Deadchest generation on lava                                                                                              |
| `GenerateOnWater`                 | boolean        | true    | Enable Deadchest generation on water                                                                                             |
| `GenerateOnRails`                 | boolean        | true    | Enable Deadchest generation on rails                                                                                             |
| `GenerateInMinecart`              | boolean        | true    | Enable Deadchest generation in minecart                                                                                          |
| `EnableWorldGuardDetection`       | boolean        | false   | Enable Worldguard integration. More info [here](worldguard.md)                                                                   |
| `ExcludedWorld`                   | list of string |         | List of worlds where DeadChests will not be generated                                                                            |
| `ExcludedItems`                   | list of string |         | List of items that will be excluded from DeadChests                                                                              |

### Next step

See [commands & permissions part](commands-and-perms.md) or go
to [home page](index.md)




