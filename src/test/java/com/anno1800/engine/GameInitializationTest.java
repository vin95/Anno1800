package com.anno1800.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

import com.anno1800.board.Board;
import com.anno1800.player.Player;

/**
 * Test suite for Game initialization.
 * Tests verify that all game materials are properly initialized.
 */
@DisplayName("Game Initialization Tests")
class GameInitializationTest {

    private Game game;
    private static final int TEST_NUM_PLAYERS = 2;

    @BeforeEach
    void setUp() {
        game = new Game(TEST_NUM_PLAYERS);
    }

    @Nested
    @DisplayName("Basic Game State Tests")
    class BasicGameStateTests {

        @Test
        @DisplayName("Game should initialize with round 1")
        void testInitialRound() {
            assertEquals(1, game.getCurrentRound(), "Game should start at round 1");
        }

        @Test
        @DisplayName("Game should initialize with PRODUCTION phase")
        void testInitialPhase() {
            assertEquals(Phase.PRODUCTION, game.getCurrentPhase(), 
                "Game should start with PRODUCTION phase");
        }

        @Test
        @DisplayName("Game should initialize with player index 0")
        void testInitialPlayerIndex() {
            assertNotNull(game.getCurrentPlayer(), "Current player should not be null");
            assertEquals("Player 1", game.getCurrentPlayer().getName(), 
                "First player should be Player 1");
        }

        @Test
        @DisplayName("Board should be initialized")
        void testBoardInitialized() {
            assertNotNull(game.getBoard(), "Board should be initialized");
        }

        @Test
        @DisplayName("Players array should be initialized")
        void testPlayersInitialized() {
            assertNotNull(game.getPlayers(), "Players array should not be null");
            assertEquals(TEST_NUM_PLAYERS, game.getPlayers().length, 
                "Should have correct number of players");
        }
    }

    @Nested
    @DisplayName("Player Tests")
    class PlayerTests {

        @Test
        @DisplayName("All players should be initialized")
        void testAllPlayersExist() {
            Player[] players = game.getPlayers();
            for (int i = 0; i < players.length; i++) {
                assertNotNull(players[i], "Player " + (i + 1) + " should not be null");
            }
        }

        @Test
        @DisplayName("Players should have unique names")
        void testPlayerNamesAreUnique() {
            Player[] players = game.getPlayers();
            for (int i = 0; i < players.length; i++) {
                for (int j = i + 1; j < players.length; j++) {
                    assertNotEquals(players[i].getName(), players[j].getName(),
                        "Players should have unique names");
                }
            }
        }

