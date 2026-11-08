# MC Java Mobile Launcher

An Android launcher prototype for **Minecraft: Java Edition**, targeting on-device execution. It is not a Bedrock launcher or a desktop remote controller.

## Current state

The Android Studio project currently provides:
- a launcher UI with version selection from Mojang's official release list
- on-demand fetching of the selected version's official metadata, with manifest SHA-1 verification
- a download button for the selected version's **client JAR only**
- HTTPS host allowlisting, redirect checks, size limits, exact-length validation, and SHA-1 verification against Mojang's version metadata
- storage of the verified client JAR in the app-private selected-profile directory
- locally saved profile settings and per-profile directories
- visible progress and error messages for metadata/download operations

The downloader does not install or execute the JAR. It does not yet download libraries or assets, install an Android-compatible Java runtime, authenticate a Minecraft account, or launch the game. A SHA-1 match verifies integrity against Mojang's published metadata; it is not a safety certification for this APK.

## Build and test

1. Open the repository root in Android Studio.
2. Install Android SDK Platform 35, NDK, and CMake 3.22.1 through SDK Manager.
3. Sync Gradle and run the `app` configuration on an ARM64 Android device or emulator.
4. Load the version list, select a release, and tap **Download verified client JAR**. Check that the progress completes and the success message reports a verified hash.

The native scaffold targets `arm64-v8a` and `x86_64`. This project has not been built or device-tested in this environment. Because a previous build showed a Play Protect harmful-app warning, do not bypass that warning or install an unreviewed APK on a personal device; use source review and an isolated emulator for testing.

## Next milestones

1. Parse and download version-specific libraries and assets with official hashes and safe storage limits.
2. Integrate and manage a distributable Android JVM/runtime for supported ABIs.
3. Implement Microsoft sign-in through a public-client OAuth flow; never ship client secrets.
4. Construct version-correct game arguments and manage the game process/logs.
5. Integrate graphics, window, and input compatibility; test on real devices.

Use a licensed Minecraft account and official metadata/game endpoints. Not affiliated with Mojang or Microsoft.

## License

MIT (see `LICENSE`).
