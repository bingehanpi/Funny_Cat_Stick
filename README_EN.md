# Funny Cat Stick

English | [简体中文](README.md)

Funny Cat Stick is a multiplayer prank mod for Minecraft Forge 1.20.1.

## Requirements

- Minecraft 1.20.1
- Forge 47.4.10 or a compatible Forge 47.x release
- Java 17
- Required on both the client and dedicated server

## What's new in 0.1.0

- Added the single-use, non-stackable Funny Cat Stick with no durability bar.
- Added a dedicated Funny Cat Stick creative-mode tab.
- Using the stick on another player consumes it and applies a 30-minute prank effect.
- While affected, a player's chat messages receive random cute phrases or emoticons without altering their original words.
- Using the item broadcasts one of five random taming messages with the owner and target names.
- The affected player receives reminders at 20 minutes, 10 minutes, 5 minutes, 1 minute, and 10 seconds remaining.
- The effect cannot be removed by ordinary methods such as milk and persists through death and reconnects.
- Every loaded `chests/*` loot table has an approximately 0.6% chance to add one Funny Cat Stick, with at most one added per generation.
- Added OP commands for applying, querying, and reducing effect duration.
- Added a development-only command that statistically tests the real loot-generation pipeline.
- Added English and Simplified Chinese localization and a vanilla-style 16×16 item texture.

## Administrator commands

These commands require permission level 2 or above. Durations support `s`, `m`, `h`, and `d`.

```text
/funnycatstick tame <owner> <targets> <duration>
/funnycatstick query <targets>
/funnycatstick reduce <targets> <duration>
```

Examples:

```text
/funnycatstick tame PlayerA PlayerB 30m
/funnycatstick query @a
/funnycatstick reduce PlayerB 5m
```

## Installation

1. Install Minecraft 1.20.1 and Forge 47.4.10.
2. Place `funny_cat_stick-0.1.0.jar` from the release page in both the client and server `mods` directories.
3. Start the client and server with the same mod version.

## Development and testing

```powershell
.\gradlew.bat build
.\gradlew.bat runServer
.\gradlew.bat runClientPlayer1
.\gradlew.bat runClientPlayer2
.\gradlew.bat stopLocalServer
```

The local test players `DevPlayer1` and `DevPlayer2` automatically receive operator level 4 before `runServer` starts. See [MULTIPLAYER_TESTING.md](MULTIPLAYER_TESTING.md) for details.

## License

All Rights Reserved.
