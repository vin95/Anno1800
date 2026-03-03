package com.anno1800.engine;

import com.anno1800.agents.AgentImpl.AgentRandom;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Game engine lifecycle.
 * Covers: construction, round tracking, isGameOver, start player,
 *         getters, and Player.initializePlayers edge cases.
 */
@DisplayName("GameEngine Tests")
class GameEngineTest {

    // ─── Player.initializePlayers edge cases ──────────────────────────────

    @Nested
    @DisplayName("Player initialization boundary checks")
    class PlayerInitTests {

        @Test
        @DisplayName("0 players → throws IllegalArgumentException")
        void zeroPlayers_throwsIAE() {
            assertThrows(IllegalArgumentException.class, () -> new Game(0));
        }

        @Test
        @DisplayName("negative players → throws IllegalArgumentException")
        void negativePlayers_throwsIAE() {
            assertThrows(IllegalArgumentException.class, () -> new Game(-1));
        }

        @Test
        @DisplayName("5 players → throws IllegalArgumentException (max is 4)")
        void fivePlayers_throwsIAE() {
            assertThrows(IllegalArgumentException.class, () -> new Game(5));
        }

        @Test
        @DisplayName("1 player → valid game created")
        void onePlayer_valid() {
            assertDoesNotThrow(() -> new Game(1, true, 1));
        }

        @Test
        @DisplayName("2 players → valid game created")
        void twoPlayers_valid() {
            assertDoesNotThrow(() -> new Game(2, true, 5));
        }

        @Test
        @DisplayName("3 players → valid game created")
        void threePlayers_valid() {
            assertDoesNotThrow(() -> new Game(3, true, 5));
        }

        @Test
        @DisplayName("4 players → valid game created (maximum)")
        void fourPlayers_valid() {
            assertDoesNotThrow(() -> new Game(4, true, 5));
        }
    }

    // ─── initial game state ────────────────────────────────────────────────

    @Nested
    @DisplayName("Initial game state after construction")
    class InitialStateTests {

        private Game game;

        @BeforeEach
        void setUp() {
            game = new Game(2, true, 10);
        }

        @Test
        @DisplayName("round starts at 1")
        void initialRound_isOne() {
            assertEquals(1, game.getCurrentRound());
        }

        @Test
        @DisplayName("testMode = true reflected in getter")
        void testMode_isTrue() {
            assertTrue(game.isTestMode());
        }

        @Test
        @DisplayName("maxRounds stored correctly")
        void maxRounds_storedCorrectly() {
            assertEquals(10, game.getMaxRounds());
        }

        @Test
        @DisplayName("in testMode start player is always index 0")
        void startPlayer_isZeroInTestMode() {
            assertEquals(0, game.getStartPlayer());
        }

        @Test
        @DisplayName("getPlayers returns correct number of players")
        void getPlayers_correctCount() {
            assertEquals(2, game.getPlayers().length);
        }

        @Test
        @DisplayName("getCurrentPlayer returns first player in testMode")
        void getCurrentPlayer_isFirstPlayerInTestMode() {
            Player current = game.getCurrentPlayer();
            assertNotNull(current);
            assertEquals(game.getPlayers()[0], current);
        }

        @Test
        @DisplayName("game not over at start")
        void gameNotOver_atStart() {
            assertFalse(game.isGameOver());
        }

        @Test
        @DisplayName("board is initialized (non-null)")
        void board_isNotNull() {
            assertNotNull(game.getBoard());
        }

        @Test
        @DisplayName("5 active ObjectiveCards drawn at start")
        void fiveActiveObjectiveCards_atStart() {
            assertEquals(5, game.getActiveObjectiveCards().size());
        }

        @Test
        @DisplayName("in testMode first ObjectiveCard is MostInvestors (unshuffled deck position 0)")
        void testMode_firstCardIsMostInvestors() {
            var active = game.getActiveObjectiveCards();
            assertFalse(active.isEmpty());
            assertInstanceOf(ObjectiveCard.MostInvestors.class, active.get(0),
                    "In testMode the first active card should be MostInvestors");
        }

