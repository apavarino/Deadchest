## Deadchest - How it works ?

This section describes the behavior of the plugin on different situation.

### Default behaviour

When a player dies. A chest is generated at the exact location where the player dies. This chest is by default locked to be only openable by the owner. At the top of the chest an hologram is generated with the name of the owner and the time remaining before the chest disappears.

> All is configurable, see [configuration part](https://apavarino.github.io/Deadchest/configuration) for more information.

Items on the chest can be collect by just left clicking on it. There is no space limitation the chest can store all of your stuff in all situation.

The plugin will **NEVER replace or destroy any solid block** of your world. An algorithm will look for the most suitable location depending on the situation

### Performances

Plugin is designed to handle big server with lot of players and lot of Deadchest at the same time.Many work is pushed to keep great performance. Deadchest only work on loaded chunk to prevent any performance drop.

### Grief protection

The plugin provides a high security against griefer. By default, all deadchests are protected to be only open by his owner. The chest cannot be destroyed moved or opened by other players. Deadchest cannot be destroyed by any type of explosion, cannot be pushed by a piston and cannot be merged with another chest. If you are using player head as Deadchest, it is protect by water destruction also.

### Nether / End / Customs world

Deadchest works on every type of world. The behaviour is the same everywhere.

### Falling out of the world

If a player dies under the map, Deadchest is generated to the lowest possible height corresponding to the dying position. In other words, the plugin will generate the chest at the X and Y coordinates where the player died. For the Z axis, the plugin will take the lowest possible location

### Dying higher than the top of world

This is exactly the same logic than dying under the world expect that will take the highest Z value for chest generation.

### Dying on block / Trap / Door / Torch / Rail etc...

On this situation this is not possible to generated a chest on the block where the player dies without destroying block. For this reason the plugin will search for the next free space at the same X et Y by increasing Z value. For exemple if you dies on the door of your house. You deadchest will be probalby generate on your roof.

> One may wonder why it is not generated right next to it. A malicious player could block access to the door by dying voluntarily. If the chest is protected to be opened only by the owner, the other player will be stuck.

### Water and lava 

The chest will be generate at the dying position.

> In a future update some options will be added to handle differently the generation for water and lava

### Curse of vanishing

Items with enchantement curse of vanishing are not pushed to Deadchest to keep the balance of the game.
