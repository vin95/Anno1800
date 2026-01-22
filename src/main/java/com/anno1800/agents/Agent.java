package com.anno1800.agents;

import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;

/**
 * Interface for game agents that can select actions.
 * Agents can be AI players, rule-based systems, or even human input wrappers.
 */
public interface Agent {
    
    /**
     * Select an action from the list of possible actions.
     * 
     * @param gameState The current game state (immutable snapshot)
     * @param possibleActions List of all valid actions the player can take
     * @param player The player this agent controls
     * @return The chosen action, or null if no action should be taken (pass turn)
     */
    Action selectAction(GameState gameState, List<Action> possibleActions, Player player);
    
    /**
     * Get the name/identifier of this agent.
     * 
     * @return Agent name for logging/display
     */
    String getName();
}
