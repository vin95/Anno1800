package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiscardResidentCardActionValidator.
 * Rules tested:
 * - DiscardResidentCard ObjectiveCard must be active
 * - Player must not have already used this action this turn
 * - Player must have ≥ 2 Explorer Chips
 * - The specified card must be in the player's hand (non-null)
 */
@DisplayName("DiscardResidentCardActionValidator Tests")
class DiscardResidentCardActionValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;
    private ResidentCard cardInHand;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
        // Give player a resident card for use in tests
        cardInHand = game.getBoard().getResidentStack1().pollFirst();
        if (cardInHand != null) {
            player.getPlayerBoard().getResidentCards().add(cardInHand);
        }
    }

    // ─── card not active ───────────────────────────────────────────────────

    @Test
    @DisplayName("DiscardResidentCard card not active → false")
    void cardNotActive_returnsFalse() {
        boolean hasCard = game.getActiveObjectiveCards().stream()
                .anyMatch(c -> c instanceof ObjectiveCard.DiscardResidentCard);
        if (hasCard) return; // Deck order changed

        if (cardInHand == null) return;
        player.getPlayerBoard().increaseAvailableExplorerChips(2);
        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── already used this turn ────────────────────────────────────────────

    @Test
    @DisplayName("card active, already used this turn → false")
    void alreadyUsedThisTurn_returnsFalse() throws Exception {
        injectDiscardResidentCardObjective();
        if (cardInHand == null) return;
        player.getPlayerBoard().increaseAvailableExplorerChips(2);
        player.getPlayerBoard().markDiscardResidentCardUsed();

        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── insufficient chips ────────────────────────────────────────────────

    @Test
    @DisplayName("card active, not used, but 0 chips → false (needs ≥ 2)")
    void notEnoughChips_returnsFalse() throws Exception {
        injectDiscardResidentCardObjective();
        if (cardInHand == null) return;
        int chips = player.getPlayerBoard().getAvailableExplorerChips();
        if (chips > 0) player.getPlayerBoard().reduceAvailableExplorerChips(chips);

        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("card active, not used, only 1 chip → false (needs ≥ 2)")
    void oneChip_returnsFalse() throws Exception {
        injectDiscardResidentCardObjective();
        if (cardInHand == null) return;
        int chips = player.getPlayerBoard().getAvailableExplorerChips();
        if (chips > 1) player.getPlayerBoard().reduceAvailableExplorerChips(chips - 1);
        else if (chips < 1) player.getPlayerBoard().increaseAvailableExplorerChips(1 - chips);

        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── null / foreign card ──────────────────────────────────────────────

    @Test
    @DisplayName("null card → false")
    void nullCard_returnsFalse() throws Exception {
        injectDiscardResidentCardObjective();
        player.getPlayerBoard().increaseAvailableExplorerChips(2);
        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(null);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("card not in player's hand → false")
    void cardNotInHand_returnsFalse() throws Exception {
        injectDiscardResidentCardObjective();
        player.getPlayerBoard().increaseAvailableExplorerChips(2);
        // Card drawn from stack but NOT added to hand
        ResidentCard foreignCard = game.getBoard().getResidentStack1().pollFirst();
        if (foreignCard == null) return;
        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(foreignCard);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── valid conditions ──────────────────────────────────────────────────

    @Test
    @DisplayName("card active, not used, >= 2 chips, card in hand → true")
    void validConditions_returnsTrue() throws Exception {
        injectDiscardResidentCardObjective();
        if (cardInHand == null) return;
        player.getPlayerBoard().increaseAvailableExplorerChips(2);

        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertTrue(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("valid: exactly 2 chips → true")
    void exactlyTwoChips_returnsTrue() throws Exception {
        injectDiscardResidentCardObjective();
        if (cardInHand == null) return;
        int chips = player.getPlayerBoard().getAvailableExplorerChips();
        if (chips > 2) player.getPlayerBoard().reduceAvailableExplorerChips(chips - 2);
        else if (chips < 2) player.getPlayerBoard().increaseAvailableExplorerChips(2 - chips);
        assertEquals(2, player.getPlayerBoard().getAvailableExplorerChips());

        Action.DiscardResidentCardAction action = new Action.DiscardResidentCardAction(cardInHand);
        assertTrue(ActionValidator.canExecute(action, player, game));
    }

    // ─── helper ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void injectDiscardResidentCardObjective() throws Exception {
        Field field = Game.class.getDeclaredField("activeObjectiveCards");
        field.setAccessible(true);
        List<ObjectiveCard> cards = (List<ObjectiveCard>) field.get(game);
        cards.add(new ObjectiveCard.DiscardResidentCard());
    }
}
