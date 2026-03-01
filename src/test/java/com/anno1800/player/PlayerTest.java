package com.anno1800.player;

import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Player class.
 * Covers name, ID, position, victory points, bonus points and total points.
 */
@DisplayName("Player Tests")
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestSpieler", new PlayerBoard(), 0);
    }

    // =========================================================================
    // Grundlegende Eigenschaften
    // =========================================================================

    @Test
    @DisplayName("Player hat korrekten Namen")
    void testPlayerHasCorrectName() {
        assertEquals("TestSpieler", player.getName());
    }

    @Test
    @DisplayName("Player hat korrekte ID")
    void testPlayerHasCorrectId() {
        assertEquals(0, player.getId());
    }

    @Test
    @DisplayName("Player startet bei Position 0")
    void testInitialPosition() {
        assertEquals(0, player.getPosition());
    }

    @Test
    @DisplayName("setPosition() setzt Position korrekt")
    void testSetPosition() {
        player.setPosition(3);
        assertEquals(3, player.getPosition());
    }

    @Test
    @DisplayName("setPosition() kann mehrfach aufgerufen werden")
    void testSetPositionMultipleTimes() {
        player.setPosition(1);
        player.setPosition(4);
        assertEquals(4, player.getPosition());
    }

    @Test
    @DisplayName("Player hat nicht-null PlayerBoard")
    void testPlayerHasNonNullBoard() {
        assertNotNull(player.getPlayerBoard());
    }

    // =========================================================================
    // Victory Points
    // =========================================================================

    @Nested
    @DisplayName("Siegpunkte")
    class VictoryPointTests {

        @Test
        @DisplayName("Player startet mit 0 Siegpunkten")
        void testInitialVictoryPoints() {
            assertEquals(0, player.getVictoryPoints());
        }

        @Test
        @DisplayName("addVictoryPoints() addiert korrekt")
        void testAddVictoryPoints() {
            player.addVictoryPoints(10);
            assertEquals(10, player.getVictoryPoints());
        }

        @Test
        @DisplayName("Mehrfaches addVictoryPoints() kumuliert sich")
        void testAddVictoryPointsAccumulates() {
            player.addVictoryPoints(5);
            player.addVictoryPoints(3);
            player.addVictoryPoints(2);
            assertEquals(10, player.getVictoryPoints());
        }

        @Test
        @DisplayName("addVictoryPoints() mit 0 ändert nichts")
        void testAddZeroVictoryPoints() {
            player.addVictoryPoints(5);
            player.addVictoryPoints(0);
            assertEquals(5, player.getVictoryPoints());
        }
    }

    // =========================================================================
    // Bonus Points
    // =========================================================================

    @Nested
    @DisplayName("Bonuspunkte")
    class BonusPointTests {

        @Test
        @DisplayName("Player startet mit 0 Bonuspunkten")
        void testInitialBonusPoints() {
            assertEquals(0, player.getBonusPoints());
        }

        @Test
        @DisplayName("addBonusPoints() addiert korrekt")
        void testAddBonusPoints() {
            player.addBonusPoints(7);
            assertEquals(7, player.getBonusPoints());
        }

        @Test
        @DisplayName("Mehrfaches addBonusPoints() kumuliert sich")
        void testAddBonusPointsAccumulates() {
            player.addBonusPoints(7);
            player.addBonusPoints(3);
            assertEquals(10, player.getBonusPoints());
        }
    }

    // =========================================================================
    // Total Points
    // =========================================================================

    @Nested
    @DisplayName("Gesamtpunkte")
    class TotalPointTests {

        @Test
        @DisplayName("getTotalPoints() gibt 0 bei keinen Punkten zurück")
        void testTotalPointsZero() {
            assertEquals(0, player.getTotalPoints());
        }

        @Test
        @DisplayName("getTotalPoints() = victoryPoints + bonusPoints")
        void testTotalPointsIsSum() {
            player.addVictoryPoints(10);
            player.addBonusPoints(7);
            assertEquals(17, player.getTotalPoints());
        }

        @Test
        @DisplayName("getTotalPoints() korrekt wenn nur VP")
        void testTotalPointsOnlyVP() {
            player.addVictoryPoints(15);
            assertEquals(15, player.getTotalPoints());
        }

        @Test
        @DisplayName("getTotalPoints() korrekt wenn nur Bonus")
        void testTotalPointsOnlyBonus() {
            player.addBonusPoints(7);
            assertEquals(7, player.getTotalPoints());
        }
    }

    // =========================================================================
    // Mehrere Spieler
    // =========================================================================

    @Nested
    @DisplayName("Mehrere Spieler-Instanzen")
    class MultiplePlayersTests {

        @Test
        @DisplayName("Verschiedene Spieler haben unterschiedliche IDs")
        void testDifferentPlayersDifferentIds() {
            Player p0 = new Player("P0", new PlayerBoard(), 0);
            Player p1 = new Player("P1", new PlayerBoard(), 1);
            Player p2 = new Player("P2", new PlayerBoard(), 2);
            assertNotEquals(p0.getId(), p1.getId());
            assertNotEquals(p1.getId(), p2.getId());
        }

        @Test
        @DisplayName("Spieler teilen keinen PlayerBoard")
        void testPlayersDoNotShareBoard() {
            Player p0 = new Player("P0", new PlayerBoard(), 0);
            Player p1 = new Player("P1", new PlayerBoard(), 1);
            assertNotSame(p0.getPlayerBoard(), p1.getPlayerBoard());
        }

        @Test
        @DisplayName("Punkte eines Spielers beeinflussen anderen nicht")
        void testPointsAreIndependent() {
            Player p0 = new Player("P0", new PlayerBoard(), 0);
            Player p1 = new Player("P1", new PlayerBoard(), 1);
            p0.addVictoryPoints(100);
            assertEquals(0, p1.getVictoryPoints());
        }
    }
}
