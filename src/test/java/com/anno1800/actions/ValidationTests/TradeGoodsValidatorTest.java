package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.data.gamedata.Goods;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TradeGoodsValidator.
 *
 * Strategy:
 *  - Neue-Welt-Ressourcen können niemals gehandelt werden (unabhängig vom Zustand).
 *  - Jede Ressource kann nur einmal pro Zug gehandelt werden.
 *  - Der Spieler braucht einen Mitspieler mit passender Fabrik und ausreichend Chips.
 */
@DisplayName("TradeGoods Validator Tests")
class TradeGoodsValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // =========================================================================
    // Neue-Welt-Ressourcen (immer false)
    // =========================================================================

    @Test
    @DisplayName("CACAO kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testCacaoCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.CACAO, 0), player, game));
    }

    @Test
    @DisplayName("SUGARCANE kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testSugarcaneCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.SUGARCANE, 0), player, game));
    }

    @Test
    @DisplayName("TOBACCO kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testTobaccoCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.TOBACCO, 0), player, game));
    }

    @Test
    @DisplayName("COFFEE_BEANS kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testCoffeeBeansCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.COFFEE_BEANS, 0), player, game));
    }

    @Test
    @DisplayName("COTTON kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testCottonCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.COTTON, 0), player, game));
    }

    @Test
    @DisplayName("RUBBER kann nicht gehandelt werden (Neue-Welt-Ressource)")
    void testRubberCannotBeTraded() {
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.RUBBER, 0), player, game));
    }

    // =========================================================================
    // Bereits gehandelte Ressource (einmal pro Zug)
    // =========================================================================

    @Test
    @DisplayName("Ressource die bereits gehandelt wurde, kann nicht erneut gehandelt werden")
    void testAlreadyTradedGoodBlockedForCurrentTurn() {
        // PLANKS als gehandelt registrieren
        player.getPlayerBoard().registerTradedGood(Goods.PLANKS);
        assertFalse(ActionValidator.canExecute(new Action.TradeGoods(Goods.PLANKS, 0), player, game));
    }

    @Test
    @DisplayName("Nach clearTradedGoodsThisTurn() kann dieselbe Ressource erneut gehandelt werden (wenn andere Bedingungen erfüllt)")
    void testAfterClearSameGoodIsUnblocked() {
        // COAL als gehandelt markieren und dann zurücksetzen
        player.getPlayerBoard().registerTradedGood(Goods.COAL);
        player.getPlayerBoard().clearTradedGoodsThisTurn();
        // COAL ist nun wieder nicht als gehandelt markiert (Validierung hängt von anderen Bedingungen ab)
        assertFalse(player.getPlayerBoard().hasAlreadyTradedThisTurn(Goods.COAL),
            "Nach clear() sollte COAL nicht mehr als gehandelt gelten");
    }
}
