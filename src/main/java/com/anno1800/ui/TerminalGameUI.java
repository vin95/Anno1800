package com.anno1800.ui;

import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionGenerator;
import com.anno1800.game.actions.ActionResult;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.state.GameState;
import com.anno1800.ui.output.GameStatePrinter;

import java.util.ArrayList;
import java.util.List;import java.util.Map;import java.util.Scanner;

/**
 * Terminal-based user interface for Anno 1800 board game.
 * Allows 2 human players to play via command-line input.
 * 
 * Features:
 * - Display current player and available actions
 * - Execute actions via terminal input
 * - Show game state at any time
 * - Show differences from previous game state
 * - Save game state after each action
 */
public class TerminalGameUI {
    
    private final Game game;
    private final Scanner scanner;
    private final ActionGenerator actionGenerator;
    private final GameStatePrinter statePrinter;
    
    // History tracking
    private GameState previousState;
    private GameState currentState;
    private final List<GameState> stateHistory;
    
    // Command constants
    private static final String CMD_SHOW_STATE = "state";
    private static final String CMD_SHOW_DIFF = "diff";
    private static final String CMD_HELP = "help";
    private static final String CMD_QUIT = "quit";
    
    public TerminalGameUI(int numPlayers) {
        this.game = new Game(numPlayers);
        this.scanner = new Scanner(System.in);
        this.actionGenerator = new ActionGenerator();
        this.statePrinter = new GameStatePrinter();
        this.stateHistory = new ArrayList<>();
        
        // Initialize game states
        this.currentState = game.getState();
        this.previousState = currentState;
        this.stateHistory.add(currentState);
    }
    
    /**
     * Start the game loop.
     */
    public void start() {
        printWelcome();
        printObjectiveCards();
        printStartPlayer();
        
        // Main game loop
        while (!game.isGameOver()) {
            playTurn();
        }
        
        printGameOver();
    }
    
    /**
     * Play one turn for the current player.
     */
    private void playTurn() {
        Player currentPlayer = game.getCurrentPlayer();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("Round %d - %s's Turn (Position %d)\n", 
            game.getCurrentRound(), 
            currentPlayer.getName(),
            currentPlayer.getPosition());
        System.out.println("=".repeat(80));
        
        // Generate available actions
        List<Action> availableActions = actionGenerator.getPossibleActions(currentPlayer, game);
        
        if (availableActions.isEmpty()) {
            System.out.println("No available actions! Skipping turn...");
            game.nextPlayer();
            return;
        }
        
        // Display actions and get player choice
        Action selectedAction = selectAction(availableActions);
        
        if (selectedAction != null) {
            executeAndSaveAction(selectedAction);
        }
        
        // Move to next player
        game.nextPlayer();
    }
    
    /**
     * Display available actions and let player select one.
     * Also handles special commands (state, diff, help, quit).
     */
    private Action selectAction(List<Action> availableActions) {
        while (true) {
            System.out.println("\nAvailable Actions:");
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < availableActions.size(); i++) {
                System.out.printf("[%d] %s\n", i + 1, formatAction(availableActions.get(i)));
            }
            
            System.out.println("\nSpecial Commands:");
            System.out.println("  'state' - Show current game state");
            System.out.println("  'diff'  - Show differences from previous state");
            System.out.println("  'help'  - Show help");
            System.out.println("  'quit'  - Exit game");
            
            System.out.print("\nYour choice: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            // Handle special commands
            if (handleSpecialCommand(input)) {
                continue;
            }
            
            // Parse action number
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= availableActions.size()) {
                    return availableActions.get(choice - 1);
                } else {
                    System.out.println("Invalid choice! Please enter a number between 1 and " + availableActions.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number or a special command.");
            }
        }
    }
    
