package com.anno1800.unit.data.gamedata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.anno1800.data.gamedata.Goods;

/**
 * Test suite for Goods.
 */
@DisplayName("Goods Tests")
class GoodsTest {

    @BeforeEach
    void setUp() {
        // Setup test fixtures
    }

    /**
     * Basic sanity check that verifies the dummy test works.
     * This ensures the test infrastructure is properly set up.
     */
    @Test
    @DisplayName("Dummy test - always passes")
    void dummyTest() {
        assertTrue(true, "This test should always pass");
    }

    /**
     * Verifies that all enum constants are properly initialized and not null.
     * This is a defensive check that should always pass for normal Java enums.
     */
    @Test
    void testEnumNotNull() {
        for (Goods good : Goods.values()) {
            assertNotNull(good, "Enum value should not be null");
        }
    }

    /**
     * Verifies that enum names remain unique and stable enough for valueOf lookups.
     */
    @Test
    void testEnumNamesAreUnique() {
        Set<String> enumNames = new HashSet<>();
        for (Goods good : Goods.values()) {
            assertTrue(enumNames.add(good.name()), "Duplicate enum name found: " + good.name());
        }
    }

    /**
     * Tests the valueOf() method with valid display names.
     */
    @Test
    void testValueOf() {
        assertEquals(Goods.PLANKS, Goods.valueOf("PLANKS"));
        assertEquals(Goods.GRAIN, Goods.valueOf("GRAIN"));
        assertEquals(Goods.POTATOES, Goods.valueOf("POTATOES"));
        assertEquals(Goods.PIGS, Goods.valueOf("PIGS"));
        assertEquals(Goods.WOOL, Goods.valueOf("WOOL"));
        assertEquals(Goods.COAL, Goods.valueOf("COAL"));
        assertEquals(Goods.BRICKS, Goods.valueOf("BRICKS"));
        assertEquals(Goods.BEER, Goods.valueOf("BEER"));
        assertEquals(Goods.BREAD, Goods.valueOf("BREAD"));
        assertEquals(Goods.GOODS, Goods.valueOf("GOODS"));
        assertEquals(Goods.STEELBARS, Goods.valueOf("STEELBARS"));
        assertEquals(Goods.SAILS, Goods.valueOf("SAILS"));
        assertEquals(Goods.SNAPS, Goods.valueOf("SNAPS"));
        assertEquals(Goods.GLASS, Goods.valueOf("GLASS"));
        assertEquals(Goods.SAUSAGE, Goods.valueOf("SAUSAGE"));
        assertEquals(Goods.SOAP, Goods.valueOf("SOAP"));
        assertEquals(Goods.CANNED_MEAT, Goods.valueOf("CANNED_MEAT"));
        assertEquals(Goods.WORK_CLOTHES, Goods.valueOf("WORK_CLOTHES"));
        assertEquals(Goods.BRASS, Goods.valueOf("BRASS"));
        assertEquals(Goods.WINDOWS, Goods.valueOf("WINDOWS"));
        assertEquals(Goods.CHAMPAGNE, Goods.valueOf("CHAMPAGNE"));
        assertEquals(Goods.GLASSES, Goods.valueOf("GLASSES"));
        assertEquals(Goods.POCKETWATCHES, Goods.valueOf("POCKETWATCHES"));
        assertEquals(Goods.SEWING_MACHINES, Goods.valueOf("SEWING_MACHINES"));
        assertEquals(Goods.COTTON_FABRIC, Goods.valueOf("COTTON_FABRIC"));
        assertEquals(Goods.COFFEE, Goods.valueOf("COFFEE"));
        assertEquals(Goods.COATS, Goods.valueOf("COATS"));
        assertEquals(Goods.DYNAMITE, Goods.valueOf("DYNAMITE"));
        assertEquals(Goods.CANNONS, Goods.valueOf("CANNONS"));
        assertEquals(Goods.RUM, Goods.valueOf("RUM"));
        assertEquals(Goods.CIGARS, Goods.valueOf("CIGARS"));
        assertEquals(Goods.CHOCOLATE, Goods.valueOf("CHOCOLATE"));
        assertEquals(Goods.WORKFORCE_3, Goods.valueOf("WORKFORCE_3"));
        assertEquals(Goods.STEAM_GEARS, Goods.valueOf("STEAM_GEARS"));
        assertEquals(Goods.CARS, Goods.valueOf("CARS"));
        assertEquals(Goods.HIGHBIKES, Goods.valueOf("HIGHBIKES"));
        assertEquals(Goods.LIGHT_BULBS, Goods.valueOf("LIGHT_BULBS"));
        assertEquals(Goods.GRAMOPHONES, Goods.valueOf("GRAMOPHONES"));
        assertEquals(Goods.BIG_BERTA, Goods.valueOf("BIG_BERTA"));
        assertEquals(Goods.WORKFORCE_4, Goods.valueOf("WORKFORCE_4"));
        assertEquals(Goods.WORKFORCE_5, Goods.valueOf("WORKFORCE_5"));
        assertEquals(Goods.CACAO, Goods.valueOf("CACAO"));
        assertEquals(Goods.SUGARCANE, Goods.valueOf("SUGARCANE"));
        assertEquals(Goods.TOBACCO, Goods.valueOf("TOBACCO"));
        assertEquals(Goods.COFFEE_BEANS, Goods.valueOf("COFFEE_BEANS"));
        assertEquals(Goods.COTTON, Goods.valueOf("COTTON"));
        assertEquals(Goods.RUBBER, Goods.valueOf("RUBBER"));
        assertEquals(Goods.EXPLORATIONCHIP, Goods.valueOf("EXPLORATIONCHIP"));
        assertEquals(Goods.TRADECHIP, Goods.valueOf("TRADECHIP"));
    }

