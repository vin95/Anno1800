package com.anno1800.actions.ValidationTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionValidator;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;

class validateActionDrawResidentCardTest {

    private static final int TEST_NUM_PLAYERS = 2;
    private Game game = new Game(TEST_NUM_PLAYERS);
    private  Action action = new Action.DrawResidentCard(1);
    private Player player = game.getCurrentPlayer();

    @Test
    @DisplayName("At the start of the game, a player can always draw a resident card")
    void testCanDrawResidentCardAtGameStart() {
        assertTrue(ActionValidator.canExecute(action, player, game), "Player should be able to draw a resident card at the start of the game.");
    }
}