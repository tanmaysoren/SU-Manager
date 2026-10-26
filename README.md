# SU-Manager

SU-Manager is a merged single app (`su.manager`): the AppManager fork (this
repo's root project) with a full Shizuku manager (Shizuku-Next) and the GPS &
IP spoofer built into it from source, in one Gradle build.

- Root project — the AppManager fork: app UI, app ops, tracking, rules. The two
  embedded projects are wired in `settings.gradle` via `includeBuild`, so
  nothing is published - everything compiles from source together.
- `Shizuku-Next/` — Shizuku manager, integrated upstream v14.0.29-next
  (32 upstream commits) plus additional start methods: `Root (custom command)`,
  `Via Shizuku` (starts through the running Shizuku itself - its own shell
  stands in for the adb session), and wireless/USB/system starts.
- `gps-&-ip-spoofer/` — GPS & IP spoofing module, folded in the same way.

## Building

1. Install JDK 21 and the Android SDK/NDK; put the SDK path in `local.properties`
   (`sdk.dir=...`).
2. `Shizuku-Next/local.properties` pins `shizuku.manager.applicationId` to
   `su.manager` so the embedded manager matches the host app - keep that as is.
3. Build with Gradle (wrapper files are not in this repo; use a local Gradle
   matching the versions in the project files):
   `gradle :app:assembleRelease`.

Release builds are signed with the public AOSP testkey (`app/testkey.p12`,
password `testkey123`) so builds install over each other.

## Notes

- The app presents the embedded Shizuku manager as `Shizuku` inside its UI.
- `Via Shizuku` start method: once the server is alive (e.g. after a reboot, the
  upstream boot pipeline can start it), this method starts everything else
  without touching wireless/USB debugging switches.
