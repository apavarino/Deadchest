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
