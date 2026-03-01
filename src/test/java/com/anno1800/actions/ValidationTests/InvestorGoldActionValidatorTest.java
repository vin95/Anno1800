package com.anno1800.actions.ValidationTests;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Investor;
import com.anno1800.game.residents.Resident;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InvestorGoldActionValidator.
 * Rules tested:
 * - InvestorExhaustForGold ObjectiveCard must be active
 * - Player must not have already used this action this turn
 * - Resident must be non-null and level 5 (Investor)
 * - Resident must be FIT (not exhausted)
 * - Resident must belong to the player
 */
@DisplayName("InvestorGoldActionValidator Tests")
class InvestorGoldActionValidatorTest {

    private static final int NUM_PLAYERS = 2;
    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new Game(NUM_PLAYERS, true, 10);
        player = game.getCurrentPlayer();
    }

    // ─── card not active ───────────────────────────────────────────────────

    @Test
    @DisplayName("InvestorExhaustForGold card not active → false")
    void cardNotActive_returnsFalse() {
        Investor investor = getFitInvestorOnBoard(player);
        if (investor == null) {
            investor = givePlayerAnInvestor(player);
        }
        Action.InvestorGoldAction action = new Action.InvestorGoldAction(investor);
        // Default testMode does NOT include InvestorExhaustForGold
        boolean hasCard = game.getActiveObjectiveCards().stream()
                .anyMatch(c -> c instanceof ObjectiveCard.InvestorExhaustForGold);
        if (hasCard) return; // Deck order changed
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── already used this turn ────────────────────────────────────────────

    @Test
    @DisplayName("card active, already used this turn → false")
    void alreadyUsedThisTurn_returnsFalse() throws Exception {
        injectInvestorGoldCard();
        Investor investor = getFitInvestorOnBoard(player);
        if (investor == null) investor = givePlayerAnInvestor(player);

        player.getPlayerBoard().markInvestorGoldUsed();

        Action.InvestorGoldAction action = new Action.InvestorGoldAction(investor);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── null / wrong-level resident ──────────────────────────────────────

    @Test
    @DisplayName("null investor → false")
    void nullInvestor_returnsFalse() throws Exception {
        injectInvestorGoldCard();
        Action.InvestorGoldAction action = new Action.InvestorGoldAction(null);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    @Test
    @DisplayName("resident level != 5 (Farmer) → false")
    void nonInvestorResident_returnsFalse() throws Exception {
        injectInvestorGoldCard();
        // Give player a Farmer (level 1)
        Resident farmer = game.getBoard().takeResident(1);
        farmer.setStatus(com.anno1800.game.residents.ResidentStatus.FIT);
        player.getPlayerBoard().getResidents().add(farmer);

        // Pass Farmer to InvestorGoldAction (it expects level 5)
        // We cast via raw action — the validator checks populationLevel at runtime
        Action.InvestorGoldAction action = new Action.InvestorGoldAction(farmer);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── investor is exhausted ────────────────────────────────────────────

    @Test
    @DisplayName("investor is exhausted → false")
    void exhaustedInvestor_returnsFalse() throws Exception {
        injectInvestorGoldCard();
        Investor investor = getFitInvestorOnBoard(player);
        if (investor == null) investor = givePlayerAnInvestor(player);
        investor.exhaust();

        Action.InvestorGoldAction action = new Action.InvestorGoldAction(investor);
        assertFalse(ActionValidator.canExecute(action, player, game));

        investor.recover(); // cleanup
    }

    // ─── investor does not belong to player ───────────────────────────────

    @Test
    @DisplayName("investor belongs to other player → false")
    void foreignInvestor_returnsFalse() throws Exception {
        injectInvestorGoldCard();
        Player other = game.getPlayers()[1];
        Investor foreignInvestor = getFitInvestorOnBoard(other);
        if (foreignInvestor == null) foreignInvestor = givePlayerAnInvestor(other);

        Action.InvestorGoldAction action = new Action.InvestorGoldAction(foreignInvestor);
        assertFalse(ActionValidator.canExecute(action, player, game));
    }

    // ─── valid conditions ──────────────────────────────────────────────────

    @Test
    @DisplayName("card active, not used, FIT investor owned by player → true")
    void validConditions_returnsTrue() throws Exception {
        injectInvestorGoldCard();
        Investor investor = getFitInvestorOnBoard(player);
        if (investor == null) investor = givePlayerAnInvestor(player);

        Action.InvestorGoldAction action = new Action.InvestorGoldAction(investor);
        assertTrue(ActionValidator.canExecute(action, player, game));
    }

    // ─── helpers ───────────────────────────────────────────────────────────

    private Investor getFitInvestorOnBoard(Player p) {
        return (Investor) p.getPlayerBoard().getResidents().stream()
                .filter(r -> r.getPopulationLevel() == 5 && r.isFit())
                .findFirst()
                .orElse(null);
    }

    private Investor givePlayerAnInvestor(Player p) {
        if (game.getBoard().getInvestors() > 0) {
            Resident r = game.getBoard().takeResident(5);
            r.setStatus(com.anno1800.game.residents.ResidentStatus.FIT);
            p.getPlayerBoard().getResidents().add(r);
            return (Investor) r;
        }
        // Create directly if board is empty
        Investor inv = new Investor();
        inv.setStatus(com.anno1800.game.residents.ResidentStatus.FIT);
        p.getPlayerBoard().getResidents().add(inv);
        return inv;
    }

    @SuppressWarnings("unchecked")
    private void injectInvestorGoldCard() throws Exception {
        Field field = Game.class.getDeclaredField("activeObjectiveCards");
        field.setAccessible(true);
        List<ObjectiveCard> cards = (List<ObjectiveCard>) field.get(game);
        cards.add(new ObjectiveCard.InvestorExhaustForGold());
    }
}
