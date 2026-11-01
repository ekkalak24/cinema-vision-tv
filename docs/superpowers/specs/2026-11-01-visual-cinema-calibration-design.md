# CinemaVision TV: Design Document
**Android TV Visual Picture Calibration App (Cinema Reference Standards)**

* **Date:** 2026-11-01
* **Status:** Draft for Review
* **Target Platform:** Android TV & Google TV (Android 9.0+ / API Level 28+)
* **License / Distribution:** Free / Open-source utility

---

## 1. Intent & Context
Most modern consumer televisions (LED, Mini-LED, OLED) ship from the factory with "Vivid" or "Standard" picture modes enabled by default. These modes feature:
* Extreme blue-shifted white points (9,000K–11,000K instead of industry standard D65 / 6,504K).
* Crushed shadow details (black crush) and clipped bright highlights.
* Heavy artificial edge sharpening resulting in halo artifacts.
* Motion interpolation ("soap opera effect") that ruins native 24fps cinematic film cadence.

Professional color calibration solutions (such as Calman, ColourSpace, and Klein/X-Rite colorimeters) require expensive hardware ($250–$3,000+) and external PC workflows. 

**CinemaVision TV** is a completely standalone, free Android TV application that guides any TV owner through calibrating their display to Hollywood / mastering studio cinema standards (Rec.709, D65 white point, BT.1886 / Gamma 2.4, 24p cadence) using only their standard TV remote control and visual reference patterns.

---

## 2. Goals, Non-Goals, and Anti-Goals

### 2.1 Goals
1. **100% Standalone on TV:** Operates entirely on Android TV using the standard TV D-pad remote. No smartphone, PC, or external calibration probe required.
2. **Bit-Accurate Test Pattern Engine:** Full-screen rendering of scientific test patterns:
   * **Pattern 1: 1:1 Pixel Mapping & Overscan** (Aspect ratio check, zero distortion/cropping).
   * **Pattern 2: Black Level / Brightness (PLUGE)** (Legal video range 16–235 vs full 0–255; flashing near-black bars 17–25 to prevent black crush while maintaining deep blacks).
   * **Pattern 3: White Level / Contrast** (Flashing highlight bars 230–234 to preserve highlight detail without color tinting or clipping).
   * **Pattern 4: Sharpness & Edge Ringing** (High-frequency pixel mesh to dial sharpness to true neutral zero).
   * **Pattern 5: Color Temperature & Grayscale Ramp** (20-step grayscale bar, neutral D65 reference card, guidance on choosing "Warm / Warm 2 / Filmmaker Mode").
   * **Pattern 6: Motion & Processing Hygiene** (Judder 24p check, clear step-by-step instructions to disable motion smoothing, dynamic contrast, and ambient light sensors).
3. **HUD Overlay Toggle & Live Cinema Match Score (%):** A transparent Heads-Up Display (HUD) showing simple instructions in Thai and English. Pressing the remote's `OK` or `Down` button hides the HUD completely so the screen displays 100% pure pattern pixels with no UI interference.
   * **Live Cinema Match Index (%):** The HUD displays a real-time percentage score (e.g. "Cinema Match: 75%") showing how close the current calibration state is to studio mastering reference. Users can confirm each completed adjustment to dynamically advance the score from ~35% (factory Vivid) up to 100% (Full Cinema Standard).
4. **Brand-Specific Quick Reference:** On-screen quick guides for major TV brands (Sony BRAVIA, TCL, Hisense, Samsung/LG external boxes, Xiaomi TV) showing exact menu paths to Picture Settings.
5. **Completely Free:** No ads, no paywalls, zero in-app purchases.

### 2.2 Non-Goals
1. **Automated Hardware Control:** Android TV OS does not permit 3rd-party apps to directly alter OEM proprietary picture processing chips (e.g. Sony Cognitive XR, TCL AiPQ). The app does not claim to change TV hardware settings via code without user remote intervention.
2. **HDR10+ / Dolby Vision 3D LUT Hardware Flashing:** Visual calibration focuses on SDR / Rec.709 and foundational display setup; flashing internal hardware 3D LUTs is out of scope.
3. **User Account / Authentication:** No sign-up, login, telemetry tracking, or cloud account requirements.

### 2.3 Anti-Goals
1. **No Adware / Popups:** Never disrupt the calibration workflow with advertisements or prompts.
2. **No False Marketing:** Avoid gimmicky claims like "AI 1-click Auto-Calibrate screen hardware" when the OS sandbox prohibits direct HAL writes.

---

## 3. Approaches Considered

| Approach | Description | Tradeoffs | Decision |
| :--- | :--- | :--- | :--- |
| **A. Visual Calibration Standalone App (Selected)** | Built-in test pattern generator + interactive visual calibration wizard directly on TV. | Pros: Free, zero extra hardware, runs directly on TV.<br>Cons: Cannot achieve laboratory Delta-E < 1 without a sensor, but reaches 85-90% cinema accuracy. | **Adopted.** Best user accessibility and zero-barrier adoption. |
| **B. Smartphone Camera Assist** | TV app displays patches while a phone camera measures screen color cast. | Pros: More objective than eye alone.<br>Cons: Phone camera white balance variance requires complex calibration; requires dual-device development. | Deferred for v2 update. |
| **C. USB Colorimeter Driver on Android TV** | Direct USB OTG support for Datacolor Spyder or Calibrite probes. | Pros: Lab-grade delta-E measurements.<br>Cons: Requires user to buy $200+ hardware; very few casual users own probes. | Deferred for Pro edition. |

