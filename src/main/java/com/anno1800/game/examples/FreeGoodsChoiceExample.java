package com.anno1800.game.examples;

import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.actions.Action;
import com.anno1800.game.actions.ActionHandler;
import com.anno1800.game.actions.ActionResult;
import com.anno1800.game.engine.Game;
import com.anno1800.game.player.Player;
import com.anno1800.game.rewards.Reward;

/**
 * Beispiel für die Verwendung von FreeGoodsChoice Rewards.
 * Zeigt den vollständigen Ablauf von der Erstellung bis zur Ausführung.
 */
public class FreeGoodsChoiceExample {

    /**
     * Beispiel: Verwendung des FreeGoodsChoice Rewards mit interaktiver Wahl
     */
    public static void demonstrateFreeGoodsChoice(Player player, Game game) {
        System.out.println("=== FreeGoodsChoice Reward Example ===");
        
        // 1. Erstelle eine FreeGoodsChoice Reward mit verfügbaren Optionen
        Goods[] options = {Goods.CACAO, Goods.COFFEE_BEANS, Goods.RUBBER, Goods.SUGARCANE, Goods.TOBACCO};
        Reward.FreeGoodsChoice originalReward = new Reward.FreeGoodsChoice(options);
        
        System.out.println("Verfügbare Optionen: " + java.util.Arrays.toString(options));
        System.out.println("Hat bereits eine Wahl: " + originalReward.hasChoice());
        
        // 2. FALSCH: Versuche den Reward ohne Wahl zu aktivieren
        try {
            Action.ActivateReward activateWithoutChoice = new Action.ActivateReward(originalReward);
            ActionHandler actionHandler = new ActionHandler(game);
            actionHandler.execute(activateWithoutChoice, player);
        } catch (IllegalStateException e) {
            System.out.println("Erwarteter Fehler: " + e.getMessage());
        }
        
        // 3. RICHTIG: Erste eine Wahl treffen
        Goods chosenGood = Goods.COFFEE_BEANS; // Beispielwahl durch Agent/User
        Action.ChooseGoods chooseAction = new Action.ChooseGoods(originalReward, chosenGood);
        
        ActionHandler actionHandler = new ActionHandler(game);
        ActionResult chooseResult = actionHandler.execute(chooseAction, player);
        
        if (chooseResult instanceof ActionResult.RewardResult rewardResult) {
            Reward.FreeGoodsChoice chosenReward = (Reward.FreeGoodsChoice) rewardResult.reward();
            System.out.println("Gewählte Ware: " + chosenReward.chosenGood());
            System.out.println("Hat jetzt eine Wahl: " + chosenReward.hasChoice());
            
            // 4. Jetzt kann der Reward aktiviert werden
            Action.ActivateReward activateWithChoice = new Action.ActivateReward(chosenReward);
            actionHandler.execute(activateWithChoice, player);
            
            System.out.println("Reward erfolgreich aktiviert! Ware wurde zum Lager hinzugefügt.");
        }
        
        System.out.println("=== Ende des Beispiels ===");
    }
    
    /**
     * Zeigt häufige Fehlerszenarien und wie sie behandelt werden
     */
    public static void demonstrateErrorCases() {
        System.out.println("\\n=== Fehlerszenarien ===");
        
        Goods[] options = {Goods.CACAO, Goods.COFFEE_BEANS};
        Reward.FreeGoodsChoice reward = new Reward.FreeGoodsChoice(options);
        
        // Fehler 1: Ungültige Wahl
        try {
            reward.withChoice(Goods.POTATOES); // POTATOES ist nicht in den Optionen
        } catch (IllegalArgumentException e) {
            System.out.println("Fehler bei ungültiger Wahl: " + e.getMessage());
        }
        
        // Fehler 2: Doppelte Wahl
        try {
            Reward.FreeGoodsChoice chosenOnce = reward.withChoice(Goods.CACAO);
            chosenOnce.withChoice(Goods.COFFEE_BEANS); // Bereits gewählt
        } catch (IllegalStateException e) {
            System.out.println("Fehler bei doppelter Wahl: " + e.getMessage());
        }
        
        System.out.println("=== Ende der Fehlerszenarien ===");
    }
}