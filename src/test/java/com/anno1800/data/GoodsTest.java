package com.anno1800.data;

import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Goods enum.
 * Verifies that all goods have correct display names and that the enum is complete.
 */
@DisplayName("Goods Enum Tests")
class GoodsTest {

    @Test
    @DisplayName("Alle Waren haben einen nicht-leeren Anzeigenamen")
    void testAllGoodsHaveNonBlankDisplayName() {
        for (Goods good : Goods.values()) {
            assertNotNull(good.getDisplayName(),
                good.name() + " sollte einen nicht-null Anzeigenamen haben");
            assertFalse(good.getDisplayName().isBlank(),
                good.name() + " sollte keinen leeren Anzeigenamen haben");
        }
    }

    @Test
    @DisplayName("toString() gibt den Anzeigenamen zurück")
    void testToStringReturnsDisplayName() {
        assertEquals("Planks", Goods.PLANKS.toString());
        assertEquals("Coal", Goods.COAL.toString());
        assertEquals("Beer", Goods.BEER.toString());
        assertEquals("Steel Bars", Goods.STEELBARS.toString());
        assertEquals("Cacao", Goods.CACAO.toString());
    }

    @Test
    @DisplayName("Basismaterialien (grün) sind vorhanden")
    void testGreenGoodsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("PLANKS"));
        assertDoesNotThrow(() -> Goods.valueOf("GRAIN"));
        assertDoesNotThrow(() -> Goods.valueOf("POTATOES"));
        assertDoesNotThrow(() -> Goods.valueOf("PIGS"));
        assertDoesNotThrow(() -> Goods.valueOf("WOOL"));
    }

    @Test
    @DisplayName("Verarbeitungsgüter (blau) sind vorhanden")
    void testBlueGoodsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("COAL"));
        assertDoesNotThrow(() -> Goods.valueOf("BRICKS"));
        assertDoesNotThrow(() -> Goods.valueOf("BEER"));
        assertDoesNotThrow(() -> Goods.valueOf("BREAD"));
        assertDoesNotThrow(() -> Goods.valueOf("STEELBARS"));
        assertDoesNotThrow(() -> Goods.valueOf("SAILS"));
        assertDoesNotThrow(() -> Goods.valueOf("SOAP"));
        assertDoesNotThrow(() -> Goods.valueOf("CANNED_MEAT"));
    }

    @Test
    @DisplayName("Neue-Welt-Materialien sind vorhanden")
    void testNewWorldGoodsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("CACAO"));
        assertDoesNotThrow(() -> Goods.valueOf("SUGARCANE"));
        assertDoesNotThrow(() -> Goods.valueOf("TOBACCO"));
        assertDoesNotThrow(() -> Goods.valueOf("COFFEE_BEANS"));
        assertDoesNotThrow(() -> Goods.valueOf("COTTON"));
        assertDoesNotThrow(() -> Goods.valueOf("RUBBER"));
    }

    @Test
    @DisplayName("Schiffs-Chips sind vorhanden")
    void testShipChipsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("EXPLORATIONCHIP"));
        assertDoesNotThrow(() -> Goods.valueOf("TRADECHIP"));
    }

    @Test
    @DisplayName("Arbeitskraft-Güter (WORKFORCE) sind vorhanden")
    void testWorkforceGoodsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("WORKFORCE_3"));
        assertDoesNotThrow(() -> Goods.valueOf("WORKFORCE_4"));
        assertDoesNotThrow(() -> Goods.valueOf("WORKFORCE_5"));
    }

    @Test
    @DisplayName("Ungültiger Warenname wirft IllegalArgumentException")
    void testInvalidGoodNameThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Goods.valueOf("INVALID_GOOD_THAT_DOES_NOT_EXIST"));
    }

    @Test
    @DisplayName("Luxusgüter (rot/lila) sind vorhanden")
    void testLuxuryGoodsExist() {
        assertDoesNotThrow(() -> Goods.valueOf("CHAMPAGNE"));
        assertDoesNotThrow(() -> Goods.valueOf("POCKETWATCHES"));
        assertDoesNotThrow(() -> Goods.valueOf("CIGARS"));
        assertDoesNotThrow(() -> Goods.valueOf("CHOCOLATE"));
        assertDoesNotThrow(() -> Goods.valueOf("STEAM_GEARS"));
        assertDoesNotThrow(() -> Goods.valueOf("CARS"));
    }
}
