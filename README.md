<h1  align="center">
    <img src="deadchest-logo.png" alt="Deadchest" width="800" /><br>
</h1>

<h2  align="center">
    <img src="http://cf.way2muchnoise.eu/full_322882_downloads.svg" alt="download"/> 
    <img src="https://img.shields.io/github/license/apavarino/deadchest" alt="licence"/>
    <img src="https://img.shields.io/github/last-commit/apavarino/deadchest" alt="commit"/>
</h2>


**Keep your inventory in a chest when you die !**

Java Edition required. Deadchest is mainly compatible with Bukkit, Spigot and Paper.

## Download

* [Curseforge](https://www.curseforge.com/minecraft/bukkit-plugins/dead-chest)
* [Bukkit](https://dev.bukkit.org/projects/dead-chest)

## Documentation

Official Deadchest Documentation is available [here](https://apavarino.github.io/Deadchest/)

## Statistics

<img align="center" src="https://bstats.org/signatures/bukkit/Deadchest.svg" alt="stats"/> 

More stats [here](https://bstats.org/plugin/bukkit/DeadChest/11385)

## Contribution

Modified by @kittycraft0 on 7/12/2025 to add inventory return upon chest timer expiration. This is a fork of The_It_G's DeadChest plugin. It adds an automatic item return feature based on a player's online time. For the original plugin, please see the official page. All credit for the core functionality goes to the original author.

Feel free to contribute to the project if you want it. here is some information to help you

The project is built with Gradle. Use the included gradle wrapper to build the project.

### Build

To build the project on linux/macOS, from the root folder of the project type

```
./gradlew build
```

On Windows

```
gradlew.bat build
```

### Generate jar

To generate the .jar file use

```
./gradlew shadowJar
```

The jar file will be generated on `/build/libs`

### Testing

To simplify testing of the plugin you can configure on your `gradle.properties` the following configuration

```
pluginDir=<path_to_your_plugin_folder_of_your_server>
```

After that, you can use

```
./gradlew copyJar --continuous
```

That will automatically rebuild the jar on each modification and copy it directly to the plugin folder of your server.