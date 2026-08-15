# StepStack

StepStack is an Android fitness app built with Kotlin and Jetpack Compose. It combines hardware step tracking with a library of guided workouts, letting you track daily activity, follow structured workout sessions with audio/visual cues, build your own routines, and review your history over time.

## Features

- **Step tracking** — uses the device's hardware `TYPE_STEP_COUNTER` and `TYPE_STEP_DETECTOR` sensors for accurate, real-time step counts, with automatic reboot and day-rollover handling. A foreground service (`StepTrackerService`) keeps tracking alive in the background, restarting after device reboot via `BootReceiver`.
- **Workout library** — dozens of built-in workouts across seven categories (Cardio, Strength, Yoga, HIIT, Stretching, Mobility, Quick) and four difficulty tiers (Beginner through Expert), each with a full exercise breakdown, animations, and instructions.
- **Guided workout sessions** — step-by-step guided playback with voice cues, timers, whistle/beep sounds, and rest periods (`AudioCues`, `GuidedWorkoutScreen`).
- **Workout builder** — create and edit your own custom routines from the exercise library.
- **History & progress** — workout history, streaks, and daily activity charts (`WorkoutHistoryScreen`, `StepsScreen`).
- **Profile & goals** — set fitness goals, activity level, and equipment access; onboarding flow captures this on first launch.
- **Auth & sync** — Firebase Authentication with cloud sync of user data via Firebase Realtime Database.
- **Health Connect integration** — optional sync with Android Health Connect for steps/exercise data.
- **Notifications** — daily reminders and milestone notifications (`NotificationSystem`).
- **Avatar uploads** — profile picture uploads via Cloudinary.

## Tech stack

- **UI**: Jetpack Compose, Material 3
- **Language**: Kotlin
- **Architecture**: MVVM (`AppViewModel`, `WorkoutViewModel`) with Compose state
- **Local storage**: Room (workout cache, activity history, sessions), SharedPreferences (settings/onboarding state)
- **Serialization**: kotlinx.serialization
- **Navigation**: Navigation Compose
- **Auth & backend**: Firebase Authentication, Firebase Realtime Database
- **Media**: Coil (image loading), Cloudinary (avatar uploads)
- **Health data**: AndroidX Health Connect, Google Play Services Fitness
- **Background work**: Foreground service + `SensorEventListener` for step tracking

## Requirements

- Android Studio (Ladybug or newer recommended)
- JDK 17
- Android SDK: `compileSdk 35`, `minSdk 26`, `targetSdk 35`
- A Firebase project with an Android app registered (for `google-services.json`)
- A Cloudinary account (for avatar uploads)

## Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/emmanuelmwangi88-ui/StepStack_app.git
   cd StepStack_app
   ```

2. **Add Firebase config**
   Download `google-services.json` from your Firebase project (Project settings → Your apps → Android app with package `com.stepstack.app`) and place it at:
   ```
   app/google-services.json
   ```

3. **Configure Cloudinary**
   In `app/src/main/java/com/stepstack/app/util/CloudinaryManager.kt`, set your own `CLOUD_NAME` and an **unsigned upload preset** — do not hardcode an API secret in the app. Create an unsigned preset in the Cloudinary dashboard under Settings → Upload.

4. **Set your local SDK path**
   Android Studio will generate `local.properties` automatically on first sync, pointing at your local Android SDK.

5. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   or open the project in Android Studio and hit Run.

## Project structure

```
app/src/main/java/com/stepstack/app/
├── audio/            # Workout audio cues (voice, whistle, beeps)
├── data/             # Room database, models, sample workout data, Firebase sync
├── navigation/        # Nav graph and route definitions
├── notifications/     # Notification channels and scheduling
├── service/           # Step tracking foreground service, boot receiver
├── ui/
│   ├── components/    # Reusable composables (BodyMap, animations, shared UI)
│   ├── screens/        # App screens (Home, Workouts, Steps, Profile, etc.)
│   └── theme/          # Colors, typography, theme
├── util/               # Cloudinary manager
└── viewmodel/          # AppViewModel, WorkoutViewModel
```

## Permissions

The app requests: `ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_HEALTH`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`, `WAKE_LOCK`, `INTERNET`, and Health Connect read permissions for steps and exercise.

## Notes

- `.gitignore` excludes build artifacts, `local.properties`, and IDE files — regenerate them locally rather than pulling from version control.
- Never commit Cloudinary or Firebase **secrets** (API secrets, service account keys) to the repo. Client-side uploads should use unsigned presets only.

## License

Add a license of your choice (MIT is a common default for personal/portfolio projects).
