# 🛡️ ShoulderGuard: Shoulder Surfing Protection

**ShoulderGuard** is an Android application designed to protect sensitive on-screen information from "shoulder surfing"—the act of unauthorized people looking over a user's shoulder in public spaces. 

The app utilizes real-time computer vision to detect when an additional person is looking at the screen and immediately triggers a privacy shield to hide sensitive content.

## 🚀 Key Features

- **Real-time Detection:** Uses the front-facing camera to monitor the environment for multiple faces.
- **Dynamic Privacy Shield:** Instantly applies a full-screen overlay when a "surfer" is detected.
- **User vs. Surfer Distinction:** Logic to differentiate the primary user (closest/largest face) from potential intruders.
- **Zero-Data Footprint:** All processing happens on-device; no images are stored or transmitted.

## 🛠️ Technical Stack

- **Language:** Kotlin
- **Camera Framework:** [CameraX](https://developer.android.com/training/camerax) (Lifecycle-aware camera management)
- **AI/ML Engine:** [Google ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection) (On-device inference)
- **UI:** Material Design with a high-priority Overlay View

## ⚙️ How It Works

### 1. Face Detection Pipeline
The app initializes a `CameraX` analysis stream. Every frame is passed to the **ML Kit Face Detector**, which returns a list of detected faces and their bounding boxes.

### 2. The "Surfer" Logic
To avoid false positives and distinguish the user from a surfer, the app implements the following logic:
- **Count Check:** If `faces.size > 1`, the system flags a potential shoulder surfing event.
- **Positioning:** The primary user is assumed to be the largest face centered in the frame. Any secondary face entering the frame triggers the protection mechanism.

### 3. Privacy Protection
Upon detection, the `PrivacyManager` toggles the visibility of a `FrameLayout` overlay (`privacyShield`). This prevents the content from being viewed without requiring an app restart or activity reload.

## 🔋 Optimization & Constraints

To meet the strict requirements of the problem statement, the following optimizations were implemented:

| Constraint | Implementation Strategy |
| :--- | :--- |
| **Privacy** | **On-Device Processing:** ML Kit is configured for local execution. No images are saved to disk or uploaded to the cloud. |
| **Battery** | **Frame Throttling:** Instead of processing 30fps, the analyzer is throttled to process every 5th frame (~6fps). This reduces CPU load and heat. |
| **Hardware** | **Front-Camera Only:** Hardcoded to use `DEFAULT_FRONT_CAMERA` to monitor the user's vicinity. |
| **Performance**| **Background Execution:** Camera analysis runs on a dedicated `ExecutorService` to ensure the UI remains smooth (no jank). |

## 📦 Installation & Setup

### Prerequisites
- Android Studio Flamingo or newer.
- A physical Android device (Camera features do not work fully on Emulators).
- Minimum SDK: 24.

### Steps
1. **Clone the Repository:**
```bash
   git clone https://github.com/yourusername/shoulder-guard.git
```
2. **Import to Android Studio:**
   - Open Android Studio $\rightarrow$ `Open` $\rightarrow$ Select the project folder.
3. **Sync Gradle:**
   - Allow Gradle to download the CameraX and ML Kit dependencies.
4. **Run the App:**
   - Connect your device via USB and click `Run`.
   - **Grant Camera Permissions** when prompted. sanjay anna payalugada

## 📸 Usage
1. Open the app to see the "Sensitive Dashboard."
2. While looking at the screen, have a second person enter the camera's field of view.
3. The screen will immediately turn dark with a **"Privacy Alert"** message.
4. Once the second person leaves, the content will automatically reappear.
