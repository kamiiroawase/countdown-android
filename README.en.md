# 红叶倒计时

[![Build](https://github.com/kamiiroawase/countdown-android/actions/workflows/build.yml/badge.svg)](https://github.com/kamiiroawase/countdown-android/actions/workflows/build.yml)
[![License: Unlicense](https://img.shields.io/badge/license-Unlicense-blue.svg)](LICENSE)

An Android countdown app for the days worth looking forward to: solar and lunar calendars, flexible repeat rules, sub-event hierarchies, home-screen widgets and on-time reminders, rendered with Jetpack Compose. The UI currently ships Chinese only.

[中文版](README.md) · [Releases](https://github.com/kamiiroawase/countdown-android/releases)

## Features

- **Solar / lunar calendars**: each target day picks its calendar; lunar counting can include leap months
- **Repeat rules**: every N days / weeks / months / years / hours with an optional end date; "a partial day counts as a day" is a toggle
- **Second-precise**: target times record hour / minute / second; the countdown shows years, months, days, hours, minutes and seconds, switching to "since…" once past
- **Sub-events**: main events carry sub-events; deleting a main event deletes its children; the detail page offers fullscreen display (with or without sub-events)
- **Views and ordering**: list and grid views; events carry an ordering weight (larger sorts first)
- **Home-screen widgets**: single-event card and event-list styles, binding one event or following the app's list (all events)
- **On-time reminders**: notifications plus exact alarms, rescheduled automatically after reboot
- **Event backgrounds**: the detail page takes a custom background image, restorable to the default in one tap
- **Time zone**: countdowns compute in the selected zone, following the system by default (zone selection comes from [ZonePicker](https://github.com/kamiiroawase/zonepicker) by the same author)
- **Backup**: event data and preferences ride the system backup; restoring or migrating devices needs no manual steps
- Sample events are pre-seeded on first launch: "Met You" (the first-launch moment) plus Spring Festival and Double Ninth Festival (lunar, yearly)

The core countdown math comes from [nexttime](https://central.sonatype.com/artifact/io.github.kamiiroawase/nexttime-android) by the same author; the lunar calendar comes from tyme, pulled in transitively.

## Download

APKs are published on [GitHub Releases](https://github.com/kamiiroawase/countdown-android/releases); Android 12 or newer is required.

## Development

```bash
git clone https://github.com/kamiiroawase/countdown-android.git
cd countdown-android
./gradlew build              # build, lint, Spotless — the same gate CI runs
./gradlew :app:installDebug  # install on a device
```

Local release signing reads `keystore.properties` at the repository root (gitignored; keys: `storeFile` / `storePassword` / `keyAlias` / `keyPassword`); without the file the release APK comes out unsigned.

### Releasing

Pushing a `v*` git tag triggers the Release workflow: after the full build gate passes, it builds an APK signed with the repository's key secret and attaches it (`countdown-<tag>.apk`) to the matching GitHub Release.

## License

[The Unlicense](LICENSE) — public domain, do as you please.
