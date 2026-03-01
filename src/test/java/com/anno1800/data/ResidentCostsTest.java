package com.anno1800.data;

import com.anno1800.game.residents.ResidentCosts;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResidentCosts.
 * Verifies settlement and upgrade costs for all population levels,
 * and that invalid levels throw exceptions.
 */
@DisplayName("ResidentCosts Tests")
class ResidentCostsTest {

    // =========================================================================
    // Settlement Costs
    // =========================================================================

    @Nested
    @DisplayName("Siedlungskosten (getSettlementCost)")
    class SettlementCostTests {

        @Test
        @DisplayName("Level 1 (Farmer): nur PLANKS")
        void testLevel1CostIsPlanks() {
            ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(1);
            assertNotNull(cost);
            assertEquals(1, cost.goods().length, "Farmer braucht genau 1 Gut");
            assertEquals(Goods.PLANKS, cost.goods()[0]);
        }

        @Test
        @DisplayName("Level 2 (Worker): 2 Güter")
        void testLevel2CostHasTwoGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(2);
            assertNotNull(cost);
            assertEquals(2, cost.goods().length);
        }

        @Test
        @DisplayName("Level 3 (Artisan): 3 Güter")
        void testLevel3CostHasThreeGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(3);
            assertNotNull(cost);
            assertEquals(3, cost.goods().length);
        }

        @Test
        @DisplayName("Level 4 (Engineer): 4 Güter")
        void testLevel4CostHasFourGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(4);
            assertNotNull(cost);
            assertEquals(4, cost.goods().length);
        }

        @Test
        @DisplayName("Level 5 (Investor): 6 Güter")
        void testLevel5CostHasSixGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(5);
            assertNotNull(cost);
            assertEquals(6, cost.goods().length);
        }

        @Test
        @DisplayName("Alle Kostenarrays sind nicht null und nicht leer")
        void testAllCostArraysNonNull() {
            for (int level = 1; level <= 5; level++) {
                ResidentCosts.Cost cost = ResidentCosts.getSettlementCost(level);
                assertNotNull(cost, "Cost for level " + level + " should not be null");
                assertNotNull(cost.goods(), "Goods array for level " + level + " should not be null");
            }
        }

        @Test
        @DisplayName("Level 0 wirft IllegalArgumentException")
        void testLevel0ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getSettlementCost(0));
        }

        @Test
        @DisplayName("Level 6 wirft IllegalArgumentException")
        void testLevel6ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getSettlementCost(6));
        }

        @Test
        @DisplayName("Negativer Level wirft IllegalArgumentException")
        void testNegativeLevelThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getSettlementCost(-1));
        }
    }

    // =========================================================================
    // Upgrade Costs
    // =========================================================================

    @Nested
    @DisplayName("Upgradekosten (getUpgradeCost)")
    class UpgradeCostTests {

        @Test
        @DisplayName("Upgrade auf Level 2: BRICKS")
        void testUpgradeToLevel2IsBricks() {
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(2);
            assertNotNull(cost);
            assertEquals(1, cost.goods().length);
            assertEquals(Goods.BRICKS, cost.goods()[0]);
        }

        @Test
        @DisplayName("Upgrade auf Level 3: 2 Güter")
        void testUpgradeToLevel3HasTwoGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(3);
            assertNotNull(cost);
            assertEquals(2, cost.goods().length);
        }

        @Test
        @DisplayName("Upgrade auf Level 4: 2 Güter (WINDOWS + STEELBARS)")
        void testUpgradeToLevel4HasTwoGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(4);
            assertNotNull(cost);
            assertEquals(2, cost.goods().length);
        }

        @Test
        @DisplayName("Upgrade auf Level 5: 2 Güter (WORKFORCE_3 + COATS)")
        void testUpgradeToLevel5HasTwoGoods() {
            ResidentCosts.Cost cost = ResidentCosts.getUpgradeCost(5);
            assertNotNull(cost);
            assertEquals(2, cost.goods().length);
        }

        @Test
        @DisplayName("Upgrade auf Level 1 wirft IllegalArgumentException (kein Upgrade auf 1)")
        void testUpgradeToLevel1ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getUpgradeCost(1));
        }

        @Test
        @DisplayName("Upgrade auf Level 6 wirft IllegalArgumentException")
        void testUpgradeToLevel6ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getUpgradeCost(6));
        }

        @Test
        @DisplayName("Upgrade auf Level 0 wirft IllegalArgumentException")
        void testUpgradeToLevel0ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ResidentCosts.getUpgradeCost(0));
        }
    }

    // =========================================================================
    // Collection methods
    // =========================================================================

    @Test
    @DisplayName("getAllSettlementCosts() gibt genau 5 Einträge zurück")
    void testGetAllSettlementCostsSize() {
        assertEquals(5, ResidentCosts.getAllSettlementCosts().size());
    }

    @Test
    @DisplayName("getAllUpgradeCosts() gibt genau 4 Einträge zurück")
    void testGetAllUpgradeCostsSize() {
        assertEquals(4, ResidentCosts.getAllUpgradeCosts().size());
    }

    @Test
    @DisplayName("getAllSettlementCosts() enthält Level 1-5 als Schlüssel")
    void testGetAllSettlementCostsKeys() {
        var costs = ResidentCosts.getAllSettlementCosts();
        for (int level = 1; level <= 5; level++) {
            assertTrue(costs.containsKey(level), "Level " + level + " sollte in der Map enthalten sein");
        }
    }

    @Test
    @DisplayName("getAllUpgradeCosts() enthält Level 2-5 als Schlüssel")
    void testGetAllUpgradeCostsKeys() {
        var costs = ResidentCosts.getAllUpgradeCosts();
        for (int level = 2; level <= 5; level++) {
            assertTrue(costs.containsKey(level), "Level " + level + " sollte in der Map enthalten sein");
        }
    }
}