    /**
     * Tests that valueOf() throws IllegalArgumentException for invalid enum names.
     * This ensures proper error handling when trying to convert invalid strings to enum values.
     */
    @Test
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            Goods.valueOf("INVALID_GOOD");
        });
    }

    /**
     * Tests that specific goods have the correct display names.
     * Verifies the human-readable names match expectations.
     */
    @Test
    void testDisplayName() {
        assertEquals("Planks", Goods.PLANKS.getDisplayName());
        assertEquals("Steel Bars", Goods.STEELBARS.getDisplayName());
        assertEquals("Exploration Chip", Goods.EXPLORATIONCHIP.getDisplayName());
    }

    /**
     * Verifies that all goods have non-null and non-empty display names.
     * This ensures every good can be properly displayed in the UI.
     */
    @Test
    void testAllDisplayNamesNotNull() {
        for (Goods good : Goods.values()) {
            assertNotNull(good.getDisplayName(), 
                good.name() + " should have a display name");
            assertFalse(good.getDisplayName().isEmpty(), 
                good.name() + " display name should not be empty");
        }
    }

    /**
     * Tests that toString() returns the display name.
     * This is important for debugging and logging output.
     */
    @Test
    void testToString() {
        assertEquals("Planks", Goods.PLANKS.toString());
        assertEquals("Coal", Goods.COAL.toString());
    }

    /**
     * Verifies that all green (basic) materials are defined in the enum.
     * Green materials are the basic tier goods in Anno 1800.
     */
    @Test
    void testGreenMaterials() {
        // Test dass grüne Materialien existieren
        assertNotNull(Goods.PLANKS);
        assertNotNull(Goods.GRAIN);
        assertNotNull(Goods.POTATOES);
        assertNotNull(Goods.PIGS);
        assertNotNull(Goods.WOOL);
    }

    /**
     * Verifies that all New World specific goods are defined in the enum.
     * These materials can only be produced in the New World region.
     */
    @Test
    void testNewWorldMaterials() {
        // Test New World spezifische Güter
        assertNotNull(Goods.CACAO);
        assertNotNull(Goods.SUGARCANE);
        assertNotNull(Goods.TOBACCO);
        assertNotNull(Goods.COFFEE_BEANS);
        assertNotNull(Goods.COTTON);
        assertNotNull(Goods.RUBBER);
    }

    /**
     * Verifies that ship-related special goods (chips) are defined.
     * These are special tokens used for exploration and trade mechanics.
     */
    @Test
    void testShipGoods() {
        assertNotNull(Goods.EXPLORATIONCHIP);
        assertNotNull(Goods.TRADECHIP);
    }
}
