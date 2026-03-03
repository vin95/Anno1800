package com.anno1800.agents.AgentImpl;

import com.anno1800.agents.Agent;
import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;
import java.util.Random;

/**
 * Simple agent that randomly selects from available actions.
 * Useful for testing and as a baseline for comparing other agents.
 * 
 * <p>This agent does NOT use scoring - it simply picks a random action.
 * Use this as a baseline to measure how much better strategic agents perform.
 */
public class AgentRandom implements Agent {
    private final String name;
    private final Random random;
    
    public AgentRandom(String name) {
        this.name = name;
        this.random = new Random(42); // Fixed seed for reproducibility
    }
    
    public AgentRandom() {
        this("AgentRandom");
    }
    
    @Override
    public Action selectAction(GameState gameState, List<Action> possibleActions, Player player) {
        if (possibleActions == null || possibleActions.isEmpty()) {
            return null; // Pass turn if no actions available
        }
        
        // Select a random action from the available options
        int index = random.nextInt(possibleActions.size());
        return possibleActions.get(index);
    }
    
    @Override
    public String getName() {
        return name;
    }
}
