# Funny Cat Stick

English | [简体中文](README.md)

Funny Cat Stick is a multiplayer content mod for Minecraft 1.20.1 Forge featuring chat pranks, personality enchantments, food, and an animated NPC.

## Mod content

- The Funny Cat Stick, multiplayer chat effects, and several personality enchantments.
- Special content such as Ji Ni Tai Mei, dedicated music, and related server settings.
- Cooked rice, fragrant rice, iron pots, and their crafting and food content.
- The animated dswhale NPC, village residences, daily behavior, and staged trading.
- Loot integration, persistent progress, and Simplified Chinese and English language support.

## Administrator commands

The following release commands require permission level 2 or above. Player arguments support vanilla selectors. Durations support `s`, `m`, `h`, and `d`, such as `30m`, `1h`, or `0.5d`.

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
- `fcs locate dswhale_residence`: finds the nearest successfully generated dswhale residence recorded in the current world data and returns clickable coordinates. It does not find residences that have not generated and does not retroactively scan old chunks.
- `fcs place dswhale_residence <x> <y> <z>`: uses the coordinates as the template origin and directly generates a complete residence, loot chest, and home-bound dswhale in the Overworld. Absolute and `~` relative coordinates are supported. The result is recorded like a naturally generated residence and can be found by `locate`. The command replaces blocks inside the template area, so check the destination first.

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

Download `funny_cat_stick-0.3.1.jar` from Releases and place it together with the Minecraft 1.20.1 Forge build of GeckoLib in both the client and server `mods` directories.

## License

All Rights Reserved.
