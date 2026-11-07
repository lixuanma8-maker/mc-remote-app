# MC Java Mobile Launcher

An Android launcher prototype for **Minecraft: Java Edition**, targeting on-device execution. It is not a Bedrock launcher or a desktop remote controller.

## Current state

This Android Studio project currently provides a launcher UI and fetches the latest stable version IDs from Mojang's public version manifest. Network errors are shown in the app, and the user can refresh the list. This does **not** download game files or launch Minecraft yet.

Still required for a playable launcher: an Android-compatible Java runtime, native/graphics integration, version-specific library and asset downloads, Microsoft authentication, game arguments, and process lifecycle management. A desktop JDK is not needed to play on the phone; the game needs an Android-compatible runtime appropriate for the selected game version.

## Build

1. Open the repository root in Android Studio.
2. Install Android SDK Platform 35, NDK, and CMake 3.22.1 through SDK Manager.
3. Sync Gradle and run the `app` configuration on an ARM64 Android device or emulator.

The native scaffold targets `arm64-v8a` and `x86_64`. This project has not been built or device-tested in this environment.

## Next milestones

1. Integrate a distributable Android JVM/runtime for supported ABIs.
2. Download version metadata, libraries, and assets with hash verification and progress reporting.
3. Implement Microsoft sign-in through a public-client OAuth flow; never ship client secrets.
4. Construct version-correct game arguments and manage the game process/logs.
5. Integrate graphics, window, and input compatibility; test on real devices.

Use a licensed Minecraft account and official metadata/game endpoints. Not affiliated with Mojang or Microsoft.

## License

MIT (see `LICENSE`).
