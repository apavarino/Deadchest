## Deadchest-OnlineReturn 1.0
+ Added real vanilla experience storage, so the value follows that of which normally occurs upon death
+ Added option to allow timer to only tick down while player is online
+ Added option for inventory to be returned to player when player returns; setting only works if the above is enabled


## Deadchest 4.11

+ Adding auto-updater
+ Code change : Update configuration code
+ Code change : Update commands system
+ Fix typo "Indestuctible" in config.yml
+ Add force option to /dc repair
+ Potentially fixing chunk keeping loading for no reason

## Deadchest 4.10

+ Fixing all performance issue

## Deadchest 4.9.0

+ Improving memory performance related to chunk loading

## Deadchest 4.8.2

+ Fixed performance issue

## Deadchest 4.8.1

+ Fixed spam error on the console

## Deadchest 4.8.0

BUGFIX :

+ Fixed hologram issue staying in the world on 1.17+ version on unload chunk
+ Fixed unsafe head as deadchest (head are now not destroyable anymore by water/lava/piston..)
+ Fixed Netherite boots in place of chestplate
+ Fixed min height for Deadchest. Plugin now handle deadchest under y 0

CHANGE :

+ You can no longer use right click to get your chest
+ If chest is abnormally removed. (like world bug, cuboid etc..), it now respawn at the same place until the timer is
  out

## Deadchest 4.7.0

FEATURES :

+ Add gamerules keepInventory support (finally !) : If enable no Deadchest is generate
+ Code optimization
+ Switch default Deadchest time from 5min to 15min
+ Add support for customs items (Slimefun/Minetinker etc...)
+ Better 1.17 support
+ API Update
+ Add removeChest() method
+ Add DeadchesPickUpEvent

BUG FIX :

+ Fix : error on /dc list when world does not exists anymore
+ Fix : Expired Deadchests was not removed on unloaded chunck

## Deadchest 4.6.0

**Features**

+ Official support for 1.17
+ Added basic API
+ Added permission to manage if user can open Deadchest
+ Added 3 new Deadchest type !
  + Barrel chest (dropBlock : 3 in config.yml)
  + Shulker chest  (dropBlock : 4 in config.yml)
  + Ender chest  (dropBlock : 5 in config.yml)

**BugFix**

+ Fix netherite stuff error with Minecraft lower than 1.16

## Deadchest 4.5.0

**FEATURES**

+ Add option to disable Deadchest in lava
+ Add option to disable Deadchest in water
+ Add option to disable Deadchest on rails
+ Add option to disable Deadchest on minecart
+ Added bstats for plugin statistics

**BUGFIX**

+ Fix player head destroying with water issue
+ Fix Task exception spamming on console in certain cases

## Deadchest 4.4.0

**FEATURES**

+ **Added player head as deadchest** ! (configurable in config.yml)
+ Added possibility to change [Deadchest] prefix by something else on command feedback
+ Added auto update configuration file system when updating the plugin

**BUGFIX**

+ Fix Task exception spamming on console in certain cases

## Deadchest 4.3.0

**FEATURES**
+ Update for Minecraft 1.16.4
+ Upgrade Worldguard support
  + Deadchest now handle region priority
  + remove dc_nobody flag
  + add dc_guest flag

**BUGFIX**
+ Invisible armorstand was not usable with Deadchest
+ Activation of worldguard detection was not working correctly

## Deadchest 4.2.0

**FEATURES**
+ Added option to exclude items from Deadchest in config.yml

**BUGFIX**
+ Fix issue with CRIMSON_DOOR on 1.15 and lower
+ Fix exception with reload metadata

## Deadchest 4.1.1

Hotfix : Patch the issue related to WorldGuard on 4.1.0.

## Deadchest 4.1.0

**FEATURES**
+ Option to enable/disable Worldguard check for deadchest generation
+ Add Turtle Helmet on auto-equip
+ Add Netherite stuff on auto-equip

**CHANGE**
+ Remove previous Worldguard support system
+ New WorldGuard support : Works now with flags :
  + **dc-owner** :  Only owner of the region can generate deadchest (true/false)
  + **dc-member** : Only member of the region can generate deadchest (true/false)
  + **dc-nobody** : Nobody can generate deadchest in the region
