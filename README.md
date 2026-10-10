# MC Remote Launcher

A remote-control Android launcher for Minecraft Java Edition.

This project is designed as a mobile app that talks to a desktop PC agent. The Android app does not run Minecraft directly; instead, it connects to a Java desktop launcher running on your PC, fetches version data, and tells the PC to start the game.

It is intended to support:
- Android app UI for selecting versions and profiles
- Desktop PC agent for launching Minecraft
- Mod installation from Modrinth and CurseForge
- Optional Microsoft OAuth / account login flow
- Local network communication between phone and desktop

## Why this architecture?

Minecraft Java Edition is a desktop application that depends on a full Java runtime, native libraries, and game assets. An Android APK cannot directly run the Java Edition in a portable way without a desktop environment and a large amount of compatibility work.

This project takes the practical route:
- Android app = remote launcher controller
- PC agent = desktop launcher host
- Minecraft = runs on the PC where it belongs

## Project structure

- `android-app/` — Android Studio project for the mobile app
- `desktop-agent/` — Java PC agent that listens for requests from the phone
- `desktop-launcher/` — desktop launcher entrypoint used by the PC agent
- `docs/` — notes and configuration guidance

## Features planned

- Version manifest fetch from Mojang
- Launch selected version on the PC
- Profile selection and profile directory support
- Mod installation from Modrinth
- CurseForge support with API key
- Microsoft OAuth / account token flow
- Local pairing or API-key configuration
- Java version selection for launcher runtime compatibility

## Java support strategy

A modern launcher should support multiple Java runtimes. Recommended compatibility policy:
- Java 8: legacy versions only
- Java 17: best default target for modern Minecraft
- Java 21: newer versions and modern modpacks
- Java 25: experimental only, not recommended for production launcher use

## Authentication notes

This project includes a Microsoft OAuth skeleton and AppAuth flow for Android. In production, you should:
- register a Microsoft app in Azure / Microsoft Entra
- use PKCE (already recommended with AppAuth)
- store tokens securely
- keep client secrets out of public client apps

For local testing, you can start with placeholders and replace them with your actual app registration details.

## Mod installation notes

Mod install support includes:
- Modrinth public API
- CurseForge API (requires an API key)
- installation into the selected profile’s `mods/` directory

Important:
- the selected mod must match the target Minecraft version and modloader
- Fabric/Forge/Quilt installations are separate concerns and should be handled explicitly
- test mods with the correct game version before using them in production

## Quick start

### Android app

1. Open `android-app/` in Android Studio.
2. Set the default PC agent URL in the app settings.
3. Set your API key and profile name.
4. Build the project and install the APK on Android.

### Desktop PC agent

1. Compile the Java agent in `desktop-agent/`.
2. Set the API key value used by the app.
3. Start the agent on the desktop PC.
4. Ensure the phone can reach the PC on the local network.
5. Use the correct host like `http://192.168.1.50:25565/`.

### CurseForge support

CurseForge requires an API key. Set it as an environment variable or config value before running the desktop PC agent:

```bash
export CURSEFORGE_API_KEY=your_key_here
```

## Security recommendations

- Use a local-only network or trusted Wi‑Fi when testing
- Use a strong API key or pairing flow for the mobile app
- Consider TLS / reverse proxy if exposing the agent beyond your LAN
- Do not hardcode secrets in public repositories
- Use OS secure storage for persisted tokens when possible

## License

MIT License

## Notes

This repository is a starter project and may evolve as the real launcher logic is implemented. The core idea is to keep the game runtime on the PC while the phone acts as a controller and a mod manager.

For a production-grade launcher, you will still need to implement:
- actual Microsoft login token exchange
- full Minecraft version download flow
- proper Java runtime selection
- native extraction and launch arguments
- profile management and modloader support

## Status

This repo is currently a scaffold and starting point for a mobile launcher/controller project.
