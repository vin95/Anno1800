package com.anno1800.data;

import com.anno1800.game.tiles.ShipCosts;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShipCosts.
 * Verifies shipyard and ship construction costs for all valid levels,
 * and that invalid levels throw exceptions.
 */
@DisplayName("ShipCosts Tests")
class ShipCostsTest {

    // =========================================================================
    // Shipyard Costs
    // =========================================================================

    @Nested
    @DisplayName("Werftbaukosten (getShipyardCost)")
    class ShipyardCostTests {

        @Test
        @DisplayName("Werft Level 1 hat keine Baukosten")
        void testLevel1ShipyardHasNoCost() {
            Goods[] cost = ShipCosts.getShipyardCost(1);
            assertNotNull(cost);
            assertEquals(0, cost.length, "Level-1-Werft sollte keine Güterkosten haben");
        }

        @Test
        @DisplayName("Werft Level 2 benötigt 3 Güter")
        void testLevel2ShipyardCostHasThreeGoods() {
            Goods[] cost = ShipCosts.getShipyardCost(2);
            assertNotNull(cost);
            assertEquals(3, cost.length);
        }

        @Test
        @DisplayName("Werft Level 3 benötigt 4 Güter")
        void testLevel3ShipyardCostHasFourGoods() {
            Goods[] cost = ShipCosts.getShipyardCost(3);
            assertNotNull(cost);
            assertEquals(4, cost.length);
        }

        @Test
        @DisplayName("Werft Level 2 enthält PLANKS, BRICKS, STEELBARS")
        void testLevel2ShipyardCostContents() {
            Goods[] cost = ShipCosts.getShipyardCost(2);
            assertContains(cost, Goods.PLANKS);
            assertContains(cost, Goods.BRICKS);
            assertContains(cost, Goods.STEELBARS);
        }

        @Test
        @DisplayName("Werft Level 3 enthält PLANKS, BRICKS, STEELBARS, WINDOWS")
        void testLevel3ShipyardCostContents() {
            Goods[] cost = ShipCosts.getShipyardCost(3);
            assertContains(cost, Goods.PLANKS);
            assertContains(cost, Goods.BRICKS);
            assertContains(cost, Goods.STEELBARS);
            assertContains(cost, Goods.WINDOWS);
        }

        @Test
        @DisplayName("Ungültiger Werftstufenwert 0 wirft IllegalArgumentException")
        void testLevel0ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipyardCost(0));
        }

        @Test
        @DisplayName("Ungültiger Werftstufenwert 4 wirft IllegalArgumentException")
        void testLevel4ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipyardCost(4));
        }

        @Test
        @DisplayName("Negativer Werftstufenwert wirft IllegalArgumentException")
        void testNegativeLevelThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipyardCost(-1));
        }
    }

    // =========================================================================
    // Ship Costs
    // =========================================================================

    @Nested
    @DisplayName("Schiffsbaukosten (getShipCost)")
    class ShipCostTests {

        @Test
        @DisplayName("Schiff Level 1: PLANKS + SAILS (2 Güter)")
        void testLevel1ShipCostIsPlanksAndSails() {
            Goods[] cost = ShipCosts.getShipCost(1);
            assertNotNull(cost);
            assertEquals(2, cost.length);
            assertContains(cost, Goods.PLANKS);
            assertContains(cost, Goods.SAILS);
        }

        @Test
        @DisplayName("Schiff Level 2: 3 Güter (inkl. STEELBARS)")
        void testLevel2ShipCostHasThreeGoods() {
            Goods[] cost = ShipCosts.getShipCost(2);
            assertNotNull(cost);
            assertEquals(3, cost.length);
            assertContains(cost, Goods.PLANKS);
            assertContains(cost, Goods.SAILS);
            assertContains(cost, Goods.STEELBARS);
        }

        @Test
        @DisplayName("Schiff Level 3: 4 Güter (inkl. CANNONS)")
        void testLevel3ShipCostHasFourGoodsIncludingCannons() {
            Goods[] cost = ShipCosts.getShipCost(3);
            assertNotNull(cost);
            assertEquals(4, cost.length);
            assertContains(cost, Goods.CANNONS);
        }

        @Test
        @DisplayName("Schiff Level 3 enthält PLANKS, SAILS, STEELBARS, CANNONS")
        void testLevel3ShipCostContents() {
            Goods[] cost = ShipCosts.getShipCost(3);
            assertContains(cost, Goods.PLANKS);
            assertContains(cost, Goods.SAILS);
            assertContains(cost, Goods.STEELBARS);
            assertContains(cost, Goods.CANNONS);
        }

        @Test
        @DisplayName("Ungültige Schiffsstufe 0 wirft IllegalArgumentException")
        void testLevel0ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipCost(0));
        }

        @Test
        @DisplayName("Ungültige Schiffsstufe 4 wirft IllegalArgumentException")
        void testLevel4ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipCost(4));
        }

        @Test
        @DisplayName("Negative Schiffsstufe wirft IllegalArgumentException")
        void testNegativeLevelThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> ShipCosts.getShipCost(-1));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void assertContains(Goods[] array, Goods expected) {
        for (Goods g : array) {
            if (g == expected) return;
        }
        fail("Array enthält nicht " + expected + " – tatsächlich: " + java.util.Arrays.toString(array));
    }
}
