package com.anno1800.actions;

import java.util.ArrayList;
import java.util.List;

import com.anno1800.engine.Game;
import com.anno1800.player.Player;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Producers;
import com.anno1800.data.FactoryData;

/**
 * Generates all possible valid actions for a player in the current game state.
 * Separation of Concerns: Action generation is separate from validation and execution.
 */
public class ActionGenerator {
    
    /**
     * Generate all possible actions for a player.
     * 
     * @param player The player to generate actions for
     * @param game The current game state
     * @return List of all valid actions the player can take
     */
    public List<Action> getPossibleActions(Player player, Game game) {
        List<Action> actions = new ArrayList<>();
        
        // Generate BuildFactory actions
        actions.addAll(generateBuildFactoryActions(player, game));
        
        // TODO: Generate other action types
        // actions.addAll(generateBuildShipyardActions(player, game));
        // actions.addAll(generateSettleResidentActions(player, game));
        // etc.
        
        // Always possible: End current phase
        actions.add(new Action.EndCurrentPhase());
        
        return actions;
    }
    
    /**
     * Generate all possible BuildFactory actions.
     * Creates an action for each available factory type that can be built.
     */
    private List<Action> generateBuildFactoryActions(Player player, Game game) {
        List<Action> buildActions = new ArrayList<>();
        
        // Get all available factory types from board
        // TODO: Get actual available factories from game board stacks
        // For now, iterate through all producer types
        
        for (Producers producerType : Producers.values()) {
            try {
                // Get the producer template
                var producer = FactoryData.getProducer(producerType);
                
                // Only consider actual factories (not plantations)
                if (producer instanceof Factory factory) {
                    Action.BuildFactory buildAction = new Action.BuildFactory(factory);
                    
                    // Only add if valid
                    if (ActionValidator.canExecute(buildAction, player, game)) {
                        buildActions.add(buildAction);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Producer type not configured, skip
            }
        }
        
        return buildActions;
    }
    
    /**
     * Generate all possible SettleResident actions.
     */
    private List<Action> generateSettleResidentActions(Player player, Game game) {
        List<Action> settleActions = new ArrayList<>();
        // TODO: Implement
        return settleActions;
    }
    
    /**
     * Generate all possible AssignWorker actions.
     */
    private List<Action> generateAssignWorkerActions(Player player, Game game) {
        List<Action> assignActions = new ArrayList<>();
        // TODO: Implement - for each factory with empty slot and available resident
        return assignActions;
    }
}
