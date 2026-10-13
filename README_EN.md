# Funny Cat Stick

English | [简体中文](README.md)

Funny Cat Stick is a multiplayer content mod for Minecraft 1.20.1 Forge built around funny cat sticks, chat effects, exploration, brewing, and an animated NPC.

## Mod content

- The Funny Cat Stick, multiplayer chat effects, personality enchantments, and related special content.
- A display stand that holds four Funny Cat Sticks, plus the rare Funny ? Stick with mob stasis and a cloning crafting recipe.
- Exploration chest loot, per-player Funny Cat Stick drop guarantees, and optional compatibility with Lootr's personal loot containers.
- Anti-Meow Potions and their brewing chain, rice, fragrant rice, iron pots, and related content.
- The animated dswhale NPC, naturally generated residences, daily behavior, and staged trading.
- Dedicated music, loot integration, persistent progress, and Simplified Chinese and English language support.

## Administrator commands

The following release commands require permission level 2 or above. Player arguments select online players and support vanilla selectors. `<owner>` must select exactly one player; `<targets>` may select one or more players. Durations must be positive and support `s`, `m`, `h`, and `d`, such as `30m`, `1h`, or `0.5d`.

```text
/funnycatstick tame <owner> <targets> <duration>
/funnycatstick query <targets>
/funnycatstick reduce <targets> <duration>
/funnycatstick music
/funnycatstick music <true|false>
/fcs locate dswhale_residence
/fcs place dswhale_residence <x> <y> <z>
```

- `tame`: applies the classic Funny Cat Stick effect to one or more targets and assigns the specified player as their owner.
- `query`: reports the remaining effect duration for one or more targets.
- `reduce`: reduces the remaining duration for one or more targets; the effect ends when it reaches zero.
- `music`: reports the server-wide Ji Ni Tai Mei music setting.
- `music true|false`: enables or disables music playback. It defaults to `false` and is saved with the current world.
- `fcs locate dswhale_residence`: queries successfully generated residences recorded in the save's Overworld and returns clickable coordinates for the one nearest to the command position. Clicking the coordinates prepares a teleport command; it does not execute it. This command only queries existing records and does not generate residences or scan unrecorded old chunks.
- `fcs place dswhale_residence <x> <y> <z>`: must be executed in the Overworld. It uses the coordinates as the template origin and directly generates a complete residence, loot chest, and home-bound dswhale. Absolute and `~` relative coordinates are supported. The result is recorded like a naturally generated residence and can be found by `locate`. The command replaces blocks inside the template area, so check the destination first.

Examples:

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
/fcs locate dswhale_residence
/fcs place dswhale_residence ~ ~ ~
```

## Requirements and installation

- Minecraft 1.20.1
- Forge 47.3.22 or a newer Forge 47.x release
- Java 17
- GeckoLib 4.8.2 or a newer GeckoLib 4.x release
- The same Funny Cat Stick and GeckoLib versions installed on both client and server
- Lootr is optional, not a required dependency; vanilla chest support works without it

Download `funny_cat_stick-0.5.0.jar` from [Releases](https://github.com/bingehanpi/Funny_Cat_Stick/releases/tag/v0.5.0) and place it together with the Minecraft 1.20.1 Forge build of GeckoLib in both the client and server `mods` directories.

## License

All Rights Reserved.
