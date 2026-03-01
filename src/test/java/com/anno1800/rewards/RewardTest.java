package com.anno1800.rewards;

import com.anno1800.game.rewards.Reward;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all Reward sealed-interface subtypes.
 * Focuses especially on FreeGoodsChoice which contains the most logic.
 */
@DisplayName("Reward Tests")
class RewardTest {

    // =========================================================================
    // FreeGoodsChoice
    // =========================================================================

    @Nested
    @DisplayName("FreeGoodsChoice")
    class FreeGoodsChoiceTests {

        @Test
        @DisplayName("Neu erstelltes FreeGoodsChoice hat noch keine Wahl getroffen")
        void testNewChoiceHasNoSelection() {
            Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            assertFalse(reward.hasChoice(), "Neue FreeGoodsChoice sollte noch keine Wahl haben");
            assertNull(reward.chosenGood(), "chosenGood() sollte null sein");
        }

        @Test
        @DisplayName("withChoice() gibt neues Reward mit gewähltem Gut zurück")
        void testWithChoiceReturnsNewRewardWithSelection() {
            Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            Reward.FreeGoodsChoice chosen = reward.withChoice(Goods.PLANKS);

            assertTrue(chosen.hasChoice());
            assertEquals(Goods.PLANKS, chosen.chosenGood());
        }

        @Test
        @DisplayName("withChoice() auf zweite Option gibt korrekte Wahl zurück")
        void testWithChoiceSecondOption() {
            Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            Reward.FreeGoodsChoice chosen = reward.withChoice(Goods.BEER);
            assertEquals(Goods.BEER, chosen.chosenGood());
        }

        @Test
        @DisplayName("Jede gültige Option kann gewählt werden")
        void testAllOptionsCanBeChosen() {
            Goods[] options = {Goods.PLANKS, Goods.BEER, Goods.COAL, Goods.BRICKS};
            for (Goods option : options) {
                Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(options);
                assertDoesNotThrow(() -> reward.withChoice(option),
                    "Option " + option + " sollte wählbar sein");
            }
        }

        @Test
        @DisplayName("withChoice() mit ungültiger Option wirft IllegalArgumentException")
        void testWithChoiceInvalidOptionThrows() {
            Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            assertThrows(IllegalArgumentException.class,
                () -> reward.withChoice(Goods.COAL),
                "Nicht in den Optionen enthaltenes Gut sollte IllegalArgumentException werfen");
        }

        @Test
        @DisplayName("withChoice() auf bereits gewähltes Reward wirft IllegalStateException")
        void testWithChoiceAlreadyChosenThrows() {
            Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            Reward.FreeGoodsChoice chosen = reward.withChoice(Goods.PLANKS);
            assertThrows(IllegalStateException.class,
                () -> chosen.withChoice(Goods.BEER),
                "Zweimaliges Wählen sollte IllegalStateException werfen");
        }

        @Test
        @DisplayName("Original-Reward bleibt nach withChoice() unverändert (Immutabilität)")
        void testOriginalRemainsUnchangedAfterWithChoice() {
            Reward.FreeGoodsChoice original = new Reward.FreeGoodsChoice(
                new Goods[]{Goods.PLANKS, Goods.BEER}
            );
            original.withChoice(Goods.PLANKS); // Result ignored
            assertFalse(original.hasChoice(), "Das originale Reward sollte weiterhin keine Wahl haben");
        }
    }

    // =========================================================================
    // Einfache Record-Typen
    // =========================================================================

    @Nested
    @DisplayName("Einfache Reward-Typen")
    class SimpleRewardTypeTests {

        @Test
        @DisplayName("NewResidents speichert Menge und Populationslevel korrekt")
        void testNewResidentsRecord() {
            Reward.NewResidents reward = new Reward.NewResidents(3, 2);
            assertEquals(3, reward.amount());
            assertEquals(2, reward.populationLevel());
        }

        @Test
        @DisplayName("UpgradeResidents speichert Menge und beide Level korrekt")
        void testUpgradeResidentsRecord() {
            Reward.UpgradeResidents reward = new Reward.UpgradeResidents(2, 1, 2);
            assertEquals(2, reward.amount());
            assertEquals(1, reward.populationLevel1());
            assertEquals(2, reward.populationLevel2());
        }

        @Test
        @DisplayName("Gold speichert Betrag korrekt")
        void testGoldRecord() {
            Reward.Gold reward = new Reward.Gold(10);
            assertEquals(10, reward.amount());
        }

        @Test
        @DisplayName("TradePoints speichert Punkte korrekt")
        void testTradePointsRecord() {
            Reward.TradePoints reward = new Reward.TradePoints(3);
            assertEquals(3, reward.points());
        }

        @Test
        @DisplayName("ExplorationPoints speichert Punkte korrekt")
        void testExplorationPointsRecord() {
            Reward.ExplorationPoints reward = new Reward.ExplorationPoints(2);
            assertEquals(2, reward.points());
        }

        @Test
        @DisplayName("ExtraAction kann instanziiert werden")
        void testExtraActionInstantiation() {
            assertNotNull(new Reward.ExtraAction());
        }

        @Test
        @DisplayName("ExpeditionCards kann instanziiert werden")
        void testExpeditionCardsInstantiation() {
            assertNotNull(new Reward.ExpeditionCards());
        }

        @Test
        @DisplayName("Verschiedene Gold-Beträge sind unterschiedliche Rewards")
        void testGoldAmountsAreDistinct() {
            Reward.Gold reward5 = new Reward.Gold(5);
            Reward.Gold reward10 = new Reward.Gold(10);
            assertNotEquals(reward5.amount(), reward10.amount());
        }
    }
}
