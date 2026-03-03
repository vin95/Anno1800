package com.anno1800.agents.AgentImpl;

import com.anno1800.agents.GameContext;
import com.anno1800.agents.ObjectiveContext;
import com.anno1800.agents.StrategyWeights;
import com.anno1800.game.actions.Action;
import com.anno1800.game.state.GameState;
import com.anno1800.game.player.Player;

/**
 * Agent that scores actions using configurable {@link StrategyWeights} combined
 * with context-sensitive bonuses derived from {@link GameContext}.
 *
 * <pre>
 *   score(action) = basicRules(action, context)
 *                 + baseWeight(actionType)          ← from StrategyWeights
 *                 + contextBonus(action, context)   ← situation-specific
 *                 + objectiveBonus(action)          ← from ObjectiveCards
 * </pre>
 *
 * Create agents with predefined strategies using the static factory methods:
 * <pre>
 *   WeightedScoringAgent.vpRush("Alice")
 *   WeightedScoringAgent.productionFocus("Bob")
 *   WeightedScoringAgent.expansionFocus("Carol")
 *   WeightedScoringAgent.balanced("Dave")
 * </pre>
 *
 * Or supply custom weights for fine-tuned experiments:
 * <pre>
 *   new WeightedScoringAgent("Custom", myWeights)
 * </pre>
 */
public class WeightedScoringAgent extends ScoringAgent {

    private final StrategyWeights weights;

    public WeightedScoringAgent(String name, StrategyWeights weights, long seed) {
        super(name, seed);
        this.weights = weights;
    }

    public WeightedScoringAgent(String name, StrategyWeights weights) {
        super(name);
        this.weights = weights;
    }

    // =========================================================================
    // Static factory methods
    // =========================================================================

