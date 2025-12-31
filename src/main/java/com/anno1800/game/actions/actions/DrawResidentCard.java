package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;

/**
 * Draw a resident card from the deck.
 */
public class DrawResidentCard {

    public static void drawResidentCard(Player player, int populationLevel, Game game) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        try {
            ResidentCard card = board.drawResidentCard(populationLevel);
            playerBoard.getResidentCards().add(card);
        } catch (IllegalStateException e) {
            // Stapel leer: Goldtransfer
            int goldAmount = (populationLevel <= 2) ? 1 : 3;
            playerBoard.spendGold(goldAmount);
            board.returnGold(goldAmount);
        }
    }
}
