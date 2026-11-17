## Summary
Briefly describe what this PR changes and why.

## Related Issue
Closes #

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Refactor / Clean Architecture improvement
- [ ] Documentation / CI / Open-source tooling

## Architecture & Clean Code Checklist
- [ ] UI Composables follow stateless `*Screen(uiState, onEvent)` pattern (no direct repository/network calls)
- [ ] Domain models & UseCases remain pure Kotlin (no Android framework imports)
- [ ] No hardcoded API keys, credentials, or copyrighted assets introduced
- [ ] `./gradlew testDebugUnitTest assembleDebug` passes locally

## Screenshots / Demo (if UI changed)
<!-- Paste before/after screenshots or screen recordings here -->
