# Shoulder Surfing Protection (PS01)



| Requirement | Implementation |
|---|---|
| Use built-in front camera | CameraX `CameraSelector.DEFAULT_FRONT_CAMERA`, no external hardware |
| Detect multiple faces | ML Kit Face Detection (on-device) running in `FaceAnalyzer.kt` |
| Distinguish primary user vs. surfer | Heuristic in `FaceAnalyzer.evaluate()`: the largest face = primary user; any other face whose bounding-box area is â‰¥20% of the primary face's area is treated as a nearby viewer |
| React quickly | Analyzer runs on every 5th frame (~5-6 checks/sec) using `STRATEGY_KEEP_ONLY_LATEST`; UI reacts via `StateFlow` the instant a threat is reported |
| No external hardware | 100% on-device: CameraX + ML Kit, no cloud calls |
| Minimize battery/CPU | Frame throttling (`FRAME_SKIP = 5`), `PERFORMANCE_MODE_FAST`, no landmark/classification modes, camera only bound while protection is enabled |
| No stored images | `imageProxy.close()` is called immediately after each frame is analyzed; no bitmap, file, or buffer is ever persisted or copied out of the analyzer 

## Project structure

```
app/src/main/java/com/example/shouldersurfing/
  MainActivity.kt              # permission handling + CameraX lifecycle binding + top-level UI
  FaceAnalyzer.kt              # CameraX ImageAnalysis.Analyzer + ML Kit + heuristic
  ProtectionViewModel.kt       # UI state (StateFlow) derived from analyzer callbacks
  ui/
    SensitiveContentScreen.kt  # demo "sensitive" screen (mock wallet/bank UI)
    ProtectionOverlay.kt       # blur + opaque scrim shown when a threat is detected
    StatusBar.kt                # status text + faces-in-view count + on/off toggle
    theme/                      # Compose Material3 theme
```

## Opening and building the app

1. **Open in Android Studio** (Koala/2024.1 or newer recommended): `File > Open` and select the
   `ShoulderSurfingProtection` folder. Android Studio will detect the Gradle Kotlin DSL project
   and offer to generate the Gradle wrapper JAR automatically the first time you sync — accept
   this (or run `gradle wrapper` once from a terminal if you have Gradle installed).
2. Let Gradle sync — it will pull CameraX, ML Kit Face Detection, and Compose dependencies from
   Google's Maven repo / Maven Central (already configured in `settings.gradle.kts`).
3. **Run** on a real device (an emulator's virtual front camera won't produce real faces to
   detect, so testing the actual protection behavior requires a physical phone/tablet, Android
   7.0 / API 24+).
4. To produce the mandatory build artifact: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
   for a debug APK, or `Build > Generate Signed Bundle / APK` for a release AAB.

## Trying it out

- On first launch, grant the camera permission when prompted.
- The screen shows a mock wallet/bank UI (balance, transactions, PIN field) — this stands in for
  "sensitive content" per the problem statement.
- Have a second person lean into the front camera's view, close enough that their face is a
  reasonable fraction of the primary face's size: the content blurs/hides within a fraction of a
  second and shows "Content protected."
- When only you are in frame again, the content reappears automatically.
- The switch at the top toggles protection on/off; the status line under it shows live face count
  and current status.

## Notes on the design decisions the team must justify at evaluation

- **Face-detection method**: ML Kit's on-device Face Detection API (fast performance mode) —
  chosen over CameraFace/OpenCV for reliability, easy CameraX integration, and zero network
  dependency.
- **Attention/proximity estimation**: relative bounding-box size is used as a proxy for
  "distance from camera," since a person actually shoulder-surfing must be physically close to
  read the screen. This is deliberately simple and explainable; a future iteration could add
  head-pose/gaze angle (ML Kit face contours + Euler angles, already partially available via
  `Face.headEulerAngleY`) to further reduce false positives from people merely standing nearby
  but not looking at the screen.
- **Privacy-trigger mechanism**: a debounced, per-frame boolean (`threatDetected`) exposed via
  `StateFlow`, so the UI overlay reacts declaratively and instantly to state changes.
- **UI protection technique**: a full-screen scrim + blur (`ProtectionOverlay.kt`) rather than
  closing the app or locking the device, so the user's task isn't interrupted, only the sensitive
  content is hidden.
- **Camera-processing architecture**: CameraX `ImageAnalysis` bound to the Activity's lifecycle,
  with a dedicated single-thread executor, frame skipping, and `STRATEGY_KEEP_ONLY_LATEST` to
  bound memory/CPU use.
