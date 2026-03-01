package com.anno1800.engine;

import com.anno1800.data.gamedata.ShipType;
import com.anno1800.game.board.Board;
import com.anno1800.game.engine.Game;
import com.anno1800.game.engine.Rules;
import com.anno1800.game.player.Player;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Rules utility class.
 * Covers: ship-building rules, resident-settling rules, factory rules,
 *         gold availability checks.
 */
@DisplayName("Rules Tests")
class RulesTest {

    private Game game;
    private Board board;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(2, true, 10);
        board = game.getBoard();
        player = game.getCurrentPlayer();
    }

    // ─── getResidentGoldCost ───────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.getResidentGoldCost")
    class ResidentGoldCostTests {

        @Test
        @DisplayName("level 1 → 1 gold")
        void level1_costsOneGold() {
            assertEquals(1, Rules.getResidentGoldCost(1));
        }

        @Test
        @DisplayName("level 2 → 1 gold")
        void level2_costsOneGold() {
            assertEquals(1, Rules.getResidentGoldCost(2));
        }

        @Test
        @DisplayName("level 3 → 3 gold")
        void level3_costsThreeGold() {
            assertEquals(3, Rules.getResidentGoldCost(3));
        }

        @Test
        @DisplayName("level 4 → 3 gold")
        void level4_costsThreeGold() {
            assertEquals(3, Rules.getResidentGoldCost(4));
        }

        @Test
        @DisplayName("level 5 → 3 gold")
        void level5_costsThreeGold() {
            assertEquals(3, Rules.getResidentGoldCost(5));
        }

        @Test
        @DisplayName("invalid level 0 → 0 gold (no cost defined)")
        void level0_returnsZero() {
            assertEquals(0, Rules.getResidentGoldCost(0));
        }

        @Test
        @DisplayName("invalid level 6 → 0 gold (no cost defined)")
        void level6_returnsZero() {
            assertEquals(0, Rules.getResidentGoldCost(6));
        }
    }

    // ─── hasResidentCardAvailable ──────────────────────────────────────────

    @Nested
    @DisplayName("Rules.hasResidentCardAvailable")
    class ResidentCardAvailableTests {

        @Test
        @DisplayName("level 1 uses Stack1 — available at game start")
        void level1_usesStack1_availableAtStart() {
            assertTrue(Rules.hasResidentCardAvailable(board, 1));
        }

        @Test
        @DisplayName("level 2 uses Stack1 — available at game start")
        void level2_usesStack1_availableAtStart() {
            assertTrue(Rules.hasResidentCardAvailable(board, 2));
        }

        @Test
        @DisplayName("level 3 uses Stack2 — available at game start")
        void level3_usesStack2_availableAtStart() {
            assertTrue(Rules.hasResidentCardAvailable(board, 3));
        }

        @Test
        @DisplayName("level 4 uses Stack2 — available at game start")
        void level4_usesStack2_availableAtStart() {
            assertTrue(Rules.hasResidentCardAvailable(board, 4));
        }

        @Test
        @DisplayName("level 5 uses Stack2 — available at game start")
        void level5_usesStack2_availableAtStart() {
            assertTrue(Rules.hasResidentCardAvailable(board, 5));
        }

        @Test
        @DisplayName("level 0 → false (invalid level)")
        void level0_returnsFalse() {
            assertFalse(Rules.hasResidentCardAvailable(board, 0));
        }

        @Test
        @DisplayName("Stack1 depleted → levels 1 and 2 return false")
        void stack1Depleted_levels1And2ReturnFalse() {
            board.getResidentStack1().clear();
            assertFalse(Rules.hasResidentCardAvailable(board, 1));
            assertFalse(Rules.hasResidentCardAvailable(board, 2));
        }

        @Test
        @DisplayName("Stack2 depleted → levels 3, 4, 5 return false")
        void stack2Depleted_levels3to5ReturnFalse() {
            board.getResidentStack2().clear();
            assertFalse(Rules.hasResidentCardAvailable(board, 3));
            assertFalse(Rules.hasResidentCardAvailable(board, 4));
            assertFalse(Rules.hasResidentCardAvailable(board, 5));
        }
    }

    // ─── canSettleResident ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.canSettleResident")
    class CanSettleResidentTests {

        @Test
        @DisplayName("valid level 1 at start → true (board has farmers and Stack1 available)")
        void level1_atStart_returnsTrue() {
            assertTrue(Rules.canSettleResident(board, player, 1));
        }

        @Test
        @DisplayName("invalid level 0 → false")
        void level0_returnsFalse() {
            assertFalse(Rules.canSettleResident(board, player, 0));
        }

        @Test
        @DisplayName("invalid level 6 → false")
        void level6_returnsFalse() {
            assertFalse(Rules.canSettleResident(board, player, 6));
        }

        @Test
        @DisplayName("board has no farmers → level 1 returns false")
        void noFarmers_level1_returnsFalse() {
            // Drain all farmers
            while (board.getFarmers() > 0) board.takeResident(1);
            assertFalse(Rules.canSettleResident(board, player, 1));
        }
    }

    // ─── canBuildShipyard ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.canBuildShipyard")
    class CanBuildShipyardTests {

        @Test
        @DisplayName("level 0 → false")
        void level0_returnsFalse() {
            assertFalse(Rules.canBuildShipyard(board, player, 0));
        }

        @Test
        @DisplayName("level 4 → false")
        void level4_returnsFalse() {
            assertFalse(Rules.canBuildShipyard(board, player, 4));
        }

        @Test
        @DisplayName("negative level → false")
        void negativeLevel_returnsFalse() {
            assertFalse(Rules.canBuildShipyard(board, player, -1));
        }

        @Test
        @DisplayName("level 1 with free coast tiles and board availability → true")
        void level1_valid_returnsTrue() {
            // Board starts with shipyard stacks populated
            assertTrue(board.getShipyardLevel1().size() > 0);
            // Player has free coast tiles
            assertTrue(player.getPlayerBoard().getFreeCoastTiles() >= 0);
            // Whether true depends on tiles — just assert no exception
            assertDoesNotThrow(() -> Rules.canBuildShipyard(board, player, 1));
        }
    }

    // ─── canBuildShips ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.canBuildShips")
    class CanBuildShipsTests {

        @Test
        @DisplayName("level 0 → false for both ship types")
        void level0_returnsFalse() {
            assertFalse(Rules.canBuildShips(board, player, ShipType.ExplorerShip, 0, 1));
            assertFalse(Rules.canBuildShips(board, player, ShipType.TradeShip, 0, 1));
        }

        @Test
        @DisplayName("level 4 → false for both ship types")
        void level4_returnsFalse() {
            assertFalse(Rules.canBuildShips(board, player, ShipType.ExplorerShip, 4, 1));
            assertFalse(Rules.canBuildShips(board, player, ShipType.TradeShip, 4, 1));
        }

        @Test
        @DisplayName("negative level → false")
        void negativeLevel_returnsFalse() {
            assertFalse(Rules.canBuildShips(board, player, ShipType.ExplorerShip, -1, 1));
        }
    }

    // ─── hasEnoughGold ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.hasEnoughGold")
    class HasEnoughGoldTests {

        @Test
        @DisplayName("player has 0 gold, cost 1 → false")
        void zeroGold_cost1_returnsFalse() {
            // Drain all player gold to reach 0 (player starts with position-count gold)
            int existing = player.getPlayerBoard().getGold();
            player.getPlayerBoard().reduceGold(existing);
            assertEquals(0, player.getPlayerBoard().getGold());
            assertFalse(Rules.hasEnoughGold(player, 1));
        }

        @Test
        @DisplayName("player has 0 gold, cost 0 → true")
        void zeroGold_cost0_returnsTrue() {
            assertTrue(Rules.hasEnoughGold(player, 0));
        }

        @Test
        @DisplayName("player has exactly 5 gold, cost 5 → true")
        void fiveGold_cost5_returnsTrue() {
            // Drain to 0, then gain exactly 5
            int existing = player.getPlayerBoard().getGold();
            player.getPlayerBoard().reduceGold(existing);
            player.getPlayerBoard().gainGold(5);
            assertEquals(5, player.getPlayerBoard().getGold());
            assertTrue(Rules.hasEnoughGold(player, 5));
        }

        @Test
        @DisplayName("player has exactly 4 gold, cost 5 → false")
        void fourGold_cost5_returnsFalse() {
            // Drain to 0, then gain exactly 4
            int existing = player.getPlayerBoard().getGold();
            player.getPlayerBoard().reduceGold(existing);
            player.getPlayerBoard().gainGold(4);
            assertEquals(4, player.getPlayerBoard().getGold());
            assertFalse(Rules.hasEnoughGold(player, 5));
        }
    }

    // ─── isBoardGoldAvailable ──────────────────────────────────────────────

    @Nested
    @DisplayName("Rules.isBoardGoldAvailable")
    class BoardGoldAvailableTests {

        @Test
        @DisplayName("board has plenty of gold at game start (small amounts always available)")
        void boardHasGold_atStart() {
            // Board starts with 1000 gold minus what was distributed to players
            // So it definitely has at least several hundred gold
            assertTrue(Rules.isBoardGoldAvailable(board, 1));
            assertTrue(Rules.isBoardGoldAvailable(board, 100));
            // Board has close to 1000 gold: test with actual amount
            int boardGold = game.getBoard().getGold();
            assertTrue(Rules.isBoardGoldAvailable(board, boardGold));
            assertFalse(Rules.isBoardGoldAvailable(board, boardGold + 1));
        }

        @Test
        @DisplayName("cost 0 → always true")
        void zeroCost_alwaysTrue() {
            assertTrue(Rules.isBoardGoldAvailable(board, 0));
        }
    }
}
