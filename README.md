# Minesweeper

A classic Minesweeper desktop game written in Java (Swing).

## Requirements

- JDK 17+
- Maven 3.9+

## Build & run

```
mvn package
java -jar target/minesweeper-1.0-SNAPSHOT.jar
```

Run the tests with `mvn test`.

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
