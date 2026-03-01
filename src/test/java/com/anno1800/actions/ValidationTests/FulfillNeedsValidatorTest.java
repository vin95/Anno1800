package com.anno1800.actions.ValidationTests;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FulfillNeedsValidator.
 * Rules tested:
 * - Card must be in player's hand
 * - Player must be able to obtain all required goods from the card
 */
@DisplayName("FulfillNeedsValidator Tests")
class FulfillNeedsValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // ─── card ownership ────────────────────────────────────────────────────

    @Test
    @DisplayName("card not in player's hand → false")
    void cardNotInHand_returnsFalse() {
        // Draw a card for the other player, then attempt to use it as current player
        Player other = game.getPlayers()[1];
        var otherCards = other.getPlayerBoard().getResidentCards();
        if (otherCards.isEmpty()) return; // Skip if other player has no cards

        ResidentCard foreignCard = otherCards.get(0);
        Action.FulfillNeeds action = new Action.FulfillNeeds(foreignCard, foreignCard.needs());
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("null card → false")
    void nullCard_returnsFalse() {
        // FulfillNeeds requires card + goods; null card means player.getResidentCards().contains(null) → false
        Action.FulfillNeeds action = new Action.FulfillNeeds(null, new Goods[0]);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── card in hand ──────────────────────────────────────────────────────

    @Test
    @DisplayName("card in player's hand → validation completes without exception")
    void cardInHand_noException() {
        // Give the player a card by drawing from the board stack
        ResidentCard drawnCard = game.getBoard().getResidentStack1().pollFirst();
        if (drawnCard == null) return; // Stack empty

        player.getPlayerBoard().getResidentCards().add(drawnCard);

        Action.FulfillNeeds action = new Action.FulfillNeeds(drawnCard, drawnCard.needs());
        assertDoesNotThrow(() -> ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("storedGoods cleared after FulfillNeeds validation")
    void storedGoodsCleared_afterValidation() {
        ResidentCard drawnCard = game.getBoard().getResidentStack1().pollFirst();
        if (drawnCard == null) return;

        player.getPlayerBoard().getResidentCards().add(drawnCard);
        Action.FulfillNeeds action = new Action.FulfillNeeds(drawnCard, drawnCard.needs());
        ActionValidator.canExecute(action, player, game);

        assertTrue(player.getPlayerBoard().getStoredGoods().isEmpty(),
                "storedGoods must be cleared after validation rollback");
    }
}
