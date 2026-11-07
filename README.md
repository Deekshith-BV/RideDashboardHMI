# RideDashboardHMI

Small landscape dashboard app for an e-bike / scooter style HMI. It shows the current
speed, whether the motor is on, and whether the kill switch is active. All the data is
faked by a simulator, so there's no hardware or backend to set up.

Stack: Kotlin, Jetpack Compose, Coroutines + Flow/StateFlow, ViewModel.

## Build and run

You'll need Android Studio and the Android SDK (compileSdk 36, minSdk 24, JDK 11).

Open the project in Android Studio, let Gradle sync, pick an emulator or device and hit Run.
The activity is locked to landscape in the manifest, so if the emulator opens in portrait
just rotate it.

From the terminal:

```bash
./gradlew assembleDebug     # build the debug APK
./gradlew installDebug      # install on a running device/emulator
```

APK ends up in `app/build/outputs/apk/debug/`.

## How it's put together

Three pieces, kept separate:

- `VehicleDataSource.kt` - the simulator. Emits a `VehicleData` every second from a Flow
  (`flow { ... delay(1000) }`) and loops through the scenarios below.
- `VehicleViewModel.kt` - collects that Flow, maps it into a `VehicleUiState`, runs it
  through `sanitizeState()` and exposes a `StateFlow`. I pass the dispatcher and the data
  source in through the constructor so the ViewModel can be tested without a real Main
  dispatcher. It uses its own CoroutineScope which gets cancelled in `onCleared()`.
- `MainActivity.kt` - the Compose UI. It just reads the state with `collectAsState()` and
  draws it, so it updates on its own whenever the state changes.

`VehicleUiState.kt` is the immutable state object that holds everything the UI needs.

The collect block is wrapped in `catch { }`, so if the stream ever errors the UI shows an
error state instead of the app dying.

## The simulated ride

One emission per second, on a loop. The first five are the required scenarios from the
assignment; the last two are just there to show off the rest of the dashboard.

| Label | Speed | Motor | Kill switch |
|-------|-------|-------|-------------|
| Parked | 0 | OFF | Inactive |
| Ready | 0 | ON | Inactive |
| Riding | 25 | ON | Inactive |
| Accelerating | 50 | ON | Inactive |
| Motor disabled | 0 | OFF | Active |
| Thruster | 98 | ON | Inactive (over-speed + TPMS) |
| Braking | 40 | ON | Inactive (regen + rapid brake) |

## Tests

```bash
./gradlew testDebugUnitTest
```

Report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.

The tests live in `VehicleViewModelTest.kt`:

- checks the simulator emits the five required scenarios in the right order with the right
  speed / motor / kill-switch values
- checks the ViewModel actually maps emitted data into the UI state
- checks a negative speed gets clamped to 0 and stale data (older than 2s) is flagged as
  `Data stale`
- checks bad/stale input is handled without throwing

## Edge cases

- Negative speeds are clamped to 0.
- Data older than 2 seconds is marked invalid and labelled `Data stale`.
- Errors from the Flow are caught and turned into an error state.

## Notes / things I didn't do

- Everything is simulated. No CAN, no real vehicle signals.
- The scenario speeds are just for the demo, not safety logic - motor off doesn't force the
  speed to zero.
- Built for landscape. On a tall phone it scrolls, but landscape is the intended layout.
- The top connectivity row and clock (paired phone, DND, notifications, call, geofence,
  time) use fixed placeholder values - they aren't part of the simulated feed.
- The settings menu and parking slider are just UI, they don't save anything.
- No DI framework, caching, instrumented UI tests or pause/resume controls - out of scope
  for this one.
