## Deadchest - Customization 

This section explains how to customize Deadchest


On your `plugins` folder you will find a file named `locale.yml`. You can edit this file to change any translation of the plugin or change any color. You can change messages that are displayed in the chat and also also customize holograms of deadchests.

### Text edition

If you want to change any translation, just update the text and put what you want. If you need to put some special characters, please put your translation between quotes or you will have an error on server startup.

There are several variables you can use


Command | Description 
--- | --- | 
`%player%` | Write the name of the current player for `holo_owner`
`%hours%` | Write the amount of hour left before chest expires for `holo_timer`
`%min%` | Write the amount of minutes left before chest expires for `holo_timer`
`%sec%` | Write the amount of secondes left before chest expires for `holo_timer`


### Customize colors and effects

As you can see, there is some special chracters with the text. This is to handle colors. Here is an image to illustrate how it works.

<div  align="center">
    <img src="https://hypixel.net/attachments/2694189" alt="Colors Code" width="600" /><br>
</div>

> You need to use `§` instead of `&`
> 
> Example : I want to have `Hello !` with  `Hel` in blue and `lo !` in red I will write `§1Hel§4lo !`

### Customize Holograms

All translations are prefixed by `loc` or `holo`. The first prefix is for chat text, the second one is for hologram

If you want to edit the timer, you need to update `holo_timer`. To edit the owner part, you have to edit `holo_owner`

### Next step
See [worldguard part](https://apavarino.github.io/Deadchest/worldguard) or go to [home page](https://apavarino.github.io/Deadchest)
