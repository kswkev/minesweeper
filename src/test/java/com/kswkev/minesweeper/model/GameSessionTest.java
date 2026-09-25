package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    /** A clock the test moves by hand. */
    private static final class TestClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static final class RecordingListener implements GameSession.Listener {
        final List<Board> started = new ArrayList<>();
        final List<Integer> records = new ArrayList<>();

        @Override
        public void gameStarted(Board board) {
            started.add(board);
        }

        @Override
        public void recordAchieved(Difficulty difficulty, int seconds) {
            records.add(seconds);
        }
    }

    private Preferences node;
    private Settings settings;
    private BestTimes bestTimes;
    private TestClock clock;
    private GameSession session;
    private RecordingListener listener;

    @BeforeEach
    void setUp() {
        node = PreferencesTestSupport.newNode();
        settings = new Settings(node);
        bestTimes = new BestTimes(node);
        clock = new TestClock();
        session = new GameSession(settings, bestTimes, clock);
        listener = new RecordingListener();
        session.setListener(listener);
    }

    @AfterEach
    void tearDown() throws BackingStoreException {
        PreferencesTestSupport.remove(node);
    }

    // --- Win streak and abandon rules ---

    @Test
    void eachWinAddsAMineToTheNextGame() {
        win();
        session.newGame();
        assertEquals(1, session.getExtraMines());
        assertEquals(11, session.getBoard().getMines());
        assertEquals("Minesweeper: Beginner +1", session.getTitle());

        win();
        session.newGame();
        assertEquals(2, session.getExtraMines());
        assertEquals(12, session.getBoard().getMines());
    }

    @Test
    void titleDescribesTheGameOnScreenNotTheNextOne() {
        win();

        assertEquals("Minesweeper: Beginner", session.getTitle());
        assertEquals(0, session.getExtraMines());
    }

    @Test
    void newGameOnUntouchedBoardKeepsExtraMines() {
        win();
        session.newGame();

        session.newGame();

        assertEquals(1, session.getExtraMines());
    }

    @Test
    void abandoningAGameInProgressResets() {
        win();
        session.newGame();
        session.getBoard().reveal(4, 4);

        session.newGame();

        assertEquals(0, session.getExtraMines());
        assertEquals(10, session.getBoard().getMines());
    }

    @Test
    void losingResets() {
        win();
        session.newGame();
        lose();

        session.newGame();

        assertEquals(0, session.getExtraMines());
    }

    @Test
    void changingDifficultyResetsAndIsRemembered() {
        win();
        session.newGame();

        BoardConfig expert = Difficulty.EXPERT.getPreset().orElseThrow();
        session.selectDifficulty(Difficulty.EXPERT, expert);

        assertEquals(0, session.getExtraMines());
        assertEquals(99, session.getBoard().getMines());
        assertEquals("Minesweeper: Expert", session.getTitle());
        assertEquals(Difficulty.EXPERT, new Settings(node).getDifficulty());
    }

    @Test
    void customDifficultyIsRemembered() {
        BoardConfig custom = new BoardConfig(10, 12, 20);

        session.selectDifficulty(Difficulty.CUSTOM, custom);

        assertEquals(20, session.getBoard().getMines());
        assertEquals(custom, new Settings(node).getBoardConfig());
    }

    @Test
    void eachNewGameIsAnnounced() {
        Board first = session.getBoard();

        session.newGame();

        assertEquals(1, listener.started.size());
        assertSame(session.getBoard(), listener.started.get(0));
        assertNotSame(first, session.getBoard());
    }

    // --- Timer ---

    @Test
    void timerStartsAtOneOnFirstRevealAndFollowsTheClock() {
        assertEquals(0, session.getElapsedSeconds());
        clock.advance(Duration.ofSeconds(30));
        assertEquals(0, session.getElapsedSeconds(), "not started yet");

        session.getBoard().reveal(4, 4);
        assertEquals(1, session.getElapsedSeconds());

        clock.advance(Duration.ofMillis(2500));
        assertEquals(3, session.getElapsedSeconds());
    }

    @Test
    void timerFreezesWhenTheGameEnds() {
        session.getBoard().reveal(4, 4);
        clock.advance(Duration.ofSeconds(5));
        lose();

        clock.advance(Duration.ofSeconds(100));

        assertEquals(6, session.getElapsedSeconds());
    }

    @Test
    void timerCapsAt999() {
        session.getBoard().reveal(4, 4);
        clock.advance(Duration.ofHours(1));

        assertEquals(999, session.getElapsedSeconds());
    }

    @Test
    void newGameResetsTheTimer() {
        session.getBoard().reveal(4, 4);
        clock.advance(Duration.ofSeconds(10));

        session.newGame();

        assertEquals(0, session.getElapsedSeconds());
    }

    // --- Best times ---

    @Test
    void firstWinIsARecord() {
        session.getBoard().reveal(4, 4);
        clock.advance(Duration.ofSeconds(41));
        win();

        assertEquals(List.of(42), listener.records);
    }

    @Test
    void slowerWinIsNotARecord() {
        bestTimes.record(Difficulty.BEGINNER, 5, "Fast");
        session.getBoard().reveal(4, 4);
        clock.advance(Duration.ofSeconds(20));

        win();

        assertTrue(listener.records.isEmpty());
    }

    @Test
    void winWithExtraMinesIsNeverARecord() {
        win(); // record: 1 second
        listener.records.clear();
        bestTimes.reset();
        session.newGame(); // +1 mine

        win();

        assertTrue(listener.records.isEmpty());
    }

    @Test
    void customWinIsNeverARecord() {
        session.selectDifficulty(Difficulty.CUSTOM, new BoardConfig(9, 9, 10));

        win();

        assertTrue(listener.records.isEmpty());
    }

    @Test
    void saveBestTimeStoresNameAndRemembersIt() {
        session.saveBestTime(Difficulty.BEGINNER, 42, "  Kev  ");

        assertEquals(new BestTimes.Entry(42, "Kev"), bestTimes.get(Difficulty.BEGINNER).orElseThrow());
        assertEquals("Kev", settings.getLastName());
    }

    @Test
    void blankNameIsSavedAsAnonymous() {
        session.saveBestTime(Difficulty.BEGINNER, 42, "   ");

        assertEquals("Anonymous", bestTimes.get(Difficulty.BEGINNER).orElseThrow().name());
    }

    // --- Settings ---

    @Test
    void questionMarkSettingReachesBoardAndIsRemembered() {
        session.setQuestionMarksEnabled(false);
        Board board = session.getBoard();
        board.cycleMark(0, 0);
        board.cycleMark(0, 0);

        assertEquals(Cell.Mark.NONE, board.getCell(0, 0).getMark());
        assertFalse(new Settings(node).isQuestionMarksEnabled());

        session.newGame();
        session.getBoard().cycleMark(0, 0);
        session.getBoard().cycleMark(0, 0);
        assertEquals(Cell.Mark.NONE, session.getBoard().getCell(0, 0).getMark(), "applies to new boards too");
    }

    @Test
    void startsWithRememberedDifficulty() {
        settings.setDifficulty(Difficulty.INTERMEDIATE);

        GameSession reopened = new GameSession(settings, bestTimes, clock);

        assertEquals(Difficulty.INTERMEDIATE, reopened.getDifficulty());
        assertEquals(40, reopened.getBoard().getMines());
    }

    private void win() {
        Board board = session.getBoard();
        if (!board.isStarted()) {
            board.reveal(board.getRows() / 2, board.getCols() / 2);
        }
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (!board.getCell(r, c).isMine()) {
                    board.reveal(r, c);
                }
            }
        }
        assertEquals(GameState.WON, board.getState());
    }

    private void lose() {
        Board board = session.getBoard();
        if (!board.isStarted()) {
            board.reveal(board.getRows() / 2, board.getCols() / 2);
        }
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    board.reveal(r, c);
                    assertEquals(GameState.LOST, board.getState());
                    return;
                }
            }
        }
    }
}
