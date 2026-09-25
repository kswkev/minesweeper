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

- **Left-click** to reveal a cell. Revealing an empty cell opens its neighbours automatically.
- **Right-click** to place or remove a flag.
- Reveal every safe cell to win; reveal a mine and you lose.
- **Game > New Game** (or F2) starts over.
