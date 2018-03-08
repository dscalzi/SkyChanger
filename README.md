![# Header](http://i.imgur.com/6TxDQ3W.png?1)

[![](http://ci.aventiumsoftworks.com/jenkins/job/SkyChanger/badge/icon)](http://ci.aventiumsoftworks.com/jenkins/job/SkyChanger/) [![](https://img.shields.io/badge/license-MIT-blue.svg)](https://bitbucket.org/AventiumSoftworks/skychanger/src/025b9ba3b4495921193754e839c75cc78dfb8a93/src/com/dscalzi/skychanger/resources/License.txt) ![](https://img.shields.io/badge/Spigot-1.8--1.12-orange.svg) ![](https://img.shields.io/badge/Java-8+-ec2025.svg) [![](https://discordapp.com/api/guilds/211524927831015424/widget.png)](https://discordapp.com/invite/Fcrh6PT)

SkyChanger is a light-weight plugin built using the Spigot API. The main function of this plugin is to change the color of the sky for yourself, a specific player, a specific world, or everyone. This plugin functions by sending packets with a specified value to the target player(s).

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

**If you would like to contribute your language or correct an incorrect translation, [follow this quick guide](https://bitbucket.org/AventiumSoftworks/skychanger/wiki/Translation%20Guide).**

You can find more extensive details on the [wiki](https://bitbucket.org/AventiumSoftworks/skychanger/wiki/).

***

# Building and Contributing

If you would like to contribute to SkyChanger, feel free to submit a pull request. The project does not use a specific code style, however please keep to the conventions used throughout the code.

To build this project you will need [Maven](https://maven.apache.org/), or an IDE which supports it, and to run the following command:

```shell
mvn clean install
```

---

# Developer API

If you want to hook SkyChanger into your own plugin or simply want to extend functionality, you may use the provided API. If you feel the API is missing anything, or should be changed, please [let us know](https://bitbucket.org/AventiumSoftworks/skychanger/issues?status=new&status=open).

**Download Latest**: [![bintray](https://api.bintray.com/packages/dscalzi/maven/SkyChanger/images/download.svg)](https://bintray.com/dscalzi/maven/SkyChanger/_latestVersion)

*Javadocs are not hosted, however they are provided on the maven repository.*

### Maven

```XML
<repository>
    <id>jcenter</id>
    <name>jcenter-bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>

<dependency>
  <groupId>com.dscalzi</groupId>
  <artifactId>SkyChanger</artifactId>
  <version>VERSION</version>
</dependency>
```

### Gradle

```gradle

repositories {
    jcenter()
}

dependencies {
    compile 'com.dscalzi:SkyChanger:VERSION'
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
	boolean result = api.changeSky(player, 3F);
	
	if(result) {
		player.sendMessage("Why did the sky turn red?");
	}
	
	// Freeze the player.
	result = api.freeze(player);
	
	if(result) {
		player.sendMessage("Where did the land go?");
		
		// Unfreeze the player.
		result = api.unfreeze(player);
		
		if(result) {
			player.sendMessage("You've been unfrozen! Press (F3 + A) to reload chunks.");
		}
	}
}
```


---

# Links

* [Spigot Resource Page](https://www.spigotmc.org/resources/skychanger.37524/)
* [Dev Bukkit Page](https://dev.bukkit.org/projects/skychanger)
* [Suggest Features or Report Bugs](https://bitbucket.org/AventiumSoftworks/skychanger/issues)