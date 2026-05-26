package com.anno1800.game.actions.actions;

import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import static com.anno1800.game.residents.ResidentStatus.FIT;

/**
 * Settle a new resident action.
 * Takes a resident from the GameBoard and adds them to the PlayerBoard with FIT status.
 * Also draws the corresponding ResidentCard according to the rules:
 * - Farmer/Worker (Level 1-2): Draw from Stack1 (popLv 2)
 * - Artisan/Engineer/Investor (Level 3-5): Draw from Stack2 (popLv 5)
 */
public class SettleResident {

    /**
     * Settles a resident of the specified level.
     * Takes the resident from GameBoard and adds them to PlayerBoard with FIT status.
     * Also draws a ResidentCard based on resident level:
     * - Level 1-2 (Farmer/Worker): Draw card with popLv 2 (Bauer/Arbeiter-Karte)
     * - Level 3-5 (Artisan/Engineer/Investor): Draw card with popLv 5 (Handwerker/Ingenieur/Investor-Karte)
     * 
     * If the card stack is empty, player must pay gold instead (1 gold per missing card).
     * 
     * @param player The player settling the resident
     * @param game The current game state
     * @param action The settle resident action
     */
    public static void settleResident(Player player, Game game, com.anno1800.game.actions.Action.SettleResident action) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        Board board = game.getBoard();
        int level = action.level();
        
        // Check if there are residents of the requested level available on the board
        if (!hasResidentAvailable(board, level)) {
            throw new IllegalStateException("No residents of level " + level + " available on the board");
        }
        
        // Get required goods
        com.anno1800.game.residents.ResidentCosts.Cost cost = com.anno1800.game.residents.ResidentCosts.getSettlementCost(level);
        
        if (cost.goods() != null && cost.goods().length > 0) {
            System.out.println("Settling resident level " + level + " requires: " + java.util.Arrays.toString(cost.goods()));
            
            // PLANNING PHASE: Determine how to obtain goods
            if (!playerBoard.canObtainGoods(cost.goods(), game)) {
                throw new IllegalStateException("Cannot obtain required goods for settling resident level " + level);
            }
            
            // EXECUTION PHASE: Actually obtain and consume goods
            playerBoard.consumeGoods(cost.goods(), game);
        }
        
        // Take a resident from the GameBoard
        Resident resident = board.takeResident(level);
        
        // Set status to FIT
        resident.setStatus(FIT);
        
        // Add to PlayerBoard
        playerBoard.getResidents().add(resident);
        
        // Draw a ResidentCard based on resident level
        // Rule: "Für jeden neuen Bauern oder Arbeiter muss 1 Bauer/Arbeiter-Bevölkerungs-Karte gezogen werden.
        //        Für jeden neuen Handwerker, Ingenieur oder Investor muss 1 Handwerker/Ingenieur/Investor-Karte gezogen werden."
        drawResidentCardForSettlement(player, board, level);
        
        System.out.println("Settled resident of level " + level + " with FIT status");
    }

    /**
     * Draws a ResidentCard for a newly settled resident.
     * - Farmer/Worker (Level 1-2): Draw from Stack1 (popLv 2)
     * - Artisan/Engineer/Investor (Level 3-5): Draw from Stack2 (popLv 5)
     * 
     * If the corresponding stack is empty, player must pay gold instead:
     * - Stack1 (popLv 2) empty: 1 gold
     * - Stack2 (popLv 5) empty: 3 gold
     * 
     * @param player The player drawing the card
     * @param board The game board
     * @param residentLevel The level of the settled resident (1-5)
     */
    private static void drawResidentCardForSettlement(Player player, Board board, int residentLevel) {
        PlayerBoard playerBoard = player.getPlayerBoard();
        
        // Determine which card stack to draw from based on resident level
        int cardPopLevel = (residentLevel <= 2) ? 2 : 5;
        
        // Try to draw the card
        boolean canDrawCard = canDrawResidentCard(board, cardPopLevel);
        
        if (canDrawCard) {
            ResidentCard card = board.drawResidentCard(cardPopLevel);
            playerBoard.getResidentCards().add(card);
            System.out.println("  -> Drew ResidentCard (popLv " + cardPopLevel + ") for settled resident");
        } else {
            // Stack is empty - player must pay gold instead
            // Rule: "Ist der entsprechende Stapel aufgebraucht, muss Gold ausgegeben werden"
            // Gold costs: popLv 2 (Bauer/Arbeiter) = 1 gold, popLv 5 (Handwerker/Ingenieur/Investor) = 3 gold
            int goldCost = getGoldCostForEmptyStack(cardPopLevel);
            if (playerBoard.getGold() >= goldCost) {
                playerBoard.spendGold(goldCost);
                System.out.println("  -> Card stack empty! Paid " + goldCost + " gold instead of drawing card");
            } else {
                throw new IllegalStateException("Cannot settle resident: Card stack is empty and player has insufficient gold (" + 
                    playerBoard.getGold() + " < " + goldCost + ")");
            }
        }
    }

    /**
     * Returns the gold cost when a card stack is empty.
     * - Stack1 (popLv 2, Bauer/Arbeiter): 1 gold
     * - Stack2 (popLv 5, Handwerker/Ingenieur/Investor): 3 gold
     * 
     * @param cardPopLevel The population level of the card stack (2 or 5)
     * @return The gold cost to pay instead of drawing a card
     */
    private static int getGoldCostForEmptyStack(int cardPopLevel) {
        return switch (cardPopLevel) {
            case 2 -> 1;  // Bauer/Arbeiter-Karten
            case 5 -> 3;  // Handwerker/Ingenieur/Investor-Karten
            default -> 1; // Fallback
        };
    }

    /**
     * Checks if a ResidentCard can be drawn from the appropriate stack.
     * 
     * @param board The game board
     * @param cardPopLevel The population level of the card stack (2 or 5)
     * @return true if a card can be drawn
     */
    private static boolean canDrawResidentCard(Board board, int cardPopLevel) {
        return switch (cardPopLevel) {
            case 2 -> !board.getResidentStack1().isEmpty();
            case 5 -> !board.getResidentStack2().isEmpty();
            default -> false;
        };
    }

    /**
     * Checks if a resident of the specified level is available on the board.
     * 
     * @param board The game board
     * @param level The population level to check
     * @return true if a resident is available
     */
    private static boolean hasResidentAvailable(Board board, int level) {
        try {
            // Try to check if the board has residents of this level
            switch (level) {
                case 1 -> { return board.getFarmers() > 0; }
                case 2 -> { return board.getWorkers() > 0; }
                case 3 -> { return board.getArtisans() > 0; }
                case 4 -> { return board.getEngineers() > 0; }
                case 5 -> { return board.getInvestors() > 0; }
                default -> { return false; }
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if a player can settle a resident of the specified level.
     * 
     * @param player The player to check
     * @param game The current game state
     * @param level The population level to settle
     * @return true if settlement is possible
     */
    public static boolean canSettleResident(Player player, Game game, int level) {
        Board board = game.getBoard();
        
        // Check if level is valid
        if (level < 1 || level > 5) {
            return false;
        }
        
        // Check if there are residents available on the board
        return hasResidentAvailable(board, level);
    }
}
