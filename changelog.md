## Deadchest 4.4.0
+ Add possibility to change [Deadchest] prefix by something else on command feedback

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
+ No more collision with holograms. That mean that you can get your deadchest by the top or the bottom of it 
  without hitting the hologram instead.
+ Remove "×" at the beginning and the end of holograms
+ Improve /dc repair command feedback
+ New system to handle deadchest holograms
+ Improve comments of local config file

**BUGFIX**
+ Deadchest is now generated correctly on GRASS_PATH and FARMLAND
+ Fixed typo issue : infinate -> infinite
