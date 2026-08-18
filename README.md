# StepStack

StepStack is an Android fitness app built with Kotlin and Jetpack Compose. It combines hardware step tracking with a library of guided workouts and a social competition layer — letting you track daily activity, follow structured workout sessions with audio/visual cues, build your own routines, compete with friends, and review your history over time.

## Features

- **Step tracking** — uses the device's hardware `TYPE_STEP_COUNTER` and `TYPE_STEP_DETECTOR` sensors for accurate, real-time step counts, with automatic reboot and day-rollover handling. A foreground service (`StepTrackerService`) keeps tracking alive in the background, restarting after device reboot via `BootReceiver`.
- **Workout library** — dozens of built-in workouts across seven categories (Cardio, Strength, Yoga, HIIT, Stretching, Mobility, Quick) and four difficulty tiers (Beginner through Expert), each with a full exercise breakdown, animations, and instructions.
- **Guided workout sessions** — step-by-step guided playback with voice cues, timers, whistle/beep sounds, and rest periods (`AudioCues`, `GuidedWorkoutScreen`).
- **Workout builder** — create and edit your own custom routines from the exercise library.
- **Competition / social** — friends, leaderboards, and step/workout challenges (`CompetitionHomeScreen`, `FriendProfileScreen`, `ChallengeDetailScreen`). Add friends, send/accept friend requests, create public or private challenges with a shared goal, and track participant progress in real time via Firestore.
- **Notifications** — in-app notification feed (`NotificationsScreen`) plus daily reminders and milestone notifications, with push notifications via Firebase Cloud Messaging.
- **History & progress** — workout history, streaks, and daily activity charts (`WorkoutHistoryScreen`, `StepsScreen`).
- **Profile & goals** — set fitness goals, activity level, and equipment access; onboarding flow captures this on first launch.
- **Auth & sync** — Firebase Authentication with cloud sync of user data via Firebase Realtime Database and Firestore.
- **Health Connect integration** — optional sync with Android Health Connect for steps/exercise data.
- **Avatar & challenge image uploads** — image uploads via Cloudinary using unsigned upload presets (no API secret shipped in the app).

## Tech stack

- **UI**: Jetpack Compose, Material 3
- **Language**: Kotlin
- **Architecture**: MVVM (`AppViewModel`, `WorkoutViewModel`) with Compose state
- **Local storage**: Room (workout cache, activity history, sessions), SharedPreferences (settings/onboarding state)
- **Serialization**: kotlinx.serialization
- **Navigation**: Navigation Compose
- **Auth & backend**: Firebase Authentication, Firebase Realtime Database, Firebase Firestore (friends/challenges/leaderboards), Firebase Cloud Messaging, Firebase Storage
- **Media**: Coil (image loading), Cloudinary (unsigned uploads)
- **Health data**: AndroidX Health Connect, Google Play Services Fitness
- **Background work**: Foreground service + `SensorEventListener` for step tracking

## Requirements

- Android Studio (Ladybug or newer recommended)
- JDK 17
- Android SDK: `compileSdk 35`, `minSdk 26`, `targetSdk 35`
- A Firebase project with an Android app registered (Auth, Realtime Database, Firestore, Cloud Messaging, and Storage enabled)
- A Cloudinary account with an **unsigned** upload preset

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
   Make sure Authentication, Realtime Database, Firestore, Cloud Messaging, and Storage are all enabled in the Firebase console, and set Firestore security rules to restrict reads/writes to authenticated users before shipping publicly.

3. **Configure Cloudinary**
   In `app/src/main/java/com/stepstack/app/util/CloudinaryManager.kt`, set your own `CLOUD_NAME` and unsigned `UPLOAD_PRESET` (create it in the Cloudinary dashboard under Settings → Upload → Add upload preset → Signing Mode: Unsigned). No API secret is needed or should ever be embedded in the app.

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
├── data/             # Room database, models, sample workout data, Firebase/Firestore sync
├── navigation/        # Nav graph and route definitions
├── notifications/     # Notification channels, FCM, and scheduling
├── service/           # Step tracking foreground service, boot receiver
├── ui/
│   ├── components/    # Reusable composables (BodyMap, animations, shared UI)
│   ├── screens/        # App screens (Home, Workouts, Steps, Profile, Competition, Friends, Challenges, etc.)
│   └── theme/          # Colors, typography, theme
├── util/               # Cloudinary manager
└── viewmodel/          # AppViewModel, WorkoutViewModel
```

## Permissions

The app requests: `ACTIVITY_RECOGNITION`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_HEALTH`, `RECEIVE_BOOT_COMPLETED`, `VIBRATE`, `WAKE_LOCK`, `INTERNET`, and Health Connect read permissions for steps and exercise.

## Notes

- `.gitignore` excludes build artifacts, `local.properties`, and IDE files going forward. Note: files already committed before `.gitignore` was added (`.gradle/`, `.idea/`, `build/`) need to be untracked once, with:
  ```bash
  git rm -r --cached .gradle .idea build
  git commit -m "Stop tracking build artifacts"
  ```
- Never commit Cloudinary or Firebase **secrets** (API secrets, service account keys) to the repo. Client-side uploads use unsigned presets only, and Firestore access should be locked down with security rules rather than relying on client-side checks alone.

## License

This project is licensed under the [MIT License](LICENSE).
