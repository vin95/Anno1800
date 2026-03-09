package com.anno1800.game.engine;

import com.anno1800.agents.Agent;
import com.anno1800.agents.ObjectiveContext;
import com.anno1800.agents.AgentImpl.ScoringAgent;
import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionGenerator;
import com.anno1800.game.actions.ActionHandler;
import com.anno1800.game.actions.ActionResult;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.player.Player;
import com.anno1800.game.state.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main game controller that manages the board and game state
 */
public class Game {
    private final Board board;
    private Player[] players;
    private Agent[] agents;
    private final ActionHandler actionHandler;
    private final ActionGenerator actionGenerator;
    
    // Game state tracking
    private int currentRound;
    private final int startPlayer;
    private int currentPlayer;
    private Integer endPhaseTriggeredInRound = null; // Track WHEN end phase was triggered
    
    // Game configuration
    private final int maxRounds; // Maximum rounds per game
    private final boolean testMode; // If true, no shuffling and deterministic start player
    private static final int ACTIONS_PER_TURN = 1; // How many actions each player can take per turn
    
    // Active ObjectiveCards for this game (5 drawn at game start)
    private List<ObjectiveCard> activeObjectiveCards = new ArrayList<>();
    
    // Cached ObjectiveContext (computed once after ObjectiveCards are drawn)
    private ObjectiveContext objectiveContext;
    
    public Game(int numPlayers) {
        this(numPlayers, false, 10);
    }
    
    public Game(int numPlayers, boolean testMode, int maxRounds) {
        this.testMode = testMode;
        this.maxRounds = maxRounds;
        this.board = Board.initializeBoard(numPlayers, testMode);
        this.players = Player.initializePlayers(numPlayers, this.board);
        this.agents = new Agent[numPlayers]; // Will be set via setAgent()
        
        // Initialize game state
        this.currentRound = 1;
        // In test mode, always use player 0 as start player for deterministic behavior
        this.startPlayer = testMode ? 0 : (int) (Math.random() * numPlayers);
        for (int i = 0; i < players.length; i++) {
            int position = ((i - startPlayer + numPlayers) % numPlayers) + 1;
            players[i].setPosition(position);
        }
        this.currentPlayer = this.startPlayer;
        
        // Initialize player boards after positions are set
        inicializeGame();
        
        // Draw 5 ObjectiveCards for the game
        initializeObjectiveCards();
        
        this.actionHandler = new ActionHandler(this);
        this.actionGenerator = new ActionGenerator();
    }
    
    /**
     * Initialize the 5 active ObjectiveCards for this game.
     */
    private void initializeObjectiveCards() {
        List<ObjectiveCard> deck = testMode ? 
            ObjectiveCard.createDeck() : 
            ObjectiveCard.createShuffledDeck();
        
        // Draw 5 cards (or all if fewer than 5)
        int cardsToDraw = Math.min(5, deck.size());
        for (int i = 0; i < cardsToDraw; i++) {
            activeObjectiveCards.add(deck.get(i));
        }
        
        System.out.println("Active Objective Cards:");
        for (ObjectiveCard card : activeObjectiveCards) {
            System.out.println("  - " + card.getTitle() + ": " + card.getDescription());
        }
        
        // Compute and cache ObjectiveContext (fixed parameters, won't change during game)
        this.objectiveContext = ObjectiveContext.compute(activeObjectiveCards, players);
        System.out.println("Expected game length: ~" + objectiveContext.expectedGameLength() + " rounds");
    }
    
    /**
     * Get the list of active ObjectiveCards for this game.
     * 
     * @return Unmodifiable list of active ObjectiveCards
     */
    public List<ObjectiveCard> getActiveObjectiveCards() {
        return Collections.unmodifiableList(activeObjectiveCards);
    }
    
    /**
     * Execute a player action.
     * 
     * @param action The action to execute
     * @return true if action was successful
     */
    public ActionResult executeAction(Action action) {
        return actionHandler.execute(action, getCurrentPlayer());
    }
    
    /**
     * Advance to the next round.
     * Called when all players have completed their turns.
     */
    public void nextRound() {
        // Check if end phase was just triggered in the current round
        if (board.isEndPhase() && endPhaseTriggeredInRound == null) {
            endPhaseTriggeredInRound = currentRound;
            System.out.println("*** END PHASE TRIGGERED in Round " + currentRound + " ***");
            System.out.println("*** Game will end after 1 more complete round ***");
        }
        
        currentRound++;
        currentPlayer = 0;  // Reset to first player
        
        // Display round info
        if (endPhaseTriggeredInRound != null) {
            int roundsSinceTrigger = currentRound - endPhaseTriggeredInRound;
            if (roundsSinceTrigger == 1) {
                System.out.println("=== Round " + currentRound + " begins (FINAL ROUND) ===");
            }
        } else {
            System.out.println("=== Round " + currentRound + " begins ===");
        }
    }
    
    /**
     * Advance to the next player.
     * If it was the last player, advance to the next round.
     */
    public void nextPlayer() {
        currentPlayer++;
        
        if (currentPlayer >= players.length) {
            // All players have finished - start new round
            nextRound();
        }
    }
    
    /**
     * Get the current round number.
     * 
     * @return Current round (starts at 1)
     */
    public int getCurrentRound() {
        return currentRound;
    }
    
