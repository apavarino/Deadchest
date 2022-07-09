## Deadchest - Troubleshooting

This section explains how to resolve most common issues that you can have with the plugin.

### Hologram remains

Type `/dc repair` next the the hologram you want to remove. You need to have enough permission on the server. If that doesn't work try this command

```
/minecraft:kill @e[sort=nearest,limit=1,type=minecraft:armor_stand]
```

### Plugin don't work after update

You must remove your deadchest folder `plugins/deadchest` to do the update. Be sure that there is no active deadchest in your server before. You can remove active deadchests with `/dc removeall` command

### Next step
See [api part](https://apavarino.github.io/Deadchest/api) or go to [home page](https://apavarino.github.io/Deadchest)
