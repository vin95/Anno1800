package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.OldWorldIsland;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiscoverOldWorldIslandValidator.
 * Rules tested:
 * - Player must have fewer than 4 Old World Islands
 * - Player must have ≥ numOldWorldIslands + 1 explorer chips
 * - Old World Islands must be available on the board
 */
@DisplayName("DiscoverOldWorldIslandValidator Tests")
class DiscoverOldWorldIslandValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    private static final Action.DiscoverOldWorldIsland ACTION = new Action.DiscoverOldWorldIsland();

    // ─── max islands reached ───────────────────────────────────────────────

    @Test
    @DisplayName("player already has 4 Old World Islands → false")
    void maxIslandsReached_returnsFalse() {
        PlayerBoard pb = player.getPlayerBoard();
        // Add 4 islands to the player (take from board)
        for (int i = 0; i < 4; i++) {
            if (!game.getBoard().getOldWorldIslands().isEmpty()) {
                OldWorldIsland island = game.getBoard().getOldWorldIslands().pollFirst();
                pb.addOldWorldIsland(island);
            }
        }
        assertEquals(4, pb.getNumOldWorldIslands());
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── insufficient explorer chips ───────────────────────────────────────

    @Test
    @DisplayName("0 islands, 0 explorer chips → false (needs ≥ 1 chip)")
    void zeroChips_zeroIslands_returnsFalse() {
        PlayerBoard pb = player.getPlayerBoard();
        // Drain all explorer chips
        int chips = pb.getAvailableExplorerChips();
        if (chips > 0) {
            pb.reduceAvailableExplorerChips(chips);
        }
        assertEquals(0, pb.getAvailableExplorerChips());
        assertEquals(0, pb.getNumOldWorldIslands());
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    @Test
    @DisplayName("1 island already, needs 2 chips but has only 1 → false")
    void oneIsland_oneChip_returnsFalse() {
        PlayerBoard pb = player.getPlayerBoard();
        // Give player exactly 1 island
        if (!game.getBoard().getOldWorldIslands().isEmpty()) {
            pb.addOldWorldIsland(game.getBoard().getOldWorldIslands().pollFirst());
        }
        assertEquals(1, pb.getNumOldWorldIslands());

        // Set chips to exactly 1 (needs 2 for 1 island)
        int current = pb.getAvailableExplorerChips();
        if (current > 1) pb.reduceAvailableExplorerChips(current - 1);
        else if (current < 1) pb.increaseAvailableExplorerChips(1 - current);
        assertEquals(1, pb.getAvailableExplorerChips());

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── enough chips ──────────────────────────────────────────────────────

    @Test
    @DisplayName("0 islands, exactly 1 chip available → true (if board has islands)")
    void zeroIslands_oneChip_boardHasIslands_returnsTrue() {
        PlayerBoard pb = player.getPlayerBoard();
        // Ensure exactly 1 chip
        int current = pb.getAvailableExplorerChips();
        if (current > 1) pb.reduceAvailableExplorerChips(current - 1);
        else if (current < 1) pb.increaseAvailableExplorerChips(1 - current);
        assertEquals(1, pb.getAvailableExplorerChips());

        // Board must still have islands
        assertFalse(game.getBoard().getOldWorldIslands().isEmpty(),
                "Board must have islands for this test");
        assertTrue(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── no islands on board ──────────────────────────────────────────────

    @Test
    @DisplayName("board has no Old World Islands → false")
    void noIslandsOnBoard_returnsFalse() {
        PlayerBoard pb = player.getPlayerBoard();
        // Give player plenty of chips
        pb.increaseAvailableExplorerChips(10);

        // Drain board islands
        while (!game.getBoard().getOldWorldIslands().isEmpty()) {
            game.getBoard().getOldWorldIslands().pollFirst();
        }
        assertTrue(game.getBoard().getOldWorldIslands().isEmpty());
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── board has islands at start ────────────────────────────────────────

    @Test
    @DisplayName("board has Old World Islands at game start")
    void boardHasIslandsAtStart() {
        assertFalse(game.getBoard().getOldWorldIslands().isEmpty(),
                "Board must have Old World Islands at game start");
    }
}
