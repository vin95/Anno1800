package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SettleResidentValidator.
 *
 * Strategy:
 *  - Ungültige Level (0, 6, -1) werden immer abgelehnt.
 *  - Zu Spielbeginn sind alle Einwohner-Level auf dem Board verfügbar.
 *  - Wenn der Board keinen Einwohner mehr hat, ist Ansiedeln nicht möglich.
 */
@DisplayName("SettleResident Validator Tests")
class SettleResidentValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // =========================================================================
    // Ungültige Level
    // =========================================================================

    @Test
    @DisplayName("Level 0: ungültig → false")
    void testLevel0ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(0), player, game));
    }

    @Test
    @DisplayName("Level 6: ungültig → false")
    void testLevel6ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(6), player, game));
    }

    @Test
    @DisplayName("Level -1: ungültig → false")
    void testNegativeLevelReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(-1), player, game));
    }

    @Test
    @DisplayName("Level 10: ungültig → false")
    void testLevel10ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(10), player, game));
    }

    // =========================================================================
    // Board-Verfügbarkeit
    // =========================================================================

    @Test
    @DisplayName("Board hat nach Spielbeginn noch Farmer (Level 1) verfügbar")
    void testBoardHasFarmersAtStart() {
        assertTrue(game.getBoard().getFarmers() > 0,
            "Board sollte noch Farmer haben");
    }

    @Test
    @DisplayName("Board hat nach Spielbeginn noch Workers (Level 2) verfügbar")
    void testBoardHasWorkersAtStart() {
        assertTrue(game.getBoard().getWorkers() > 0,
            "Board sollte noch Worker haben");
    }

    @Test
    @DisplayName("Board hat nach Spielbeginn noch Artisans (Level 3) verfügbar")
    void testBoardHasArtisansAtStart() {
        assertTrue(game.getBoard().getArtisans() > 0,
            "Board sollte noch Artisans haben");
    }

    @Test
    @DisplayName("Board hat nach Spielbeginn noch Engineers (Level 4) verfügbar")
    void testBoardHasEngineersAtStart() {
        assertTrue(game.getBoard().getEngineers() > 0,
            "Board sollte noch Engineers haben");
    }

    @Test
    @DisplayName("Board hat nach Spielbeginn noch Investors (Level 5) verfügbar")
    void testBoardHasInvestorsAtStart() {
        assertTrue(game.getBoard().getInvestors() > 0,
            "Board sollte noch Investors haben");
    }

    // =========================================================================
    // Keine Einwohner auf dem Board
    // =========================================================================

    @Test
    @DisplayName("Wenn keine Farmer mehr: Level 1 kann nicht angesiedelt werden")
    void testCannotSettleWhenNoFarmersOnBoard() {
        // Alle Farmer vom Board entfernen
        int currentFarmers = game.getBoard().getFarmers();
        for (int i = 0; i < currentFarmers; i++) {
            game.getBoard().takeResident(1);
        }
        assertEquals(0, game.getBoard().getFarmers());
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(1), player, game));
    }

    @Test
    @DisplayName("Wenn keine Investors mehr: Level 5 kann nicht angesiedelt werden")
    void testCannotSettleWhenNoInvestorsOnBoard() {
        int currentInvestors = game.getBoard().getInvestors();
        for (int i = 0; i < currentInvestors; i++) {
            game.getBoard().takeResident(5);
        }
        assertEquals(0, game.getBoard().getInvestors());
        assertFalse(ActionValidator.canExecute(new Action.SettleResident(5), player, game));
    }
}