+ When a player dies on ladder, inside a door or in vines, deadchest now try to place the deadchest next to it instead of placing it at the top

**BUGFIX**
+ **Fix Deadchest dupe with books**
+ Sound of getting chest was heard by everyone

## Deadchest 4.0.0

**FEATURES**
+ Official support for 1.16.X
+ Adding colors and styling for holograms and texts
+ Adding new localization system with powerfull configuration
+ Adding timer customization
+ Adding Log system : All events related to deadchest are now stocked in a file
+ Adding WorldGuard support : A Deadchest is not generate if the player is not member/owner of the region
  where he died
+ Adding autocompletion for commands
+ Adding option in config file to enable items dropping on the floor when a deadchest time out

**CHANGES**
+ Code refactoring
+ Improve stability
+ No more collision with holograms. That mean that you can get your deadchest by the top or the bottom of it without hitting the hologram instead.
+ Remove "×" at the beginning and the end of holograms
+ Improve /dc repair command feedback
+ New system to handle deadchest holograms
+ Improve comments of local config file

**BUGFIX**
+ Deadchest is now generated correctly on GRASS_PATH and FARMLAND
+ Fixed typo issue : infinate -> infinite

## Deadchest 3.5.0

+ Increase the performance of the plugin by decreasing a lot the memory use. The performances will be especially notable for servers which has a lot of players.

## Deadchest 3.4.0

+ Add option to disable/enable message with deadchest position on death

## Deadchest 3.3.0

Fix hologram that stay after getting deadchest (this time it’s the right one.)
Add missing translation
Upgrade translation system
Generate deadchest when player dies upper than map max height
Item in armors slots with Curse of Vanishing was not removing of deadchest
Auto-equip armor no longer equip item with curse of binding

## Deadchest 3.2.0

FEATURES 
+ Message on death to give location of the deadchest
+ Items with Curse of Vanashing are no longer stored in deadchests
+ Added option to disable deadchest in creative mode
+ Added option to choose how deadchest drop items (inventory or ground)
+ Added new permission deadchest.giveBack (op by default)
+ Added new command /dc giveback <PlayerName> to get back the content of the oldest deadhcest of a player to him.
  If you want to recover several deadchest for a player, you juste have to execute the command again.

CHANGES 
+ Update localisation system
+ Add more localisation
+ Change permission : deadchest.infinyChest by deadchest.infinityChest
+ deadchest.ChestPass permission in now enable by default for admin

BUGFIX 
+ "Excluded world" config option was not generated on config.yml on plugin loading
+ Corrupted Deadchest when dying top of a world
+ DeadChests can be merge with normal chest
+ Correction typo infiny --> infinity
+ No feedback when player type /dc list all if there is no deadchest
+ No feedback when player type /dc list (playerName) if there is no deadchest
+ Location was not updated during a /dc reload

## Deadchest 3.1.0

+ Plugin now manage death out of the world ( finally ! )
+ Stuff go now directly to the inventory instead of be dropping on the ground (except if inventory is full)
+ Smart auto equip system for armors and elytra on opening deadchest
+ Add help section /help dc
+ Add world name on/dc list
+ [BUGFIX] Hologram can be equip with stuff (fun but useless)
+ [BUGFIX] Bed explosion in nether and end break deadchests
+ [BUGFIX] Corrupted time with infiny chest when using /dc list ,
+ [BUGFIX] Remove /dclist
+ [BUGFIX] Add feedback for command /dc

## Deadchest 3.0.0

New features :
+ Add option to DeadChestDuration. 0 = infiny chest duration
+ Add option to maxDeadChestPerPlayer. 0 = infiny chests
+ Add command /dc removeinfinate to remove all infiny chest (deadchest.admin)
+ Add command /dc removeall to remove all deadchests (deadchest.admin)
+ Add command /dc remove <Player> all deadchests of a player (deadchest.remove.other)
+ Add command /dc remove to remove all deadchest of the current player (deadchest.remove.own)
+ Add permission deadchest.remove.own
+ Add option RequirePermissionToGenerate to choose if players need permission to use DeadChest
+ Add option RequirePermissionToListOwn to choose if players need permission to list their dead chests
+ Add permission deadchest.list.own for /dc list
+ Add option AutoCleanupOnStart to remove all existing deadchests on startup
+ Add command /dc list <all/Player> to display deadchests of all or a specific player (deadchest.list.other)
+ Add a new config file (locale.yml) . You can edit text of the plugin to the langage you want
+ New option to disable DeadChest on certain worlds in config.yml
  Change :
