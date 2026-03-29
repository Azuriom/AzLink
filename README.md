# AzLink

[![Tests](https://img.shields.io/github/actions/workflow/status/Azuriom/AzLink/build.yml?branch=master&style=flat-square)](https://github.com/Azuriom/AzLink/actions/workflows/build.yml)
[![Chat](https://img.shields.io/discord/625774284823986183?color=5865f2&label=Discord&logo=discord&logoColor=fff&style=flat-square)](https://azuriom.com/discord)

AzLink is a plugin to link a Minecraft or Hytale server or proxy with [Azuriom](https://azuriom.com/).

This plugin currently supports the following platforms:
* [Bukkit/Spigot/Paper/Folia](https://papermc.io/)
* [BungeeCord](https://github.com/SpigotMC/BungeeCord)
* [Sponge](https://www.spongepowered.org/)
* [Velocity](https://velocitypowered.com/)
* [Nukkit](https://cloudburstmc.org/articles/)
* [Hytale](https://hytale.com/)

> [!NOTE]
> NeoForge and Fabric versions of the plugin are available in the [dev/mods branch](https://github.com/Azuriom/AzLink/tree/dev/mods).

## Setup

### Installation

The plugin works with the same .jar for all the platforms, except Bukkit/Spigot 1.7.10 which requires the legacy version of the plugin.

You just need to download the plugin, add it to the `plugins` folder of your server, and restart your server.

## Building

AzLink uses [Gradle](https://gradle.org/) for dependency management and builds.

Java **25** JDK or newer is required.

### Compiling from source

```sh
./gradlew build
```

The output JAR files are located in `universal/build/libs` and `universal-legacy/build/libs`.

## PlaceholderAPI Placeholders

On Bukkit-based servers (Spigot, Paper, Folia, ...), AzLink supports [PlaceholderAPI](https://placeholderapi.com/).

By default, the available placeholders are:
* `%azlink_money%`: the amount of money the player has on the Azuriom website

When the shop and/or vote plugins are installed on your Azuriom website, the integration can be enabled in the `config.yml` of the plugin.

### Vote Placeholders
* `%azlink_vote_can_total%`: number of vote sites the player can currently vote on
* `%azlink_vote_can_[id]%`: whether the player can vote on vote site with given ID
* `%azlink_vote_can_[id]_delay%`: delay before player can vote on vote site with given ID
* `%azlink_vote_can_[id]_timestamp%`: date and time when player can vote on vote site with given ID
* `%azlink_vote_user_votes%`: number of votes the player has made this month
* `%azlink_vote_user_position%`: player's position in current month vote ranking
* `%azlink_vote_sites_count%`: number of vote sites enabled on the website
* `%azlink_vote_sites_[id]_name%`: name of vote site with given ID
* `%azlink_vote_sites_[id]_url%`: URL of vote site with given ID
* `%azlink_vote_top_[position]_name%`: name of player at given position in vote ranking
* `%azlink_vote_top_[position]_votes%`: vote count of player at given position in ranking

### Shop Placeholders
* `%azlink_shop_goal_progress%`: current progress of the shop goal this month
* `%azlink_shop_goal_total%`: total amount of the shop goal this month
* `%azlink_shop_goal_percentage%`: percentage progress of the shop goal this month
* `%azlink_shop_top_[position]_name%`: name of top customer at given position this month
* `%azlink_shop_top_[position]_amount%`: amount spent by top customer at given position
* `%azlink_shop_top_[position]_currency%`: currency of top customer at given position
* `%azlink_shop_recent_[position]_name%`: name of recent purchase at given position
* `%azlink_shop_recent_[position]_amount%`: amount of recent purchase at given position
* `%azlink_shop_recent_[position]_currency%`: currency of recent purchase at given position
* `%azlink_shop_recent_[position]_timestamp%`: date and time of recent purchase at given position
