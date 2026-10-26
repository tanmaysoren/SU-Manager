# SU-Manager

**One app — `su.manager`.** SU-Manager merges the [AppManager](https://github.com/MuntashirAkon/AppManager)
app-operations toolkit and a full [Shizuku-Next](https://github.com/rushiranpise/Shizuku-Next)
manager into a single Android application, with a GPS & IP spoofer module
built into the same Gradle build. There is no separate Shizuku app to install:
the manager ships inside this app and everything in this repository compiles
from source in one build.

---

## What's inside

| Path | What it is |
|---|---|
| Root project (`app/`, `libcore/`, `hiddenapi/`, …) | The AppManager fork: app UI, app operations, components, rules, backups — and the **host app** under the id `su.manager` |
| `Shizuku-Next/` | The embedded Shizuku manager (server, shell, starter, UI), based on upstream v14.0.29-next (32 upstream commits merged on top of the fork's baseline), with additional start methods |
| `gps-&-ip-spoofer/` | GPS & IP spoofing module, folded into the build the same way |

The wiring lives in [`settings.gradle`](settings.gradle): the two embedded
projects are `includeBuild`ed and their artifacts substituted at source level
(`moe.shizuku:manager` → project `:Shizuku-Next:manager`, the spoofer → its
app module), so nothing downstream can drift from source.

---

## Features

### From AppManager

- Full app details — activities, services, receivers, permissions, signatures
- App Ops manager, component blocking with rule import/export
- Instant blocking, tracker classification
- APK installer behaviour configuration, signature checks
- Backup/restore (GZip, options presets, split APK support)
- Log viewer, file manager, VirusTotal scanning
- Works under **every** mode of operation (No-root / ADB / Root / Shizuku), with
  feature auto-degradation per mode

### SU-Manager / Shizuku additions

- **Via Shizuku start method** – starts *through the running Shizuku server
  itself*: its own shell stands in for the adb session, so wireless and USB
  debugging switches are never touched.
- **Root (custom command) start method** – the start payload is piped into a
  root shell you configure (plain `su`, KernelSU/APatch paths, custom command).
- **Boot pipeline** – after a reboot, the server can come back on its own
  (watchdog direct-boot handling, locked-boot start, unlock-aware decisions).
- **Hiding systems** – hide developer options / USB & wireless debugging /
  root managers from other apps (signal lists, close-and-reopen on launch,
  stale-hold protection so a dead watcher can't block starts forever).
- **Breaker cool-off** – restart breaker that rearms after a reset window
  instead of failing silently forever.
- Fixed landscape crash (app previously crashed on rotation).

---

## How the Shizuku integration works

When the Shizuku server is running, it lives as a `shell`-uid daemon, spawned
by whatever start method chose to bring it up:

| Process | Runs as | Spawned by | Purpose |
|---|---|---|---|
| `shizuku_server` | `shell` (uid 2000), context `u:r:shell:s0` | start method / boot pipeline | Layers priv APIs over adb-level privileges |
| `su.manager:priv:0` | `shell` | the server session | Hosts the app's privileged services when working Via Shizuku |
| `am_local_server` | `shell` | Via Shizuku start | Stands in for the app's local privileged server (no adb port touched) |

- **Via Shizuku** needs a server that is *already alive* (after a reboot, the
  boot pipeline usually provides one; otherwise start once with another
  method).
- The app top-level UI shows both halves: the Shizuku manager (status, running
  state, start/stop/restart) and the AppManager tool itself.
- The manager is presented as **"Shizuku"** inside the app UI.

### Start methods

| Method | How it works | Needs |
|---|---|---|
| Wireless debugging | mdns discovery + pairing, started over the wireless ADB | Wireless debugging on |
| USB debugging | classic ADB over USB | USB + debugging on |
| Without Wi-Fi | experimental wireless start, no network discovery hunt | Wireless debugging on |
| System | runs Shizuku as system uid (Samsung only, FOTA agent) | Samsung firmware |
| **Via Shizuku** | the running server's own shell spawns the rest of it | Running server |
| **Root (custom command)** | payload piped into a root shell you configure | Root (any su provider) |
| Computer / ADB | copy the classic adb start command | PC with adb |

---

## Building

Requirements:

| Requirement | Version used |
|---|---|
| JDK | 21 |
| Android SDK | 36 (Android 16) |
| Android NDK | 27.0.12077973 |
| Gradle | 9.0 (wrapper binaries not included; use a local install) |

Steps:

1. Put your SDK path in `local.properties`:
   `sdk.dir=/path/to/android/sdk`
2. Keep `Shizuku-Next/local.properties`'s
   `shizuku.manager.applicationId=su.manager` — it pins the embedded manager
   to the host application id so the manager merges into the host build.
3. Build:

   ```
   gradle :app:assembleRelease
   ```

   (The `docs/` module builds documentation separately, `gradle :docs:buildDocs`,
   and can be skipped with `-x docs:buildDocs`.)

### Signing

Release builds are signed with the **public AOSP testkey**
(`app/testkey.p12`, store/key password `testkey123`, alias `amtestkey`) — the
same key on-device builds sign the app with, so successive installs update in
place (`adb install -r app-release.apk`). The debug configuration uses
`app/dev_keystore.jks` for development builds only.

---

## Repository layout

```
.
├─ app/                    # the host app (UI, activities, services)
├─ libcore/                # compat / io / ui libraries
├─ hiddenapi/              # hidden-API stubs used for compilation
├─ libopenpgp/ libserver/ server/   # OpenPGP lib, local privileged server
├─ docs/                   # mdBook documentation
├─ fastlane/ arts/         # metadata and art
├─ Shizuku-Next/           # embedded Shizuku manager (Apache-2.0)
│  ├─ manager/             # the manager app module compiled in
│  ├─ server/ shell/ starter/ stub/ rish/ …
├─ gps-&-ip-spoofer/       # GPS & IP spoofer module
├─ settings.gradle         # includeBuild wiring + substitutions
└─ COPYING / LICENSES/     # app license (GPL-3.0-or-later) + notices
```

---

## License

- **The SU-Manager app** (root project + the GPS & IP spoofer module) is
  licensed **GPL-3.0-or-later** — see [`COPYING`](COPYING) and the
  [`LICENSES/`](LICENSES) directory for third-party notices.
- **The embedded Shizuku manager** (`Shizuku-Next/`) keeps its upstream
  license: **Apache 2.0** — see [`Shizuku-Next/LICENSE`](Shizuku-Next/LICENSE).

### Attribution

- [AppManager](https://github.com/MuntashirAkon/AppManager) © MuntashirAkon —
  GPL-3.0-or-later. This repository is a fork with additional features.
- [Shizuku](https://github.com/RikkaApps/Shizuku) /
  [Shizuku-Next](https://github.com/rushiranpise/Shizuku-Next) — Apache 2.0,
  compiled into this app as a library module.

When distributing builds, publish the GPL-3.0 source of the app and keep the
Apache 2.0 notices for the Shizuku parts (they are already in-tree).

---

## FAQ / Troubleshooting

**"This app is not in Shizuku's privileged apps list."**
That is expected: this app *is* the Shizuku manager. The server special-cases
its own manager (the `isManager` branch in the attach logic), so it does not
appear in the authorisation list like third-party Shizuku apps do.

**What does the prompt "keep allowed / allow all the time" mean?**
Regular Shizuku authorisation — features working through the server keep their
grant for as long as they do not force-stop it.

**Why does adb stop accepting connections?**
On devices where the app hides USB debugging, the transport disappears when
the switch is off. Re-enable wireless/USB debugging (or let a start through
Via Shizuku, which never needs to touch the switches).

**The server did not start after a reboot.**
The boot pipeline starts it whenever the start method allows — wireless
starts need wireless debugging on, etc. Any start made once by hand via
another method lets Via Shizuku take over later without touching switches
again.

**Installing new builds over an existing one fails.**
Builds must share the same signature; release ones all use the AOSP testkey,
so `adb install -r` works. Debug builds use the dev keystore and will not
replace a release install.

---

*AppManager is a trademark-independent name; this fork ships as **SU-Manager**
with package id `su.manager`.*
