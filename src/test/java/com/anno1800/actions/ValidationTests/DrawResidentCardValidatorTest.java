package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for DrawResidentCardValidator.
 *
 * Strategy:
 *  - At game start, all stacks contain cards → valid levels should return true.
 *  - Invalid population levels (0, -1, 8) always return false regardless of state.
 */
@DisplayName("DrawResidentCard Validator Tests")
class DrawResidentCardValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // =========================================================================
    // Gültige Level (Spielanfang – alle Stacks belegt)
    // =========================================================================

    @Test
    @DisplayName("Level 1 (Farmer-Karte): Spieler kann Karte ziehen")
    void testCanDrawLevel1AtStart() {
        assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(1), player, game));
    }

    @Test
    @DisplayName("Level 2 (Worker-Karte): Spieler kann Karte ziehen")
    void testCanDrawLevel2AtStart() {
        assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(2), player, game));
    }

    @Test
    @DisplayName("Level 3 (Artisan-Karte): Spieler kann Karte ziehen")
    void testCanDrawLevel3AtStart() {
        assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(3), player, game));
    }

    @Test
    @DisplayName("Level 4 (Engineer-Karte): Spieler kann Karte ziehen")
    void testCanDrawLevel4AtStart() {
        assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(4), player, game));
    }

    @Test
    @DisplayName("Level 5 (Investor-Karte): Spieler kann Karte ziehen")
    void testCanDrawLevel5AtStart() {
        assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(5), player, game));
    }

    // =========================================================================
    // Ungültige Level
    // =========================================================================

    @Test
    @DisplayName("Level 0: ungültig → false")
    void testLevel0ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(0), player, game));
    }

    @Test
    @DisplayName("Level -1: ungültig → false")
    void testNegativeLevelReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(-1), player, game));
    }

    @Test
    @DisplayName("Level 8: ungültig → false")
    void testLevel8ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(8), player, game));
    }

    @Test
    @DisplayName("Level 100: ungültig → false")
    void testLevel100ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(100), player, game));
    }

    // =========================================================================
    // Leerer Stack
    // =========================================================================

    @Test
    @DisplayName("Level 1-2 wenn Stack 1 leer: canDraw → false")
    void testCannotDrawWhenStack1Empty() {
        // Stack 1 leeren
        while (!game.getBoard().getResidentStack1().isEmpty()) {
            game.getBoard().getResidentStack1().pop();
        }
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(1), player, game));
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(2), player, game));
    }

    @Test
    @DisplayName("Level 3-5 wenn Stack 2 leer: canDraw → false")
    void testCannotDrawWhenStack2Empty() {
        while (!game.getBoard().getResidentStack2().isEmpty()) {
            game.getBoard().getResidentStack2().pop();
        }
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(3), player, game));
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(4), player, game));
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(5), player, game));
    }

    @Test
    @DisplayName("Level 6-7 wenn Stack 3 leer: canDraw → false")
    void testCannotDrawWhenStack3Empty() {
        while (!game.getBoard().getResidentStack3().isEmpty()) {
            game.getBoard().getResidentStack3().pop();
        }
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(6), player, game));
        assertFalse(ActionValidator.canExecute(new Action.DrawResidentCard(7), player, game));
    }

    @Test
    @DisplayName("Leerer Stack 1 blockiert nicht Stack 2/3")
    void testEmptyStack1DoesNotBlockHigherLevels() {
        while (!game.getBoard().getResidentStack1().isEmpty()) {
            game.getBoard().getResidentStack1().pop();
        }
        // Stack 2 sollte noch befüllt sein
        if (!game.getBoard().getResidentStack2().isEmpty()) {
            assertTrue(ActionValidator.canExecute(new Action.DrawResidentCard(3), player, game));
        }
    }
}
