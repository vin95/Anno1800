package com.anno1800.player;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.rewards.Reward;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlayerBoard.
 * Covers tile counting, trade tracking, free action flags and pending rewards.
 * Uses only the public API of PlayerBoard.
 */
@DisplayName("PlayerBoard Tests")
class PlayerBoardTest {

    private PlayerBoard board;

    @BeforeEach
    void setUp() {
        board = new PlayerBoard();
    }

    // =========================================================================
    // Kachel-Kapazität
    // =========================================================================

    @Nested
    @DisplayName("Kachelzählung (Tiles)")
    class TileCountingTests {

        @Test
        @DisplayName("Initiale Landkacheln = 10")
        void testInitialLandTiles() {
            assertEquals(10, board.getLandTiles());
        }

        @Test
        @DisplayName("Initiale Küstenkacheln = 5")
        void testInitialCoastTiles() {
            assertEquals(5, board.getCoastTiles());
        }

        @Test
        @DisplayName("Initiale Seekacheln = 5")
        void testInitialSeaTiles() {
            assertEquals(5, board.getSeaTiles());
        }

        @Test
        @DisplayName("Neues PlayerBoard: 0 freie Landkacheln (alle von Start-Fabriken belegt)")
        void testInitialFreeLandTiles() {
            assertEquals(0, board.getFreeLandTiles(),
                "Alle Landkacheln sollten von Start-Fabriken (10x) belegt sein");
        }

        @Test
        @DisplayName("Neues PlayerBoard: 5 freie Küstenkacheln (keine Werften)")
        void testInitialFreeCoastTiles() {
            assertEquals(5, board.getFreeCoastTiles());
        }

        @Test
        @DisplayName("Neues PlayerBoard: 5 freie Seekacheln (keine Schiffe)")
        void testInitialFreeSeaTiles() {
            assertEquals(5, board.getFreeSeaTiles());
        }

        @Test
        @DisplayName("FreeLandTiles = LandTiles - NumFactoriesOnLand")
        void testFreeLandTilesCalculation() {
            int expected = board.getLandTiles() - board.getNumFactoriesOnLand();
            assertEquals(expected, board.getFreeLandTiles());
        }
    }

    // =========================================================================
    // Handel-Tracking
    // =========================================================================

    @Nested
    @DisplayName("Handel-Tracking (Trade Tracking)")
    class TradeTrackingTests {

