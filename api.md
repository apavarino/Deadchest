## Deadchest - API

Deadchest allow you to manage plugin data with an API since version `4.6.0`. This section explains how to use the API of Deadchest.


### Methods  

Here is a list of method that you can use 

Command | Description | Version
--- | --- | --- |
`public static List<ChestData> getChests(Player player)` | Get all chests of the given player | since `4.6.0`
`public static boolean giveBackChest(Player player, ChestData chest)` | Get back the given chest to the given player | since `4.6.0`
`public static boolean removeChest(ChestData chest)` | Remove the given chest | since `4.7.0`

> All theses methods are in DeadchestAPI class.

### Events 

Command | Description | Version
--- | --- | --- |
`DeadchestPickUpEvent` | Triggered when a player pick a Deadchest | since `4.7.0`

### Notes

If you are a developer and you need more tools from Deadchest to work with. Contact me on Discord.

### Next step
See [contribution](https://apavarino.github.io/Deadchest/contribution) or go to [home page](https://apavarino.github.io/Deadchest)
