# CinemaVision TV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a 100% standalone, free Android TV visual picture calibration app that renders Native 4K bit-accurate test patterns and guides users to tune their TV picture to cinema reference standards (D65, Rec.709, BT.1886, 24p) using only the TV remote.

**Architecture:** Dual-layer Android TV display system: a hardware-direct background `SurfaceView` locked to Native 4K (3840×2160) for unscaled pixel-accurate calibration patterns and 24p video playback via Media3/ExoPlayer, overlaid by a Jetpack Compose for TV interactive Heads-Up Display (HUD) with brand-specific guidance and auto-hiding controls.

**Tech Stack:** Kotlin 2.0, Android SDK (Min API 28, Target API 34), AndroidX TV (Compose for TV), AndroidX Media3 ExoPlayer, JUnit 4, Kotlin Coroutines.

**Spec:** [2026-11-01-visual-cinema-calibration-design.md](file:///C:/Users/pisan/.gemini/antigravity/scratch/cinema-vision-tv/docs/superpowers/specs/2026-11-01-visual-cinema-calibration-design.md)

## Global Constraints

- Standalone Android TV application; zero external PC, smartphone, or cloud server dependencies.
- Android TV Remote (D-Pad) navigation only: `KEYCODE_DPAD_LEFT`, `RIGHT`, `UP`, `DOWN`, `CENTER`/`OK`, `BACK`.
- Bit-accurate RGB levels: Legal/Limited Video Range (16–235) default with Full Range (0–255) toggle.
- Native 4K display resolution (`SurfaceHolder.setFixedSize(3840, 2160)`) on 4K display modes with clean fallback to 1920×1080 on 1080p panels.
- Minimum SDK: Android 9.0 (API 28). Target SDK: Android 14 (API 34).
- Bilingual localization: Thai (`values-th/strings.xml`) and English (`values/strings.xml`).

## Review Focus

1. **Resolution mismatch on 1080p TVs:** App must not crash or clip when `setFixedSize(3840, 2160)` is requested on a non-4K TV; display mode detection must query `Display.Mode` physical dimensions.
2. **UI overlay interfering with pattern measuring/viewing:** Any button press on `OK` or `DPAD_CENTER` must toggle HUD visibility between 100% visible and 0% (fully hidden), and HUD must automatically fade after 6 seconds of inactivity.
3. **Black crush in Legal Range:** PLUGE pattern must render bar 16 with RGB (16, 16, 16) exactly matching background, and bar 17 with RGB (17, 17, 17) to ensure near-black visibility without lift.
4. **ExoPlayer lifecycle on TV:** Backgrounding the app or sleeping the TV during Pattern 6 (24p video) must release player resources cleanly and reinitialize on resume without black screen or ANR.
5. **D-pad focus trapping:** The Compose TV overlay must never swallow `DPAD_LEFT` / `DPAD_RIGHT` when HUD is hidden; all directional navigation must seamlessly advance calibration steps.

---

### Task 1: Android TV Project Structure & Gradle Configuration

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `gradle/wrapper/gradle-wrapper.properties`

**Interfaces:**
- Consumes: None (Root project setup)
- Produces: Runnable Android TV module with Compose for TV and Media3 dependencies

- [ ] **Step 1: Write `settings.gradle.kts` and root `build.gradle.kts`**
Set up plugin management, Google and Maven Central repositories, and include `:app`.

- [ ] **Step 2: Write `app/build.gradle.kts`**
Configure Android application with `compileSdk = 34`, `minSdk = 28`, `targetSdk = 34`, enable Compose compiler, add dependencies:
```kotlin
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.androidx.activity.compose)
implementation(libs.androidx.tv.foundation)
implementation(libs.androidx.tv.material)
implementation(libs.androidx.media3.exoplayer)
implementation(libs.androidx.media3.ui)
testImplementation(libs.junit)
```

- [ ] **Step 3: Write `app/src/main/AndroidManifest.xml`**
Declare Android TV requirements:
```xml
<uses-feature android:name="android.software.leanback" android:required="false" />
<uses-feature android:name="android.hardware.touchscreen" android:required="false" />
<activity
    android:name=".MainActivity"
    android:banner="@drawable/tv_banner"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:screenOrientation="landscape"
    android:theme="@style/Theme.CinemaVision"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

- [ ] **Step 4: Verify project compilation**
Run: `./gradlew assembleDebug` or test gradle configuration parse.
Expected: Build configuration succeeds.

- [ ] **Step 5: Commit**
```bash
git add settings.gradle.kts build.gradle.kts app/
git commit -m "chore: setup Android TV project structure and dependencies"
```

---

### Task 2: Core Domain Calibration Models & RGB Math Engine

**Files:**
- Create: `app/src/main/java/com/cinemavision/tv/domain/ColorRange.kt`
- Create: `app/src/main/java/com/cinemavision/tv/domain/PatternStep.kt`
- Create: `app/src/main/java/com/cinemavision/tv/domain/BrandGuidance.kt`
- Create: `app/src/main/java/com/cinemavision/tv/domain/CinemaAccuracyCalculator.kt`
- Test: `app/src/test/java/com/cinemavision/tv/domain/ColorRangeTest.kt`
- Test: `app/src/test/java/com/cinemavision/tv/domain/PatternStepTest.kt`
- Test: `app/src/test/java/com/cinemavision/tv/domain/CinemaAccuracyCalculatorTest.kt`

**Interfaces:**
- Consumes: Standard Kotlin types
- Produces:
  - `enum class ColorRange { LIMITED, FULL }` with luminance methods `getBlackLevel()`, `getReferenceWhite()`, `getPlugeValues()`, `getWhiteClipValues()`.
  - `enum class PatternStep(val id: Int, val titleRes: Int, ...)` representing the 7 calibration stages.
  - `class CinemaAccuracyCalculator` providing baseline accuracy (~35%), weighted step contributions (Mode: +20%, Aspect: +10%, Black: +15%, White: +10%, Sharpness: +10%, Grayscale: +10%, Motion: +10%), and calculating total Cinema Reference Match percentage (0-100%).
  - `enum class TvBrand { SONY, TCL, HISENSE, SAMSUNG_BOX, LG_BOX, UNIVERSAL }` and `BrandTipProvider`.

- [ ] **Step 1: Write failing tests for `ColorRangeTest`**
```kotlin
@Test
fun testLimitedRangeLevels() {
    val range = ColorRange.LIMITED
    assertEquals(16, range.blackLevel)
    assertEquals(235, range.referenceWhite)
    val pluge = range.getPlugeBars()
    assertTrue(pluge.contains(17))
    assertTrue(pluge.contains(18))
    assertTrue(pluge.contains(19))
    assertTrue(pluge.contains(20))
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests com.cinemavision.tv.domain.ColorRangeTest`
Expected: FAIL (Class not found)

- [ ] **Step 3: Implement `ColorRange.kt`, `PatternStep.kt`, and `BrandGuidance.kt`**
Provide mathematical calculations for 8-bit RGB video range, 20-step grayscale ramp values (`val stepGrayLevels = (0..20).map { ... }`), and brand recommendation maps.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests com.cinemavision.tv.domain.ColorRangeTest`
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/cinemavision/tv/domain/ app/src/test/
git commit -m "feat: add color range math and pattern step domain models"
```

---

### Task 3: Native 4K Canvas Pattern Generator (`PatternCanvasEngine`)

**Files:**
- Create: `app/src/main/java/com/cinemavision/tv/ui/pattern/PatternCanvasEngine.kt`
- Create: `app/src/main/java/com/cinemavision/tv/ui/pattern/PatternSurfaceView.kt`
- Test: `app/src/test/java/com/cinemavision/tv/ui/pattern/PatternCanvasEngineTest.kt`

**Interfaces:**
- Consumes: `ColorRange`, `PatternStep`
- Produces: `PatternCanvasEngine` capable of rendering bit-accurate patterns to an Android `Canvas` at 3840×2160 or 1920×1080.

- [ ] **Step 1: Write failing tests for `PatternCanvasEngineTest`**
Test pattern geometry coordinates calculation (aspect ratio bounds, safe zone margin 5%, 20-step grayscale bar widths).

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests com.cinemavision.tv.ui.pattern.PatternCanvasEngineTest`
Expected: FAIL

- [ ] **Step 3: Implement `PatternCanvasEngine.kt`**
Implement drawing methods:
- `drawOverscanGrid(canvas: Canvas, width: Int, height: Int)`: Draws 1-pixel outer border, 16:9 safe frame, coordinate indicators, center crosshair.
- `drawPluge(canvas: Canvas, width: Int, height: Int, range: ColorRange, flashState: Boolean)`: Draws background at `range.blackLevel`, negative black bars (12, 14), and flashing near-black bars (17, 18, 19, 20, 24).
- `drawWhiteClipping(canvas: Canvas, width: Int, height: Int, range: ColorRange, flashState: Boolean)`: Draws reference white (235) and flashing highlight bars (230, 232, 234, 238, 242).
- `drawSharpnessGrid(canvas: Canvas, width: Int, height: Int)`: Draws alternating 1-pixel black/white horizontal & vertical lines, anti-ringing concentric circles, and diagonal steps.
- `drawGrayscaleRamp(canvas: Canvas, width: Int, height: Int, range: ColorRange)`: Draws 20 equal-width grayscale steps from 0% to 100% luminance plus continuous smooth gradient bar.

- [ ] **Step 4: Implement `PatternSurfaceView.kt`**
Create a custom `SurfaceView` with a render loop coroutine running at 60Hz:
- Enforce `holder.setFixedSize(targetWidth, targetHeight)` where `targetWidth = 3840` when 4K mode is detected, else `1920`.
- Control blinking cycle (1.0 Hz) for flashing bars in PLUGE and White Clipping patterns.

- [ ] **Step 5: Run tests to verify they pass**
Run: `./gradlew test --tests com.cinemavision.tv.ui.pattern.PatternCanvasEngineTest`
Expected: PASS

- [ ] **Step 6: Commit**
```bash
git add app/src/main/java/com/cinemavision/tv/ui/pattern/
git commit -m "feat: implement native 4k canvas pattern engine and SurfaceView"
```

---

### Task 4: 24p True Film Cadence Media3 Engine

**Files:**
- Create: `app/src/main/java/com/cinemavision/tv/ui/player/JudderPlayerManager.kt`
- Create: `app/src/main/res/raw/judder_24p_test.mp4` (Lightweight 24fps motion test video asset)
- Test: `app/src/test/java/com/cinemavision/tv/ui/player/JudderPlayerManagerTest.kt`

**Interfaces:**
- Consumes: Android `Context`, `SurfaceHolder`
- Produces: `JudderPlayerManager` to manage ExoPlayer lifecycle, seamless looping, and 24fps playback.

- [ ] **Step 1: Write failing unit test for player state lifecycle**
Verify that `initializePlayer()` configures repeat mode `REPEAT_MODE_ALL`, volume 0 (muted), and proper media URI.

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests com.cinemavision.tv.ui.player.JudderPlayerManagerTest`
Expected: FAIL

- [ ] **Step 3: Implement `JudderPlayerManager.kt` and add test asset**
Configure `ExoPlayer.Builder(context).build()`:
- Set media item to `raw/judder_24p_test.mp4`.
- Set repeat mode to infinite loop.
- Implement `attachSurface(surface: Surface)` and `release()`.

- [ ] **Step 4: Run test to verify it passes**
Run: `./gradlew test --tests com.cinemavision.tv.ui.player.JudderPlayerManagerTest`
Expected: PASS

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/cinemavision/tv/ui/player/ app/src/main/res/raw/
git commit -m "feat: implement 24p judder video player manager with Media3"
```

---

### Task 5: Jetpack Compose for TV Interactive HUD Overlay & Navigation

**Files:**
- Create: `app/src/main/java/com/cinemavision/tv/ui/hud/CalibrationHudOverlay.kt`
- Create: `app/src/main/java/com/cinemavision/tv/ui/hud/BrandSelectorCard.kt`
- Create: `app/src/main/res/values/strings.xml` (English localization)
- Create: `app/src/main/res/values-th/strings.xml` (Thai localization)
- Test: `app/src/test/java/com/cinemavision/tv/ui/hud/HudStateTest.kt`

**Interfaces:**
- Consumes: `PatternStep`, `TvBrand`, `ColorRange`
- Produces: Compose for TV UI layer floating over the `SurfaceView`

- [ ] **Step 1: Write failing test for `HudStateTest`**
Test auto-hide countdown logic: after 6 seconds with no remote input, `isHudVisible` transitions from `true` to `false`.

- [ ] **Step 2: Run test to verify it fails**
Run: `./gradlew test --tests com.cinemavision.tv.ui.hud.HudStateTest`
Expected: FAIL

- [ ] **Step 3: Implement string resources (`strings.xml` and `values-th/strings.xml`)**
Add translations for all 7 steps, brand instructions (Sony: "โหมดภาพ: Custom / Pro, อุณหภูมิสี: Expert 1", TCL: "โหมดภาพ: Movie, อุณหภูมิสี: Warm -5", Hisense: "โหมดภาพ: Filmmaker Mode, อุณหภูมิสี: Warm"), and HUD control tips.

- [ ] **Step 4: Implement `CalibrationHudOverlay.kt`**
Build Compose layout:
- Floating semi-transparent card (Dark slate `#CC121820` with subtle border).
- Step indicator pills: `[1] [2] [3] [4] [5] [6] [7]`.
- Live Cinema Match Gauge (`CinemaAccuracyMeter`): Shows current % (e.g. `Cinema Match: 75%` with gold/cyan progress bar and step contribution `+15%`).
- Step confirmation toggle button (`Mark as Calibrated [✓]`) updating overall accuracy score.
- Brand Tip Card displaying the specific menu setting path for the selected brand.
- Keyboard/D-pad event listener:
  - `DPAD_LEFT` -> Previous Step
  - `DPAD_RIGHT` -> Next Step
  - `DPAD_CENTER` / `OK` -> Toggle HUD visibility immediately
  - `DPAD_UP` / `DOWN` -> Switch selected TV brand / toggle step calibrated status

- [ ] **Step 5: Run tests to verify they pass**
Run: `./gradlew test --tests com.cinemavision.tv.ui.hud.HudStateTest`
Expected: PASS

- [ ] **Step 6: Commit**
```bash
git add app/src/main/java/com/cinemavision/tv/ui/hud/ app/src/main/res/
git commit -m "feat: implement Compose for TV HUD overlay with bilingual brand guidance"
```

---

### Task 6: Main TV Activity Integration & Dual-Layer Display

**Files:**
- Create: `app/src/main/java/com/cinemavision/tv/MainActivity.kt`
- Create: `app/src/main/res/values/styles.xml`
- Create: `app/src/main/res/drawable/tv_banner.png` (Leanback banner)

**Interfaces:**
- Consumes: `PatternSurfaceView`, `JudderPlayerManager`, `CalibrationHudOverlay`
- Produces: Complete launching Android TV activity

- [ ] **Step 1: Create `MainActivity.kt` with dual-layer container**
In `onCreate()`:
- Configure window flags: `FLAG_KEEP_SCREEN_ON`, `SYSTEM_UI_FLAG_IMMERSIVE_STICKY`, hide system bars.
- Set up root `FrameLayout`:
  - Layer 0 (Bottom): `PatternSurfaceView` (for Steps 1–5, 7).
  - Layer 1 (Middle): `PlayerView` (visible only on Step 6 - Judder 24p).
  - Layer 2 (Top): `ComposeView` hosting `CalibrationHudOverlay`.
- Handle key events: intercept `KEYCODE_DPAD_*` and delegate to calibration state manager.

- [ ] **Step 2: Add Leanback styles and launcher banner**
Define `Theme.CinemaVision` inheriting from `Theme.AppCompat.NoActionBar` with black background and leanback launcher banner resource.

- [ ] **Step 3: Verify end-to-end unit tests**
Run: `./gradlew test`
Expected: All tests pass.

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/cinemavision/tv/MainActivity.kt app/src/main/res/
git commit -m "feat: integrate main activity with dual-layer 4K surface and compose overlay"
```

---

### Task 7: Verification & Build Artifacts

**Files:**
- Modify: `README.md`
- Test: Complete test suite and debug APK build

- [ ] **Step 1: Run all unit tests**
Run: `./gradlew test --info`
Expected: 100% tests passing.

- [ ] **Step 2: Assemble debug APK**
Run: `./gradlew assembleDebug`
Expected: `app-debug.apk` generated successfully in `app/build/outputs/apk/debug/`.

- [ ] **Step 3: Write comprehensive `README.md`**
Document installation via `adb install`, remote control mapping, calibration guide, and brand tips.

- [ ] **Step 4: Commit**
```bash
git add README.md
git commit -m "docs: add comprehensive project README and calibration guide"
```
