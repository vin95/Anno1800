package com.anno1800.game.player;

import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.residents.*;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.rewards.Reward;
import org.junit.jupiter.api.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ObjectiveCard end-game point calculations.
 * This class lives in com.anno1800.game.player to access PlayerBoard's
 * package-private fields (numNewWorldIslands, numOldWorldIslands, etc.).
 */
@DisplayName("ObjectiveCard Punkteberechnungs-Tests")
class ObjectiveCardPointsTest {

    private Player p0, p1, p2;

    @BeforeEach
    void setUp() {
        p0 = new Player("P0", new PlayerBoard(), 0);
        p1 = new Player("P1", new PlayerBoard(), 1);
        p2 = new Player("P2", new PlayerBoard(), 2);
    }

    // =========================================================================
    // MostInvestors
    // =========================================================================

    @Nested
    @DisplayName("MostInvestors (10 VP / 4 VP)")
    class MostInvestorsTests {

        @Test
        @DisplayName("Keine Investoren: keine Punkte")
        void testNoInvestors_NoPoints() {
            var card = new ObjectiveCard.MostInvestors();
            assertTrue(card.calculateEndGamePoints(new Player[]{p0, p1}).isEmpty());
        }

        @Test
        @DisplayName("Nur ein Spieler mit Investoren: 10 VP für Erstplatzierten")
        void testOnlyOnePlayerHasInvestors_Gets10Points() {
            p0.getPlayerBoard().getResidents().add(new Investor());
            p0.getPlayerBoard().getResidents().add(new Investor());

            var points = new ObjectiveCard.MostInvestors()
                .calculateEndGamePoints(new Player[]{p0, p1});

            assertEquals(10, points.getOrDefault(p0, 0));
            assertEquals(0, points.getOrDefault(p1, 0));
        }

        @Test
        @DisplayName("Gleichstand bei Erstplatz: beide bekommen 10 VP")
        void testTieForFirst_BothGet10Points() {
            p0.getPlayerBoard().getResidents().add(new Investor());
            p1.getPlayerBoard().getResidents().add(new Investor());

            var points = new ObjectiveCard.MostInvestors()
                .calculateEndGamePoints(new Player[]{p0, p1});

            assertEquals(10, points.getOrDefault(p0, 0));
            assertEquals(10, points.getOrDefault(p1, 0));
        }

        @Test
        @DisplayName("Zweitplatzierter bekommt 4 VP")
        void testSecondPlaceGets4Points() {
            p0.getPlayerBoard().getResidents().add(new Investor());
            p0.getPlayerBoard().getResidents().add(new Investor());
            p1.getPlayerBoard().getResidents().add(new Investor());

            var points = new ObjectiveCard.MostInvestors()
                .calculateEndGamePoints(new Player[]{p0, p1});

            assertEquals(10, points.getOrDefault(p0, 0));
            assertEquals(4, points.getOrDefault(p1, 0));
        }

        @Test
        @DisplayName("Dritter Spieler ohne Investoren bekommt 0 VP")
        void testThirdPlayerNoInvestors_ZeroPoints() {
            p0.getPlayerBoard().getResidents().add(new Investor());
            p0.getPlayerBoard().getResidents().add(new Investor());
            p1.getPlayerBoard().getResidents().add(new Investor());

            var points = new ObjectiveCard.MostInvestors()
                .calculateEndGamePoints(new Player[]{p0, p1, p2});

            assertEquals(0, points.getOrDefault(p2, 0));
        }
    }

    // =========================================================================
    // MostEngineers
    // =========================================================================

    @Nested
    @DisplayName("MostEngineers (10 VP / 4 VP)")
    class MostEngineersTests {

