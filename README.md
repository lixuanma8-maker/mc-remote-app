# MC Java Mobile Launcher

An Android launcher prototype for **Minecraft: Java Edition**, targeting on-device execution. It is not a Bedrock launcher or a desktop remote controller.

## Current state

The Android Studio project now provides:
- a launcher UI with version selection from Mojang's official release list
- on-demand fetching of the selected version's official metadata
- SHA-1 integrity verification of version metadata against Mojang's version manifest
- display of version, Java requirement, asset index, main class, library count, and client JAR size
- a Java runtime selector
- locally saved profile settings and per-profile directories under the app's private storage
- visible error handling for metadata/version fetch failures

Metadata inspection does **not** download game files, authenticate an account, or launch Minecraft. A checksum confirms data matches Mojang's manifest; it is not a safety certification for this app or APK.

## Build

1. Open the repository root in Android Studio.
2. Install Android SDK Platform 35, NDK, and CMake 3.22.1 through SDK Manager.
3. Sync Gradle and run the `app` configuration on an ARM64 Android device or emulator.

The native scaffold targets `arm64-v8a` and `x86_64`. This project has not been built or device-tested in this environment.

## Next milestones

1. Install and manage a distributable Android JVM/runtime for supported ABIs.
2. Download version metadata, libraries, and assets with hash verification and progress reporting.
3. Implement Microsoft sign-in through a public-client OAuth flow; never ship client secrets.
4. Construct version-correct game arguments and manage the game process/logs.
5. Integrate graphics, window, and input compatibility; test on real devices.

Use a licensed Minecraft account and official metadata/game endpoints. Not affiliated with Mojang or Microsoft.

## License

MIT (see `LICENSE`).
