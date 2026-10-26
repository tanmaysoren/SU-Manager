# SU-Manager

**One app — `su.manager`.** SU-Manager merges the [AppManager](https://github.com/MuntashirAkon/AppManager)
app-operations toolkit and a full [Shizuku-Next](https://github.com/rushiranpise/Shizuku-Next)
manager into a single Android application, with the GPS & IP spoofer module
built alongside. Everything in this repository compiles from source in one
Gradle build.

## What's inside

| Path | What it is |
|---|---|
| `app/` … (root project) | The AppManager fork: app UI, app ops, tracking, rules, also acting as the host app (`su.manager`) |
| `Shizuku-Next/` | The embedded Shizuku manager, based on upstream v14.0.29-next (32 upstream commits merged), compiled by the host build |
| `gps-&-ip-spoofer/` | GPS & IP spoofing module, folded in the same way |

The wiring lives in [`settings.gradle`](settings.gradle): the two embedded
projects are `includeBuild`ed and substituted in place of their published
artifacts, so nothing downstream can drift from source.

## Highlights

- **Via Shizuku start method** – once the Shizuku server is alive (the boot
  pipeline can start it on its own after a reboot), everything else starts
  *through the running Shizuku* — its own shell stands in for the adb session,
  so wireless and USB debugging switches are never touched.
- **Root (custom command) start method** – the start payload is piped into a
  root shell you configure (works with KernelSU/APatch `su` paths).
- Hiding systems (developer options / debugging switches / root managers),
  watchdog + breaker cool-off, landscape UI fixes and other SU-Manager
  customizations are built in.
- AppManager's full feature set (app details, ops, components, rules,
  backups) operating through the mode of operation you pick.

## Building

Requirements: JDK 21, Android SDK/NDK.

1. Put your SDK path in `local.properties` (`sdk.dir=...`).
2. Keep `Shizuku-Next/local.properties`'s
   `shizuku.manager.applicationId=su.manager` — it pins the embedded manager
   to the host application id.
3. This repo ships without Gradle wrapper binaries; build with a local Gradle
   matching the project's version requirements:

   ```
   gradle :app:assembleRelease
   ```

Release builds are signed with the public AOSP testkey
(`app/testkey.p12`, password `testkey123`, alias `amtestkey`), so successive
builds install over each other.

## License

- **The SU-Manager app** (this repository's root project and the GPS & IP
  spoofer module) is licensed **GPL-3.0-or-later** — see [`COPYING`](COPYING)
  and the [`LICENSES/`](LICENSES) directory for third-party notices.
- **The embedded Shizuku manager** (`Shizuku-Next/`) keeps its own upstream
  license: **Apache 2.0** — see [`Shizuku-Next/LICENSE`](Shizuku-Next/LICENSE).

### Attribution

- [AppManager](https://github.com/MuntashirAkon/AppManager) © MuntashirAkon —
  GPL-3.0-or-later. This repository is a fork with additional features.
- [Shizuku](https://github.com/RikkaApps/Shizuku) / [Shizuku-Next](https://github.com/rushiranpise/Shizuku-Next)
  — Apache 2.0, compiled into this app as a library module.

When distributing builds, publish both the GPL-3.0 source of the app and keep
the Apache 2.0 notices for the Shizuku parts (they are already in-tree).