        @Test
        @DisplayName("Anfangs kein Gut als gehandelt markiert")
        void testInitiallyNoTradedGoods() {
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.PLANKS));
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.COAL));
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.BEER));
        }

        @Test
        @DisplayName("Nach registerTradedGood(): hasAlreadyTradedThisTurn() true")
        void testRegisterTradeMarksTradedGood() {
            board.registerTradedGood(Goods.PLANKS);
            assertTrue(board.hasAlreadyTradedThisTurn(Goods.PLANKS));
        }

        @Test
        @DisplayName("Registriertes Gut beeinflusst andere Güter nicht")
        void testRegisteringOneGoodDoesNotAffectOthers() {
            board.registerTradedGood(Goods.PLANKS);
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.COAL));
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.BEER));
        }

        @Test
        @DisplayName("Mehrere Güter können im selben Zug gehandelt werden")
        void testMultipleGoodsCanBeRegistered() {
            board.registerTradedGood(Goods.PLANKS);
            board.registerTradedGood(Goods.COAL);
            board.registerTradedGood(Goods.BEER);
            assertTrue(board.hasAlreadyTradedThisTurn(Goods.PLANKS));
            assertTrue(board.hasAlreadyTradedThisTurn(Goods.COAL));
            assertTrue(board.hasAlreadyTradedThisTurn(Goods.BEER));
        }

        @Test
        @DisplayName("clearTradedGoodsThisTurn() setzt Tracking zurück")
        void testClearTradedGoodsResetTracking() {
            board.registerTradedGood(Goods.PLANKS);
            board.registerTradedGood(Goods.COAL);
            board.clearTradedGoodsThisTurn();
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.PLANKS));
            assertFalse(board.hasAlreadyTradedThisTurn(Goods.COAL));
        }

        @Test
        @DisplayName("getTradedGoodsThisTurn() gibt korrekte Menge zurück")
        void testGetTradedGoodsReturnsCorrectSet() {
            board.registerTradedGood(Goods.PLANKS);
            board.registerTradedGood(Goods.BEER);
            var traded = board.getTradedGoodsThisTurn();
            assertEquals(2, traded.size());
            assertTrue(traded.contains(Goods.PLANKS));
            assertTrue(traded.contains(Goods.BEER));
        }

        @Test
        @DisplayName("Doppeltes registerTradedGood() zählt nur einmal (Set-Semantik)")
        void testDoubleRegisterCounted_Once() {
            board.registerTradedGood(Goods.PLANKS);
            board.registerTradedGood(Goods.PLANKS);
            assertEquals(1, board.getTradedGoodsThisTurn().size());
        }

        @Test
        @DisplayName("Nach clear() ist der Satz leer")
        void testTradedGoodsEmptyAfterClear() {
            board.registerTradedGood(Goods.COAL);
            board.clearTradedGoodsThisTurn();
            assertTrue(board.getTradedGoodsThisTurn().isEmpty());
        }
    }

    // =========================================================================
    // Freie-Aktion-Flags
    // =========================================================================

    @Nested
    @DisplayName("Freie-Aktion-Flags")
    class FreeActionFlagTests {

        @Test
        @DisplayName("ExtraAction-Flag initial false")
        void testExtraActionFlagInitiallyFalse() {
            assertFalse(board.hasUsedExtraActionThisTurn());
        }

        @Test
        @DisplayName("Nach markExtraActionUsed(): Flag ist true")
        void testMarkExtraActionUsedSetsFlag() {
            board.markExtraActionUsed();
            assertTrue(board.hasUsedExtraActionThisTurn());
        }

        @Test
        @DisplayName("DiscardResidentCard-Flag initial false")
        void testDiscardResidentCardFlagInitiallyFalse() {
            assertFalse(board.hasUsedDiscardResidentCardThisTurn());
        }

        @Test
        @DisplayName("Nach markDiscardResidentCardUsed(): Flag ist true")
        void testMarkDiscardResidentCardUsedSetsFlag() {
            board.markDiscardResidentCardUsed();
            assertTrue(board.hasUsedDiscardResidentCardThisTurn());
        }

        @Test
        @DisplayName("InvestorGold-Flag initial false")
        void testInvestorGoldFlagInitiallyFalse() {
            assertFalse(board.hasUsedInvestorGoldThisTurn());
        }

        @Test
        @DisplayName("Nach markInvestorGoldUsed(): Flag ist true")
        void testMarkInvestorGoldUsedSetsFlag() {
            board.markInvestorGoldUsed();
            assertTrue(board.hasUsedInvestorGoldThisTurn());
        }

        @Test
        @DisplayName("clearFreeActionFlagsThisTurn() setzt alle drei Flags zurück")
        void testClearAllFlagsResetsAll() {
            board.markExtraActionUsed();
            board.markDiscardResidentCardUsed();
            board.markInvestorGoldUsed();

            board.clearFreeActionFlagsThisTurn();

            assertFalse(board.hasUsedExtraActionThisTurn(), "ExtraAction-Flag sollte zurückgesetzt sein");
            assertFalse(board.hasUsedDiscardResidentCardThisTurn(), "DiscardResidentCard-Flag sollte zurückgesetzt sein");
            assertFalse(board.hasUsedInvestorGoldThisTurn(), "InvestorGold-Flag sollte zurückgesetzt sein");
        }

        @Test
        @DisplayName("clear lässt Flags unabhängig zurücksetzen (nicht nur das erste)")
        void testFlagsAreIndependentlyReset() {
            board.markExtraActionUsed();
            // Nur ExtraAction markiert, dann clear
            board.clearFreeActionFlagsThisTurn();
            // Alle sollten jetzt false sein
            assertFalse(board.hasUsedExtraActionThisTurn());
            assertFalse(board.hasUsedDiscardResidentCardThisTurn());
            assertFalse(board.hasUsedInvestorGoldThisTurn());
        }
    }

    // =========================================================================
    // Ausstehende Belohnungen
    // =========================================================================

    @Nested
    @DisplayName("Ausstehende Belohnungen (Pending Rewards)")
    class PendingRewardTests {

        @Test
        @DisplayName("Anfangs keine ausstehenden Belohnungen")
        void testInitiallyNoPendingRewards() {
            assertTrue(board.getPendingRewards().isEmpty());
        }

        @Test
        @DisplayName("addPendingReward() fügt zur Liste hinzu")
        void testAddPendingRewardAddsToList() {
            Reward reward = new Reward.Gold(5);
            board.addPendingReward(reward);
            assertEquals(1, board.getPendingRewards().size());
            assertTrue(board.getPendingRewards().contains(reward));
        }

        @Test
        @DisplayName("Mehrere Belohnungen können hinzugefügt werden")
        void testMultipleRewardsCanBeAdded() {
            board.addPendingReward(new Reward.Gold(1));
            board.addPendingReward(new Reward.Gold(2));
            board.addPendingReward(new Reward.ExtraAction());
            assertEquals(3, board.getPendingRewards().size());
        }

        @Test
        @DisplayName("removePendingReward() entfernt die Belohnung und gibt true zurück")
        void testRemovePendingRewardSucceeds() {
            Reward reward = new Reward.Gold(5);
            board.addPendingReward(reward);
            boolean removed = board.removePendingReward(reward);
            assertTrue(removed);
            assertTrue(board.getPendingRewards().isEmpty());
        }

        @Test
        @DisplayName("removePendingReward() auf nicht vorhandene Belohnung gibt false zurück")
        void testRemoveNonExistentRewardReturnsFalse() {
            Reward reward = new Reward.Gold(5);
            boolean removed = board.removePendingReward(reward);
            assertFalse(removed);
        }

        @Test
        @DisplayName("Belohnungen werden in Reihenfolge des Hinzufügens gespeichert")
        void testRewardsOrderIsPreserved() {
            Reward r1 = new Reward.Gold(1);
            Reward r2 = new Reward.Gold(2);
            Reward r3 = new Reward.ExtraAction();
            board.addPendingReward(r1);
            board.addPendingReward(r2);
            board.addPendingReward(r3);
            assertEquals(r1, board.getPendingRewards().get(0));
            assertEquals(r2, board.getPendingRewards().get(1));
            assertEquals(r3, board.getPendingRewards().get(2));
        }
    }

    // =========================================================================
    // Anfangszustand
    // =========================================================================

    @Nested
    @DisplayName("Anfangszustand nach Konstruktor")
    class InitialStateTests {

        @Test
        @DisplayName("Gold initial 0")
        void testInitialGoldIsZero() {
            assertEquals(0, board.getGold());
        }

        @Test
        @DisplayName("Trade Chips initial 0")
        void testInitialTradeChipsZero() {
            assertEquals(0, board.getAvailableTradeChips());
        }

        @Test
        @DisplayName("Explorer Chips initial 0")
        void testInitialExplorerChipsZero() {
            assertEquals(0, board.getAvailableExplorerChips());
        }

        @Test
        @DisplayName("Schiffe initial 0")
        void testInitialShipsZero() {
            assertEquals(0, board.getNumShips());
        }

        @Test
        @DisplayName("Werften initial 0")
        void testInitialShipyardsZero() {
            assertEquals(0, board.getNumShipyards());
        }

        @Test
        @DisplayName("Einwohnerliste initial leer")
        void testInitialResidentsEmpty() {
            assertTrue(board.getResidents().isEmpty());
        }

        @Test
        @DisplayName("Einwohnerkartenliste initial leer")
        void testInitialResidentCardsEmpty() {
            assertTrue(board.getResidentCards().isEmpty());
        }

        @Test
        @DisplayName("Expeditionskartenliste initial leer")
        void testInitialExpeditionCardsEmpty() {
            assertTrue(board.getExpeditionCards().isEmpty());
        }

        @Test
        @DisplayName("Handelsschiffliste initial leer")
        void testInitialTradeShipsEmpty() {
            assertTrue(board.getTradeShips().isEmpty());
        }

        @Test
        @DisplayName("Erkundungsschiffliste initial leer")
        void testInitialExplorerShipsEmpty() {
            assertTrue(board.getExplorerShips().isEmpty());
        }

        @Test
        @DisplayName("Alte-Welt-Inseln initial 0")
        void testInitialOldWorldIslandsZero() {
            assertEquals(0, board.getNumOldWorldIslands());
        }

        @Test
        @DisplayName("Neue-Welt-Inseln initial 0")
        void testInitialNewWorldIslandsZero() {
            assertEquals(0, board.getNumNewWorldIslands());
        }
    }
}