    /**
     * Handle special commands like 'state', 'diff', 'help', 'quit'.
     * @return true if a command was handled (continue loop), false if not a command
     */
    private boolean handleSpecialCommand(String input) {
        switch (input) {
            case CMD_SHOW_STATE:
                System.out.println("\n");
                statePrinter.printDetailed(currentState);
                return true;
                
            case CMD_SHOW_DIFF:
                showStateDifference();
                return true;
                
            case CMD_HELP:
                printHelp();
                return true;
                
            case CMD_QUIT:
                System.out.println("\nExiting game...");
                System.exit(0);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Execute an action and save the new game state.
     */
    private void executeAndSaveAction(Action action) {
        System.out.println("\nExecuting: " + formatAction(action));
        
        // Execute action
        ActionResult result = game.executeAction(action);
        
        // All ActionResult types are successful - we just check if it's not null
        if (result != null) {
            System.out.println("✓ Action executed successfully!");
            
            // Save previous state and capture new state
            previousState = currentState;
            currentState = game.getState();
            stateHistory.add(currentState);
            
            System.out.println("\n[Game state saved - " + stateHistory.size() + " states in history]");
        } else {
            System.out.println("✗ Action failed!");
        }
    }
    
    /**
     * Show differences between current and previous game state.
     */
    private void showStateDifference() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DIFFERENCES FROM PREVIOUS STATE");
        System.out.println("=".repeat(80));
        
        if (previousState == null || previousState == currentState) {
            System.out.println("No changes yet (game just started).");
            return;
        }
        
        System.out.println("\nRound: " + previousState.round() + " → " + currentState.round());
        
        if (previousState.currentPlayerIndex() < previousState.players().size() &&
            currentState.currentPlayerIndex() < currentState.players().size()) {
            System.out.println("Current Player: " + 
                previousState.players().get(previousState.currentPlayerIndex()).name() + " → " +
                currentState.players().get(currentState.currentPlayerIndex()).name());
        }
        
        // Compare each player's state
        for (int i = 0; i < currentState.players().size(); i++) {
            var prevPlayer = previousState.players().get(i);
            var currPlayer = currentState.players().get(i);
            
            if (hasPlayerStateChanged(prevPlayer, currPlayer)) {
                System.out.println("\n" + currPlayer.name() + "'s changes:");
                printPlayerDifferences(prevPlayer, currPlayer);
            }
        }
        
        // Compare board state
        if (hasBoardStateChanged(previousState.boardState(), currentState.boardState())) {
            System.out.println("\nBoard changes:");
            printBoardDifferences(previousState.boardState(), currentState.boardState());
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Check if player state has changed.
     */
    private boolean hasPlayerStateChanged(GameState.PlayerState prev, GameState.PlayerState curr) {
        return prev.gold() != curr.gold() ||
               prev.factoryCount() != curr.factoryCount() ||
               prev.residentCount() != curr.residentCount() ||
               prev.residentCardCount() != curr.residentCardCount() ||
               prev.tradeShipCount() != curr.tradeShipCount() ||
               prev.explorerShipCount() != curr.explorerShipCount() ||
               prev.shipyardCount() != curr.shipyardCount();
    }
    
    /**
     * Print differences in player state.
     */
    private void printPlayerDifferences(GameState.PlayerState prev, GameState.PlayerState curr) {
        if (prev.gold() != curr.gold()) {
            System.out.printf("  Gold: %d → %d (%+d)\n", prev.gold(), curr.gold(), curr.gold() - prev.gold());
        }
        if (prev.factoryCount() != curr.factoryCount()) {
            System.out.printf("  Factories: %d → %d (%+d)\n", prev.factoryCount(), curr.factoryCount(), 
                curr.factoryCount() - prev.factoryCount());
        }
        if (prev.residentCount() != curr.residentCount()) {
            System.out.printf("  Residents: %d → %d (%+d)\n", prev.residentCount(), curr.residentCount(), 
                curr.residentCount() - prev.residentCount());
        }
        if (prev.residentCardCount() != curr.residentCardCount()) {
            System.out.printf("  Resident Cards: %d → %d (%+d)\n", prev.residentCardCount(), curr.residentCardCount(), 
                curr.residentCardCount() - prev.residentCardCount());
        }
        if (prev.tradeShipCount() != curr.tradeShipCount()) {
            System.out.printf("  Trade Ships: %d → %d (%+d)\n", prev.tradeShipCount(), curr.tradeShipCount(), 
                curr.tradeShipCount() - prev.tradeShipCount());
        }
        if (prev.explorerShipCount() != curr.explorerShipCount()) {
            System.out.printf("  Explorer Ships: %d → %d (%+d)\n", prev.explorerShipCount(), curr.explorerShipCount(), 
                curr.explorerShipCount() - prev.explorerShipCount());
        }
    }
    
    /**
     * Check if board state has changed.
     */
    private boolean hasBoardStateChanged(GameState.BoardState prev, GameState.BoardState curr) {
        return prev.gold() != curr.gold() ||
               prev.farmers() != curr.farmers() ||
               prev.workers() != curr.workers() ||
               prev.availableFactories() != curr.availableFactories();
    }
    
    /**
     * Print differences in board state.
     */
    private void printBoardDifferences(GameState.BoardState prev, GameState.BoardState curr) {
        if (prev.gold() != curr.gold()) {
            System.out.printf("  Gold pool: %d → %d (%+d)\n", prev.gold(), curr.gold(), curr.gold() - prev.gold());
        }
        if (prev.farmers() != curr.farmers()) {
            System.out.printf("  Farmers: %d → %d (%+d)\n", prev.farmers(), curr.farmers(), curr.farmers() - prev.farmers());
        }
        if (prev.workers() != curr.workers()) {
            System.out.printf("  Workers: %d → %d (%+d)\n", prev.workers(), curr.workers(), curr.workers() - prev.workers());
        }
        if (prev.availableFactories() != curr.availableFactories()) {
            System.out.printf("  Available Factories: %d → %d (%+d)\n", prev.availableFactories(), 
                curr.availableFactories(), curr.availableFactories() - prev.availableFactories());
        }
    }
    
    /**
     * Format an action for display.
     */
    private String formatAction(Action action) {
        return switch (action) {
            case Action.BuildFactory bf -> 
                String.format("Build Factory: %s (costs: %s)", 
                    bf.factory().getType(), 
                    formatCosts(bf.factory().costs()));
            case Action.DemolishFactory df -> 
                String.format("Demolish Factory: %s", df.factory().getType());
            case Action.OverbuildDefaultFactory obf -> 
                String.format("Overbuild %s with %s", 
                    obf.defaultFactory().getType(), 
                    obf.newFactory().getType());
            case Action.SettleResident sr -> 
                String.format("Settle Resident (Level %d)", sr.level());
            case Action.UpgradeResident ur -> 
                String.format("Upgrade %d Resident(s)", ur.residents().length);
            case Action.FulfillNeeds fn -> 
                String.format("Fulfill Needs: ResidentCard Level %d", fn.residentCard().populationLevel());
            case Action.SwapResidentCards src -> 
                String.format("Swap %d Resident Card(s)", src.cardsToSwap().length);
            case Action.BuildShipyard bs -> 
                String.format("Build Shipyard (Level %d)", bs.level());
            case Action.BuildShips bsh -> 
                String.format("Build %d Ship(s) - Level %d %s", 
                    bsh.amount(), bsh.level(), bsh.shipType());
            case Action.AssignWorker aw -> 
                String.format("Assign Worker to %s", aw.factory().getType());
            case Action.ExhaustWorker ew -> 
                String.format("Exhaust Resident (Level %d)", ew.resident().getPopulationLevel());
            case Action.DoOvertime dot -> 
                String.format("Do Overtime: Make Resident (Level %d) FIT", 
                    dot.populationLevel());
            case Action.ProduceGoods pg -> 
                String.format("Produce Goods in %s", pg.factory().getType());
            case Action.TradeGoods tg -> 
                String.format("Trade %s from Player %d", tg.good(), tg.player() + 1);
            case Action.ImportGood ig -> 
                String.format("Import %s from New World", ig.good());
            case Action.DiscoverOldWorldIsland doi -> 
                "Discover Old World Island";
            case Action.DiscoverNewWorldIsland dni -> 
                "Discover New World Island";
            case Action.Expedition exp -> 
                "Go on Expedition";
            case Action.Carneval c -> 
                "Activate Carneval";
            case Action.DrawResidentCard drc -> 
                String.format("Draw Resident Card (Level %d)", drc.populationLevel());
            case Action.ActivateReward ar -> 
                String.format("Activate Reward: %s", ar.reward());
            case Action.ChooseGoods cg -> 
                String.format("Choose Good: %s from %s", cg.chosenGood(), cg.reward());
        };
    }
    
    /**
     * Format costs array for display.
     */
    private String formatCosts(com.anno1800.data.gamedata.Goods[] costs) {
        if (costs == null || costs.length == 0) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < costs.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(costs[i]);
        }
        return sb.toString();
    }
    
    /**
     * Print welcome message.
     */
    private void printWelcome() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ".repeat(25) + "ANNO 1800 - BOARD GAME");
        System.out.println(" ".repeat(25) + "Terminal Edition");
        System.out.println("=".repeat(80));
        System.out.println("\nWelcome to Anno 1800!");
        System.out.println("This is a 2-player terminal-based game.");
        System.out.println("\nType 'help' at any time to see available commands.");
    }
    
    /**
     * Print start player information.
     */
    private void printStartPlayer() {
        Player startPlayer = game.getCurrentPlayer();
        System.out.println("\n" + "-".repeat(80));
        System.out.println("Starting Player: " + startPlayer.getName() + " (Position " + startPlayer.getPosition() + ")");
        System.out.println("-".repeat(80));
    }
    
    /**
     * Print active objective cards.
     */
    private void printObjectiveCards() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ".repeat(25) + "OBJECTIVE CARDS");
        System.out.println("=".repeat(80));
        System.out.println("\nThese objectives are active for this game:\n");
        
        int i = 1;
        for (ObjectiveCard card : game.getBoard().getActiveObjectiveCards()) {
            System.out.printf("%d. %s\n", i++, card.getTitle());
            System.out.printf("   %s\n\n", card.getDescription());
        }
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * Print help information.
     */
    private void printHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("HELP - Available Commands");
        System.out.println("=".repeat(80));
        System.out.println("\nDuring action selection:");
        System.out.println("  1-9     - Select an action by its number");
        System.out.println("  'state' - Display the complete current game state");
        System.out.println("  'diff'  - Show differences from the previous state");
        System.out.println("  'help'  - Show this help message");
        System.out.println("  'quit'  - Exit the game");
        System.out.println("\nGame Flow:");
        System.out.println("  - Players take turns executing actions");
        System.out.println("  - After each action, a new game state is saved");
        System.out.println("  - You can view the current state or differences at any time");
        System.out.println("=".repeat(80));
    }
    
