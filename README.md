# SHT study

A personal habit tracker. Android app built with Kotlin + Jetpack Compose.

## About

A small app for tracking daily habits: a list of habits, a "done today" toggle,
and (eventually) streaks and statistics.

This is a learning project, built from scratch, step by step. Each iteration
is a working version of the app. Don’t throw tomatoes at me.
123456789
## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Build:** Gradle (Kotlin DSL) with AGP 9.x and built-in Kotlin
- **Local database:** Room (SQLite)
- **Async:** Kotlin Coroutines + Flow
- **Architecture:** UI → ViewModel → DAO → Room
- **minSdk:** 34
- **targetSdk / compileSdk:** 37

Planned additions:
- Completion history (per-day records instead of a single boolean)
- Streaks
- Reminders (notifications)
- Dark theme and appearance settings

## Current state

### Iteration 1 — skeleton and static list
### Iteration 2 — in-memory interactivity
### Iteration 3 — persistent storage with Room
### Iteration 4 — ViewModel and cleaner architecture
### Iteration 5 — completion history and data model fix

### Iteration 6 — current streak calculation

- Added `StreakCalculator` — a pure Kotlin utility with no Room or Compose
  dependencies. Its single job is to calculate the current streak from a list
  of dates.
- `CompletionDao` now exposes `getAllCompletions(): Flow<List<CompletionEntity>>`
  to fetch all completion records at once.
- New `HabitUiModel` data class (in the `ui` package) — represents a habit
  as the UI needs it: `id`, `title`, `doneToday`, `streak`.
  - `HabitWithStatus` (in `data`) remains a strict Room mapping of the SQL
    JOIN result. Separation of layers: Room maps what the DB returns, UI gets
    a richer model with computed fields.
- `HabitViewModel` now combines two flows:
  - `habitDao.getAllWithStatus(today)` — habits + doneToday flag
  - `completionDao.getAllCompletions()` — all completion records
    Using `combine`, it groups completions by `habitId`, calculates the streak
    for each habit via `StreakCalculator`, and emits `List<HabitUiModel>`.
- UI shows the streak next to the habit status: `Выполнено сегодня · 🔥 5`.

**Streak logic (soft mode):**
- The streak counts consecutive days up to today **or yesterday**.
- If the last completion is today → streak is alive.
- If the last completion is yesterday → streak is still alive (there is time
  until midnight to complete today).
- If the last completion is older than yesterday → streak is 0.

**Verification:**
- Verified `StreakCalculator` manually through a temporary debug function
  covering 8 cases (empty, today, today+yesterday, 5 days in a row, gaps,
  future dates, etc.). All cases produced correct results.
- Verified integration by inserting test completions directly into the
  database: habits with records for today + yesterday + day-before-yesterday
  correctly display a streak of 3.

**Known limitation:** `today` is captured once when the ViewModel is created.
If the app stays open past midnight, the streak and "done today" flag won't
update until the ViewModel is recreated (e.g., app restart). Will be fixed
in a future iteration.

## Roadmap

- [x] In-memory habit list with add / toggle / delete
- [x] Persistent storage with Room
- [x] ViewModel layer
- [x] Completion history (per-day records instead of `doneToday` boolean)
- [x] Fix the "midnight" problem: `doneToday` now resets naturally
- [x] Current streak calculation
- [ ] Unit tests for `StreakCalculator` and other pure logic
- [ ] Best streak — longest consecutive sequence ever recorded
- [ ] History and simple statistics (monthly heatmap)
- [ ] Fix `today` staleness: recreate date when the app stays open past midnight
- [ ] Reminders via WorkManager / AlarmManager
- [ ] Real icons instead of text symbols (●, ○, +, ✕, 🔥)
- [ ] Dark theme and appearance settings
- [ ] Final design and app nam

## Known limitations

- **`today` staleness:** `HabitViewModel` captures `LocalDate.now()` once at
  creation. If the app stays open past midnight, "done today" and the streak
  won't update until the ViewModel is recreated (app restart). Acceptable for
  now; will be fixed by listening to date-change events or recreating the flow
  on schedule.
- **No unit tests yet.** `StreakCalculator` was verified manually; automated
  tests are on the roadmap.
- **No dark theme toggle** — the app follows the system setting only.
- **No reminders / notifications.**
- **No backup or export.** All data is local to the device; there is no way to
  save or migrate it to another phone.
- **Streak is "soft".** If yesterday was completed but today has not been yet,
  the streak is still considered alive. Some habit trackers use a "strict"
  mode where the streak resets at midnight if today isn't marked. A strict
  mode may be added as a setting later.
- **No history view.** Records exist in the database, but there's no UI to
  browse them.
