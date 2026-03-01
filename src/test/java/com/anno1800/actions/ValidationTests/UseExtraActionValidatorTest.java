package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UseExtraActionValidator.
 * Rules tested:
 * - ExtraAction ObjectiveCard must be active in the game
 * - Player must not have already used this action this turn
 * - Player must have ≥ 3 Gold
 * - Player must have ≥ 3 Explorer Chips
 *
 * Note: In testMode the active cards are MostInvestors, MostEngineers,
 * NewWorldExplorer, BasicGoodsProducer, ExplorerTrader — ExtraAction is NOT
 * among them. The test injects it via reflection.
 */
@DisplayName("UseExtraActionValidator Tests")
class UseExtraActionValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    private static final Action.UseExtraAction ACTION = new Action.UseExtraAction();

    // ─── card not active ───────────────────────────────────────────────────

    @Test
    @DisplayName("ExtraAction card not active → false")
    void extraActionCardNotActive_returnsFalse() {
        // Default testMode does NOT include ExtraAction in the first 5 cards
        boolean hasExtraAction = game.getActiveObjectiveCards().stream()
                .anyMatch(c -> c instanceof ObjectiveCard.ExtraAction);
        if (hasExtraAction) return; // Deck order changed, skip

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── card active but conditions not met ────────────────────────────────

    @Test
    @DisplayName("ExtraAction active, already used this turn → false")
    void alreadyUsedThisTurn_returnsFalse() throws Exception {
        injectExtraActionCard();
        player.getPlayerBoard().gainGold(3);
        player.getPlayerBoard().increaseAvailableExplorerChips(3);
        player.getPlayerBoard().markExtraActionUsed();

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    @Test
    @DisplayName("ExtraAction active, not used, but < 3 gold → false")
    void notEnoughGold_returnsFalse() throws Exception {
        injectExtraActionCard();
        // Drain gold to 0
        int gold = player.getPlayerBoard().getGold();
        player.getPlayerBoard().reduceGold(gold);
        player.getPlayerBoard().increaseAvailableExplorerChips(3);

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    @Test
    @DisplayName("ExtraAction active, not used, gold >= 3, but < 3 explorer chips → false")
    void notEnoughExplorerChips_returnsFalse() throws Exception {
        injectExtraActionCard();
        player.getPlayerBoard().gainGold(10);
        // Set chips to exactly 2
        int chips = player.getPlayerBoard().getAvailableExplorerChips();
        if (chips > 2) player.getPlayerBoard().reduceAvailableExplorerChips(chips - 2);
        else if (chips < 2) player.getPlayerBoard().increaseAvailableExplorerChips(2 - chips);

        assertFalse(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── valid conditions ──────────────────────────────────────────────────

    @Test
    @DisplayName("ExtraAction active, not used, >= 3 gold, >= 3 chips → true")
    void validConditions_returnsTrue() throws Exception {
        injectExtraActionCard();
        player.getPlayerBoard().gainGold(3);
        player.getPlayerBoard().increaseAvailableExplorerChips(3);

        assertTrue(ActionValidator.canExecute(ACTION, player, game));
    }

    @Test
    @DisplayName("ExtraAction active, not used, exactly 3 gold, exactly 3 chips → true")
    void exactlyEnough_returnsTrue() throws Exception {
        injectExtraActionCard();
        // Drain to exactly 3 gold
        int gold = player.getPlayerBoard().getGold();
        player.getPlayerBoard().reduceGold(gold);
        player.getPlayerBoard().gainGold(3);
        // Set chips to exactly 3
        int chips = player.getPlayerBoard().getAvailableExplorerChips();
        if (chips > 3) player.getPlayerBoard().reduceAvailableExplorerChips(chips - 3);
        else if (chips < 3) player.getPlayerBoard().increaseAvailableExplorerChips(3 - chips);

        assertTrue(ActionValidator.canExecute(ACTION, player, game));
    }

    // ─── helper ────────────────────────────────────────────────────────────

    /**
     * Uses reflection to add an ExtraAction ObjectiveCard to Game's private
     * activeObjectiveCards list, simulating a game where this card was drawn.
     */
    @SuppressWarnings("unchecked")
    private void injectExtraActionCard() throws Exception {
        Field field = Game.class.getDeclaredField("activeObjectiveCards");
        field.setAccessible(true);
        List<ObjectiveCard> cards = (List<ObjectiveCard>) field.get(game);
        cards.add(new ObjectiveCard.ExtraAction());
    }
}
