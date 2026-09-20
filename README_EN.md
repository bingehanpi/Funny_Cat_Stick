# Funny Cat Stick

English | [简体中文](README.md)

Funny Cat Stick is a multiplayer prank mod for Minecraft 1.20.1 Forge. Its single-use stick affects another player and temporarily adds a catgirl personality to their chat messages.

## Features

- The non-stackable Funny Cat Stick is consumed when used on another player and applies a 30-minute effect.
- The target's original message is preserved while personality-specific phrases or emoticons are added.
- Effects persist through death and reconnects, with reminders at 20, 10, 5, and 1 minute and 10 seconds remaining.
- Using a stick broadcasts the owner and target to the server.
- Ordinary chest loot has an approximately 0.6% chance to gain one additional Funny Cat Stick.
- Simplified Chinese and English are supported.

## What's new in 0.2.0

- Added personality enchantments that can only be applied to the Funny Cat Stick. Each changes the target's chat style.
- Added nine normal personalities: Tough, Yandere, Tsundere, Energetic, Lazy, Sharp-Tongued, Timid, Airheaded, and Nekomata Deity.
- Normal personality enchantments are available through vanilla-style sources such as enchanting tables, villager trades, fishing, and enchanted-book loot. The stick can only receive these normal enchantments at an enchanting table.
- Added the treasure enchantment Ji Ni Tai Mei Catgirl. Its enchanted book is currently available only as a rare open-water fishing reward.
- Personality enchantments are mutually exclusive. A target's active personality is determined by the most recent stick used on them.
- Ji Ni Tai Mei messages may receive a prefix, suffix, or both.
- Ji Ni Tai Mei sticks can play a dedicated track only for the owner and target. This server-wide feature is disabled by default and controlled by administrators.
- Fixed full enchanting tables incorrectly reporting limited enchanting power and locking options.

## Administrator commands

These commands require permission level 2 or above. Player arguments support vanilla selectors. Durations support `s`, `m`, `h`, and `d`, such as `30m` or `1h`.

```text
/funnycatstick tame <owner> <targets> <duration>
/funnycatstick query <targets>
/funnycatstick reduce <targets> <duration>
/funnycatstick music
/funnycatstick music <true|false>
```

- `tame`: applies the classic Funny Cat Stick effect to the targets.
- `query`: reports the targets' remaining effect duration.
- `reduce`: reduces the targets' remaining effect duration.
- `music`: reports the server-wide Ji Ni Tai Mei music setting.
- `music true|false`: enables or disables music playback. It defaults to `false` and is saved with the current world.

Examples:

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
/funnycatstick music true
```

## Requirements and installation

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- The same mod version installed on both client and server

Place `funny_cat_stick-0.2.0.jar` from the release page in both the client and server `mods` directories, then start the game.

## Development and testing

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
.\gradlew.bat runClientPlayer1
.\gradlew.bat runClientPlayer2
.\gradlew.bat stopLocalServer
```

See [MULTIPLAYER_TESTING.md](MULTIPLAYER_TESTING.md) for multiplayer testing details.

## License

All Rights Reserved.
