# Testing Guide & Test Evidence

> Part 1 shows **how** to test a mobile app. Part 2 is **your evidence** — graded under B1/B2.
> All results must come from actually running your app (see `INSTRUCTION.md` §7).

---

## Part 1 — How to Test

### 1.1 Automated tests

| Level | What to test | Examples of tools |
|-------|-------------|-------------------|
| Unit | ViewModels (event → state), repositories with a fake API, validation logic | JUnit/MockK, `flutter_test`, Jest, XCTest |
| UI / widget | A screen renders each UI state correctly | Compose UI tests, widget tests, React Native Testing Library |
| End-to-end (optional) | A full user flow on a device | Maestro, Espresso, XCUITest, Detox, `integration_test` |

A ViewModel test typically: fake the repository → trigger an event → assert the emitted states
(e.g., `Loading` then `Success(items)`, or `Loading` then `Error`).

### 1.2 Manual scenarios

| Scenario | How |
|----------|-----|
| Offline | Enable airplane mode, reopen the app and each data screen |
| Slow network | Emulator network throttling, or a delay in your backend |
| Server error / down | `make api-down` while the app is running |
| Empty data | Empty a collection in `backend/db.json` and `make api-reset` |
| Rotation & process death | Rotate on each screen; "Don't keep activities" (Android developer options) |
| Screen sizes & dark mode | Small phone, large phone, tablet; light and dark theme |

---

## Part 2 — Our Test Evidence

### Automated tests

Run from the repository root on 2026-10-24:

```powershell
cd app
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug --no-daemon

cd ..\backend
.\gradlew.bat test --no-daemon
```

| Suite | Result actually observed | Coverage added in this change |
|-------|--------------------------|-------------------------------|
| Android JVM unit tests | **15 passed, 0 failed, 0 skipped** across 6 suites | Debt balance conservation, persisted settlements, and offline repository fallback for both cached-empty and missing-cache cases |
| Android lint (`debug`) | **Completed with 0 errors and 50 warnings** | The remaining warnings are non-blocking maintenance/style findings; they are not recorded as test passes |
| Spring Boot/backend tests | **52 passed, 0 failed, 1 skipped** across 16 suites (53 discovered) | Settlement validation and idempotent group-to-wallet re-sync, including applying only the latest amount difference |

The skipped backend case is `MySqlMigrationIntegrationTest`: Docker/Testcontainers was unavailable in this run, so MySQL 8.4/Flyway V1–V9 validation is still pending. The Android build also completed Kotlin compilation successfully. These results do not replace device/emulator testing of Room fallback, lifecycle, layout, or network conditions.

### Manual scenarios

| # | Scenario | Screen(s) | Result (screenshot / note) | Pass? |
|:-:|----------|-----------|----------------------------|:-----:|
| 1 | Offline | Dashboard, transactions, wallets | Not run yet — populate cache online, enable airplane mode, reopen each screen | ⏳ |
| 2 | Slow network / timeout | All remote data screens | Not run yet | ⏳ |
| 3 | Server down | All remote data screens | Not run yet; verify cached personal data and Error + Retry elsewhere | ⏳ |
| 4 | Empty data | Transactions, wallets, savings, groups | Not run yet | ⏳ |
| 5 | Rotation / process death | Forms and detail screens | Not run yet | ⏳ |
| 6 | Small screen / tablet / dark mode | Main flows | Not run yet | ⏳ |

*(Add notes on bugs found by these tests and how you fixed them.)*
