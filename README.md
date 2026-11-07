# MC Java Mobile Launcher

An Android launcher prototype for **Minecraft: Java Edition**. This is not a Bedrock launcher and does not run the game on a desktop PC. The intended target is on-device Android execution, in the same broad category as community launchers such as Zalith Launcher and PojavLauncher.

## Current state

This commit adds an Android Studio project, an initial version/runtime/profile UI, and a tiny C++ JNI ABI diagnostic. It is an **early scaffold, not yet a playable launcher**: the JVM/runtime, graphics compatibility layer, Minecraft library/assets downloader, Microsoft authentication, and game process startup are not implemented. The Play button deliberately reports that the backend is missing rather than implying the game can already launch.

Android Java Edition launchers require more than a standard JRE/JDK. They need a compatible ARM Java runtime, native library and graphics integration (often OpenGL translation), version-specific asset/library resolution, and a valid authenticated session. A desktop JDK is generally not needed on the phone to play; some game versions require different runtime generations.

## Open in Android Studio

1. Open the repository root as a Gradle project.
2. Install Android SDK Platform 35, NDK, and CMake 3.22.1 through SDK Manager.
3. Sync Gradle and run the `app` configuration on an ARM64 Android device or emulator.

The native target currently builds for `arm64-v8a` and `x86_64`. This scaffold has not been built or device-tested in this environment.

## Next implementation milestones

1. Integrate a legally distributable Android JVM/runtime and its native dependencies for supported ABIs.
2. Add Mojang version manifest, library and asset downloads with checksums and resumable progress.
3. Add Microsoft sign-in using an appropriate public-client OAuth flow; never ship client secrets.
4. Implement profile-specific game arguments and process lifecycle/log capture.
5. Integrate Android graphics/window/input compatibility and test on real devices.

Use a licensed Minecraft account and obtain game files through official metadata and endpoints. This project is not affiliated with or endorsed by Mojang or Microsoft.

## License

MIT (see `LICENSE`).
