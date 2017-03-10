![# Header](http://i.imgur.com/6TxDQ3W.png?1)

##Contents##
* [Feature List](#markdown-header-feature-list)
* [Commands](#markdown-header-commands)
    * [Packet Number Key](#markdown-header-packet-number-key)
* [Permissions](#markdown-header-permissions)
    * [Permission Tree](#markdown-header-permission-tree)
* [Configuration File](#markdown-header-configuration-file)
* [Screenshots](#markdown-header-screenshots)

***

SkyChanger is a light-weight plugin built using the Spigot API. The main function of this plugin is to change the color of the sky for yourself, a specific player, or everyone. This plugin functions by sending a packet with a specified ID to the target player(s). **Every packet number that is not 0 will cause rain to appear, and higher magnitude packer numbers increase the intensity of the rain. For an optimal experience it is recommended to use this plugin with [Optifine][optifinelink] and to turn rain and rain particles off. If this is not possible or inconvenient, rain is automatically turned off in `Desert` and `Mesa` biomes. You may want to use this plugin exclusively there.** 

***

#Feature List



* Allow players to change the color of their personal sky.
* Change the sky color for specific players.
* Change the sky color for everyone online.
* Configurable limits to the range of packets that can be sent.
* Usage messages tailored to specific users based on permission level.
* Metrics tracking by [bStats](https://bstats.org/plugin/bukkit/SkyChanger).

***

#Commands



Command | Description | Required Permission
:------ | :---- | :-----------
**/SkyChanger <#>** | Change the color of your personal sky using a packet number. | `skychanger.changesky.self`
**/SkyChanger <#> [player]** | Change the sky color for a specific player. The player argument may either be a name or UUID. | `skychanger.changesky.others`
**/SkyChanger <#> [@​a]** | Change the sky color for everyone online. | `skychanger.changesky.all`
**/SkyChanger reload** | Reload the configuration file. | `skychanger.reload`
**/SkyChanger version** | Display plugin version information. | -

Command usage messages are tailored to the permission level of each user. For example, if a user only had the permission `skychanger.changesky.self`, the usage message they would see is `/SkyChanger <#>`. If a user had the permission `skychanger.changesky.*`, the usage message would be `/SkyChanger <#> [player | @​a]`. Further, if a user only had the permission `skychanger.changesky.others`, the usage message would be displayed as `/SkyChanger <#> <player>`

Also, if a player does not have permission for a command altogether they will not see it on the help page brought up by typing either `/SkyChanger` or `/SkyChanger help`.

####**Packet Number Key**

The packet numbers will be bound to the range you specify in the config.yml, however anyone with the permission `skychanger.bypasslimit` will be able to specify any number for the packet. **The highest and lowest numbers for a packet are `2147483647` and `-12147483648`, respectively. This is because the packet number is a 32-bit signed integer.**

*Below is a table of useful packet numbers to know*

**NUMBERS OF HIGH MAGNITUDE WILL INCREASE RAIN AND RAIN PARTICLES, THIS CAN CAUSE YOU TO LAG OUT AT A CERTAIN POINT. TAKE CAUTION WHEN PICKING A PACKET NUMBER!**

Number/Range | Description
:----------- | :----------
[`-2147483648`, `-2`] | No observable difference from -1, however there will be more rain and particles.
`-1` | Makes the stars brighter at night.
`0` | Sunny sky.
`1` | Rain.
`2` | Brown colored sky.
[`3`, `6`] | Nether sky, larger numbers cause more darkness.
[`7`, `~15`] | Black sky with yellow tinted light. Reduced shadows with higher numbers.
[`~15`, `2147483647`] | No observable change, however there will be more rain and particles.

These numbers are just the standard effects. The effects will change if you have night vision on, for example. The effects work best in the Overworld and may not produce any changes in the Nether or End.

**If you want to reset your sky, your best option is to use a packer number of `0` or reconnect to the server you're on.**

***

#Permissions

Permission | Descrption | Default
:--------- | :--------- | :-----
`skychanger.*` | Access to all SkyChanger commands. | OP
`skychanger.changesky.*` | Access to every part of the main SkyChanger command. | OP
`skychanger.changesky.self` | Access to change your personal sky color. | OP
`skychanger.changesky.others` | Access to changing a specific person's sky color. | OP
`skychanger.changesky.all` | Access to changing the sky color of all online players. | OP
`skychanger.bypasslimit` | Bypass the packet range limits set in the config.yml. | OP
`skychanger.reload` | Access to reload the configuration. | OP

####**Permission Tree**

skychanger.*
>
> skychanger.changesky.*
>
>> skychanger.changesky.self
>>
>> skychanger.changesky.others
>>
>> skychanger.changesky.all
>
> skychanger.bypasslimit
>
> skychanger.reload

***

#Configuration File



```
#!YAML

#-----------------------------------------------
#               Sky Changer Config
#-----------------------------------------------

# DO NOT CHANGE THIS VALUE.
# CHANGING IT COULD RESULT IN DATA LOSS.
ConfigVersion: 1.0

#--------------[General Settings]---------------
general_settings:

  # Upper packet limit.
  upper_limit: 50
  
  # Lower packet limit.
  lower_limit: -50

  # Opt out of metrics collection by https://bstats.org/
  # Note that if opting out you must fully restart
  # your server for the changes to take effect. If opting
  # back in you may simply run /SkyChanger reload.
  #
  # Please consider keeping this value false :)
  metrics_opt_out: false

```

***

#Screenshots

*All screenshots taken with [Optifine][optifinelink] and with both rain and rain particles off.*

![Packet 0 at Night](http://i.imgur.com/SysNS9s.png "Packet 0 at Night")
*Packet 0 at Night*

![Packet -1 at Night](http://i.imgur.com/CAwAPre.png "Packet -1 at Night")
*Packet -1 at Night*

![Packet 0 at Day](http://i.imgur.com/CJR9Rkt.png "Packet 0 at Day")
*Packet 0 at Day*

![Packet 2 at Day](http://i.imgur.com/352OY2Y.png "Packet 2 at Day")
*Packet 2 at Day*

![Packet 3 at Day](http://i.imgur.com/c57mFUf.png "Packet 3 at Day")
*Packet 3 at Day*

![Packet 4 at Day](http://i.imgur.com/idaYiJs.png "Packet 4 at Day")
*Packet 4 at Day*

![Packet 5 at Day](http://i.imgur.com/w3ikbvn.png "Packet 5 at Day")
*Packet 5 at Day*

![Packet 6 at Day](http://i.imgur.com/FG7ywbz.png "Packet 6 at Day")
*Packet 6 at Day*

![Packet 7 at Day](http://i.imgur.com/razF75g.png "Packet 7 at Day")
*Packet 7 at Day*

![Packet 8 at Day](http://i.imgur.com/c7TPui3.png "Packet 8 at Day")
*Packet 8 at Day*

![Packet 15 at Day](http://i.imgur.com/ISq65Rl.png "Packet 15 at Day")
*Packet 15 at Day*

![Packet 7 with NightVision at Day](http://i.imgur.com/VqRHsl2.png "Packet 7 with NightVision at Day")
*Packet 7 with NightVision at Day*

[optifinelink]: http://optifine.net/ "Optifine Website"