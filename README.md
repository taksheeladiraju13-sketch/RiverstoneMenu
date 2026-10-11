# Riverstone Menu v1.0 — source project

A Paper plugin for the RiverstoneSMP network.

## Features
- Gives a named compass (`§a§lRiverstone Menu`) in hotbar slot 9.
- Restores it on join, after respawn, and periodically if missing.
- Prevents dropping, moving, swapping, or storing the special compass in normal inventory interactions.
- Right-click opens a black/green 27-slot server menu.
- Buttons connect to Velocity server names `Riverstone-pvp` and `lobby` using the BungeeCord-compatible plugin-message channel.
- `/riverstonemenu` (aliases `/rmenu`, `/rs-menu`) opens the menu.
- Works for Java and Geyser/Bedrock players as long as the plugin is installed on the Paper backend they are playing on.

## Important setup
1. Build this project with Gradle on a computer that has internet access and Java 21+:
   `gradle build`
2. Copy `build/libs/RiverstoneMenu-1.0.0.jar` into the `plugins` folder of **each Paper backend** where the compass should exist (lobby, survival, and PvP).
3. Restart those Paper servers.
4. In `velocity.toml`, make sure `bungee-plugin-message-channel = true` is enabled. Restart Velocity after changing it.
5. Server names in `src/main/resources/config.yml` must exactly match the names in Velocity's `[servers]` section.

## Vanilla pause menu limitation
A server plugin cannot replace or open the actual client-side Minecraft Esc/Pause screen. The menu's “Normal Minecraft Menu” button explains how to open it on the player's device.

## Build compatibility note
This source project targets Paper API 1.21.4 as a stable compile baseline. Before using on your current Minecraft/Paper 26.2 network, build/test against the exact Paper API version used by your server if that API is available. The plugin uses long-standing Bukkit APIs, but no one should claim it is tested on 26.2 until it has actually been built and tested there.
