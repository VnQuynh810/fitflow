# Contributing to FitFlow

Thank you for your interest in contributing to **FitFlow (6 Packs in 36 Days)**! Whether you are fixing bugs, adding exercises, translating strings, or improving UI performance, your contributions are welcome.

## Getting Started

1. **Prerequisites**:
   - Android Studio Ladybug (2024.2+) or newer
   - JDK 17
   - Android SDK 35

2. **Clone & Build**:
   ```bash
   git clone https://github.com/VnQuynh810/fitflow.git
   cd fitflow
   ./gradlew assembleDebug
   ```
   The project builds cleanly out of the box **without** requiring any API keys.

3. **Optional — AI Coach API Key**:
   - Add `GEMINI_API_KEY=your_api_key_here` to your root `local.properties` (git-ignored), **or** enter a key inside the app under **Workout Settings -> AI Coach API Key**.

4. **Optional — Offline Exercise GIF Pack**:
   - By default, FitFlow streams and caches exercise animations from the [`Exercise_GIFs`](https://github.com/wind5293/Exercise_GIFs) release CDN via Coil.
   - To bundle GIFs locally for offline development, run:
     ```powershell
     pwsh ./scripts/download_gifs.ps1
     ```

## Clean Code & Architecture Rules

Please review [ARCHITECTURE.md](./ARCHITECTURE.md) before opening a Pull Request:

- **Feature-First Packaging**: Place new screens, UI states, and ViewModels under `feature/<feature_name>/`.
- **Stateless Composables**: Split screens into `*Route` (collects `StateFlow` from `@HiltViewModel`) and `*Screen(uiState, onEvent)` (pure UI with `@Preview`). Keep individual files concise (< 350 lines).
- **Pure Domain Layer**: `core/model` and `core/domain` must not import `android.*` framework classes (except `Parcelable` if needed) so unit tests run directly on the JVM.
- **Single Source of Truth**: Persist structured relational records in Room (`core/database`) and user flags/settings in Jetpack DataStore (`core/datastore`). Do not introduce raw `SharedPreferences` or mutable static singletons.
- **Licensing**: Only contribute code, icons, images, and audio that are your original work or licensed under MIT / CC0 / Apache-2.0 compatible terms.

## Submitting Pull Requests

1. Create a descriptive branch (`feat/water-widget`, `fix/streak-midnight-calc`, `refactor/planner-cards`).
2. Follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).
3. Verify tests and debug build before pushing:
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```
