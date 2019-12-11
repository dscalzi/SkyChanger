![# Header](http://i.imgur.com/6TxDQ3W.png?1)

[<img src="https://ci.appveyor.com/api/projects/status/3j1tc074rvi6a3mr?retina=true" height='20.74px'></img>](https://ci.appveyor.com/project/dscalzi/skychanger) [![](https://pluginbadges.glitch.me/api/v1/dl/Downloads-limegreen.svg?bukkit=skychanger&spigot=skychanger.37524&ore=SkyChanger&github=dscalzi/SkyChanger&style=flat)](https://github.com/dscalzi/PluginBadges) [![](https://img.shields.io/github/license/dscalzi/SkyChanger.svg)](https://github.com/dscalzi/SkyChanger/blob/master/LICENSE.txt) ![](https://img.shields.io/badge/Spigot-1.8.x--1.15.x-orange.svg) [![](https://discordapp.com/api/guilds/211524927831015424/widget.png)](https://discordapp.com/invite/Fcrh6PT)

SkyChanger is a light-weight plugin for Spigot and Sponge. The main function of this plugin is to change the color of the sky for yourself, a specific player, a specific world, or everyone. This plugin functions by sending packets with a specified value to the target player(s).

***

# Feature List

* Allow players to change the color of their personal sky.
* Change the sky color for specific players.
* Change the sky color for a specific world.
* Change the sky color for everyone online.
* Freeze/Unfreeze yourself, others, a world, or everyone online.
* Configurable limits to the range of packets that can be sent.
* Usage messages tailored to specific users based on permission level.
* Multilanguage support.
* Metrics tracking by [bStats](https://bstats.org/plugin/bukkit/SkyChanger).

**If you would like to contribute your language or correct an incorrect translation, [follow this quick guide](https://github.com/dscalzi/SkyChanger/wiki/Translation-Guide).**

You can find more extensive details on the [wiki](https://github.com/dscalzi/SkyChanger/wiki).

***

# Building and Contributing

If you would like to contribute to SkyChanger, feel free to submit a pull request. The project does not use a specific code style, however please keep to the conventions used throughout the code.

You can build SkyChanger using [Gradle](https://gradle.org/). Clone the repository and run the following command.

```shell
$ gradlew build
```

---

# Developer API

If you want to hook SkyChanger into your own plugin or simply want to extend functionality, you may use the provided API. If you feel the API is missing anything, or should be changed, please [let us know](https://github.com/dscalzi/SkyChanger/issues).

**Download Latest**:

Bukkit: [![bintray](https://api.bintray.com/packages/dscalzi/maven/SkyChanger-Bukkit/images/download.svg)](https://bintray.com/dscalzi/maven/SkyChanger-Bukkit/_latestVersion) Sponge: [![bintray](https://api.bintray.com/packages/dscalzi/maven/SkyChanger-Sponge/images/download.svg)](https://bintray.com/dscalzi/maven/SkyChanger-Sponge/_latestVersion)

*Javadocs are not hosted, however they are provided on the maven repository.*

### Maven

```XML
<repository>
    <id>jcenter</id>
    <name>jcenter-bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>

<!-- For Bukkit -->
<dependency>
  <groupId>com.dscalzi</groupId>
  <artifactId>SkyChanger-Bukkit</artifactId>
  <version>VERSION</version>
</dependency>

<!-- For Sponge -->
<dependency>
  <groupId>com.dscalzi</groupId>
  <artifactId>SkyChanger-Sponge</artifactId>
  <version>VERSION</version>
</dependency>
```

### Gradle

```gradle

repositories {
    jcenter()
}

dependencies {
    // For Bukkit
    compile 'com.dscalzi:SkyChanger-Bukkit:VERSION'
    // For Sponge
    compile 'com.dscalzi:SkyChanger-Sponge:VERSION'
}
```

### Example Usage

```java
/**
* Example usage of the API. The following implementation
* would not be practical, it exists only to demonstrate
* the API capabilities.
* 
* @param player The player to experiment on.
*/
public void skychangerTests(Player player) {
    // Get a reference to the API.
    final SkyAPI api = SkyChanger.getAPI();

    // Change the sky and save the result.
    // Equivalent to /SkyChanger 3
    boolean result1 = api.changeSky(SkyChanger.wrapPlayer(player), SkyPacket.FADE_VALUE, 3F);

    if(result1) {
        player.sendMessage("Why did the sky turn red?");
    }

    // Equivalent to /SkyChanger 4 8
    boolean result2 = api.changeSky(SkyChanger.wrapPlayer(player), SkyPacket.FADE_VALUE, 4F)
                 && api.changeSky(SkyChanger.wrapPlayer(player), SkyPacket.FADE_TIME, 8F);

    if(result2) {
        player.sendMessage("Why did the sky turn light blue?");
    }

    // Freeze the player.
    boolean result3 = api.freeze(SkyChanger.wrapPlayer(player));

    if(result3) {
        player.sendMessage("Where did the land go?");

        // Unfreeze the player.
        result3 = api.unfreeze(SkyChanger.wrapPlayer(player));

        if(result3) {
            player.sendMessage("You've been unfrozen! Press (F3 + A) to reload chunks.");
        }
    }
}
```


---

# Links

* [Spigot Resource Page](https://www.spigotmc.org/resources/skychanger.37524/)
* [Dev Bukkit Page](https://dev.bukkit.org/projects/skychanger)
* [Sponge Ore Page](https://ore.spongepowered.org/TheKraken7/SkyChanger)
* [Suggest Features or Report Bugs](https://github.com/dscalzi/SkyChanger/issues)