    /**
     * Print game over message and final scores.
     */
    private void printGameOver() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ".repeat(30) + "GAME OVER!");
        System.out.println("=".repeat(80));
        
        // Calculate objective card points
        System.out.println("\nObjective Card Scoring:");
        System.out.println("-".repeat(80));
        
        Player[] players = game.getPlayers();
        
        for (ObjectiveCard objective : game.getBoard().getActiveObjectiveCards()) {
            System.out.println("\n" + objective.getTitle() + ": " + objective.getDescription());
            
            Map<Player, Integer> objectivePoints = objective.calculateEndGamePoints(players);
            if (!objectivePoints.isEmpty()) {
                for (Map.Entry<Player, Integer> entry : objectivePoints.entrySet()) {
                    Player player = entry.getKey();
                    int points = entry.getValue();
                    player.addVictoryPoints(points);
                    System.out.printf("  %s: +%d VP\n", player.getName(), points);
                }
            } else {
                System.out.println("  No points awarded");
            }
        }
        
        System.out.println("\nFinal Scores:");
        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            
            // Calculate gold-based victory points (gold / 3, rounded down)
            int goldAmount = player.getPlayerBoard().getGold();
            int goldVictoryPoints = goldAmount / 3;
            player.addVictoryPoints(goldVictoryPoints);
            
            System.out.printf("\n%s (Player %d):\n", player.getName(), i + 1);
            System.out.printf("  Victory Points:  %d\n", player.getVictoryPoints());
            System.out.printf("  Bonus Points:    %d\n", player.getBonusPoints());
            System.out.printf("  Gold:            %d (= %d VP)\n", goldAmount, goldVictoryPoints);
            System.out.printf("  Total Points:    %d\n", player.getTotalPoints());
        }
        
        // Determine winner
        Player winner = players[0];
        for (Player player : players) {
            if (player.getTotalPoints() > winner.getTotalPoints()) {
                winner = player;
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" ".repeat(25) + "🏆 WINNER: " + winner.getName() + " 🏆");
        System.out.println(" ".repeat(22) + "Total Points: " + winner.getTotalPoints());
        System.out.println("=".repeat(80));
        
        System.out.println("\nTotal states saved: " + stateHistory.size());
        System.out.println("Thank you for playing Anno 1800!");
    }
    
    /**
     * Main entry point for terminal game.
     */
    public static void main(String[] args) {
        System.out.println("Initializing game...");
        TerminalGameUI ui = new TerminalGameUI(2); // 2 human players
        ui.start();
    }
}