        @Test
        @DisplayName("Each player should have a player board")
        void testEachPlayerHasBoard() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertNotNull(player.getPlayerBoard(), 
                    player.getName() + " should have a player board");
            }
        }

        @Test
        @DisplayName("Each player should have factories")
        void testEachPlayerHasFactories() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertNotNull(player.getPlayerBoard().getFactories(),
                    player.getName() + " should have factories array");
                assertTrue(player.getPlayerBoard().getFactories().length > 0,
                    player.getName() + " should have at least one factory");
            }
        }

        @Test
        @DisplayName("Each player should have residents")
        void testEachPlayerHasResidents() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertNotNull(player.getPlayerBoard().getResidents(),
                    player.getName() + " should have residents array");
                assertTrue(player.getPlayerBoard().getResidents().length > 0,
                    player.getName() + " should have at least one resident");
            }
        }

        @Test
        @DisplayName("Each player should have ships")
        void testEachPlayerHasShips() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertNotNull(player.getPlayerBoard().getTradeShips(),
                    player.getName() + " should have trade ships");
                assertNotNull(player.getPlayerBoard().getExplorerShips(),
                    player.getName() + " should have explorer ships");
            }
        }

        @Test
        @DisplayName("Each player should have shipyards")
        void testEachPlayerHasShipyards() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertNotNull(player.getPlayerBoard().getShipyards(),
                    player.getName() + " should have shipyards");
                assertTrue(player.getPlayerBoard().getShipyards().length > 0,
                    player.getName() + " should have at least one shipyard");
            }
        }

        @Test
        @DisplayName("Each player should have correct tile counts")
        void testEachPlayerHasTiles() {
            Player[] players = game.getPlayers();
            for (Player player : players) {
                assertTrue(player.getPlayerBoard().getFreeLandTiles() >= 0,
                    player.getName() + " should have non-negative land tiles");
                assertTrue(player.getPlayerBoard().getFreeCoastTiles() >= 0,
                    player.getName() + " should have non-negative coast tiles");
                assertTrue(player.getPlayerBoard().getFreeSeaTiles() >= 0,
                    player.getName() + " should have non-negative sea tiles");
            }
        }
    }

    @Nested
    @DisplayName("Board Tests")
    class BoardTests {

        @Test
        @DisplayName("Board should have 35 factory stacks")
        void testFactoryStacksCount() {
            Board board = game.getBoard();
            assertEquals(35, board.getFactoryStacks().size(),
                "Board should have exactly 35 factory stacks");
        }

        @Test
        @DisplayName("Each factory stack should not be null")
        void testFactoryStacksNotNull() {
            Board board = game.getBoard();
            for (int i = 0; i < board.getFactoryStacks().size(); i++) {
                assertNotNull(board.getFactoryStacks().get(i),
                    "Factory stack " + i + " should not be null");
            }
        }

        @Test
        @DisplayName("Each factory stack should contain factories")
        void testFactoryStacksContainFactories() {
            Board board = game.getBoard();
            for (int i = 0; i < board.getFactoryStacks().size(); i++) {
                assertFalse(board.getFactoryStacks().get(i).isEmpty(),
                    "Factory stack " + i + " should not be empty");
            }
        }

        @Test
        @DisplayName("Resident card stacks should be initialized")
        void testResidentStacksInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getResidentStack1(), "Resident stack 1 should not be null");
            assertNotNull(board.getResidentStack2(), "Resident stack 2 should not be null");
            assertNotNull(board.getResidentStack3(), "Resident stack 3 should not be null");
        }

        @Test
        @DisplayName("Resident card stacks should contain cards")
        void testResidentStacksNotEmpty() {
            Board board = game.getBoard();
            assertFalse(board.getResidentStack1().isEmpty(), 
                "Resident stack 1 should contain cards");
            assertFalse(board.getResidentStack2().isEmpty(), 
                "Resident stack 2 should contain cards");
            assertFalse(board.getResidentStack3().isEmpty(), 
                "Resident stack 3 should contain cards");
        }

        @Test
        @DisplayName("Expedition card stack should be initialized")
        void testExpeditionStackInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getExpeditionStack(), 
                "Expedition card stack should not be null");
            assertFalse(board.getExpeditionStack().isEmpty(), 
                "Expedition card stack should contain cards");
        }

        @Test
        @DisplayName("Shipyard stacks should be initialized")
        void testShipyardStacksInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getShipyardLevel1(), "Shipyard level 1 stack should not be null");
            assertNotNull(board.getShipyardLevel2(), "Shipyard level 2 stack should not be null");
            assertNotNull(board.getShipyardLevel3(), "Shipyard level 3 stack should not be null");
        }

        @Test
        @DisplayName("Trade ship stacks should be initialized")
        void testTradeShipStacksInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getTradeShipLevel1(), 
                "Trade ship level 1 stack should not be null");
            assertNotNull(board.getTradeShipLevel2(), 
                "Trade ship level 2 stack should not be null");
            assertNotNull(board.getTradeShipLevel3(), 
                "Trade ship level 3 stack should not be null");
        }

        @Test
        @DisplayName("Explorer ship stacks should be initialized")
        void testExplorerShipStacksInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getExplorerShipLevel1(), 
                "Explorer ship level 1 stack should not be null");
            assertNotNull(board.getExplorerShipLevel2(), 
                "Explorer ship level 2 stack should not be null");
            assertNotNull(board.getExplorerShipLevel3(), 
                "Explorer ship level 3 stack should not be null");
        }

        @Test
        @DisplayName("Old World islands should be initialized")
        void testOldWorldIslandsInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getOldWorldIslands(), 
                "Old World islands stack should not be null");
            assertFalse(board.getOldWorldIslands().isEmpty(), 
                "Old World islands stack should contain islands");
        }

        @Test
        @DisplayName("New World islands should be initialized")
        void testNewWorldIslandsInitialized() {
            Board board = game.getBoard();
            assertNotNull(board.getNewWorldIslands(), 
                "New World islands stack should not be null");
            assertFalse(board.getNewWorldIslands().isEmpty(), 
                "New World islands stack should contain islands");
        }

        @Test
        @DisplayName("Board should have population pools")
        void testPopulationPoolsInitialized() {
            Board board = game.getBoard();
            assertTrue(board.getFarmers() > 0, "Board should have farmers");
            assertTrue(board.getWorkers() > 0, "Board should have workers");
            assertTrue(board.getArtisans() > 0, "Board should have artisans");
            assertTrue(board.getEngineers() > 0, "Board should have engineers");
            assertTrue(board.getInvestors() > 0, "Board should have investors");
        }

        @Test
        @DisplayName("Board should have initial gold")
        void testBoardGoldInitialized() {
            Board board = game.getBoard();
            assertTrue(board.getGold() > 0, "Board should have initial gold");
        }
    }

    @Nested
    @DisplayName("Multi-player Tests")
    class MultiPlayerTests {

        @Test
        @DisplayName("Game should support 2 players")
        void testTwoPlayers() {
            Game twoPlayerGame = new Game(2);
            assertEquals(2, twoPlayerGame.getPlayers().length);
        }

        @Test
        @DisplayName("Game should support 3 players")
        void testThreePlayers() {
            Game threePlayerGame = new Game(3);
            assertEquals(3, threePlayerGame.getPlayers().length);
        }

        @Test
        @DisplayName("Game should support 4 players")
        void testFourPlayers() {
            Game fourPlayerGame = new Game(4);
            assertEquals(4, fourPlayerGame.getPlayers().length);
        }

        @Test
        @DisplayName("Game should reject 0 players")
        void testZeroPlayersThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Game(0),
                "Game should reject 0 players");
        }

        @Test
        @DisplayName("Game should reject negative player count")
        void testNegativePlayersThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Game(-1),
                "Game should reject negative player count");
        }

        @Test
        @DisplayName("Game should reject more than 4 players")
        void testTooManyPlayersThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> new Game(5),
                "Game should reject more than 4 players");
        }
    }
}
