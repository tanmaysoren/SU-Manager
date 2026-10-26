# SU-Manager

SU-Manager is a merged single app (`su.manager`): the AppManager fork with a full
Shizuku manager (Shizuku-Next) built into it from source, plus the GPS & IP
spoofer module.

- `AppManager-master/` — AppManager fork (app UI, app ops, tracking, rules). The
  Gradle build embeds the two other projects via `includeBuild`.
- `Shizuku-Next/` — Shizuku manager, integrated upstream v14.0.29-next
  (32 upstream commits) with additional start methods: `Root (custom command)`,
  `Via Shizuku` (starts through the running Shizuku itself - its own shell stands
  in for the adb session), and wireless/USB/system starts.
- `gps-&-ip-spoofer/` — GPS & IP spoofing module, also embedded the same way.

## Building

1. Install JDK 21 and the Android SDK/NDK.
2. Put your SDK path in `local.properties` (`sdk.dir=...`).
3. In `Shizuku-Next/local.properties` set `shizuku.manager.applicationId` to the
   host application id (`su.manager`) so the embedded manager matches the host.
4. `./gradlew :app:assembleRelease` from `AppManager-master/`.
   (Gradle wrapper files are not included in this repo; use a local Gradle that
   matches `gradle/wrapper` versions, which are declared in the project files.)

Release builds are signed with the public AOSP testkey (`app/testkey.p12`,
password `testkey123`) so builds install over each other.

## Notes

- The app presents the embedded Shizuku manager as `Shizuku` inside its UI.
- `Via Shizuku` start method: once the server is alive (e.g. after a reboot, the
  upstream boot pipeline can start it), this method starts everything else
  without touching wireless/USB debugging switches.
