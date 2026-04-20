# 🏗️ FitFlow Architecture Guide (Web to Android Migration)

This project is built using a **Clean Architecture** pattern in React that mirrors the best practices for **Android Jetpack Compose** development. Use this as a map to build your native Android app.

## 📂 Mapping the Folder Structure

| Web (React) | Android (Jetpack Compose) | Purpose |
| :--- | :--- | :--- |
| `src/types.ts` | `data/models/*.kt` | **Domain Layer**: Core data structures (Exercise, Plan, Stats). |
| `src/services/` | `data/repository/` | **Data Layer**: API calls (Gemini), Local Persistence (Room/DataStore). |
| `src/App.tsx` (Logic) | `ui/viewmodels/` | **ViewModel**: State management, business logic, handling UI events. |
| `src/App.tsx` (JSX) | `ui/screens/` | **View Layer**: Composable functions for Home, Planner, Onboarding. |
| `src/constants.ts` | `utils/Constants.kt` | Static configuration and default values. |

## 🚀 Feature Implementation Logic

### 1. BMI & AI Plan Generation
- **Web**: `geminiService.ts` uses the `@google/genai` SDK.
- **Android**: Use the Firebase Vertex AI SDK or direct Retrofit calls to the Gemini API. Pass the user's BMI and Goal to generate the 30-day JSON.

### 2. Streak System
- **Logic**: Track `lastWorkoutDate` and `currentStreak`.
- **Constraint**: If `today - lastWorkoutDate > 1`, reset streak. If `today - lastWorkoutDate >= 3`, trigger a notification.
- **Android Implementation**: Use `WorkManager` for daily background checks to update streaks and send reminders.

### 3. Flexible Planner
- **Web**: State-driven Record of dates to `WorkoutDay`.
- **Android**: Use a `LazyColumn` or `VerticalGrid` representing the 30 days. Store modifications in a local **Room Database**.

### 4. Custom Exercises
- **Logic**: A local list that merges `DEFAULT_EXERCISES` with user-added ones via `localStorage`.
- **Android**: Use a Repository that combines hardcoded resource lists with a Room DB table for custom entries.

### 5. Notifications
- **Web**: Simulated.
- **Android**: Use `NotificationManager` and `AlarmManager` to schedule local notifications for "Workout Time" and "Inactivity Reminders".

## 🎨 Sophisticated Dark Theme (Jetpack Compose Mapping)

To replicate the current design in Android, use these values in your `Theme.kt`:

### 1. Colors (`Colors.kt`)
```kotlin
val BackgroundDark = Color(0xFF050505)
val CardDark = Color(0xFF111111)
val AccentNeon = Color(0xFFC1FF72)
val TextDim = Color(0xFFE0E0E0)
val TextMuted = Color(0x66FFFFFF) // White with 40% opacity
```

### 2. Typography
- **Primary Font**: Inter (Add to `res/font/inter.ttf`)
- **Display Style**:
  - `FontWeight.Black`, `FontStyle.Italic` for Headers.
  - `LetterSpacing` = 0.1em or higher for labels.

### 3. Components
- **Card**: Use `Surface` with `shape = RoundedCornerShape(32.dp)` and `color = CardDark`.
- **Glow Effect**: Use `Modifier.shadow` or a custom draw modifier with `BlurMaskFilter` to create the neon glow around stats.

## 🚀 Progressive Web App (PWA)
The project is configured as a PWA. You can test the "Mobile Experience" now:
1. Open this app on your phone's browser.
2. Select **"Add to Home Screen"**.
3. It will behave like a standalone app with its own icon and splash screen.
