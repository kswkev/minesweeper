package com.kswkev.minesweeper.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Runs one game after another: the win streak, abandon detection, the timer, best-time
 * eligibility and saved settings. Contains no UI code.
 */
public final class GameSession {

    /** Receives session events. Callbacks run on whatever thread drives the board. */
    public interface Listener {
        /** A new board is ready to be shown. */
        void gameStarted(Board board);

        /** A win beat the stored best time; the UI should ask for a name and call {@link #saveBestTime}. */
        void recordAchieved(Difficulty difficulty, int seconds);
    }

    public static final int MAX_SECONDS = 999;
    private static final String DEFAULT_NAME = "Anonymous";

    private static final Listener NO_LISTENER = new Listener() {
        @Override
        public void gameStarted(Board board) {
        }

        @Override
        public void recordAchieved(Difficulty difficulty, int seconds) {
        }
    };

    private final Settings settings;
    private final BestTimes bestTimes;
    private final Clock clock;
    private final MineProgression progression;

    private Listener listener = NO_LISTENER;
    private Difficulty difficulty;
    private Board board;
    private int gameExtraMines;
    private Instant startedAt;
    private Integer finalSeconds;

    public GameSession(Settings settings, BestTimes bestTimes, Clock clock) {
        this.settings = settings;
        this.bestTimes = bestTimes;
        this.clock = clock;
        this.difficulty = settings.getDifficulty();
        this.progression = new MineProgression(settings.getBoardConfig());
        startGame();
    }

    public void setListener(Listener listener) {
        this.listener = listener == null ? NO_LISTENER : listener;
    }

    /** New Game / smiley: leaving a game in progress counts as abandoning it. */
    public void newGame() {
        if (board.isStarted() && board.getState() == GameState.PLAYING) {
            progression.onAbandon();
        }
        startGame();
    }

    /** Switches difficulty (and remembers it), dropping any win-streak mines. */
    public void selectDifficulty(Difficulty level, BoardConfig config) {
        difficulty = level;
        settings.setDifficulty(level);
        if (level == Difficulty.CUSTOM) {
            settings.setCustomConfig(config);
        }
        progression.reset(config);
        startGame();
    }

    public void setQuestionMarksEnabled(boolean enabled) {
        settings.setQuestionMarksEnabled(enabled);
        board.setQuestionMarksEnabled(enabled);
    }

    public void saveBestTime(Difficulty level, int seconds, String name) {
        String trimmed = name == null ? "" : name.trim();
        String saved = trimmed.isEmpty() ? DEFAULT_NAME : trimmed;
        settings.setLastName(saved);
        bestTimes.record(level, seconds, saved);
    }

    private void startGame() {
        board = new Board(progression.current());
        board.setQuestionMarksEnabled(settings.isQuestionMarksEnabled());
        gameExtraMines = progression.getExtraMines();
        startedAt = null;
        finalSeconds = null;
        // Registered first so the timer and result are settled before the UI redraws.
        Board current = board;
        board.addChangeListener(() -> onBoardChanged(current));
        listener.gameStarted(board);
    }

    private void onBoardChanged(Board changed) {
        if (changed != board || finalSeconds != null) {
            return;
        }
        if (startedAt == null && board.isStarted()) {
            startedAt = clock.instant();
        }
        GameState state = board.getState();
        if (state == GameState.PLAYING) {
            return;
        }
        finalSeconds = runningSeconds();
        if (state == GameState.LOST) {
            progression.onLoss();
            return;
        }
        progression.onWin();
        // Only standard games (preset difficulty, no extra mines) can set a best time.
        if (gameExtraMines == 0 && bestTimes.isRecord(difficulty, finalSeconds)) {
            listener.recordAchieved(difficulty, finalSeconds);
        }
    }

    /** Seconds shown on the timer: 0 before the first reveal, then counting from 1; frozen when the game ends. */
    public int getElapsedSeconds() {
        return finalSeconds != null ? finalSeconds : runningSeconds();
    }

    private int runningSeconds() {
        if (startedAt == null) {
            return 0;
        }
        long elapsed = Duration.between(startedAt, clock.instant()).getSeconds() + 1;
        return (int) Math.min(elapsed, MAX_SECONDS);
    }

    /** Window title for the current game, e.g. "Minesweeper: Beginner +2". */
    public String getTitle() {
        return "Minesweeper: " + difficulty.getDisplayName() + (gameExtraMines > 0 ? " +" + gameExtraMines : "");
    }

    public Board getBoard() {
        return board;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** Win-streak mines added to the current game. */
    public int getExtraMines() {
        return gameExtraMines;
    }

    public Settings getSettings() {
        return settings;
    }

    public BestTimes getBestTimes() {
        return bestTimes;
    }
}
