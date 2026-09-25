# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Java 17, Maven 3.9, Swing. There are no runtime dependencies (JUnit 5 is test-only).

```
mvn test                                        # all tests
mvn test -Dtest=GameSessionTest                 # one class
mvn test "-Dtest=BoardTest#chordWithWrongFlagLoses"   # one method (quote it in PowerShell)
mvn verify                                      # what CI runs
mvn package; java -jar target/minesweeper-<version>.jar
./packaging/package-windows.ps1 [-Msi]          # after mvn package; portable zip, and the MSI with -Msi (needs WiX 3)
```

In PowerShell, quote any `-D` argument that contains a dot (e.g. `"-Dexpression=project.version"`). Otherwise PowerShell splits it at the dot. The packaging script avoids this by reading the version from `pom.xml` with `[xml]`.

## Architecture

The code has two packages under `com.kswkev.minesweeper`. `model` has no Swing imports and holds all game rules. `ui` only renders the model and forwards clicks to it. All tests are in `model`, so they run headless in CI.

**`model/Board`** is a single game.
- Mines are **not placed until the first `reveal`**, which keeps the clicked cell (and its 3×3 neighbourhood when there's room) mine-free.
- `Board.withMines(...)` (package-private) builds a board with fixed mine positions, and is what tests use.
- `Board` fires `addChangeListener` callbacks once per state-changing action. `chord` reveals several cells but fires only once, via `revealWithoutNotify`.
- Winning flags every mine automatically.
- Cells carry a `Cell.Mark` (NONE/FLAG/QUESTION).

**`model/GameSession`** runs one game after another. It owns:
- the win streak (`MineProgression`): each win adds one mine to the next game, and a loss, an abandon or a difficulty change resets it
- abandon detection: New Game on a board that has been started and is still PLAYING
- the timer, computed from an injected `java.time.Clock` (0 before the first reveal, then counting from 1, frozen at the end, capped at 999)
- best-time eligibility: only preset difficulties with no extra mines
- saving settings
- the window title

It notifies the UI through `GameSession.Listener` (`gameStarted`, `recordAchieved`).

**Listener order matters.** `GameSession` registers its board listener *before* `GameFrame.gameStarted` builds `BoardPanel` and `HeaderPanel`, which register theirs. So the timer and win/loss bookkeeping are settled before the UI redraws. Keep that order when adding listeners.

**Persistence:** `Settings` (difficulty, custom board, ? marks, last name) and `BestTimes` share one `java.util.prefs` node, `com/kswkev/minesweeper`, created in `Main`. Both take the node in their constructor.
- Tests must use `PreferencesTestSupport.newNode()`/`remove()` (a throwaway node) so they never touch the player's real settings.
- The win streak is deliberately not saved: quitting counts as abandoning.

**`BoardConfig`** is a validated record: rows 5–24, cols 5–30, mines 1…rows·cols−9. The −9 keeps room for the safe first-click block. Presets live on `Difficulty`, and `CUSTOM` has none.

**UI:** the smiley, flag and mine icons are drawn with Java2D (`SmileyIcon`, `FlagIcon`, `MineIcon`), not emoji, because Windows' default fonts can't render emoji in Swing. Chording is a middle-click, or a left-click on a revealed number.

## Branches and releases

- Work happens on a per-version branch cut from `develop` (`v1`, `v2`, …) and merged back to `develop` by PR.
- **Every push/merge to `main` publishes a release** (`.github/workflows/release.yml`). It uses the `pom.xml` `<version>`, which must be a plain `MAJOR.MINOR.PATCH` that hasn't been tagged yet. The release creates the tag `v<version>` and a GitHub Release with the jar, MSI and portable zip.
- Bump the version on `develop` before opening a develop→main PR. CI's `version-check` job blocks PRs to `main` that reuse a released version.
- CI (`ci.yml`) also builds the Windows packages on every PR, so packaging breaks show up in review.
- The MSI's `--win-upgrade-uuid` in `packaging/package-windows.ps1` must never change, or installed copies won't upgrade in place.