+ Massive code rewrite
+ Performance optimization
+ deadchest.keepInventory permission change to deadchest.generate
+ /dcinfo change to /dc list
+ Upgrade of config.yml file to be more friendly

## Deadchest 2.8.0

+ Upgrade save system to handle worlds that are not currently running
+ Patch NullPointerException for Task
+ Patch issue with Multiverse
+ Minor fix

## Deadchest 2.7.0

+ Upgrade save system to handle worlds that are not currently running
+ Patch NullPointerException for Task
+ Patch issue with Multiverse
+ Minor fix

## Deadchest 2.6.0

+ Fix bad chest position when player dies in cave

## Deadchest 2.4.0

+ Add compatibility with NPC
+ Add command /dcinfo to view the position of your dead chests and the remaining time ! (need deadchest.info permission)

## Deadchest 2.3.0

BUGFIX : Holographic display was staying when a deadchest is removed (This is the third time I try to remove this damn bug, I hope this time it will works fine for everyone !)
BUGFIX : Remaining time is corrupted on deadchest in certain cases
BUGFIX : Holographic bug when two dead chest are near
BUGFIX : Attempt to get full compatibility with NPC plugins

## Deadchest 2.2.0

+ Patch nether issue
+ Patch incorrect location of deadchest in certain cases when player dies

## Deadchest 2.1.0
+ Change permission deadchest.keepInventory to true by default.

## Deadchest 2.0.0

FEATURES :
+ Add parameter maxDeadChestPerPlayer, corresponding to the maximum number of deadchest that a player can have. if this
  number is exceeded. Inventory is dropped on the floor.
+ Add permission deadchest.admin (need to type commands)
+ Add command /dc reload to reload the plugin
+ Add command /dc repair to clear holographic display on chest if something went wrong

MINOR FEATURES :
+ Add sound when player open deadchest
+ Add effect when player open deadchest

CHANGE :
+ Massive code rewrite
+ Performance optimization
+ Improved stability
+ Plugin configuration and deadchest data are now separate in two different files
+ Remove permission deadChest.noDropChest
+ Permission deadChest.keepInventory is now disable by default
+ Removing parameter EnableForOP
+ Removing some useless logs on enable and disable

BUGFIX :
+ Players with Essential plugin was keeping their inventory on death allowing duplication inventory.
+ Deadchest was generate even player inventory is empty
+ In some case, ClassCastException error was occured
+ If two deadchest was near, that removed some holographic display on chest
+ Infiny chest had a corrupted left time
+ Holographic display was not removing if the deadchest was destroyed
+ Deadchest was not removed in memory if destroyed
+ If player was dying in a wall, deadchest was replacing the wall block

## Deadchest 1.7.0

+ FIX ISSUE : Duplication item in strange condition
+ FIX ISSUE : If a player dies on a semi-block like campfire, the player deadchest appear on this block and destroy it.
+ FIX ISSUE : On restarting server dead chest become inaccessible for owner

## Deadchest 1.5.0

+ Patch error spamming console

## Deadchest 1.4.0

+ Performance optimization
+ Increase response time by x20
+ Improve placement of holographic display
+ Patch issue with offline player and indestructible chest
+ Patch issue with saving date of dead chest
+ Patch issue with updating data on disable
+ Patch issue when player disconnect he can't get back his own chest
+ Add holographic timer before the chest disappear

## Deadchest 1.3.0

+ BUGFIX : Comments on config file disappear after reload
+ ADD OPTION : EnableForOP in config file. Disable or not deadChest for OP
+ ADD PERMISSIONS :
  + deadchest.noDropChest : player don't drop dead chest on death
  + deadchest.chestPass : Player can open all deadChest
  + deadchest.infinyChest : Player dead chest never disappaear

## Deadchest 1.2.0

+ Add permission deadchest.keepInventory enable by default for all.
+ Update config file description

## Deadchest 1.1.0

+ Change default deadChestDuration to 600
+ BUFIX : config file reset on reload
+ Optimization
+ Add header with some explanation on config.yml file

## Deadchest 1.0.0

+ Initial version
