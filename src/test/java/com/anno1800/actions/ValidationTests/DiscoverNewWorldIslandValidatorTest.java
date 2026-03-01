package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.tiles.NewWorldIsland;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiscoverNewWorldIslandValidator.
 * Rules tested:
 * - Player must have fewer than 4 New World Islands
 * - Player must have ≥ numNewWorldIslands + 1 explorer chips
 * - New World Islands must be available on the board
 */
@DisplayName("DiscoverNewWorldIslandValidator Tests")
class DiscoverNewWorldIslandValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    private static final Action.DiscoverNewWorldIsland ACTION = new Action.DiscoverNewWorldIsland();

    // ─── max islands reached ───────────────────────────────────────────────

    @Test
    @DisplayName("player already has 4 New World Islands → false")
    void maxIslandsReached_returnsFalse() throws Exception {
        // Use reflection to set numNewWorldIslands = 4 directly,
        // because addNewWorldIsland triggers addPlantation which overflows the fixed-size array
        Field field = PlayerBoard.class.getDeclaredField("numNewWorldIslands");
        field.setAccessible(true);
        field.setInt(player.getPlayerBoard(), 4);

        assertEquals(4, player.getPlayerBoard().getNumNewWorldIslands());
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── insufficient explorer chips ───────────────────────────────────────

    @Test
    @DisplayName("0 New World Islands, 0 chips → false (needs ≥ 1)")
    void zeroChips_zeroIslands_returnsFalse() {
        PlayerBoard pb = player.getPlayerBoard();
        int chips = pb.getAvailableExplorerChips();
        if (chips > 0) pb.reduceAvailableExplorerChips(chips);
        assertEquals(0, pb.getAvailableExplorerChips());
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    @Test
    @DisplayName("1 island, only 1 chip (needs 2) → false")
    void oneIsland_oneChip_returnsFalse() throws Exception {
        Field field = PlayerBoard.class.getDeclaredField("numNewWorldIslands");
        field.setAccessible(true);
        field.setInt(player.getPlayerBoard(), 1);
        assertEquals(1, player.getPlayerBoard().getNumNewWorldIslands());

        int current = player.getPlayerBoard().getAvailableExplorerChips();
        if (current > 1) player.getPlayerBoard().reduceAvailableExplorerChips(current - 1);
        else if (current < 1) player.getPlayerBoard().increaseAvailableExplorerChips(1 - current);
        assertEquals(1, player.getPlayerBoard().getAvailableExplorerChips());

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── valid conditions ──────────────────────────────────────────────────

    @Test
    @DisplayName("0 islands, exactly 1 chip, board has islands → true")
    void zeroIslands_oneChip_boardHasIslands_returnsTrue() {
        PlayerBoard pb = player.getPlayerBoard();
        int current = pb.getAvailableExplorerChips();
        if (current > 1) pb.reduceAvailableExplorerChips(current - 1);
        else if (current < 1) pb.increaseAvailableExplorerChips(1 - current);

        assertFalse(game.getBoard().getNewWorldIslands().isEmpty(),
                "Board must have New World Islands");
        assertTrue(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── no islands on board ──────────────────────────────────────────────

    @Test
    @DisplayName("no New World Islands on board → false")
    void noIslandsOnBoard_returnsFalse() {
        player.getPlayerBoard().increaseAvailableExplorerChips(10);
        while (!game.getBoard().getNewWorldIslands().isEmpty()) {
            game.getBoard().getNewWorldIslands().pollFirst();
        }
        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── board state at start ─────────────────────────────────────────────

    @Test
    @DisplayName("board has New World Islands at game start")
    void boardHasNewWorldIslandsAtStart() {
        assertFalse(game.getBoard().getNewWorldIslands().isEmpty(),
                "Board must have New World Islands at start");
    }
}
