## Deadchest - Configuration

Make sure you have [installed](https://apavarino.github.io/Deadchest/installation) the plugin before reading this step.

This section describes how to configure Deadchest through  the`config.yml` file


Option | Value | Default | Description
--- | --- | --- | ---
`auto-update` | boolean  | true | Enable plugin auto update. In case of major change the plugin will not update by itself and just display message information
`OnlyOwnerCanOpenDeadChest` | boolean  | true | If true only the owner can open the chest. If false, anyone can open it
`DeadChestDuration` | number  | 360 | Duration (in sec) before the chest expires. 0 is for infinite duration (no expiration)
`IndestuctibleChest` | boolean  | true | Enable protection of the chest against griefing
`maxDeadChestPerPlayer` | number  | 15 | Maximum number of Deadchest than a player can have at the same time
`logDeadChestOnConsole` | boolean  | false | Log on the console new deadchests
`RequirePermissionToGenerate` | boolean  | false | Should players need permission `deadchest.generate` to generate a DeadChest
`RequirePermissionToGetChest` | boolean  | false | Should players need permission `deadchest.get` to retrieve their DeadChest
`RequirePermissionToListOwn`  | boolean  | false | Should players need permission `deadchest.list.own` to view their DeadChests (using `/dc list`)
`AutoCleanupOnStart`  | boolean  | false | Remove existing DeadChests on server start
`GenerateDeadChestInCreative`  | boolean  | true | generate Deadchests in creative mode
`DisplayDeadChestPositionOnDeath`  | boolean  | true | Display a message to the player with the location of the Deadchest when he dies
`DropMode`  | 1,2  | 1 | 1 - Items are added directly to the players inventory until it's full, excess gets dropped <br>2 - All items are dropped on the ground.
`DropBlock`  | 1,2,3,4,5  | 1 | 1 - Chest<br>2 - Player Head<br>3 - Barrel<br>4 - Shulker Box<br>5 - Ender Chest
`StoreXP` |  boolean  |  false | Store Player XP on Deadchest
`StoreXPPercentage` |  number  |  100 | % of XP stored on the deadchest. Only works if StoreXP is true
`ItemsDroppedAfterTimeOut`  | boolean | false | false - DeadChest contents are removed when the timer expires.<br>true  - DeadChest contents are dropped on the ground when the timer expires.
`GenerateOnLava`  | boolean  | true | Enable Deadchest generation on lava
`GenerateOnWater`  | boolean  | true | Enable Deadchest generation on water
`GenerateOnRails`  | boolean  | true | Enable Deadchest generation on rails
`GenerateInMinecart`  | boolean  | true | Enable Deadchest generation in minecart
`EnableWorldGuardDetection` | boolean  | false | Enable Worldguard integration. More info [here](https://apavarino.github.io/Deadchest/worldguard)
`item-durability-loss-on-death` | number  | 100 | Percentage of stuff durability loss on death
`ExcludedWorld`  | list of string  |  | List of worlds that DeadChests will NOT be generated in
`ExcludedItems`  | list of string  |  | List of items that will be excluded from DeadChests

### Next step
See [commands & permissions part](https://apavarino.github.io/Deadchest/commands-and-perms) or go to [home page](https://apavarino.github.io/Deadchest)


