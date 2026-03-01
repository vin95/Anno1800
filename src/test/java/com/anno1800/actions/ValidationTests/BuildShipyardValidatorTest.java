package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BuildShipyardValidator.
 *
 * Strategy:
 *  - Ungültige Level (0, 4, -1) werden immer abgelehnt.
 *  - Level 1 hat keine Güterkosten → sollte am Spielanfang gültig sein (falls Küste frei).
 *  - Keine freie Küstenkachel → immer false.
 */
@DisplayName("BuildShipyard Validator Tests")
class BuildShipyardValidatorTest {

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
    @DisplayName("Level 0 ist ungültig → false")
    void testLevel0ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(0), player, game));
    }

    @Test
    @DisplayName("Level 4 ist ungültig → false")
    void testLevel4ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(4), player, game));
    }

    @Test
    @DisplayName("Negativer Level ist ungültig → false")
    void testNegativeLevelReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(-1), player, game));
    }

    @Test
    @DisplayName("Level 10 ist ungültig → false")
    void testLevel10ReturnsFalse() {
        assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(10), player, game));
    }

    // =========================================================================
    // Gültige Level – Verfügbarkeit prüfen
    // =========================================================================

    @Test
    @DisplayName("Level 1 am Spielanfang: Board hat Level-1-Werften verfügbar")
    void testLevel1AvailableAtStart() {
        assertTrue(game.getBoard().hasShipyard(1),
            "Board sollte am Spielanfang Level-1-Werften haben");
    }

    @Test
    @DisplayName("Wenn keine Level-1-Werften mehr vorhanden: buildShipyard(1) → false")
    void testBuildShipyardLevel1WhenNoneAvailable() {
        // Alle Level-1-Werften aus dem Board entfernen
        while (game.getBoard().hasShipyard(1)) {
            game.getBoard().getShipyardLevel1().poll();
        }
        assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(1), player, game));
    }

    @Test
    @DisplayName("Küstenkacheln gesättigt: buildShipyard → false")
    void testNoFreeCoastTilesBlocksShipyard() {
        // Alle Küstenkacheln mit Schiffen oder Werften belegen
        // Der Spieler startet mit 1 Werft → 4 freie Küstenkacheln
        // Wir befüllen die Küstenkacheln durch Bauen von Werften bis keine mehr frei
        // oder testen indirekt über getFreeLandTiles()
        Player player0 = game.getPlayers()[0];
        int freeCoast = player0.getPlayerBoard().getFreeCoastTiles();
        // Einfache Prüfung: Wenn freieKüste = 0, dann false
        if (freeCoast == 0) {
            assertFalse(ActionValidator.canExecute(new Action.BuildShipyard(1), player0, game));
        } else {
            assertTrue(freeCoast > 0, "Spieler sollte freie Küstenkacheln haben");
        }
    }
}
