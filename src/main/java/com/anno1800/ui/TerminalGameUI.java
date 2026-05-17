package com.anno1800.ui;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionGenerator;
import com.anno1800.game.actions.ActionResult;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.player.PlayerBoard;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.residents.ResidentStatus;
import com.anno1800.game.state.GameState;
import com.anno1800.data.gamedata.ShipType;
import com.anno1800.ui.output.GameStatePrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    
    // Test mode state saving
    private final String testGameDir;
    private int actionCounter = 0;
    
    // Command constants
    private static final String CMD_SHOW_STATE = "state";
    private static final String CMD_SHOW_DIFF = "diff";
    private static final String CMD_HELP = "help";
    private static final String CMD_QUIT = "quit";
    private static final String GAME_STATES_DIR = "game-states";
    
    public TerminalGameUI(int numPlayers) {
        this(numPlayers, false, 10);
    }
    
    public TerminalGameUI(int numPlayers, boolean testMode, int maxRounds) {
        this.game = new Game(numPlayers, testMode, maxRounds);
        this.scanner = new Scanner(System.in);
        this.actionGenerator = new ActionGenerator();
        this.statePrinter = new GameStatePrinter();
        this.stateHistory = new ArrayList<>();
        
        // Setup test game directory if in test mode
        if (testMode) {
            this.testGameDir = createTestGameDirectory();
            System.out.println("Test game states will be saved to: " + testGameDir);
        } else {
            this.testGameDir = null;
        }
        
        // Initialize game states
        this.currentState = game.getState();
        this.previousState = currentState;
        this.stateHistory.add(currentState);
        
        // Save initial state if in test mode
        if (testMode) {
            saveTestGameState(currentState, "initial");
        }
    }

    /**
     * Creates a seeded game for reproducible debug sessions.
     * Game states are always saved after each action.
     * @param numPlayers Number of players
     * @param seed       Random seed for reproducible shuffling and start player
     * @param maxRounds  Maximum rounds (game aborts after this many rounds)
     */
    public TerminalGameUI(int numPlayers, long seed, int maxRounds) {
        this.game = new Game(numPlayers, seed, maxRounds);
        this.scanner = new Scanner(System.in);
        this.actionGenerator = new ActionGenerator();
        this.statePrinter = new GameStatePrinter();
        this.stateHistory = new ArrayList<>();

        // Always save game states for seeded/debug games
        this.testGameDir = createTestGameDirectory();
        System.out.println("Game states will be saved to: " + testGameDir);

        this.currentState = game.getState();
        this.previousState = currentState;
        this.stateHistory.add(currentState);
        saveTestGameState(currentState, "initial");
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
            
            // Check if action is a free action that doesn't consume the turn
            if (!(selectedAction instanceof Action.ViewResidentCards)) {
                // Move to next player only if it's not a free action
                game.nextPlayer();
            } else {
                // For free actions, allow the player to take another action
                System.out.println("\n[Free action - you can take another action]");
                playTurn(); // Recursively call playTurn to allow another action
            }
        } else {
            // If no action was selected, move to next player
            game.nextPlayer();
        }
    }
    
    /**
     * Display available actions and let player select one.
     * Also handles special commands (state, diff, help, quit).
     * SwapResidentCards, BuildFactory, BuildShipyard, BuildShips, UpgradeResident, and OverbuildDefaultFactory actions are grouped and handled interactively.
     */
    private Action selectAction(List<Action> availableActions) {
        // Filter out complex actions and group them
        List<Action> displayActions = new ArrayList<>();
        List<Action> swapResidentCardsActions = new ArrayList<>();
        List<Action> buildFactoryActions = new ArrayList<>();
        List<Action> buildShipyardActions = new ArrayList<>();
        List<Action> buildShipsActions = new ArrayList<>();
        List<Action> upgradeResidentActions = new ArrayList<>();
        List<Action> settleResidentActions = new ArrayList<>();
        List<Action> overbuildDefaultFactoryActions = new ArrayList<>();
        List<Action> fulfillNeedsActions = new ArrayList<>();
        boolean hasSwapResidentCards = false;
        boolean hasBuildFactory = false;
        boolean hasBuildShipyard = false;
        boolean hasBuildShips = false;
        boolean hasUpgradeResident = false;
        boolean hasSettleResident = false;
        boolean hasFulfillNeeds = false;
        
        for (Action action : availableActions) {
            if (action instanceof Action.SwapResidentCards) {
                swapResidentCardsActions.add(action);
                if (!hasSwapResidentCards) {
                    displayActions.add(action); // Add only one as placeholder
                    hasSwapResidentCards = true;
                }
            } else if (action instanceof Action.BuildFactory) {
                buildFactoryActions.add(action);
                if (!hasBuildFactory) {
                    displayActions.add(action); // Add only one as placeholder
                    hasBuildFactory = true;
                }
            } else if (action instanceof Action.BuildShipyard) {
                buildShipyardActions.add(action);
                if (!hasBuildShipyard) {
                    displayActions.add(action); // Add only one as placeholder
                    hasBuildShipyard = true;
                }
            } else if (action instanceof Action.BuildShips) {
                buildShipsActions.add(action);
                if (!hasBuildShips) {
                    displayActions.add(action); // Add only one as placeholder
                    hasBuildShips = true;
                }
            } else if (action instanceof Action.UpgradeResident) {
                upgradeResidentActions.add(action);
                if (!hasUpgradeResident) {
                    displayActions.add(action); // Add only one as placeholder
                    hasUpgradeResident = true;
                }
            } else if (action instanceof Action.SettleResident) {
                settleResidentActions.add(action);
                if (!hasSettleResident) {
                    displayActions.add(action); // Add only one as placeholder
                    hasSettleResident = true;
                }
            } else if (action instanceof Action.OverbuildDefaultFactory) {
                overbuildDefaultFactoryActions.add(action);
                // Don't display - will be handled within BuildFactory flow
            } else if (action instanceof Action.FulfillNeeds) {
                fulfillNeedsActions.add(action);
                if (!hasFulfillNeeds) {
                    displayActions.add(action); // Add only one as placeholder
                    hasFulfillNeeds = true;
                }
            } else {
                displayActions.add(action);
            }
        }
        
        while (true) {
            System.out.println("\nAvailable Actions:");
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < displayActions.size(); i++) {
                Action action = displayActions.get(i);
                if (action instanceof Action.SwapResidentCards) {
                    System.out.printf("[%d] Swap Resident Cards (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.BuildFactory) {
                    System.out.printf("[%d] Build Factory (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.BuildShipyard) {
                    System.out.printf("[%d] Build Shipyard (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.BuildShips) {
                    System.out.printf("[%d] Build Ships (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.UpgradeResident) {
                    System.out.printf("[%d] Upgrade Residents (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.SettleResident) {
                    System.out.printf("[%d] Settle Resident (Interactive Selection)\n", i + 1);
                } else if (action instanceof Action.FulfillNeeds) {
                    System.out.printf("[%d] Fulfill Needs (Interactive Selection)\n", i + 1);
                } else {
                    System.out.printf("[%d] %s\n", i + 1, formatAction(action));
                }
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
                if (choice >= 1 && choice <= displayActions.size()) {
                    Action selectedAction = displayActions.get(choice - 1);
                    
                    // Special handling for SwapResidentCards
                    if (selectedAction instanceof Action.SwapResidentCards) {
                        return selectSwapResidentCards(swapResidentCardsActions);
                    }
                    
                    // Special handling for BuildFactory
                    if (selectedAction instanceof Action.BuildFactory) {
                        return selectBuildFactory(buildFactoryActions, overbuildDefaultFactoryActions);
                    }
                    
                    // Special handling for BuildShipyard
                    if (selectedAction instanceof Action.BuildShipyard) {
                        return selectBuildShipyard(buildShipyardActions);
                    }
                    
                    // Special handling for BuildShips
                    if (selectedAction instanceof Action.BuildShips) {
                        return selectBuildShips(buildShipsActions);
                    }
                    
                    // Special handling for UpgradeResident
                    if (selectedAction instanceof Action.UpgradeResident) {
                        return selectUpgradeResident(upgradeResidentActions);
                    }
                    
                    // Special handling for SettleResident
                    if (selectedAction instanceof Action.SettleResident) {
                        return selectSettleResident(settleResidentActions);
                    }
                    
                    // Special handling for FulfillNeeds
                    if (selectedAction instanceof Action.FulfillNeeds) {
                        return selectFulfillNeeds(fulfillNeedsActions);
                    }
                    
                    return selectedAction;
                } else {
                    System.out.println("Invalid choice! Please enter a number between 1 and " + displayActions.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number or a special command.");
            }
        }
    }
    
    /**
     * Interactive selection of cards to swap.
     * Allows step-by-step selection of 1-3 cards, or AI-agent style full list view.
     */
    private Action selectSwapResidentCards(List<Action> swapActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        List<ResidentCard> availableCards = board.getResidentCards();
        
        if (availableCards.isEmpty()) {
            System.out.println("No resident cards available to swap!");
            return null;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SWAP RESIDENT CARDS - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("You can swap 1, 2, or 3 resident cards.");
        System.out.println("Type 'all' to see all " + swapActions.size() + " possible combinations (AI mode).");
        System.out.println("Type 'cancel' to return to action selection.");
        System.out.println();
        
        List<ResidentCard> selectedCards = new ArrayList<>();
        
        while (selectedCards.size() < 3) {
            System.out.println("\nAvailable Resident Cards:");
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < availableCards.size(); i++) {
                ResidentCard card = availableCards.get(i);
                String selectedMarker = selectedCards.contains(card) ? " [SELECTED]" : "";
                System.out.printf("[%d] Level %d - Needs: %s%s\n", 
                    i + 1, 
                    card.populationLevel(),
                    formatNeeds(card.needs()),
                    selectedMarker);
            }
            
            System.out.println();
            if (selectedCards.isEmpty()) {
                System.out.println("Select first card to swap (or 'all' for full list, 'cancel' to abort):");
            } else {
                System.out.println("Selected cards: " + selectedCards.size());
                System.out.println("Select next card, type 'submit' to confirm, or 'cancel' to abort:");
            }
            
            System.out.print("Your choice: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            if (input.equals("all")) {
                // Show all combinations and let player choose
                return selectFromAllSwapCombinations(swapActions);
            }
            
            if (input.equals("submit")) {
                if (selectedCards.isEmpty()) {
                    System.out.println("You must select at least one card before submitting!");
                    continue;
                }
                // Find and return the matching action
                return findMatchingSwapAction(swapActions, selectedCards);
            }
            
            // Try to parse card selection
            try {
                int cardIndex = Integer.parseInt(input) - 1;
                if (cardIndex >= 0 && cardIndex < availableCards.size()) {
                    ResidentCard selectedCard = availableCards.get(cardIndex);
                    if (selectedCards.contains(selectedCard)) {
                        System.out.println("Card already selected! Choose a different card.");
                    } else {
                        selectedCards.add(selectedCard);
                        System.out.println("✓ Added card to selection.");
                        
                        // Auto-submit if 3 cards selected
                        if (selectedCards.size() == 3) {
                            return findMatchingSwapAction(swapActions, selectedCards);
                        }
                    }
                } else {
                    System.out.println("Invalid card number!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a card number, 'submit', 'all', or 'cancel'.");
            }
        }
        
        return null;
    }
    
    /**
     * Show all swap combinations and let player choose.
     */
    private Action selectFromAllSwapCombinations(List<Action> swapActions) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL SWAP COMBINATIONS (" + swapActions.size() + " total)");
        System.out.println("=".repeat(80));
        
        for (int i = 0; i < swapActions.size(); i++) {
            Action.SwapResidentCards action = (Action.SwapResidentCards) swapActions.get(i);
            System.out.printf("[%d] Swap %d card(s): ", i + 1, action.cardsToSwap().length);
            for (int j = 0; j < action.cardsToSwap().length; j++) {
                if (j > 0) System.out.print(", ");
                System.out.print("Level " + action.cardsToSwap()[j].populationLevel());
            }
            System.out.println();
        }
        
        while (true) {
            System.out.print("\nSelect combination (1-" + swapActions.size() + ") or 'cancel': ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= swapActions.size()) {
                    return swapActions.get(choice - 1);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input!");
            }
        }
    }
    
    /**
     * Find the action that matches the selected cards.
     */
    private Action findMatchingSwapAction(List<Action> swapActions, List<ResidentCard> selectedCards) {
        for (Action action : swapActions) {
            Action.SwapResidentCards swapAction = (Action.SwapResidentCards) action;
            
            if (swapAction.cardsToSwap().length == selectedCards.size()) {
                // Check if all cards match (order doesn't matter)
                boolean allMatch = true;
                for (ResidentCard card : selectedCards) {
                    boolean found = false;
                    for (ResidentCard actionCard : swapAction.cardsToSwap()) {
                        if (card == actionCard) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allMatch = false;
                        break;
                    }
                }
                
                if (allMatch) {
                    return swapAction;
                }
            }
        }
        
        System.out.println("Error: Could not find matching swap action!");
        return null;
    }
    
    /**
     * Interactive selection of factory to build.
     * Handles placement logic: land tiles, coast tiles, or overbuilding existing factories.
     */
    private Action selectBuildFactory(List<Action> buildActions, List<Action> overbuildActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        
        int freeLandTiles = board.getFreeLandTiles();
        int freeCoastTiles = board.getFreeCoastTiles();
        int numShipyards = board.getShipyards().size();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BUILD FACTORY - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("Free Land Tiles: " + freeLandTiles);
        System.out.println("Free Coast Tiles: " + freeCoastTiles);
        System.out.println("Shipyards: " + numShipyards);
        System.out.println();
        
        // Step 1: Select factory type to build
        System.out.println("Available Factory Types to Build:");
        System.out.println("-".repeat(80));
        
        // Group actions by factory type
        java.util.Map<String, Action.BuildFactory> factoryMap = new java.util.LinkedHashMap<>();
        for (Action action : buildActions) {
            if (action instanceof Action.BuildFactory bf) {
                String factoryType = bf.factory().getType().getDisplayName();
                if (!factoryMap.containsKey(factoryType)) {
                    factoryMap.put(factoryType, bf);
                }
            }
        }
        
        java.util.List<String> factoryTypes = new java.util.ArrayList<>(factoryMap.keySet());
        for (int i = 0; i < factoryTypes.size(); i++) {
            String factoryType = factoryTypes.get(i);
            Action.BuildFactory bf = factoryMap.get(factoryType);
            System.out.printf("[%d] %s (costs: %s)%n", 
                i + 1, 
                factoryType,
                formatCosts(bf.factory().costs()));
        }
        
        System.out.println();
        System.out.println("Type 'cancel' to return to action selection.");
        
        Action.BuildFactory selectedFactory = null;
        while (selectedFactory == null) {
            System.out.print("\nSelect factory to build: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= factoryTypes.size()) {
                    String selectedType = factoryTypes.get(choice - 1);
                    selectedFactory = factoryMap.get(selectedType);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
        
        // Step 2: Determine placement based on available tiles
        System.out.println("\n" + "-".repeat(80));
        System.out.println("PLACEMENT DECISION");
        System.out.println("-".repeat(80));
        
        // Logic according to requirements:
        // 1. If free land tiles > 0 -> build on land (no overbuilding)
        // 2. If free coast tiles > 1 -> build on coast (no overbuilding)
        // 3. If free coast tiles = 1 AND shipyards >= 2 -> build on coast (no overbuilding)
        // 4. If no free tiles -> ask which default factory to overbuild
        
        if (freeLandTiles > 0) {
            System.out.println("Factory will be placed on a free LAND tile.");
            System.out.println("(No existing factory will be overbuilt)");
            return selectedFactory;
        }
        
        if (freeCoastTiles > 1) {
            System.out.println("Factory will be placed on a free COAST tile.");
            System.out.println("(No existing factory will be overbuilt)");
            return selectedFactory;
        }
        
        if (freeCoastTiles == 1 && numShipyards >= 2) {
            System.out.println("Factory will be placed on the last free COAST tile.");
            System.out.println("(You have " + numShipyards + " shipyards, so this is allowed)");
            return selectedFactory;
        }
        
        // Need to overbuild a default factory
        System.out.println("No free tiles available.");
        System.out.println("You must overbuild a default factory to place this new factory.");
        System.out.println();
        
        // Filter overbuild actions for the selected factory type
        java.util.List<Action.OverbuildDefaultFactory> matchingOverbuilds = new java.util.ArrayList<>();
        for (Action action : overbuildActions) {
            if (action instanceof Action.OverbuildDefaultFactory obf) {
                // Check if this overbuild uses the selected factory type
                if (obf.newFactory().getType().equals(selectedFactory.factory().getType())) {
                    matchingOverbuilds.add(obf);
                }
            }
        }
        
        if (matchingOverbuilds.isEmpty()) {
            System.out.println("ERROR: No default factories available to overbuild!");
            System.out.println("(This shouldn't happen - returning to action selection...)");
            return null;
        }
        
        // Show available default factories to overbuild
        System.out.println("Select which DEFAULT FACTORY to overbuild:");
        System.out.println("-".repeat(80));
        
        for (int i = 0; i < matchingOverbuilds.size(); i++) {
            Action.OverbuildDefaultFactory obf = matchingOverbuilds.get(i);
            System.out.printf("[%d] %s%n", 
                i + 1, 
                obf.defaultFactory().getType().getDisplayName());
        }
        
        System.out.println();
        System.out.println("Type 'cancel' to return to action selection.");
        
        while (true) {
            System.out.print("\nSelect default factory to overbuild: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= matchingOverbuilds.size()) {
                    return matchingOverbuilds.get(choice - 1);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
    }
    
    /**
     * Interactive selection of shipyard to build.
     * Allows selection of shipyard level (1-3).
     */
    private Action selectBuildShipyard(List<Action> buildShipyardActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        int freeCoastTiles = board.getFreeCoastTiles();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("BUILD SHIPYARD - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("You have " + freeCoastTiles + " free coast tile(s).");
        System.out.println();
        
        System.out.println("Select Shipyard Level:");
        for (int i = 0; i < buildShipyardActions.size(); i++) {
            Action.BuildShipyard action = (Action.BuildShipyard) buildShipyardActions.get(i);
            System.out.printf("[%d] Shipyard Level %d\n", i + 1, action.level());
        }
        System.out.println();
        System.out.println("Type 'cancel' to return to action selection.");
        
        while (true) {
            System.out.print("\nSelect shipyard level: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= buildShipyardActions.size()) {
                    return buildShipyardActions.get(choice - 1);
                } else {
                    System.out.println("Invalid choice! Enter a number between 1 and " + buildShipyardActions.size() + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
    }
    
    /**
     * Interactive selection of ships to build.
     * Allows step-by-step selection of ship type, level, and amount.
     */
    private Action selectBuildShips(List<Action> buildShipsActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        int numShipyards = board.getShipyards().size();
        
        System.out.println("\\n" + "=".repeat(80));
        System.out.println("BUILD SHIPS - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("You have " + numShipyards + " shipyard(s).");
        System.out.println("You can build up to " + numShipyards + " ship(s) per action.");
        System.out.println();
        
        // Step 1: Select ship type
        System.out.println("Select Ship Type:");
        System.out.println("[1] Trade Ship");
        System.out.println("[2] Explorer Ship");
        System.out.println();
        System.out.println("Type 'cancel' to return to action selection.");
        
        ShipType selectedType = null;
        while (selectedType == null) {
            System.out.print("\\nSelect ship type: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            if (input.equals("1")) {
                selectedType = ShipType.TradeShip;
            } else if (input.equals("2")) {
                selectedType = ShipType.ExplorerShip;
            } else {
                System.out.println("Invalid choice! Enter 1 or 2.");
            }
        }
        
        // Step 2: Select ship level
        System.out.println("\\nSelect Ship Level:");
        System.out.println("[1] Level 1 (costs: Planks, Sails)");
        System.out.println("[2] Level 2 (costs: Planks, Sails, Steelbars)");
        System.out.println("[3] Level 3 (costs: Planks, Sails, Steelbars, Cannons)");
        
        int selectedLevel = 0;
        while (selectedLevel == 0) {
            System.out.print("\\nSelect ship level: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int level = Integer.parseInt(input);
                if (level >= 1 && level <= 3) {
                    selectedLevel = level;
                } else {
                    System.out.println("Invalid choice! Enter 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
        
        // Step 3: Select amount
        System.out.println("\\nSelect Amount:");
        System.out.println("(You can build 1-" + numShipyards + " ships)");
        
        int selectedAmount = 0;
        while (selectedAmount == 0) {
            System.out.print("\\nEnter amount: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int amount = Integer.parseInt(input);
                if (amount >= 1 && amount <= numShipyards) {
                    selectedAmount = amount;
                } else {
                    System.out.println("Invalid amount! Must be between 1 and " + numShipyards + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
        
        // Find matching action
        for (Action action : buildShipsActions) {
            if (action instanceof Action.BuildShips bs) {
                if (bs.shipType() == selectedType && bs.level() == selectedLevel && bs.amount() == selectedAmount) {
                    return bs;
                }
            }
        }
        
        System.out.println("\nError: Could not find matching BuildShips action!");
        System.out.println("(The selected combination may not be valid)");
        return null;
    }
    
    /**
     * Interactive selection of residents to upgrade.
     * Allows step-by-step selection of residents (up to 3).
     */
    private Action selectUpgradeResident(List<Action> upgradeActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        List<Resident> residents = board.getResidents();
        
        if (residents.isEmpty()) {
            System.out.println("No residents available to upgrade!");
            return null;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("UPGRADE RESIDENTS - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("You can upgrade up to 3 residents.");
        System.out.println("Type 'all' to see all upgrade combinations.");
        System.out.println("Type 'cancel' to return to action selection.");
        System.out.println();
        
        List<Resident> selectedResidents = new ArrayList<>();
        
        while (selectedResidents.size() < 3) {
            System.out.println("\nAvailable Residents:");
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < residents.size(); i++) {
                Resident resident = residents.get(i);
                String selectedMarker = selectedResidents.contains(resident) ? " [SELECTED]" : "";
                String status = resident.getStatus() == ResidentStatus.FIT ? "FIT" : "EXHAUSTED";
                System.out.printf("[%d] %s (Level %d, %s)%s%n", 
                    i + 1, 
                    resident.getClass().getSimpleName(),
                    resident.getPopulationLevel(),
                    status,
                    selectedMarker);
            }
            
            System.out.println();
            if (selectedResidents.isEmpty()) {
                System.out.println("Select first resident to upgrade (or 'all' for full list, 'cancel' to abort):");
            } else {
                System.out.println("Selected residents: " + selectedResidents.size());
                System.out.println("Select next resident, type 'submit' to confirm, or 'cancel' to abort:");
            }
            
            System.out.print("Your choice: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            if (input.equals("all")) {
                return selectFromAllUpgradeCombinations(upgradeActions);
            }
            
            if (input.equals("submit")) {
                if (selectedResidents.isEmpty()) {
                    System.out.println("You must select at least one resident before submitting!");
                    continue;
                }
                return findMatchingUpgradeAction(upgradeActions, selectedResidents);
            }
            
            try {
                int residentIndex = Integer.parseInt(input) - 1;
                if (residentIndex >= 0 && residentIndex < residents.size()) {
                    Resident selectedResident = residents.get(residentIndex);
                    if (selectedResidents.contains(selectedResident)) {
                        System.out.println("Resident already selected! Choose a different resident.");
                    } else {
                        selectedResidents.add(selectedResident);
                        System.out.println("\u2713 Added resident to selection.");
                        
                        if (selectedResidents.size() == 3) {
                            return findMatchingUpgradeAction(upgradeActions, selectedResidents);
                        }
                    }
                } else {
                    System.out.println("Invalid resident number!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a resident number, 'submit', 'all', or 'cancel'.");
            }
        }
        
        return null;
    }
    
    /**
     * Show all upgrade combinations and let player choose.
     */
    private Action selectFromAllUpgradeCombinations(List<Action> upgradeActions) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL UPGRADE COMBINATIONS (" + upgradeActions.size() + " total)");
        System.out.println("=".repeat(80));
        
        for (int i = 0; i < upgradeActions.size(); i++) {
            Action.UpgradeResident action = (Action.UpgradeResident) upgradeActions.get(i);
            System.out.printf("[%d] Upgrade %d resident(s): ", i + 1, action.residents().length);
            for (int j = 0; j < action.residents().length; j++) {
                if (j > 0) System.out.print(", ");
                Resident r = action.residents()[j];
                System.out.print(r.getClass().getSimpleName() + " L" + r.getPopulationLevel());
            }
            System.out.println();
        }
        
        while (true) {
            System.out.print("\nSelect combination (1-" + upgradeActions.size() + ") or 'cancel': ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= upgradeActions.size()) {
                    return upgradeActions.get(choice - 1);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input!");
            }
        }
    }
    
    /**
     * Find the action that matches the selected residents.
     */
    private Action findMatchingUpgradeAction(List<Action> upgradeActions, List<Resident> selectedResidents) {
        for (Action action : upgradeActions) {
            Action.UpgradeResident upgradeAction = (Action.UpgradeResident) action;
            
            if (upgradeAction.residents().length == selectedResidents.size()) {
                boolean allMatch = true;
                for (Resident resident : selectedResidents) {
                    boolean found = false;
                    for (Resident actionResident : upgradeAction.residents()) {
                        if (resident == actionResident) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allMatch = false;
                        break;
                    }
                }
                
                if (allMatch) {
                    return upgradeAction;
                }
            }
        }
        
        System.out.println("Error: Could not find matching upgrade action!");
        return null;
    }
    
    /**
     * Interactive selection of resident level to settle.
     */
    private Action selectSettleResident(List<Action> settleActions) {
        Player currentPlayer = game.getCurrentPlayer();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SETTLE RESIDENT - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("Type 'cancel' to return to action selection.");
        System.out.println();
        
        // Group actions by population level
        java.util.Map<Integer, Action.SettleResident> levelMap = new java.util.TreeMap<>();
        for (Action action : settleActions) {
            if (action instanceof Action.SettleResident sr) {
                levelMap.put(sr.level(), sr);
            }
        }
        
        System.out.println("Available Resident Levels to Settle:");
        System.out.println("-".repeat(80));
        
        java.util.List<Integer> levels = new java.util.ArrayList<>(levelMap.keySet());
        for (int i = 0; i < levels.size(); i++) {
            int level = levels.get(i);
            String levelName = getLevelName(level);
            System.out.printf("[%d] Level %d - %s\n", i + 1, level, levelName);
        }
        
        System.out.println();
        
        while (true) {
            System.out.print("Select resident level to settle: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= levels.size()) {
                    int selectedLevel = levels.get(choice - 1);
                    return levelMap.get(selectedLevel);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
    }
    
    /**
     * Interactive selection of ResidentCard to fulfill.
     * Shows all resident cards with indication if they can be fulfilled.
     */
    private Action selectFulfillNeeds(List<Action> fulfillActions) {
        Player currentPlayer = game.getCurrentPlayer();
        PlayerBoard board = currentPlayer.getPlayerBoard();
        List<ResidentCard> availableCards = board.getResidentCards();
        
        if (availableCards.isEmpty()) {
            System.out.println("No resident cards available to fulfill!");
            return null;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FULFILL NEEDS - Interactive Selection");
        System.out.println("=".repeat(80));
        System.out.println("Type 'cancel' to return to action selection.");
        System.out.println();
        
        // Map each card to its possible fulfill actions
        java.util.Map<ResidentCard, java.util.List<Action.FulfillNeeds>> cardToActions = new java.util.LinkedHashMap<>();
        for (Action action : fulfillActions) {
            if (action instanceof Action.FulfillNeeds fn) {
                cardToActions.computeIfAbsent(fn.residentCard(), k -> new java.util.ArrayList<>()).add(fn);
            }
        }
        
        System.out.println("Available Resident Cards:");
        System.out.println("-".repeat(80));
        
        java.util.List<ResidentCard> cardsList = new java.util.ArrayList<>(cardToActions.keySet());
        for (int i = 0; i < cardsList.size(); i++) {
            ResidentCard card = cardsList.get(i);
            boolean canFulfill = cardToActions.containsKey(card) && !cardToActions.get(card).isEmpty();
            String status = canFulfill ? "(can play card)" : "(cannot play card)";
            
            System.out.printf("[%d] Level %d - Needs: %s → Reward: %s %s\n", 
                i + 1, 
                card.populationLevel(),
                formatNeeds(card.needs()),
                card.reward(),
                status);
        }
        
        System.out.println();
        
        while (true) {
            System.out.print("Select card to fulfill: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= cardsList.size()) {
                    ResidentCard selectedCard = cardsList.get(choice - 1);
                    java.util.List<Action.FulfillNeeds> actionsForCard = cardToActions.get(selectedCard);
                    
                    if (actionsForCard == null || actionsForCard.isEmpty()) {
                        System.out.println("This card cannot be fulfilled (missing goods)!");
                        continue;
                    }
                    
                    // If only one way to fulfill, return it
                    if (actionsForCard.size() == 1) {
                        return actionsForCard.get(0);
                    }
                    
                    // Multiple ways to fulfill - let user choose
                    return selectFulfillNeedsOption(actionsForCard, selectedCard);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Enter a number or 'cancel'.");
            }
        }
    }
    
    /**
     * Select from multiple ways to fulfill a card's needs.
     */
    private Action selectFulfillNeedsOption(java.util.List<Action.FulfillNeeds> options, ResidentCard card) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Multiple ways to fulfill this card:");
        System.out.println("-".repeat(80));
        
        for (int i = 0; i < options.size(); i++) {
            Action.FulfillNeeds action = options.get(i);
            System.out.printf("[%d] Using goods: %s\n", i + 1, formatNeeds(action.goods()));
        }
        
        System.out.println();
        
        while (true) {
            System.out.print("Select option (or 'cancel'): ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("cancel")) {
                return null;
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                } else {
                    System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input!");
            }
        }
    }
    
    /**
     * Get the name of a population level.
     */
    private String getLevelName(int level) {
        return switch (level) {
            case 1 -> "Farmer";
            case 2 -> "Worker";
            case 3 -> "Artisan";
            case 4 -> "Engineer";
            case 5 -> "Investor";
            default -> "Unknown";
        };
    }
    
    /**
     * Format card needs for display.
     */
    private String formatNeeds(Goods[] needs) {
        if (needs == null || needs.length == 0) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < needs.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(needs[i]);
        }
        return sb.toString();
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
            
            // Save to file if in test mode
            if (game.isTestMode()) {
                actionCounter++;
                saveTestGameState(currentState, "action_" + actionCounter);
            }
            
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
        return prev.resources().gold() != curr.resources().gold() ||
               prev.buildings().factoryCount() != curr.buildings().factoryCount() ||
               prev.residents().count() != curr.residents().count() ||
               prev.cards().residentCardCount() != curr.cards().residentCardCount() ||
               prev.ships().tradeShips().totalCount() != curr.ships().tradeShips().totalCount() ||
               prev.ships().explorerShips().totalCount() != curr.ships().explorerShips().totalCount() ||
               prev.buildings().shipyardCount() != curr.buildings().shipyardCount();
    }
    
    /**
     * Print differences in player state.
     */
    private void printPlayerDifferences(GameState.PlayerState prev, GameState.PlayerState curr) {
        if (prev.resources().gold() != curr.resources().gold()) {
            System.out.printf("  Gold: %d → %d (%+d)\n", prev.resources().gold(), curr.resources().gold(), 
                curr.resources().gold() - prev.resources().gold());
        }
        if (prev.buildings().factoryCount() != curr.buildings().factoryCount()) {
            System.out.printf("  Factories: %d → %d (%+d)\n", prev.buildings().factoryCount(), curr.buildings().factoryCount(), 
                curr.buildings().factoryCount() - prev.buildings().factoryCount());
        }
        if (prev.residents().count() != curr.residents().count()) {
            System.out.printf("  Residents: %d → %d (%+d)\n", prev.residents().count(), curr.residents().count(), 
                curr.residents().count() - prev.residents().count());
        }
        if (prev.cards().residentCardCount() != curr.cards().residentCardCount()) {
            System.out.printf("  Resident Cards: %d → %d (%+d)\n", prev.cards().residentCardCount(), curr.cards().residentCardCount(), 
                curr.cards().residentCardCount() - prev.cards().residentCardCount());
        }
        if (prev.ships().tradeShips().totalCount() != curr.ships().tradeShips().totalCount()) {
            System.out.printf("  Trade Ships: %d → %d (%+d)\n", prev.ships().tradeShips().totalCount(), curr.ships().tradeShips().totalCount(), 
                curr.ships().tradeShips().totalCount() - prev.ships().tradeShips().totalCount());
        }
        if (prev.ships().explorerShips().totalCount() != curr.ships().explorerShips().totalCount()) {
            System.out.printf("  Explorer Ships: %d → %d (%+d)\n", prev.ships().explorerShips().totalCount(), curr.ships().explorerShips().totalCount(), 
                curr.ships().explorerShips().totalCount() - prev.ships().explorerShips().totalCount());
        }
    }
    
    /**
     * Check if board state has changed.
     */
    private boolean hasBoardStateChanged(GameState.BoardState prev, GameState.BoardState curr) {
        return prev.resources().gold() != curr.resources().gold() ||
               prev.resources().farmers() != curr.resources().farmers() ||
               prev.resources().workers() != curr.resources().workers() ||
               prev.factories().availableFactories() != curr.factories().availableFactories();
    }
    
    /**
     * Print differences in board state.
     */
    private void printBoardDifferences(GameState.BoardState prev, GameState.BoardState curr) {
        if (prev.resources().gold() != curr.resources().gold()) {
            System.out.printf("  Gold pool: %d → %d (%+d)\n", prev.resources().gold(), curr.resources().gold(), curr.resources().gold() - prev.resources().gold());
        }
        if (prev.resources().farmers() != curr.resources().farmers()) {
            System.out.printf("  Farmers: %d → %d (%+d)\n", prev.resources().farmers(), curr.resources().farmers(), curr.resources().farmers() - prev.resources().farmers());
        }
        if (prev.resources().workers() != curr.resources().workers()) {
            System.out.printf("  Workers: %d → %d (%+d)\n", prev.resources().workers(), curr.resources().workers(), curr.resources().workers() - prev.resources().workers());
        }
        if (prev.factories().availableFactories() != curr.factories().availableFactories()) {
            System.out.printf("  Available Factories: %d → %d (%+d)\n", prev.factories().availableFactories(), 
                curr.factories().availableFactories(), curr.factories().availableFactories() - prev.factories().availableFactories());
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
            case Action.ViewResidentCards vrc -> 
                "View Resident Cards (FREE ACTION)";
            case Action.UseExtraAction uea -> 
                "Use Extra Action (3 Gold + 3 Explorer Chips) (FREE ACTION)";
            case Action.DiscardResidentCardAction drca -> 
                String.format("Discard Resident Card Level %d (2 Explorer Chips) (FREE ACTION)", 
                    drca.card().populationLevel());
            case Action.InvestorGoldAction iga -> 
                "Exhaust Investor for 5 Gold (FREE ACTION)";
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
        System.out.printf("This is a %d-player terminal-based game.\n", game.getPlayers().length);
        if (game.isTestMode()) {
            System.out.println("TEST MODE: Game will run for " + game.getMaxRounds() + " rounds without shuffling.");
        }
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
     * Create a new test game directory with auto-incrementing number.
     */
    private String createTestGameDirectory() {
        try {
            Path baseDir = Paths.get(GAME_STATES_DIR);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
            
            // Find the next available test game number
            int testNum = 1;
            Path testDir;
            do {
                testDir = baseDir.resolve(String.format("Testgame-%02d", testNum));
                testNum++;
            } while (Files.exists(testDir));
            
            Files.createDirectories(testDir);
            return testDir.toString();
        } catch (IOException e) {
            System.err.println("Warning: Could not create test game directory: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Save game state to file in test game directory.
     */
    private void saveTestGameState(GameState state, String label) {
        if (testGameDir == null) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/%s_%s.json", testGameDir, label, timestamp);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("{");
                writer.printf("  \"timestamp\": \"%s\",%n", state.timestamp());
                writer.printf("  \"round\": %d,%n", state.round());
                writer.printf("  \"currentPlayer\": %d,%n", state.currentPlayerIndex());
                writer.printf("  \"label\": \"%s\",%n", label);
                
                // Complete Board State
                writer.println("  \"boardState\": {");
                writer.println("    \"factories\": {");
                writer.printf("      \"availableFactories\": %d%n", state.boardState().factories().availableFactories());
                writer.println("    },");
                writer.println("    \"shipyards\": {");
                writer.printf("      \"level1\": %d,%n", state.boardState().shipyards().level1());
                writer.printf("      \"level2\": %d,%n", state.boardState().shipyards().level2());
                writer.printf("      \"level3\": %d%n", state.boardState().shipyards().level3());
                writer.println("    },");
                writer.println("    \"ships\": {");
                writer.println("      \"tradeShips\": {");
                writer.printf("        \"level1\": %d,%n", state.boardState().ships().tradeShips().level1());
                writer.printf("        \"level2\": %d,%n", state.boardState().ships().tradeShips().level2());
                writer.printf("        \"level3\": %d%n", state.boardState().ships().tradeShips().level3());
                writer.println("      },");
                writer.println("      \"explorerShips\": {");
                writer.printf("        \"level1\": %d,%n", state.boardState().ships().explorerShips().level1());
                writer.printf("        \"level2\": %d,%n", state.boardState().ships().explorerShips().level2());
                writer.printf("        \"level3\": %d%n", state.boardState().ships().explorerShips().level3());
                writer.println("      }");
                writer.println("    },");
                writer.println("    \"cards\": {");
                writer.printf("      \"residentStack1\": %d,%n", state.boardState().residentCards().stack1Size());
                writer.printf("      \"residentStack2\": %d,%n", state.boardState().residentCards().stack2Size());
                writer.printf("      \"residentStack3\": %d,%n", state.boardState().residentCards().stack3Size());
                writer.printf("      \"totalResidentCards\": %d,%n", state.boardState().residentCards().totalAvailable());
                writer.printf("      \"expeditionCards\": %d%n", state.boardState().expeditions().stackSize());
                writer.println("    },");
                writer.println("    \"islands\": {");
                writer.printf("      \"oldWorldIslands\": %d,%n", state.boardState().islands().oldWorldSize());
                writer.printf("      \"newWorldIslands\": %d%n", state.boardState().islands().newWorldSize());
                writer.println("    },");
                writer.println("    \"populationPool\": {");
                writer.printf("      \"farmers\": %d,%n", state.boardState().resources().farmers());
                writer.printf("      \"workers\": %d,%n", state.boardState().resources().workers());
                writer.printf("      \"artisans\": %d,%n", state.boardState().resources().artisans());
                writer.printf("      \"engineers\": %d,%n", state.boardState().resources().engineers());
                writer.printf("      \"investors\": %d%n", state.boardState().resources().investors());
                writer.println("    },");
                writer.println("    \"resources\": {");
                writer.printf("      \"goldPool\": %d,%n", state.boardState().resources().gold());
                writer.printf("      \"tradeChips\": %d,%n", state.boardState().resources().tradeChips());
                writer.printf("      \"explorerChips\": %d%n", state.boardState().resources().explorerChips());
                writer.println("    }");
                writer.println("  },");
                
                // Complete Player States
                writer.println("  \"players\": [");
                for (int i = 0; i < state.players().size(); i++) {
                    GameState.PlayerState player = state.players().get(i);
                    writer.println("    {");
                    writer.printf("      \"name\": \"%s\",%n", player.name());
                    writer.printf("      \"position\": %d,%n", player.position());
                    writer.println("      \"tiles\": {");
                    writer.printf("        \"freeLand\": %d,%n", player.tiles().freeLandTiles());
                    writer.printf("        \"freeCoast\": %d,%n", player.tiles().freeCoastTiles());
                    writer.printf("        \"freeSea\": %d%n", player.tiles().freeSeaTiles());
                    writer.println("      },");
                    writer.println("      \"buildings\": {");
                    writer.printf("        \"factories\": %d,%n", player.buildings().factoryCount());
                    writer.printf("        \"shipyards\": %d%n", player.buildings().shipyardCount());
                    writer.println("      },");
                    writer.println("      \"ships\": {");
                    writer.printf("        \"tradeShips\": %d,%n", player.ships().tradeShips().totalCount());
                    writer.printf("        \"explorerShips\": %d%n", player.ships().explorerShips().totalCount());
                    writer.println("      },");
                    writer.println("      \"resources\": {");
                    writer.printf("        \"gold\": %d,%n", player.resources().gold());
                    writer.printf("        \"tradeChips\": %d,%n", player.resources().availableTradeChips());
                    writer.printf("        \"explorerChips\": %d%n", player.resources().availableExplorerChips());
                    writer.println("      },");
                    writer.println("      \"cards\": {");
                    writer.printf("        \"residentCards\": %d%n", player.cards().residentCardCount());
                    writer.println("      },");
                    writer.println("      \"residents\": {");
                    writer.printf("        \"total\": %d,%n", player.residents().count());
                    
                    // Count residents by level
                    int[] levelCounts = new int[6]; // 0-5, we use 1-5
                    int onBoard = 0, working = 0, fit = 0, exhausted = 0;
                    for (var resident : player.residents().residents()) {
                        levelCounts[resident.level()]++;
                        switch (resident.status()) {
                            case "ON_BOARD" -> onBoard++;
                            case "AT_WORK" -> working++;
                            case "FIT" -> fit++;
                            case "EXHAUSTED" -> exhausted++;
                        }
                    }
                    
                    writer.println("        \"byLevel\": {");
                    writer.printf("          \"level1\": %d,%n", levelCounts[1]);
                    writer.printf("          \"level2\": %d,%n", levelCounts[2]);
                    writer.printf("          \"level3\": %d,%n", levelCounts[3]);
                    writer.printf("          \"level4\": %d,%n", levelCounts[4]);
                    writer.printf("          \"level5\": %d%n", levelCounts[5]);
                    writer.println("        },");
                    writer.println("        \"byStatus\": {");
                    writer.printf("          \"onBoard\": %d,%n", onBoard);
                    writer.printf("          \"working\": %d,%n", working);
                    writer.printf("          \"fit\": %d,%n", fit);
                    writer.printf("          \"exhausted\": %d%n", exhausted);
                    writer.println("        }");
                    writer.println("      }");
                    writer.print("    }");
                    if (i < state.players().size() - 1) {
                        writer.println(",");
                    } else {
                        writer.println();
                    }
                }
                
                writer.println("  ]");
                writer.println("}");
            }
            
            System.out.println("  [Saved test game state: " + filename + "]");
        } catch (IOException e) {
            System.err.println("Warning: Could not save test game state: " + e.getMessage());
        }
    }
    
    /**
     * Main entry point for terminal game.
     * 
     * Supports CLI args for scripted/debug mode:
     *   debug [seed] [numPlayers] [maxRounds]
     *   Example: debug 42 3 200
     * If no args are given, interactive mode is used.
     */
    public static void main(String[] args) {
        // Check for debug/seeded mode via CLI args
        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            long seed = args.length > 1 ? Long.parseLong(args[1]) : System.currentTimeMillis();
            int numPlayers = args.length > 2 ? Integer.parseInt(args[2]) : 3;
            int maxRounds = args.length > 3 ? Integer.parseInt(args[3]) : 200;
            System.out.println("=".repeat(80));
            System.out.println("DEBUG MODE - Seeded Game");
            System.out.printf("  Players: %d | Seed: %d | Max rounds: %d%n", numPlayers, seed, maxRounds);
            System.out.println("=".repeat(80));
            System.out.println("\nInitializing game...");
            TerminalGameUI ui = new TerminalGameUI(numPlayers, seed, maxRounds);
            ui.start();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
        // Ask for number of players
        int numPlayers = 2;
        while (true) {
            System.out.print("How many players? (1-4): ");
            try {
                String input = scanner.nextLine().trim();
                numPlayers = Integer.parseInt(input);
                if (numPlayers >= 1 && numPlayers <= 4) {
                    break;
                }
                System.out.println("Please enter a number between 1 and 4.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Ask if test game
        boolean testMode = false;
        System.out.print("Start test game? (y/n): ");
        String testInput = scanner.nextLine().trim().toLowerCase();
        testMode = testInput.equals("y") || testInput.equals("yes");
        
        // Ask for max rounds if test mode
        int maxRounds = 10;
        if (testMode) {
            while (true) {
                System.out.print("How many rounds for the test game? ");
                try {
                    String input = scanner.nextLine().trim();
                    maxRounds = Integer.parseInt(input);
                    if (maxRounds >= 1) {
                        break;
                    }
                    System.out.println("Please enter a positive number.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
        }
        
        System.out.println("\nInitializing game...");
        TerminalGameUI ui = new TerminalGameUI(numPlayers, testMode, maxRounds);
        ui.start();
    }
}
