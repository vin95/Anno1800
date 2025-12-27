package com.anno1800.engine;

import com.anno1800.actions.Action;
import com.anno1800.actions.ActionGenerator;
import com.anno1800.actions.ActionValidator;
import com.anno1800.player.Player;

import java.util.List;

/**
 * Example showing how to use ActionGenerator and ActionValidator.
 * Demonstrates separation of concerns: Generate → Validate → Execute
 */
public class ActionGeneratorExample {
    
    public static void main(String[] args) {
        // 1. Create game
        Game game = new Game(2);
        Player currentPlayer = game.getCurrentPlayer();
        
        System.out.println("=== Action Generation Demo ===\n");
        System.out.printf("Current Player: %s%n", currentPlayer.getName());
        System.out.printf("Round: %d, Phase: %s%n%n", 
            game.getCurrentRound(), 
            game.getCurrentPhase());
        
        // 2. Generate all possible actions
        ActionGenerator generator = new ActionGenerator();
        List<Action> possibleActions = generator.getPossibleActions(currentPlayer, game);
        
        System.out.printf("Generated %d possible actions:%n", possibleActions.size());
        
        // 3. Display actions
        for (int i = 0; i < possibleActions.size(); i++) {
            Action action = possibleActions.get(i);
            boolean isValid = ActionValidator.canExecute(action, currentPlayer, game);
            
            System.out.printf("%d. %s - Valid: %s%n", 
                i + 1, 
                formatAction(action),
                isValid ? "✓" : "✗");
        }
        
        // 4. Agent would select one action (simplified random selection for demo)
        if (!possibleActions.isEmpty()) {
            Action selectedAction = possibleActions.get(0);
            System.out.printf("%nAgent selected: %s%n", formatAction(selectedAction));
            
            // 5. Validate before execution
            if (ActionValidator.canExecute(selectedAction, currentPlayer, game)) {
                System.out.println("Action is valid, executing...");
                try {
                    game.executeAction(selectedAction);
                    System.out.println("✓ Action executed successfully!");
                } catch (UnsupportedOperationException e) {
                    System.out.println("✗ Action execution not implemented yet");
                }
            } else {
                System.out.println("✗ Action is not valid!");
            }
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static String formatAction(Action action) {
        return switch (action) {
            case Action.BuildFactory bf -> 
                "BuildFactory(" + bf.factory().getType() + ")";
            case Action.EndCurrentPhase ecp -> 
                "EndCurrentPhase";
            case Action.AssignWorker aw -> 
                "AssignWorker";
            default -> action.getClass().getSimpleName();
        };
    }
}