    /**
     * Get the index of the current player.
     * 
     * @return Current player index (0-based)
     */
    public int getCurrentPlayerIndex() {
        return currentPlayer;
    }
    
    /**
     * Get the current player.
     * 
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }
    
    /**
     * Create a snapshot of the current game state.
     * 
     * @return Immutable GameState snapshot
     */
    public GameState getState() {
        return GameState.createSnapshot(
            board,
            players,
            currentRound,
            currentPlayer,
            startPlayer
        );
    }
    
    /**
     * Check if the game has ended.
     * Game ends after maxRounds OR when end phase was triggered and 1 additional round was played.
     * 
     * End phase logic:
     * - When triggered in round N: finish round N, play round N+1, then game over
     * - Example: Triggered in round 5 → finish round 5 → play round 6 → game ends before round 7
     * - Formula: Game ends when currentRound > endPhaseTriggeredInRound + 1
     * 
     * @return true if game is over
     */
    public boolean isGameOver() {
        // Normal end: exceeded max rounds
        if (currentRound > maxRounds) {
            return true;
        }
        
        // End phase triggered: check if we've completed trigger round + 1 final round
        // currentRound > triggerRound + 1 means we're trying to start a 3rd round after trigger
        if (endPhaseTriggeredInRound != null && currentRound > endPhaseTriggeredInRound + 1) {
            return true;
        }
        
        return false;
    }

    /**
     * Start the game and run the main game loop.
     * Each round, all players take their turns in order.
     * Game continues until isGameOver() returns true.
     */
    public void start() {
        System.out.println("=== Game Start ===");
        System.out.println("Players: " + players.length);
        System.out.println("Max Rounds: " + maxRounds);
        if (testMode) {
            System.out.println("TEST MODE: No shuffling, deterministic start player");
        }
        System.out.println("Starting Player: Player " + (startPlayer + 1));
        System.out.println();
        
        // Validate that all players have agents assigned
        for (int i = 0; i < players.length; i++) {
            if (agents[i] == null) {
                throw new IllegalStateException("Agent not set for Player " + (i + 1) + 
                    ". Use setAgent() before starting the game.");
            }
        }
        
        // Main game loop
        while (!isGameOver()) {
            playRound();
        }
        
        // Game over
        System.out.println("\n=== Game Over ===");
        System.out.println("Game completed after " + currentRound + " rounds.");
    }
    
    /**
     * Play one complete round where each player takes their turn.
     */
    private void playRound() {
        System.out.println("\n=== Round " + currentRound + " ===");
        
        // Each player takes their turn in order
        for (int i = 0; i < players.length; i++) {
            currentPlayer = (startPlayer + i) % players.length;
            playTurn(currentPlayer);
        }
        
        // Move to next round
        nextRound();
    }
    
    /**
     * Execute one player's turn.
     * The player's agent selects and executes actions.
     * 
     * @param playerIndex Index of the player taking the turn
     */
    private void playTurn(int playerIndex) {
        Player player = players[playerIndex];
        Agent agent = agents[playerIndex];
        
        System.out.println("\n--- Player " + (playerIndex + 1) + "'s Turn (Agent: " + agent.getName() + ") ---");
        
        // Player can take multiple actions per turn
        for (int actionNum = 0; actionNum < ACTIONS_PER_TURN; actionNum++) {
            // Generate all possible actions
            List<Action> possibleActions = actionGenerator.getPossibleActions(player, this);
            
            if (possibleActions.isEmpty()) {
                System.out.println("  No valid actions available. Turn ends.");
                break;
            }
            
            // Let agent choose an action
            GameState gameState = getState();
            Action chosenAction = agent.selectAction(gameState, possibleActions, player);
            
            if (chosenAction == null) {
                System.out.println("  Player passes. Turn ends.");
                break;
            }
            
            // Execute the chosen action
            System.out.println("  Executing: " + chosenAction);
            ActionResult result = executeAction(chosenAction);
            System.out.println("  Action executed. Result: " + result);
        }
        
        // Clear turn-specific state
        // Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
        player.getPlayerBoard().clearTradedGoodsThisTurn();
        
        // Clear free action flags for ObjectiveCard actions
        player.getPlayerBoard().clearFreeActionFlagsThisTurn();
    }
    
    /**
     * Set the agent for a specific player.
     * Must be called before start() for all players.
     * 
     * @param playerIndex The player index (0-based)
     * @param agent The agent to control this player
     */
    public void setAgent(int playerIndex, Agent agent) {
        if (playerIndex < 0 || playerIndex >= players.length) {
            throw new IllegalArgumentException("Invalid player index: " + playerIndex);
        }
        this.agents[playerIndex] = agent;        
        // If this is a ScoringAgent, inject the ObjectiveContext
        if (agent instanceof ScoringAgent scoringAgent) {
            scoringAgent.setObjectiveContext(objectiveContext);
        }    }

    private void inicializeGame() {
        for (Player player : players) {
            player.getPlayerBoard().initializePlayerBoard(player, board);
        }
    }
    
    public Board getBoard() {
        return board;
    }

    public int getStartPlayer() {
        return startPlayer;
    }
    
    public Player[] getPlayers() {
        return players;
    }
    
    public boolean isTestMode() {
        return testMode;
    }
    
    public int getMaxRounds() {
        return maxRounds;
    }
}
