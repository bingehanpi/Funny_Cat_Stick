# Funny Cat Stick

English | [简体中文](README.md)

Funny Cat Stick is a multiplayer content mod for Minecraft 1.20.1 Forge, centered on chat pranks, personality enchantments, and an animated NPC.

## Mod content

- The single-use Funny Cat Stick applies a catgirl chat effect to another player while preserving their original message.
- Personality enchantments change the resulting chat style. The mod includes both normal enchantments obtainable through vanilla-style sources and a special treasure enchantment.
- Effects persist through death and reconnects and notify affected players as their remaining time approaches zero.
- A special personality can play dedicated music controlled by server administrators; only the relevant players hear it.
- Ordinary chest loot has a small chance to gain one additional Funny Cat Stick.
- Adds the animated NPC `dswhale` (大肥鱼) and a Creative-mode spawn egg. Dswhale supports basic activity, looking, opening doors, returning home, and sleeping at night.
- Newly generated villages in the `#minecraft:village` structure tag can receive a nearby dswhale residence. This also supports modded villages that correctly use the tag.
- Each residence contains one home-bound dswhale and a dedicated loot chest.
- Simplified Chinese and English are supported.

## Administrator commands

The following release commands require permission level 2 or above. Player arguments support vanilla selectors. Durations support `s`, `m`, `h`, and `d`, such as `30m`, `1h`, or `0.5d`.

```text
/funnycatstick tame <owner> <targets> <duration>
/funnycatstick query <targets>
/funnycatstick reduce <targets> <duration>
/funnycatstick music
/funnycatstick music <true|false>
/fcs locate dswhale_residence
```

- `tame`: applies the classic Funny Cat Stick effect to one or more targets and assigns the specified player as their owner.
- `query`: reports the remaining effect duration for one or more targets.
- `reduce`: reduces the remaining duration for one or more targets; the effect ends when it reaches zero.
- `music`: reports the server-wide Ji Ni Tai Mei music setting.
- `music true|false`: enables or disables music playback. It defaults to `false` and is saved with the current world.
- `fcs locate dswhale_residence`: finds the nearest successfully generated dswhale residence recorded in the current world data and returns clickable coordinates. It does not find residences that have not generated and does not retroactively scan old chunks.

Examples:

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
/fcs locate dswhale_residence
```

## Requirements and installation

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- GeckoLib 4.8.4 or a compatible GeckoLib 4.x release
- The same Funny Cat Stick and GeckoLib versions installed on both client and server

Download `funny_cat_stick-0.3.0.jar` from Releases and place it together with the Minecraft 1.20.1 Forge build of GeckoLib in both the client and server `mods` directories.

## License

All Rights Reserved.
