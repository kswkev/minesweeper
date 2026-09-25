# Minesweeper

A classic Minesweeper desktop game written in Java (Swing).

## Download

Get the latest version from the [Releases page](https://github.com/kswkev/minesweeper/releases/latest):

- **`Minesweeper-<version>.msi`**: Windows installer with Start menu and desktop shortcuts. No Java needed.
- **`Minesweeper-<version>-windows.zip`**: portable Windows version. Unzip and run `Minesweeper.exe`. No Java needed.
- **`minesweeper-<version>.jar`**: any OS with Java 17+ installed: `java -jar minesweeper-<version>.jar`.

## Build from source

Requires JDK 17+ and Maven 3.9+.

```
mvn package
java -jar target/minesweeper-1.0.0.jar
```

Run the tests with `mvn test`.

To build the Windows downloads locally (the MSI also needs [WiX Toolset 3](https://wixtoolset.org)):

```
./packaging/package-windows.ps1        # portable zip (version read from pom.xml)
./packaging/package-windows.ps1 -Msi   # zip + MSI
```

## Releasing

Every merge to `main` publishes a release automatically, using the version in `pom.xml`:

1. Bump `<version>` in `pom.xml` (e.g. `1.0.0` → `1.1.0`) on `develop`.
2. Open a PR from `develop` to `main`. CI fails the PR if that version has already been released.
3. Merge it. The Release workflow tests the build, creates the `v<version>` tag and a GitHub Release with the jar, MSI and zip attached.

## How to play

- **Left-click** to reveal a cell. Revealing an empty cell opens its neighbours automatically. The first click is always safe.
- **Right-click** to cycle a hidden cell through flag, `?` and blank. Turn `?` marks off with **Game > Marks (?)**.
- **Chord:** middle-click (or left-click) a revealed number whose mines are all flagged to open its remaining neighbours.
- Reveal every safe cell to win; reveal a mine and you lose.
- The header shows mines left, a smiley reset button and the elapsed time.
- **Game > New Game** (or F2, or the smiley) starts over. Choose Beginner, Intermediate, Expert or a Custom board from the Game menu.

### Win streak

Each win adds one extra mine to the next game (shown in the title, e.g. "Beginner +2"). Losing, abandoning a game in progress, changing difficulty or closing the app resets to the normal mine count.

### Best times

The fastest win on Beginner, Intermediate and Expert is saved and shown under **Game > Best Times**. Only standard games count: custom boards and games with extra mines can't set a record.

Your difficulty, custom board size and `?` mark setting are remembered between launches.
