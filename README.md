# CinemaVision TV

> **Bilingual Documentation:** [English](#english) | [ภาษาไทย](#thai)

---

<a name="english"></a>
## English Documentation

### CinemaVision TV: Android TV Cinema Reference Calibration App
**Achieve Hollywood Mastering Studio Standards (D65 · Rec.709 · BT.1886 · 24p) Using Only Your TV Remote**

[![Platform](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Google%20TV-green.svg)](https://developer.android.com/tv)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28%20(Android%209.0)-blue.svg)]()
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-blue.svg)]()
[![Resolution](https://img.shields.io/badge/Resolution-Native%204K%20UHD%20(3840x2160)-gold.svg)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)]()
[![Tests](https://img.shields.io/badge/Tests-73%20Passed%20(100%25)-brightgreen.svg)]()

---

### Why CinemaVision TV Exists

Over 90% of modern consumer televisions (OLED, Mini-LED, QLED, and LED) ship from factories set to **"Vivid"** or **"Dynamic"** store-demo modes. While designed to stand out on bright retail showroom floors, these default settings severely degrade picture quality in home viewing environments:

1. **Extreme Blue Tint (9,000K – 11,000K):** Distorts skin tones into sickly blue/magenta casts instead of the worldwide cinema mastering standard **D65 (6,504K)**.
2. **Crushed Shadow Details (Black Crush):** Dark movie scenes become murky blobs where actors' black clothing and shadows lose all texture.
3. **Blown Highlights (White Clipping):** Bright clouds, explosions, and skies clip into flat white sheets, discarding fine details.
4. **Artificial Edge Ringing:** Excessive factory sharpness injects harsh white halos around characters and objects.
5. **Soap Opera Effect (MEMC Motion Smoothing):** Aggressive motion interpolation turns cinematic 24 fps films into cheap daytime soap operas, accompanied by jarring motion artifacts.

**The Solution:** Professional hardware calibration (using Klein/X-Rite colorimeters and Calman software) costs hundreds to thousands of dollars and requires complex PC setups. **CinemaVision TV** is a **100% free, standalone Android TV app** that delivers native 4K bit-accurate test patterns and step-by-step visual guidance directly on your TV. In just 10 minutes, you can bring your television up to cinema reference accuracy using only your TV remote control.

---

### Key Features

* **100% Standalone on TV:** Operates entirely within Android TV / Google TV. No computer, smartphone, account registration, or external probe required.
* **Native 4K Bit-Accurate Canvas Engine:** Renders mathematical patterns on a dedicated hardware `SurfaceView` locked to true 3840×2160 unscaled 1:1 pixels (with clean fallback to 1920×1080 on Full HD displays).
* **Live Cinema Match Index (%) Meter:** Real-time accuracy meter that starts at baseline ~35% (factory default) and increases progressively up to 100% (Cinema Reference) as you verify each step.
* **Media3 24.000 fps Cadence Engine:** Dedicated hardware video loop running at true 24.000 fps cadence to instantly reveal and eliminate motion smoothing judder.
* **Bilingual TV HUD Overlay:** Heads-Up Display in English and Thai with an auto-hide timer (6 seconds) so you can evaluate patterns on an unobstructed full screen.
* **Brand-Specific Menu Paths:** Tailored guides for **Sony BRAVIA**, **TCL**, **Hisense**, **Samsung Box**, **LG Box**, and **Universal / Other Brands**.
* **Legal (16–235) & Full (0–255) Range Support:** Defaults to broadcast/streaming legal video range (Studio 16–235) with support for full PC RGB.

---

### TV Remote Control Guide

| Remote Button | Function |
| :--- | :--- |
| **D-Pad Right (►)** | Advance to next calibration step (Steps 1 → 7) |
| **D-Pad Left (◄)** | Return to previous calibration step |
| **D-Pad Up / Down (▲ / ▼)** | Cycle TV brand menu guidance (Sony, TCL, Hisense, Samsung, LG, Universal) |
| **OK / Center Button** | Mark / Unmark current step as calibrated (updates Live Cinema Match %) |
| **Back Button** | When HUD is visible: immediately hides HUD for full pattern view<br>When HUD is hidden: exits app |

> **HUD Auto-Hide:** The interactive HUD automatically fades away after 6 seconds of inactivity. Press any D-pad button or `OK` to bring it back.

---

### The 7-Step Cinema Calibration Walkthrough

```
[1. Picture Mode] ──► [2. Aspect Ratio] ──► [3. Black Level] ──► [4. White Level]
       (+20%)                 (+10%)                 (+15%)                 (+10%)
                                                                              │
[7. 24p Judder]   ◄── [6. D65 Grayscale] ◄── [5. Sharpness]   ◄───────────────┘
       (+15%)                 (+10%)                 (+10%)
```

#### Step 1: Base Picture Mode Setup (+20% Accuracy)
* **Goal:** Switch away from Vivid/Standard mode to disable exaggerated saturation, oversaturated blues, and dynamic picture tampering.
* **Target Setting:** Select `Movie`, `Cinema`, `Filmmaker Mode`, or `Custom`.
* **Action:** Turn off `Eco Mode`, `Ambient Light Sensor`, and `Dynamic Contrast`.

#### Step 2: 1:1 Pixel Mapping & Overscan (+10% Accuracy)
* **Goal:** Ensure every pixel from the video stream maps 1:1 to your TV's physical panel pixels without edge cropping or scaling blur.
* **Target Pattern:** 1-pixel outer frame with directional border arrows and 16:9 safe frame.
* **Action:** In TV picture settings, set Aspect Ratio to `Just Scan: On`, `Full`, `1:1`, `Direct`, or `Fit to Screen`. All four arrow tips must touch the outer edges of your TV screen with the 1-pixel outer line fully visible.

#### Step 3: Black Level & Shadow Detail / PLUGE (+15% Accuracy)
* **Goal:** Prevent black crush while preserving ink-like deep black levels (Legal black = 16).
* **Target Pattern:** PLUGE pattern with background at reference black (16), below-black bars (12, 14), and flashing near-black bars (17, 18, 19, 20, 24).
* **Action:** Adjust TV `Brightness` (or `Black Level`). Bar 16 must blend completely into the background, while bar 17 and above must flash faintly and remain visible in a darkened room.

#### Step 4: White Level & Contrast Clipping (+10% Accuracy)
* **Goal:** Maximize dynamic contrast without clipping specular highlights or inducing color shifts (Legal white = 235).
* **Target Pattern:** Reference white background with flashing highlight bars (230, 232, 234, 238, 242).
* **Action:** Adjust TV `Contrast`. Ensure bars up to 234 flash distinctly without washing out into pure uniform white and without turning pink or cyan.

#### Step 5: Sharpness & Anti-Ringing (+10% Accuracy)
* **Goal:** Remove artificial edge sharpening halos and restore original director resolution.
* **Target Pattern:** High-frequency 1-pixel checkerboard, concentric circles, and fine line grids.
* **Action:** Reduce TV `Sharpness` towards `0` (or `50` on Sony default neutral). Adjust until the harsh glowing white fringes/halos around black lines disappear without blurring the lines themselves.

#### Step 6: Color Temperature (D65 Grayscale) (+10% Accuracy)
* **Goal:** Align white point to the D65 cinema standard (6,504 Kelvin) and eliminate cold blue tint.
* **Target Pattern:** 20-step neutral grayscale gradient ramp from 0% to 100% luminance with reference gray cards.
* **Action:** Set TV `Color Temperature` to `Warm`, `Warm 2`, `Expert 1`, or `Warm 50`. Confirm that the 20 grayscale steps look neutral and uniform from black to white without green, pink, or blue color casts.

#### Step 7: 24p Motion & Cadence (+15% Accuracy)
* **Goal:** Restore true 24.000 fps cinematic film cadence and eliminate the unnatural "soap opera effect".
* **Target Pattern:** Bundled 24.000 fps Media3 video clip showing smooth motion traversal across the screen.
* **Action:** Turn off Motion Interpolation: set `Motionflow`, `TruMotion`, `Action Clarity`, or `Auto Motion Plus` to `Off` (or `True Cinema` / `Film`).

---

### Brand-Specific Picture Settings Guide

| TV Brand | Picture Mode | Color Temp (D65) | Sharpness | Motion Smoothing | Aspect Ratio (1:1) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Sony BRAVIA** | Custom / Pro | Expert 1 | 50 (Neutral) / 0 | Motionflow: Off / True Cinema | Wide mode: Full / +1 Area |
| **TCL Google TV** | Movie | Warm -5 | 0 | Action Clarity / LED Motion: Off | Auto Format: Off, Just Scan: On |
| **Hisense Google TV** | Filmmaker / Theater Day | Warm | 0 | Ultra Smooth Motion: Off | Direct / 1:1 |
| **Samsung Box (Tizen)**| Movie / Filmmaker | Warm 2 | 0 | Auto Motion Plus: Off | 16:9 Standard / Fit to Screen: On |
| **LG Box (webOS)** | Cinema / Filmmaker | Warm 50 | 0 | TruMotion: Off | Original / Just Scan: On |
| **Universal / Other** | Movie / Cinema | Warm / Warm 2 | 0 | Motion Smoothing: Off | Just Scan / 1:1 / Full |

---

### Cinema Match Accuracy Calculation

The **Live Cinema Match Index (%)** calculates real-time fidelity based on verified steps:

$$\text{Accuracy} = \min\left(100, \text{Baseline (35\%)} + \sum_{\text{calibrated}} \text{Step Weight}\right)$$

| Score Range | Fidelity Level | Description |
| :---: | :--- | :--- |
| **35% – 49%** | **Factory Default** | Factory Vivid mode; extreme blue tint, heavy black crush, motion smoothing active. |
| **50% – 74%** | **Improved** | Proper picture preset and overscan disabled; cleaner picture geometry. |
| **75% – 89%** | **High Fidelity** | Precise black and white clipping calibrated; natural sharpness and shadow details. |
| **90% – 100%** | **Cinema Reference** | Full studio mastering alignment: D65 white point, Rec.709/BT.1886 gamma, and native 24p cadence. |

---

### Installation & Build Guide

#### Prerequisites
* Android Studio Iguana / Jellyfish or later (with Kotlin 2.0 support)
* Android SDK 34 (Android 14) and Android NDK (optional)
* Android TV / Google TV device running Android 9.0+ (API 28+)

#### 1. Build from Source
```bash
# Clone the repository
git clone https://github.com/cinemavision-tv/cinema-vision-tv.git
cd cinema-vision-tv

# Run test suite (73 tests across all modules)
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

#### 2. Install via ADB (Network Debugging)
1. On your Android TV, open **Settings → Device Preferences → About**.
2. Click **Build** 7 times to enable **Developer Options**.
3. Go to **Settings → Developer Options** and enable **Network Debugging**.
4. Check your TV's IP address in **Settings → Network & Internet**.
5. From your development terminal:
```bash
adb connect <TV_IP_ADDRESS>:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### 3. Install via USB Flash Drive
1. Copy `app-debug.apk` onto a FAT32/exFAT formatted USB flash drive.
2. Insert the USB drive into your Android TV.
3. Open any TV File Manager (e.g. *File Commander* or *FX File Explorer*) from Google Play on TV.
4. Select `app-debug.apk` and choose **Install** (allow installation from unknown sources if prompted).

---

<br><br>

---

<a name="thai"></a>
## ภาษาไทย (Thai Documentation)

### CinemaVision TV: แอปปรับจูนภาพจอ Android TV สู่มาตรฐานโรงภาพยนตร์
**ปรับแต่งจอทีวีสู่มาตรฐานสตูดิโอฮอลลีวูด (D65 · Rec.709 · BT.1886 · 24p) ด้วยรีโมตทีวีเครื่องเดียว**

---

### ทำไมต้องมีแอป CinemaVision TV?

ทีวีรุ่นใหม่กว่า 90% (OLED, Mini-LED, QLED, LED) ที่เพิ่งแกะกล่องออกมา จะถูกตั้งค่าเริ่มต้นจากโรงงานเป็นโหมด **"สดใส (Vivid / Store Dynamic)"** ซึ่งสร้างขึ้นเพื่อเน้นความสว่างจัดจ้านแข่งกับไฟนีออนในห้างสรรพสินค้า แต่เมื่อนำมาดูหนังในบ้าน ค่าเริ่มต้นนี้กลับทำลายคุณภาพของภาพยนตร์อย่างรุนแรง:

1. **ภาพอมฟ้าจัด (9,000K – 11,000K):** อุณหภูมิสีเย็นเกินไป ทำให้สีผิวคนกลายเป็นสีฟ้าอมม่วง ไม่ตรงตามมาตรฐานฮอลลีวูด **D65 (6,504K)**
2. **เงาดำจมหาย (Black Crush):** ในฉากมืด เสื้อผ้าสีดำหรือเงาในเงามืดจะกลืนเป็นก้อนดำสนิท รายละเอียดในที่มืดหายไปหมด
3. **ไฮไลต์สว่างล้น (White Clipping):** ก้อนเมฆ แสงแดด ระเบิด หรือผิวน้ำที่สว่างจัด จะกลืนกลายเป็นแผ่นสีขาวล้วน สูญเสียมิติของภาพ
4. **เส้นขอบขาวหลอกตา (Edge Ringing):** การเร่ง Sharpness มากเกินไปทำให้เกิดเส้นขอบเรืองแสงสีขาวรอบตัวละคร
5. **ภาพลื่นแบบละครเวที (Soap Opera Effect):** ระบบแทรกเฟรมภาพ (MEMC) บังคับเร่งภาพ 24fps ให้กลายเป็น 60/120fps จนหนังฟอร์มยักษ์ดูเหมือนละครเวทีต้นทุนต่ำ

**ทางออก:** การจ้างช่างปรับเทียบสีระดับมืออาชีพพร้อมเครื่องมือวัด (Colorimeter) มีค่าใช้จ่าย 5,000 - 30,000 บาท **CinemaVision TV** จึงถูกพัฒนาขึ้นเพื่อให้เป็น **แอป Android TV ฟรี 100%** ที่มีแพทเทิร์นทดสอบความละเอียด 4K แท้ระดับพิกเซล พร้อมคู่มือภาษาไทยแนะนำการปรับทีวีทีละขั้นตอน จูนเสร็จภายใน 10 นาที โดยใช้เพียงรีโมตทีวีเดิม ไม่ต้องใช้อุปกรณ์ภายนอกใดๆ เพิ่มเติม

---

### ฟีเจอร์เด่นของ CinemaVision TV

* **ทำงานบนทีวี 100% (Standalone):** ทำงานได้ในตัว Android TV / Google TV โดยตรง ไม่ต้องเชื่อมต่อคอมพิวเตอร์หรือสมาร์ทโฟน ไม่ต้องลงทะเบียน ไม่มีโฆษณา ฟรีตลอดชีพ
* **แพทเทิร์น Native 4K Bit-Accurate:** แสดงผลบนฮาร์ดแวร์ `SurfaceView` ขนาด 3840×2160 พิกเซล 1:1 คมชัดทุกเม็ดพิกเซล (ปรับสเกลลง 1080p อัตโนมัติบนจอ Full HD)
* **เกจวัดความแม่นยำภาพยนตร์สด (Live Cinema Match Index %):** มาตรวัดความเที่ยงตรงของภาพแบบเรียลไทม์ เริ่มต้นที่ ~35% (ค่าโรงงาน) และเพิ่มขึ้นจนถึง 100% (Cinema Reference) เมื่อปรับตามคำแนะนำครบทุกขั้นตอน
* **เอนจินวิดีโอ 24.000 fps Cadence แท้:** เล่นคลิปวิดีโอทดสอบเฟรมเรต 24p ผ่าน Media3 เพื่อตรวจจับและปิดระบบแทรกเฟรมภาพหลอกตาได้อย่างแม่นยำ
* **เมนู HUD สองภาษา พร้อมระบบซ่อนอัตโนมัติ:** หน้าต่างแนะนำเป็นภาษาไทยและอังกฤษ มีตัวนับถอยหลังซ่อน HUD อัตโนมัติใน 6 วินาที เพื่อให้มองเห็นแพทเทิร์นเต็มจออย่างชัดเจน
* **คำแนะนำเมนูตรงตามยี่ห้อทีวี:** แนะนำชื่อเมนูเฉพาะของ **Sony BRAVIA**, **TCL**, **Hisense**, **Samsung Box**, **LG Box** และ **แบรนด์ทั่วไป**
* **รองรับช่วงสัญญาณสีทั้ง Limited (16–235) และ Full (0–255):** สลับได้ตามแหล่งสัญญาณหนังและสตรีมมิ่ง

---

### การควบคุมด้วยรีโมตทีวี (TV Remote Control)

| ปุ่มบนรีโมต | หน้าที่การทำงาน |
| :--- | :--- |
| **ลูกศรขวา (►)** | ไปยังขั้นตอนการปรับจูนถัดไป (ขั้นตอน 1 ถึง 7) |
| **ลูกศรซ้าย (◄)** | ย้อนกลับไปยังขั้นตอนก่อนหน้า |
| **ลูกศรขึ้น / ลง (▲ / ▼)** | สลับยี่ห้อทีวีเพื่อดูชื่อเมนูเฉพาะ (Sony, TCL, Hisense, Samsung, LG, ทั่วไป) |
| **ปุ่ม OK / ตรงกลาง** | ทำเครื่องหมายว่า "ปรับตั้งค่าแล้ว" (เพิ่มคะแนน Cinema Match %) |
| **ปุ่มย้อนกลับ (Back)** | ขณะเปิด HUD: กดเพื่อซ่อน HUD ทันทีเพื่อตรวจเช็กภาพเต็มจอ<br>ขณะซ่อน HUD: กดเพื่อออกจากแอป |

> **ระบบซ่อนอัตโนมัติ:** หน้าต่างคำแนะนำ (HUD) จะค่อยๆ จางหายไปเองหลังจากไม่มีการกดปุ่มเป็นเวลา 6 วินาที กดปุ่มใดๆ หรือกดปุ่ม `OK` เพื่อเรียกเมนูกลับมา

---

### คู่มือการปรับจูนทีวี 7 ขั้นตอน (Calibration Walkthrough)

#### ขั้นตอนที่ 1: เลือกโหมดภาพเริ่มต้น (+20% ความแม่นยำ)
* **เป้าหมาย:** ออกจากโหมด Vivid / Standard เพื่อหยุดการเร่งสีและเร่งคอนทราสต์ที่ผิดเพี้ยน
* **การปรับ:** เลือกโหมดภาพเป็น `Movie`, `Cinema`, `Filmmaker Mode` หรือ `Custom`
* **คำแนะนำ:** ปิดระบบประหยัดพลังงาน (Eco Mode) และปิดเซนเซอร์วัดแสงห้อง (Ambient Light Sensor)

#### ขั้นตอนที่ 2: อัตราส่วนภาพและการครอบตัด 1:1 (+10% ความแม่นยำ)
* **เป้าหมาย:** ให้พิกเซลจากวิดีโอส่งตรงถึงหน้าจอแบบ 1:1 ไม่ถูกขยายซูมหรือตัดขอบภาพ (Overscan)
* **แพทเทิร์น:** กรอบสี่เหลี่ยม 1 พิกเซลรอบขอบจอ พร้อมหัวลูกศรแตะขอบจอทั้ง 4 ด้าน
* **การปรับ:** ตั้งค่า Aspect Ratio / Picture Size ให้เป็น `Just Scan: On`, `Full`, `1:1`, `Direct` หรือ `Fit to Screen` จนปลายลูกศรแตะขอบจอพอดี และมองเห็นเส้นกรอบ 1 พิกเซลครบทั้ง 4 ด้าน

#### ขั้นตอนที่ 3: ระดับสีดำและรายละเอียดในเงามืด (Brightness / PLUGE) (+15% ความแม่นยำ)
* **เป้าหมาย:** คืนรายละเอียดในฉากมืด โดยที่สีดำยังคงดำสนิท ไม่กลายเป็นสีเทา
* **แพทเทิร์น:** แถบ PLUGE พื้นหลังเป็นสีดำมาตรฐาน (16) และมีแถบกระพริบระดับ 17, 18, 19, 20, 24
* **การปรับ:** ปรับค่า `Brightness` (หรือ `Black Level`) จนกระทั่งแถบ 16 กลืนสนิทกับพื้นหลัง และมองเห็นแถบ 17 ขึ้นไปกระพริบอย่างแผ่วเบาในห้องมืด

#### ขั้นตอนที่ 4: ระดับสีขาวและไฮไลต์ไม่ล้น (Contrast / White Clipping) (+10% ความแม่นยำ)
* **เป้าหมาย:** สว่างเต็มมิติโดยที่รายละเอียดสีขาวสว่างไม่กลืนหายเป็นก้อนเดียว
* **แพทเทิร์น:** พื้นหลังสีขาวอ้างอิง พร้อมแถบไฮไลต์กระพริบระดับ 230, 232, 234, 238, 242
* **การปรับ:** ปรับค่า `Contrast` จนมองเห็นแถบกระพริบได้ถึงระดับ 234 โดยไม่กลืนหายเป็นสีขาวล้วน และไม่มีสีเพี้ยนเป็นสีชมพูหรือฟ้า

#### ขั้นตอนที่ 5: ความคมชัดและเส้นเงาขอบ (Sharpness & Anti-Ringing) (+10% ความแม่นยำ)
* **เป้าหมาย:** นำความคมชัดสังเคราะห์ออก เพื่อให้ภาพเนียนตาเป็นธรรมชาติเหมือนฟิล์มภาพยนตร์
* **แพทเทิร์น:** ตารางหมากรุก 1 พิกเซลความถี่สูง วงกลมซ้อน และเส้นตารางตัดกัน
* **การปรับ:** ลดค่า `Sharpness` ลงจนเหลือ `0` (หรือ `50` สำหรับทีวี Sony ซึ่งเป็นค่าศูนย์กลางธรรมชาติ) สังเกตว่าเส้นขอบขาวเรืองแสง (halo) รอบเส้นสีดำจะหายไปจนหมด

#### ขั้นตอนที่ 6: อุณหภูมิสีมาตรฐานสตูดิโอ D65 (+10% ความแม่นยำ)
* **เป้าหมาย:** ปรับสมดุลแสงขาวให้อยู่ที่ 6,500 Kelvin (D65) ให้โทนสีผิวตรงตามที่ผู้กำกับต้องการ
* **แพทเทิร์น:** แถบไล่เฉดสีเทา 20 ระดับ (Grayscale Ramp) จากดำสนิทถึงขาวบริสุทธิ์
* **การปรับ:** ตั้งค่า `Color Temperature` เป็น `Warm`, `Warm 2`, `Expert 1` หรือ `Warm 50` สังเกตแถบสีเทาทั้ง 20 ขั้นตอนต้องเป็นสีเทากลางที่สะอาด ไม่มีเฉดเขียว ฟ้า หรือแดงปน

#### ขั้นตอนที่ 7: การเคลื่อนไหว 24p และกำจัด Judder (+15% ความแม่นยำ)
* **เป้าหมาย:** คืนจังหวะการเคลื่อนไหว 24 เฟรมต่อวินาทีแท้จริง และปิดระบบแทรกเฟรมที่ทำให้ภาพดูลื่นหลอกตา
* **แพทเทิร์น:** คลิปวิดีโอ 24.000 fps แท้ แสดงการเคลื่อนที่ของวัตถุตัดผ่านหน้าจอ
* **การปรับ:** ปิดระบบแทรกเฟรมภาพ: ตั้งค่า `Motionflow`, `TruMotion`, `Action Clarity` หรือ `Auto Motion Plus` ให้เป็น `Off` (หรือ `True Cinema` / `Film`)

---

### ตารางแนะนำเมนูแยกตามยี่ห้อทีวี

| ยี่ห้อทีวี | โหมดภาพ (Picture Mode) | อุณหภูมิสี (Color Temp) | ความคมชัด (Sharpness) | ระบบแทรกเฟรม (Motion) | อัตราส่วนภาพ (Aspect Ratio) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Sony BRAVIA** | Custom / Pro | Expert 1 | 50 (ค่ากลาง) / 0 | Motionflow: Off / True Cinema | Wide mode: Full / +1 Area |
| **TCL Google TV** | Movie | Warm -5 | 0 | Action Clarity / LED Motion: Off | Auto Format: Off, Just Scan: On |
| **Hisense Google TV** | Filmmaker / Theater Day | Warm | 0 | Ultra Smooth Motion: Off | Direct / 1:1 |
| **Samsung Box (Tizen)**| Movie / Filmmaker | Warm 2 | 0 | Auto Motion Plus: Off | 16:9 Standard / Fit to Screen: On |
| **LG Box (webOS)** | Cinema / Filmmaker | Warm 50 | 0 | TruMotion: Off | Original / Just Scan: On |
| **แบรนด์ทั่วไป / กล่องอื่นๆ** | Movie / Cinema | Warm / Warm 2 | 0 | Motion Smoothing: Off | Just Scan / 1:1 / Full |

---

### การคำนวณคะแนนความแม่นยำ (Cinema Match Index %)

ระดับความแม่นยำคำนวณจากคะแนนพื้นฐานรวมกับน้ำหนักของแต่ละขั้นตอนที่ปรับสำเร็จ:

$$\text{คะแนนความแม่นยำ} = \min\left(100, \text{ค่าเริ่มต้น (35\%)} + \sum \text{น้ำหนักขั้นตอนที่ปรับแล้ว}\right)$$

| ระดับคะแนน | สถานะความเที่ยงตรง | คำอธิบาย |
| :---: | :--- | :--- |
| **35% – 49%** | **Factory Default** | ค่าเริ่มต้นโรงงาน ภาพอมฟ้าหนัก เงามืดจม ไฮไลต์ล้น และเปิดระบบแทรกเฟรม |
| **50% – 74%** | **Improved** | โหมดภาพถูกต้องและปิด Overscan แล้ว มิติภาพเริ่มชัดเจน |
| **75% – 89%** | **High Fidelity** | ระดับสีดำและสีขาวถูกต้อง ไม่มีเส้นขอบหลอกตา รายละเอียดในเงามืดชัดเจน |
| **90% – 100%** | **Cinema Reference** | ได้มาตรฐานโรงภาพยนตร์สมบูรณ์แบบ ทั้งสมดุลแสงขาว D65 และเฟรมเรต 24p แท้จริง |

---

### วิธีการติดตั้งและทดสอบ (Installation & Setup)

#### ข้อกำหนดของระบบ
* ทีวีระบบ Android TV หรือ Google TV (Android 9.0 ขึ้นไป / API Level 28+)
* คอมพิวเตอร์ติดตั้ง Android SDK หรือโปรแกรมจัดการไฟล์บนทีวี

#### 1. การติดตั้งผ่าน ADB ผ่านเครือข่าย Wi-Fi
1. บนทีวี ไปที่ **การตั้งค่า → การตั้งค่ากำหนดอุปกรณ์ → เกี่ยวกับ**
2. กดที่หัวข้อ **บิลด์ (Build Number)** ซ้ำๆ 7 ครั้ง จนระบบแจ้งว่าคุณเป็นนักพัฒนาซอฟต์แวร์แล้ว
3. ไปที่ **การตั้งค่า → ตัวเลือกสำหรับนักพัฒนา** และเปิดใช้งาน **การแก้จุดบกพร่องเครือข่าย (Network Debugging)**
4. ดูหมายเลข IP ของทีวีในเมนู **เครือข่ายและอินเทอร์เน็ต**
5. เปิด Terminal บนคอมพิวเตอร์ แล้วสั่งติดตั้ง:
```bash
adb connect <IP_ของทีวี>:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### 2. การติดตั้งผ่านแฟลชไดรฟ์ USB
1. คัดลอกไฟล์ `app-debug.apk` ลงในแฟลชไดรฟ์ USB (ฟอร์แมตแบบ FAT32 หรือ exFAT)
2. นำแฟลชไดรฟ์ไปเสียบที่ช่อง USB ของ Android TV
3. เปิดแอปตัวจัดการไฟล์บนทีวี (เช่น *File Commander* หรือ *FX File Explorer*)
4. กดเลือกไฟล์ `app-debug.apk` แล้วกด **ติดตั้ง (Install)**

---

### สถาปัตยกรรมทางเทคนิค (Technical Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│  Layer 2 (Top): Jetpack Compose for TV (Calibration HUD)    │
│  - Live Cinema Match Gauge (%)                              │
│  - Brand Tip Card (Sony / TCL / Hisense / Samsung / LG)     │
│  - Step Pills & Remote Controller Key Event Handling        │
│  - Auto-hide coroutine timer (6s inactivity)                │
├─────────────────────────────────────────────────────────────┤
│  Layer 1 (Middle): androidx.media3.ui.PlayerView            │
│  - Step 7 only: 24.000 fps Cadence Judder Video Loop        │
│  - Hardware accelerated decoder with surface rendering     │
├─────────────────────────────────────────────────────────────┤
│  Layer 0 (Bottom): PatternSurfaceView (Hardware Surface)    │
│  - Steps 1-6: Bit-accurate 4K Canvas Pattern Generator      │
│  - SurfaceHolder.setFixedSize(3840, 2160)                   │
│  - 1.0 Hz blinking cycle for PLUGE & White Clipping bars    │
└─────────────────────────────────────────────────────────────┘
```

* **Module Breakdown:**
  * `domain`: Pure Kotlin business logic (`ColorRange`, `PatternStep`, `TvBrand`, `CinemaAccuracyCalculator`).
  * `ui.pattern`: Hardware Canvas mathematical pattern engine (`PatternCanvasEngine`, `PatternSurfaceView`).
  * `ui.player`: Media3 ExoPlayer lifecycle manager (`JudderPlayerManager`).
  * `ui.hud`: Leanback & Compose for TV overlay (`CalibrationHudOverlay`, `CinemaAccuracyMeter`, `BrandSelectorCard`, `HudState`).
  * `MainActivity`: Immersive sticky container coordinating dual-layer rendering and hardware remote dispatch.

---

### สรุปผลการทดสอบ (Test Verification Suite)

โครงการนี้ผ่านการทดสอบแบบ Unit Test ครบถ้วน 100% (73 tests):
* `ColorRangeTest`: 6 tests (Legal 16-235 vs Full 0-255 math, PLUGE bars, white clipping levels)
* `PatternStepTest`: 3 tests (Step transitions, weights sum to 90%, IDs 1-7)
* `CinemaAccuracyCalculatorTest`: 4 tests (Baseline 35%, step addition, 100% cap, fidelity tiers)
* `BrandGuidanceTest`: 8 tests (Sony, TCL, Hisense, Samsung, LG, Universal fallback)
* `PatternGeometryTest`: 13 tests (4K/1080p safe zone, 20-step grayscale ramp coordinates, 1px overscan border)
* `JudderPlayerManagerTest`: 10 tests (Media3 configuration, repeat mode, muted audio, lifecycle release)
* `HudStateTest`: 13 tests (6-second auto-hide timeout, step navigation, brand switching, calibration toggling)
* `MainActivityCoordinationTest`: 16 tests (Dual-layer switching, key dispatching, pause/resume lifecycle)

```
> Task :app:compileTestKotlin
> Task :app:test
JUnit version 4.12
.........................................................................
Time: 0.655s

OK (73 tests)

BUILD SUCCESSFUL
```

---

### License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
