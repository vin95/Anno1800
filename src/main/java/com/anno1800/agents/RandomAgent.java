package com.anno1800.agents;

import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

import java.util.List;
import java.util.Random;

/**
 * Simple agent that randomly selects from available actions.
 * Useful for testing and as a baseline for comparing other agents.
 */
public class RandomAgent implements Agent {
    private final String name;
    private final Random random;
    
    public RandomAgent(String name) {
        this.name = name;
        this.random = new Random(42); // Fixed seed for reproducibility
    }
    
    public RandomAgent() {
        this("RandomAgent");
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