---

## 4. System Architecture & Technical Specifications

### 4.1 Technology Stack & Architectural Decisions (Builder Calls)
* **Language:** Kotlin 2.0+
* **Target SDK:** Android 14 (API 34), **Min SDK:** Android 9.0 (API 28)
* **Video Playback Engine (Pattern 6 - Judder 24p Check):** `androidx.media3:media3-exoplayer` playing a lightweight bundled 23.976/24.000 fps MP4 test clip (smooth white bar traversing black background). *Builder Call Reason: Android's Choreographer runs at 60Hz; an actual 24p video stream is required to properly trigger and verify 3:2 pulldown and TV motion smoothing removal.*
* **Brand Guidance Architecture:** Contextual per-brand HUD. The app data model stores a matrix of `[Step × Brand × Language]` (covering Sony, TCL, Hisense, Samsung/LG external boxes, and Universal). *Builder Call Reason: Different TV brands use confusingly different names for the same setting (e.g. Sony calls D65 "Expert 1", TCL calls it "Warm -5", Samsung calls it "Warm 2"); showing the exact brand menu path on each step's HUD prevents user confusion.*
* **UI & HUD Framework:** Android Jetpack Compose for TV for navigation, menus, and HUD overlay card.
* **Rendering Layer for Patterns:** Dual-layer architecture: A background `SurfaceView` dedicated to hardware-accurate test patterns with forced Native 4K resolution (`SurfaceHolder.setFixedSize(3840, 2160)` on 4K display modes, falling back cleanly to 1920×1080 on 1080p panels), overlaid by a Compose for TV layer for interactive HUD and remote navigation. *User Decision: Enforce Native 4K for maximum pixel-accurate 1:1 test patterns.*

### 4.2 Pattern Generation Specification
1. **RGB Range Handling:**
   * Support both **Limited Video Range (16–235)** and **Full RGB (0–255)** toggle.
   * Standard broadcast & streaming movies use Legal/Limited Range (Black = 16, Reference White = 235).
   * Patterns dynamically calculate RGB hex codes to guarantee exact luminance levels.
2. **Full-Screen Immersion:**
   * `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` / Android 11+ `WindowInsetsController` to ensure navigation bar and status bar are fully hidden.
   * Screen wake lock (`FLAG_KEEP_SCREEN_ON`) during pattern display.
3. **Heads-Up Display (HUD):**
   * Semi-transparent floating card at the bottom/side.
   * Disappears automatically after 5 seconds or via remote toggle (`OK` / `Enter`).
   * Explains:
     1. "What to look for" (e.g., *Bars 17 and above should flash faintly; bar 16 should blend into the background*).
     2. "Which TV menu setting to adjust" (e.g., *Adjust 'Brightness' up or down*).

### 4.3 Calibration Workflow (User Journey)
```
[1. Welcome & Brand Selection]
      │
      ▼
[2. Step 1: Base Picture Mode Setup]
   (Switch to "Movie / Filmmaker / Custom", Color Temp: "Warm / Warm 2", Turn OFF Eco/Motion)
      │
      ▼
[3. Step 2: 1:1 Pixel Mapping & Overscan]
   (Check outer border arrows; adjust TV Aspect Ratio to "Just Scan / Full / 1:1")
      │
      ▼
[4. Step 3: Black Level & Shadow Detail (Brightness)]
   (PLUGE pattern; adjust Brightness until bar 16 is invisible and bar 17+ flashes)
      │
      ▼
[5. Step 4: White Level & Highlight Clipping (Contrast)]
   (Highlight clipping pattern; adjust Contrast until bar 234 flashes without color shift)
      │
      ▼
[6. Step 5: Sharpness & Edge Ringing]
   (High-frequency grid; lower Sharpness until artificial halos/white edges vanish)
      │
      ▼
[7. Step 6: Color & Grayscale Uniformity]
   (20-step gray ramp; verify neutral tones without magenta/green cast)
      │
      ▼
[8. Step 7: Film Cadence & Post-Processing Checklist]
   (Judder test; confirm Motion Smoothing / TruMotion / Motionflow is OFF)
      │
      ▼
[9. Calibration Complete & Reference Cinema Gallery]
   (View high-dynamic-range cinema stills with accurate grading)
```

---

## 5. What is Left to the Builder
1. **Art Assets & Localization:** Iconography, bilingual strings (`values/strings.xml` and `values-th/strings.xml`), and reference demonstration stills.
2. **Animation / Navigation Polish:** Leanback-compatible focus highlights for TV remote usability.
3. **Build Tooling:** Standard Gradle wrapper, Android TV leanback launcher intent filter (`android.intent.category.LEANBACK_LAUNCHER`), and AndroidManifest configuration.