    /** VP Rush: Karten so schnell wie möglich ausspielen. */
    public static WeightedScoringAgent vpRush(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.vpRush());
    }

    /** Production Focus: Starke Produktionsbasis aus Fabriken aufbauen. */
    public static WeightedScoringAgent productionFocus(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.productionFocus());
    }

    /** Expansion Focus: Möglichst viele Inseln entdecken. */
    public static WeightedScoringAgent expansionFocus(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.expansionFocus());
    }

    /** Balanced: Kein klarer Fokus – Entscheidung hauptsächlich durch Kontext. */
    public static WeightedScoringAgent balanced(String name) {
        return new WeightedScoringAgent(name, StrategyWeights.balanced());
    }

    // =========================================================================
    // Core scoring
    // =========================================================================

    @Override
    protected double scoreAction(Action action, GameState gameState, Player player, 
                                GameContext context, ObjectiveContext objectiveContext) {
        return getBaseWeight(action) 
             + getContextBonus(action, context, objectiveContext)
             + getObjectiveBonus(action, objectiveContext);
    }

    // =========================================================================
    // Base weights: map each action type to its configured weight
    // =========================================================================

    private double getBaseWeight(Action action) {
        return switch (action) {
            case Action.FulfillNeeds ignored              -> weights.fulfillNeeds();
            case Action.SettleResident ignored            -> weights.settleResident();
            case Action.UpgradeResident ignored           -> weights.upgradeResident();
            case Action.BuildFactory ignored              -> weights.buildFactory();
            case Action.OverbuildDefaultFactory ignored   -> weights.overbuildFactory();
            case Action.DemolishFactory ignored           -> weights.demolishFactory();
            case Action.BuildShipyard ignored             -> weights.buildShipyard();
            case Action.BuildShips ignored                -> weights.buildShips();
            case Action.DiscoverOldWorldIsland ignored    -> weights.discoverOldWorldIsland();
            case Action.DiscoverNewWorldIsland ignored    -> weights.discoverNewWorldIsland();
            case Action.Expedition ignored                -> weights.expedition();
            case Action.Carneval ignored                  -> weights.carneval();
            case Action.SwapResidentCards ignored         -> weights.swapResidentCards();
            case Action.DoOvertime ignored                -> weights.doOvertime();
            case Action.ProduceGoods ignored              -> weights.produceGoods();
            case Action.TradeGoods ignored                -> weights.tradeGoods();
            case Action.ImportGood ignored                -> weights.importGood();
            case Action.DrawResidentCard ignored          -> weights.drawResidentCard();
            case Action.ActivateReward ignored            -> weights.activateReward();
            case Action.UseExtraAction ignored            -> weights.useExtraAction();
            case Action.DiscardResidentCardAction ignored -> weights.discardResidentCard();
            case Action.InvestorGoldAction ignored        -> weights.investorGoldAction();
            case Action.AssignWorker ignored              -> weights.assignWorker();
            case Action.ExhaustWorker ignored             -> weights.exhaustWorker();
            case Action.ChooseGoods ignored               -> weights.chooseGoods();
            case Action.ViewResidentCards ignored         -> weights.viewResidentCards();
        };
    }

    // =========================================================================
    // Context bonuses: situation-specific additions on top of the base weight
    // =========================================================================

    private double getContextBonus(Action action, GameContext context, ObjectiveContext objectiveContext) {
        return switch (action) {
            case Action.FulfillNeeds a                   -> bonusFulfillNeeds(a, context, objectiveContext);
            case Action.UpgradeResident a                -> bonusUpgradeResident(a, context);
            case Action.SettleResident a                 -> bonusSettleResident(a, context);
            case Action.BuildFactory ignored             -> bonusBuildFactory(context);
            case Action.OverbuildDefaultFactory ignored  -> bonusBuildFactory(context);
            case Action.BuildShipyard ignored            -> bonusBuildShipyard(context);
            case Action.DiscoverNewWorldIsland ignored   -> bonusDiscoverNewWorld(context);
            case Action.DrawResidentCard a               -> bonusDrawResidentCard(a, context, objectiveContext);
            case Action.Expedition ignored               -> bonusExpedition(context);
            default                                      -> 0.0;
        };
    }

    /**
     * FulfillNeeds:
     * +VP-Wert der Karte (3/5/8 je nach Bevölkerungsstufe)
     * +15 wenn es die letzte Karte ist (7 Feuerwerks-Punkte + Spiel schnell beenden)
     * Berücksichtigt ResidentCardsPenalty bei der Endspielentscheidung
     */
    private double bonusFulfillNeeds(Action.FulfillNeeds action, GameContext context, 
                                     ObjectiveContext objectiveContext) {
        double bonus = 0.0;
        com.anno1800.game.cards.ResidentCard card = action.residentCard();
        if (card != null) {
            bonus += switch (card.populationLevel()) {
                case 2, 3 -> 3.0;  // Bauer/Arbeiter: 3 VP
                case 4, 5 -> 8.0;  // Handwerker/Ingenieur/Investor: 8 VP
                case 7    -> 5.0;  // Neue-Welt-Karten: 5 VP
                default   -> 2.0;
            };
        }
        // Letzte Karte → Spielende einleiten + 7 Feuerwerks-Bonuspunkte
        // Aber bedenke: Wenn ResidentCardsPenalty aktiv ist, ist es besser, ALLE
        // Karten zu spielen bevor man endet
        if (context.myCardCount() <= 1) {
            bonus += 15.0;
        } else if (context.myCardCount() <= 2 && objectiveContext.hasResidentCardPenalty()) {
            // Mit Penalty-Karte: Extra Anreiz, vorletzte Karte zu spielen
            bonus += 8.0;
        }
        return bonus;
    }

    /**
     * UpgradeResident:
     * +Zielstufe × 1.5 pro Bewohner.
     * Skaliert runter wenn Spiel bald endet (Investment lohnt sich nicht mehr).
     */
    private double bonusUpgradeResident(Action.UpgradeResident action, GameContext context) {
        if (action.residents() == null) return 0.0;
        double scale = context.isEndPhase() ? 0.3 : 1.0;
        double bonus = 0.0;
        for (com.anno1800.game.residents.Resident r : action.residents()) {
            bonus += (r.getPopulationLevel() + 1) * 1.5 * scale;
        }
        return bonus;
    }

    /**
     * SettleResident:
     * +Stufe × 1.5. Weniger wert wenn Spiel fast vorbei.
     */
    private double bonusSettleResident(Action.SettleResident action, GameContext context) {
        double scale = context.isEndPhase() ? 0.3 : 1.0;
        return action.level() * 1.5 * scale;
    }

    /**
     * BuildFactory:
     * -5 wenn keine freien Landplätze. Kein Bonus wenn Spiel fast vorbei.
     */
    private double bonusBuildFactory(GameContext context) {
        if (context.freeLandTiles() <= 0) return -5.0;
        return context.isEndPhase() ? 0.0 : 2.0;
    }

    /**
     * BuildShipyard:
     * -3 wenn keine freien Küstenplätze.
     */
    private double bonusBuildShipyard(GameContext context) {
        if (context.freeCoastTiles() <= 0) return -3.0;
        return context.isEndPhase() ? 0.0 : 1.0;
    }

    /**
     * DiscoverNewWorldIsland:
     * +3 wenn Entdecker-Schiffe vorhanden. -2 wenn Spiel fast vorbei.
     */
    private double bonusDiscoverNewWorld(GameContext context) {
        if (context.isEndPhase()) return -2.0;
        return context.canExplore() ? 3.0 : 0.0;
    }

    /**
     * DrawResidentCard:
     * +Stufe × 1.5. Kaum Wert wenn Spiel fast vorbei.
     * Stark reduziert wenn ResidentCardsPenalty aktiv ist (jede Karte = -2 VP am Ende).
     */
    private double bonusDrawResidentCard(Action.DrawResidentCard action, GameContext context,
                                         ObjectiveContext objectiveContext) {
        double scale = context.isEndPhase() ? 0.1 : 1.0;
        
        // Wenn ResidentCardsPenalty aktiv: Karten ziehen ist riskanter
        if (objectiveContext.hasResidentCardPenalty() && !context.isEndPhase()) {
            scale *= 0.6; // Reduziere Wert um 40%
        }
        
        return action.populationLevel() * 1.5 * scale;
    }

    /**
     * Expedition:
     * +2 wenn noch viele Runden verbleiben. -1 wenn Spiel fast vorbei.
     */
    private double bonusExpedition(GameContext context) {
        if (context.isEndPhase()) return -1.0;
        return context.estimatedRoundsLeft() >= 3 ? 2.0 : 0.0;
    }

    // =========================================================================
    // Objective bonuses: strategy adjustments based on active ObjectiveCards
    // =========================================================================

    /**
     * Adds bonuses based on active ObjectiveCards.
     * This evaluates fixed parameters that were pre-computed at game start.
     * 
     * Examples:
     * - If MostInvestors is active, boost UpgradeResident(5) actions
     * - If NewWorldExplorer is active, boost DiscoverNewWorldIsland
     * - If specific factory objectives are active, boost BuildFactory for those types
     */
    private double getObjectiveBonus(Action action, ObjectiveContext objectiveContext) {
        double bonus = 0.0;
        
        // Example: Boost strategies aligned with active objectives
        switch (action) {
            case Action.UpgradeResident a -> {
                // If MostInvestors objective is active, prioritize upgrading to level 5
                if (objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.MostInvestors.class)) {
                    for (var resident : a.residents()) {
                        if (resident.getPopulationLevel() == 4) { // Upgrading to Investor (5)
                            bonus += 3.0;
                        }
                    }
                }
                // Similar for MostEngineers
                if (objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.MostEngineers.class)) {
                    for (var resident : a.residents()) {
                        if (resident.getPopulationLevel() == 3) { // Upgrading to Engineer (4)
                            bonus += 3.0;
                        }
                    }
                }
            }
            
            case Action.DiscoverNewWorldIsland ignored -> {
                // If NewWorldExplorer is active (6 VP per island), boost this action
                if (objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.NewWorldExplorer.class)) {
                    bonus += 4.0;
                }
            }
            
            case Action.Expedition ignored -> {
                // If expedition-related objectives are active, boost expeditions
                if (objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.MostExpeditionCards.class) ||
                    objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.ArtifactBonus.class) ||
                    objectiveContext.hasObjective(com.anno1800.game.cards.ObjectiveCard.AnimalBonus.class)) {
                    bonus += 3.0;
                }
            }
            
            // Note: Factory-specific bonuses would require knowing the factory type
            // from the action, which may not be available in the Action object itself.
            // These could be added in more detailed subclasses or with action introspection.
            
            default -> {}
        }
        
        return bonus;
    }

    // =========================================================================
    // Accessors
    // =========================================================================

    public StrategyWeights getWeights() {
        return weights;
    }
}