        @Test
        @DisplayName("Ein Spieler mit 3 Engineers: 10 VP")
        void testSinglePlayerWith3Engineers_Gets10Points() {
            for (int i = 0; i < 3; i++) {
                p0.getPlayerBoard().getResidents().add(new Engineer());
            }
            var points = new ObjectiveCard.MostEngineers()
                .calculateEndGamePoints(new Player[]{p0, p1});
            assertEquals(10, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("Keine Engineers: keine Punkte")
        void testNoEngineers_NoPoints() {
            assertTrue(new ObjectiveCard.MostEngineers()
                .calculateEndGamePoints(new Player[]{p0, p1}).isEmpty());
        }
    }

    // =========================================================================
    // NewWorldExplorer
    // =========================================================================

    @Nested
    @DisplayName("NewWorldExplorer (6 VP pro Insel)")
    class NewWorldExplorerTests {

        @Test
        @DisplayName("0 Inseln: keine Punkte")
        void testNoIslands_NoPoints() {
            assertTrue(new ObjectiveCard.NewWorldExplorer()
                .calculateEndGamePoints(new Player[]{p0}).isEmpty());
        }

        @Test
        @DisplayName("1 Insel = 6 VP")
        void testOneIsland_6Points() {
            p0.getPlayerBoard().numNewWorldIslands = 1;
            var points = new ObjectiveCard.NewWorldExplorer()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(6, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("3 Inseln = 18 VP")
        void testThreeIslands_18Points() {
            p0.getPlayerBoard().numNewWorldIslands = 3;
            var points = new ObjectiveCard.NewWorldExplorer()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(18, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("Zwei Spieler erhalten proportionale Punkte")
        void testTwoPlayersGetProportionalPoints() {
            p0.getPlayerBoard().numNewWorldIslands = 2;
            p1.getPlayerBoard().numNewWorldIslands = 3;
            var points = new ObjectiveCard.NewWorldExplorer()
                .calculateEndGamePoints(new Player[]{p0, p1});
            assertEquals(12, points.getOrDefault(p0, 0));
            assertEquals(18, points.getOrDefault(p1, 0));
        }
    }

    // =========================================================================
    // SingleIslandBonus
    // =========================================================================

    @Nested
    @DisplayName("SingleIslandBonus (18 VP bei ≤1 Alter-Welt-Insel)")
    class SingleIslandBonusTests {

        @Test
        @DisplayName("0 Alte-Welt-Inseln: 18 VP")
        void testZeroOldWorldIslands_Gets18Points() {
            var points = new ObjectiveCard.SingleIslandBonus()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(18, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("1 Alte-Welt-Insel: 18 VP")
        void testOneOldWorldIsland_Gets18Points() {
            p0.getPlayerBoard().numOldWorldIslands = 1;
            var points = new ObjectiveCard.SingleIslandBonus()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(18, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("2 Alte-Welt-Inseln: keine Punkte")
        void testTwoOldWorldIslands_NoPoints() {
            p0.getPlayerBoard().numOldWorldIslands = 2;
            var points = new ObjectiveCard.SingleIslandBonus()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(0, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("4 Alte-Welt-Inseln: keine Punkte")
        void testFourOldWorldIslands_NoPoints() {
            p0.getPlayerBoard().numOldWorldIslands = 4;
            var points = new ObjectiveCard.SingleIslandBonus()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(0, points.getOrDefault(p0, 0));
        }
    }

    // =========================================================================
    // ResidentCardsPenalty
    // =========================================================================

    @Nested
    @DisplayName("ResidentCardsPenalty (-2 VP pro Einwohnerkarte in der Hand)")
    class ResidentCardsPenaltyTests {

        @Test
        @DisplayName("Keine Karten: keine Strafe")
        void testNoCards_NoPenalty() {
            assertTrue(new ObjectiveCard.ResidentCardsPenalty()
                .calculateEndGamePoints(new Player[]{p0}).isEmpty());
        }

        @Test
        @DisplayName("1 Karte = -2 VP")
        void testOneCard_Minus2Points() {
            p0.getPlayerBoard().getResidentCards().add(
                new ResidentCard(1, new Goods[]{}, new Reward.Gold(1))
            );
            var points = new ObjectiveCard.ResidentCardsPenalty()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(-2, points.getOrDefault(p0, 0));
        }

        @Test
        @DisplayName("3 Karten = -6 VP")
        void testThreeCards_Minus6Points() {
            for (int i = 0; i < 3; i++) {
                p0.getPlayerBoard().getResidentCards().add(
                    new ResidentCard(1, new Goods[]{}, new Reward.Gold(1))
                );
            }
            var points = new ObjectiveCard.ResidentCardsPenalty()
                .calculateEndGamePoints(new Player[]{p0});
            assertEquals(-6, points.getOrDefault(p0, 0));
        }
    }

    // =========================================================================
    // MostExpeditionCards
    // =========================================================================

    @Nested
    @DisplayName("MostExpeditionCards (10 VP / 4 VP)")
    class MostExpeditionCardsTests {

        @Test
        @DisplayName("Keine Expeditionskarten: keine Punkte")
        void testNoExpeditionCards_NoPoints() {
            assertTrue(new ObjectiveCard.MostExpeditionCards()
                .calculateEndGamePoints(new Player[]{p0, p1}).isEmpty());
        }

        @Test
        @DisplayName("Spieler mit mehr Karten erhält 10 VP")
        void testMoreCardsGets10Points() {
            p0.getPlayerBoard().getExpeditionCards().add(
                new com.anno1800.game.cards.ExpeditionCard(3, 4, 2, 3));
            p0.getPlayerBoard().getExpeditionCards().add(
                new com.anno1800.game.cards.ExpeditionCard(3, 4, 2, 3));

            var points = new ObjectiveCard.MostExpeditionCards()
                .calculateEndGamePoints(new Player[]{p0, p1});
            assertEquals(10, points.getOrDefault(p0, 0));
            assertEquals(0, points.getOrDefault(p1, 0));
        }
    }

    // =========================================================================
    // ObjectiveCard Metadaten
    // =========================================================================

    @Nested
    @DisplayName("Karten-Metadaten (Titel & Beschreibung)")
    class ObjectiveCardMetaTests {

        @Test
        @DisplayName("MostInvestors hat nicht-leeren Titel")
        void testMostInvestorsHasTitle() {
            assertFalse(new ObjectiveCard.MostInvestors().getTitle().isBlank());
        }

        @Test
        @DisplayName("MostInvestors hat nicht-leere Beschreibung")
        void testMostInvestorsHasDescription() {
            assertFalse(new ObjectiveCard.MostInvestors().getDescription().isBlank());
        }

        @Test
        @DisplayName("NewWorldExplorer hat nicht-leeren Titel")
        void testNewWorldExplorerHasTitle() {
            assertFalse(new ObjectiveCard.NewWorldExplorer().getTitle().isBlank());
        }

        @Test
        @DisplayName("SingleIslandBonus hat nicht-leeren Titel")
        void testSingleIslandBonusHasTitle() {
            assertFalse(new ObjectiveCard.SingleIslandBonus().getTitle().isBlank());
        }

        @Test
        @DisplayName("ExtraAction modifiziert Aktionen")
        void testExtraActionModifiesActions() {
            assertTrue(new ObjectiveCard.ExtraAction().modifiesActions());
        }

        @Test
        @DisplayName("ExplorerTrader modifiziert Aktionen")
        void testExplorerTraderModifiesActions() {
            assertTrue(new ObjectiveCard.ExplorerTrader().modifiesActions());
        }

        @Test
        @DisplayName("MostInvestors modifiziert keine Aktionen (Standard false)")
        void testMostInvestorsDoesNotModifyActions() {
            assertFalse(new ObjectiveCard.MostInvestors().modifiesActions());
        }
    }
}