        @Test
        @DisplayName("in testMode fifth ObjectiveCard is ExplorerTrader (unshuffled deck position 4)")
        void testMode_fifthCardIsExplorerTrader() {
            var active = game.getActiveObjectiveCards();
            assertEquals(5, active.size());
            assertInstanceOf(ObjectiveCard.ExplorerTrader.class, active.get(4),
                    "In testMode the fifth active card should be ExplorerTrader");
        }
    }

    // ─── round progression ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Round progression and isGameOver")
    class RoundProgressionTests {

        @Test
        @DisplayName("game is over when currentRound > maxRounds")
        void gameOver_afterMaxRounds() {
            Game game = new Game(2, true, 1);
            // Round 1 starts — not over
            assertFalse(game.isGameOver());
            // Advance one round (nextRound increments to 2 > maxRounds=1)
            game.nextRound();
            assertTrue(game.isGameOver());
        }

        @Test
        @DisplayName("nextRound increments round number")
        void nextRound_incrementsRound() {
            Game game = new Game(2, true, 10);
            assertEquals(1, game.getCurrentRound());
            game.nextRound();
            assertEquals(2, game.getCurrentRound());
            game.nextRound();
            assertEquals(3, game.getCurrentRound());
        }

        @Test
        @DisplayName("game not over with many rounds remaining")
        void gameNotOver_manyRoundsRemaining() {
            Game game = new Game(2, true, 100);
            game.nextRound();
            game.nextRound();
            assertFalse(game.isGameOver());
        }

        @Test
        @DisplayName("isGameOver false at exactly maxRounds")
        void gameNotOver_atExactlyMaxRounds() {
            // With maxRounds=3, game is over when currentRound > 3, i.e. currentRound = 4
            Game game = new Game(2, true, 3);
            game.nextRound(); // round 2
            game.nextRound(); // round 3
            assertFalse(game.isGameOver(), "Game should still be active at round 3 (max=3)");
            game.nextRound(); // round 4 > 3
            assertTrue(game.isGameOver(), "Game should be over when round 4 > maxRounds 3");
        }
    }

    // ─── agent validation ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Agent assignment")
    class AgentTests {

        @Test
        @DisplayName("setAgent with invalid index → throws IllegalArgumentException")
        void setAgent_invalidIndex_throwsIAE() {
            Game game = new Game(2, true, 5);
            assertThrows(IllegalArgumentException.class,
                    () -> game.setAgent(5, new AgentRandom()));
        }

        @Test
        @DisplayName("setAgent with negative index → throws IllegalArgumentException")
        void setAgent_negativeIndex_throwsIAE() {
            Game game = new Game(2, true, 5);
            assertThrows(IllegalArgumentException.class,
                    () -> game.setAgent(-1, new AgentRandom()));
        }

        @Test
        @DisplayName("start() without agents → throws IllegalStateException")
        void start_noAgents_throwsISE() {
            Game game = new Game(2, true, 5);
            // Only assign agent to player 0, not player 1 → should fail
            game.setAgent(0, new AgentRandom());
            assertThrows(IllegalStateException.class, game::start);
        }
    }

    // ─── player state after initialization ─────────────────────────────────

    @Nested
    @DisplayName("Player state after game initialization")
    class PlayerStateTests {

        @Test
        @DisplayName("all players have positions assigned after init")
        void allPlayers_havePositions() {
            Game game = new Game(4, true, 5);
            for (Player p : game.getPlayers()) {
                assertTrue(p.getPosition() >= 1 && p.getPosition() <= 4,
                        "Player position should be between 1 and 4");
            }
        }

        @Test
        @DisplayName("all players start with zero victory points")
        void allPlayers_zeroVictoryPoints() {
            Game game = new Game(3, true, 5);
            for (Player p : game.getPlayers()) {
                assertEquals(0, p.getVictoryPoints());
            }
        }

        @Test
        @DisplayName("all players have player boards initialized")
        void allPlayers_havePlayerBoards() {
            Game game = new Game(2, true, 5);
            for (Player p : game.getPlayers()) {
                assertNotNull(p.getPlayerBoard());
            }
        }

        @Test
        @DisplayName("all player positions are unique")
        void allPlayers_uniquePositions() {
            Game game = new Game(4, true, 5);
            long uniquePositions = java.util.Arrays.stream(game.getPlayers())
                    .mapToInt(Player::getPosition)
                    .distinct()
                    .count();
            assertEquals(4, uniquePositions, "All player positions must be unique");
        }
    }
}
