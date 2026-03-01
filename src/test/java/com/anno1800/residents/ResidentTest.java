package com.anno1800.residents;

import com.anno1800.game.residents.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all Resident subtypes (Farmer, Worker, Artisan, Engineer, Investor).
 * Covers population levels, status transitions, exhaust/recover lifecycle,
 * and recover costs.
 */
@DisplayName("Resident Tests")
class ResidentTest {

    // =========================================================================
    // Population Levels
    // =========================================================================

    @Nested
    @DisplayName("Populationsstufen")
    class PopulationLevelTests {

        @Test
        @DisplayName("Farmer hat Populationsstufe 1")
        void testFarmerLevel() {
            assertEquals(1, new Farmer().getPopulationLevel());
        }

        @Test
        @DisplayName("Worker hat Populationsstufe 2")
        void testWorkerLevel() {
            assertEquals(2, new Worker().getPopulationLevel());
        }

        @Test
        @DisplayName("Artisan hat Populationsstufe 3")
        void testArtisanLevel() {
            assertEquals(3, new Artisan().getPopulationLevel());
        }

        @Test
        @DisplayName("Engineer hat Populationsstufe 4")
        void testEngineerLevel() {
            assertEquals(4, new Engineer().getPopulationLevel());
        }

        @Test
        @DisplayName("Investor hat Populationsstufe 5")
        void testInvestorLevel() {
            assertEquals(5, new Investor().getPopulationLevel());
        }
    }

    // =========================================================================
    // Initial Status
    // =========================================================================

    @Nested
    @DisplayName("Initialer Status")
    class InitialStatusTests {

        @Test
        @DisplayName("Neuer Farmer hat Status ON_BOARD")
        void testFarmerInitialStatus() {
            assertEquals(ResidentStatus.ON_BOARD, new Farmer().getStatus());
        }

        @Test
        @DisplayName("Neuer Investor hat Status ON_BOARD")
        void testInvestorInitialStatus() {
            assertEquals(ResidentStatus.ON_BOARD, new Investor().getStatus());
        }

        @Test
        @DisplayName("Alle neuen Einwohner starten mit ON_BOARD")
        void testAllResidentsInitiallyOnBoard() {
            Resident[] allResidents = {
                new Farmer(), new Worker(), new Artisan(), new Engineer(), new Investor()
            };
            for (Resident r : allResidents) {
                assertEquals(ResidentStatus.ON_BOARD, r.getStatus(),
                    r.getClass().getSimpleName() + " sollte ON_BOARD-Status haben");
            }
        }
    }

    // =========================================================================
    // Status Transitions
    // =========================================================================

    @Nested
    @DisplayName("Statusübergänge")
    class StatusTransitionTests {

        @Test
        @DisplayName("exhaust() setzt Status auf EXHAUSTED")
        void testExhaustSetsExhaustedStatus() {
            Farmer farmer = new Farmer();
            farmer.setStatus(ResidentStatus.FIT);
            farmer.exhaust();
            assertEquals(ResidentStatus.EXHAUSTED, farmer.getStatus());
        }

        @Test
        @DisplayName("exhaust() → isExhausted() gibt true zurück")
        void testExhaustedIsExhausted() {
            Worker worker = new Worker();
            worker.setStatus(ResidentStatus.FIT);
            worker.exhaust();
            assertTrue(worker.isExhausted());
        }

        @Test
        @DisplayName("recover() setzt Status auf FIT")
        void testRecoverSetsFitStatus() {
            Artisan artisan = new Artisan();
            artisan.setStatus(ResidentStatus.EXHAUSTED);
            artisan.recover();
            assertEquals(ResidentStatus.FIT, artisan.getStatus());
        }

        @Test
        @DisplayName("recover() → isFit() gibt true zurück")
        void testRecoveredIsFit() {
            Engineer engineer = new Engineer();
            engineer.setStatus(ResidentStatus.EXHAUSTED);
            engineer.recover();
            assertTrue(engineer.isFit());
        }

        @Test
        @DisplayName("ON_BOARD-Einwohner ist nicht FIT")
        void testOnBoardIsNotFit() {
            assertFalse(new Farmer().isFit(), "ON_BOARD-Einwohner sollte nicht FIT sein");
        }

        @Test
        @DisplayName("ON_BOARD-Einwohner ist nicht EXHAUSTED")
        void testOnBoardIsNotExhausted() {
            assertFalse(new Investor().isExhausted(), "ON_BOARD-Einwohner sollte nicht EXHAUSTED sein");
        }

        @Test
        @DisplayName("FIT-Einwohner ist nicht EXHAUSTED")
        void testFitIsNotExhausted() {
            Farmer farmer = new Farmer();
            farmer.setStatus(ResidentStatus.FIT);
            assertFalse(farmer.isExhausted());
        }

        @Test
        @DisplayName("EXHAUSTED-Einwohner ist nicht FIT")
        void testExhaustedIsNotFit() {
            Farmer farmer = new Farmer();
            farmer.setStatus(ResidentStatus.EXHAUSTED);
            assertFalse(farmer.isFit());
        }

        @Test
        @DisplayName("setStatus() akzeptiert alle ResidentStatus-Werte")
        void testSetAllStatusValues() {
            Farmer farmer = new Farmer();
            for (ResidentStatus status : ResidentStatus.values()) {
                assertDoesNotThrow(() -> farmer.setStatus(status));
                assertEquals(status, farmer.getStatus());
            }
        }

        @Test
        @DisplayName("Exhaust → Recover: vollständiger Lebenszyklus")
        void testFullExhaustRecoverCycle() {
            Worker worker = new Worker();
            worker.setStatus(ResidentStatus.FIT);

            assertTrue(worker.isFit());
            worker.exhaust();
            assertTrue(worker.isExhausted());
            assertFalse(worker.isFit());
            worker.recover();
            assertTrue(worker.isFit());
            assertFalse(worker.isExhausted());
        }
    }

    // =========================================================================
    // Recover Costs
    // =========================================================================

    @Nested
    @DisplayName("Wiederherstellungskosten (getRecoverCost)")
    class RecoverCostTests {

        @Test
        @DisplayName("Farmer-Wiederherstellungskosten sind nicht null")
        void testFarmerRecoverCostNotNull() {
            assertNotNull(new Farmer().getRecoverCost());
        }

        @Test
        @DisplayName("Farmer: Wiederherstellungskosten = 1 Gut (PLANKS)")
        void testFarmerRecoverCostIsPlanks() {
            ResidentCosts.Cost cost = new Farmer().getRecoverCost();
            assertEquals(1, cost.goods().length);
        }

        @Test
        @DisplayName("Investor: Wiederherstellungskosten = 6 Güter (Level-5-Siedlungskosten)")
        void testInvestorRecoverCostHasSixGoods() {
            ResidentCosts.Cost cost = new Investor().getRecoverCost();
            assertEquals(6, cost.goods().length);
        }

        @Test
        @DisplayName("Alle Einwohner haben nicht-null Wiederherstellungskosten")
        void testAllResidentsHaveRecoverCost() {
            Resident[] allResidents = {
                new Farmer(), new Worker(), new Artisan(), new Engineer(), new Investor()
            };
            for (Resident r : allResidents) {
                assertNotNull(r.getRecoverCost(),
                    r.getClass().getSimpleName() + " sollte Wiederherstellungskosten haben");
                assertNotNull(r.getRecoverCost().goods());
            }
        }
    }
}
