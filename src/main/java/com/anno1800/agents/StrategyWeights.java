package com.anno1800.agents;

import com.anno1800.agents.AgentImpl.WeightedScoringAgent;
import com.anno1800.game.cards.ObjectiveCard;

/**
 * Defines per-action-type base weight multipliers for a strategy.
 * Higher values make the agent prefer that action category.
 *
 * These weights are the starting point; {@link WeightedScoringAgent} adds
 * context-sensitive bonuses on top (e.g. VP value of a card, free land tiles).
 *
 * Use the static factory methods for predefined strategies:
 * <ul>
 *   <li>{@link #shipBuilding()}           – Viele Schiffe bauen</li>
 *   <li>{@link #tradeShipFocus()}         – Viele Handelsschiffe bauen / viel handeln</li>
 *   <li>{@link #explorationFocus()}       – Viele Expeditionschiffe / viele Inseln / viele Expeditionen</li>
 *   <li>{@link #residentMassProduction()} – Viele Residents / viele Baumaterial-Fabriken</li>
 *   <li>{@link #eliteResidentProduction()} – Hohe Resident-Levels / viele Baumaterial-Fabriken</li>
 *   <li>{@link #adaptiveResidentStrategy()} – Angemessene Levelverteilung (early: Bauern/Arbeiter, late: Handwerker/Ingenieure/Investoren)</li>
 *   <li>{@link #factoryVariety()}         – Große Variety an Factories</li>
 *   <li>{@link #balanced()}               – Kein klarer Fokus, rein kontextbasiert</li>
 * </ul>
 */
public record StrategyWeights(
        // === Main actions ===
        double buildFactory,
        double buildShipyard,
        double buildShips,
        double fulfillNeeds,
        double settleResident,
        double upgradeResident,
        double swapResidentCards,
        double discoverOldWorldIsland,
        double discoverNewWorldIsland,
        double expedition,
        double carneval
) {

    // =========================================================================
    // Predefined Strategies
    // =========================================================================

    /**
     * Ship Building: Viele Schiffe bauen.
     * Fokus: Maximale Schiffsproduktion durch viele Werften.
     */
    public static StrategyWeights shipBuilding() {
        return new StrategyWeights(
                2.0,   // buildFactory           ← Niedrig: nicht Kern der Strategie
                9.0,   // buildShipyard          ← SEHR HOCH: ohne Werften keine Schiffe
                10.0,  // buildShips             ← SEHR HOCH: Hauptziel
                6.0,   // fulfillNeeds           ← Mittel-Hoch: VP sammeln
                5.0,   // settleResident         ← Mittel: Arbeiter für Werften
                2.0,   // upgradeResident        ← Niedrig: nicht prioritär
                2.0,   // swapResidentCards      ← Niedrig
                6.0,   // discoverOldWorldIsland ← Mittel-Hoch: mehr Platz für Werften
                4.0,   // discoverNewWorldIsland ← Mittel: zusätzlicher Platz
                5.0,   // expedition             ← Mittel: follgt aus verfügbaren Schiffen
                1.0    // carneval               ← Niedrig: situational
        );
    }

    /**
     * Trade Ship Focus: Viele Handelsschiffe bauen und viel handeln.
     * Fokus: Handelsschiffe + Neue-Welt-Handel + Warenproduktion.
     */
    public static StrategyWeights tradeShipFocus() {
        return new StrategyWeights(
                6.0,   // buildFactory           ← Hoch: Waren zum Handeln produzieren
                7.0,   // buildShipyard          ← Hoch: Trade Ships brauchen Werften
                8.0,   // buildShips             ← SEHR HOCH: Trade Ships!
                6.0,   // fulfillNeeds           ← Mittel-Hoch: VP sammeln
                5.0,   // settleResident         ← Mittel: Arbeiter
                3.0,   // upgradeResident        ← Niedrig-Mittel: nicht Priorität
                3.0,   // swapResidentCards      ← Niedrig-Mittel
                5.0,   // discoverOldWorldIsland ← Mittel: mehr Platz
                9.0,   // discoverNewWorldIsland ← SEHR HOCH: Handel mit Neuer Welt
                2.0,   // expedition             ← Niedrig
                4.0    // carneval               ← Mittel: Ressourcen zurücksetzen
        );
    }

    /**
     * Exploration Focus: Viele Expeditionschiffe, viele Inseln, viele Expeditionen.
     * Fokus: Maximale Entdeckungen + Expeditionskarten sammeln.
     */
    public static StrategyWeights explorationFocus() {
        return new StrategyWeights(
                2.0,   // buildFactory           ← Niedrig: nicht Fokus
                7.0,   // buildShipyard          ← Hoch: Expeditionsschiffe brauchen Werften
                8.0,   // buildShips             ← SEHR HOCH: Expeditionsschiffe!
                6.0,   // fulfillNeeds           ← Mittel-Hoch: VP sammeln
                4.0,   // settleResident         ← Niedrig-Mittel
                2.0,   // upgradeResident        ← Niedrig
                2.0,   // swapResidentCards      ← Niedrig
                10.0,  // discoverOldWorldIsland ← SEHR HOCH: Kern der Strategie
                10.0,  // discoverNewWorldIsland ← SEHR HOCH: Kern der Strategie
                10.0,  // expedition             ← SEHR HOCH: Expeditionskarten = VP
                1.0    // carneval               ← Niedrig
        );
    }

    /**
     * Resident Mass Production: Viele Residents + viele Baumaterial-Fabriken.
     * Fokus: Große Menge an Residents (alle Stufen) + Planks/Bricks/Steelbars/Windows/Coats/Coal.
     */
    public static StrategyWeights residentMassProduction() {
        return new StrategyWeights(
                8.0,   // buildFactory           ← SEHR HOCH: Baumaterial-Fabriken
                2.0,   // buildShipyard          ← Niedrig: nicht Fokus
                2.0,   // buildShips             ← Niedrig
                8.0,   // fulfillNeeds           ← SEHR HOCH: Karten ausspielen für mehr Residents
                10.0,  // settleResident         ← SEHR HOCH: Hauptziel
                3.0,   // upgradeResident        ← Niedrig-Mittel: Masse statt Elite
                5.0,   // swapResidentCards      ← Mittel: unpassende Karten wegwerfen
                6.0,   // discoverOldWorldIsland ← Mittel-Hoch: mehr Platz
                2.0,   // discoverNewWorldIsland ← Niedrig
                2.0,   // expedition             ← Niedrig
                4.0    // carneval               ← Mittel: Ressourcen zurücksetzen
        );
    }

    /**
     * Elite Resident Production: Hohe Resident-Levels + viele Baumaterial-Fabriken.
     * Fokus: Upgraden zu Handwerker/Ingenieure/Investoren + hochwertige Factories.
     */
    public static StrategyWeights eliteResidentProduction() {
        return new StrategyWeights(
                8.0,   // buildFactory           ← SEHR HOCH: Baumaterial-Fabriken
                2.0,   // buildShipyard          ← Niedrig
                2.0,   // buildShips             ← Niedrig
                7.0,   // fulfillNeeds           ← Hoch: Karten ausspielen
                5.0,   // settleResident         ← Mittel: Basis legen
                10.0,  // upgradeResident        ← SEHR HOCH: Hauptziel!
                7.0,   // swapResidentCards      ← Hoch: schlechte Karten für bessere tauschen
                6.0,   // discoverOldWorldIsland ← Mittel-Hoch: mehr Platz
                2.0,   // discoverNewWorldIsland ← Niedrig
                3.0,   // expedition             ← Niedrig-Mittel: VP Bonus
                4.0    // carneval               ← Mittel
        );
    }

    /**
     * Adaptive Resident Strategy: Angemessene Levelverteilung je nach Spielphase.
     * Early Game: Viele Bauern/Arbeiter
     * Late Game: Viele Handwerker/Ingenieure/Investoren
     * Fokus: Ausgewogenes Kartenmanagement + situatives Upgraden.
     */
    public static StrategyWeights adaptiveResidentStrategy() {
        return new StrategyWeights(
                6.0,   // buildFactory           ← Mittel-Hoch: angepasst an Residents
                2.0,   // buildShipyard          ← Niedrig
                2.0,   // buildShips             ← Niedrig
                10000.0,  // fulfillNeeds           ← SEHR HOCH: Karten ausspielen zentral
                8.0,   // settleResident         ← SEHR HOCH: viele Residents
                8.0,   // upgradeResident        ← SEHR HOCH: aber ausgewogen upgraden
                8.0,   // swapResidentCards      ← SEHR HOCH: Kartenmanagement wichtig!
                5.0,   // discoverOldWorldIsland ← Mittel: mehr Platz
                3.0,   // discoverNewWorldIsland ← Niedrig-Mittel
                3.0,   // expedition             ← Niedrig-Mittel: VP Bonus
                4.0    // carneval               ← Mittel
        );
    }

    /**
     * Factory Variety: Große Variety an Factories.
     * Fokus: Möglichst viele verschiedene Fabriken bauen + höhere Residents für Zugang zu mehr Factory-Typen.
     */
    public static StrategyWeights factoryVariety() {
        return new StrategyWeights(
                10.0,  // buildFactory           ← SEHR HOCH: Hauptziel!
                3.0,   // buildShipyard          ← Niedrig-Mittel
                3.0,   // buildShips             ← Niedrig-Mittel
                7.0,   // fulfillNeeds           ← Mittel-Hoch: VP sammeln
                5.0,   // settleResident         ← Mittel: Basis
                8.0,   // upgradeResident        ← SEHR HOCH: höhere Residents = mehr Fabriken
                5.0,   // swapResidentCards      ← Mittel: Kartenmanagement
                8.0,   // discoverOldWorldIsland ← SEHR HOCH: mehr Platz für Fabriken!
                5.0,   // discoverNewWorldIsland ← Mittel: Variety durch Neue Welt
                2.0,   // expedition             ← Niedrig
                4.0    // carneval               ← Mittel
        );
    }

    /**
     * Balanced: Kein klarer Fokus – gleiche Basisgewichte für alle Aktionstypen.
     * Die Entscheidung ergibt sich fast ausschließlich aus den Kontext-Boni.
     */
    public static StrategyWeights balanced() {
        return new StrategyWeights(
                5.0,   // buildFactory
                5.0,   // buildShipyard
                5.0,   // buildShips
                8.0,   // fulfillNeeds           ← Etwas höher: immer wichtig für VP
                5.0,   // settleResident
                5.0,   // upgradeResident
                5.0,   // swapResidentCards
                5.0,   // discoverOldWorldIsland
                5.0,   // discoverNewWorldIsland
                5.0,   // expedition
                3.0    // carneval               ← Etwas niedriger: sehr situational
        );
    }

    // =========================================================================
    // Dynamic Adjustment based on ObjectiveCards
    // =========================================================================

    /**
     * Creates adjusted weights based on active ObjectiveCards.
     * This modifies the base strategy weights to align with scoring opportunities.
     * 
     * <p>Adjustment logic:
     * <ul>
     *   <li><b>ResidentCardsPenalty</b>: +3 fulfillNeeds (play cards faster)</li>
     *   <li><b>Factory Objectives</b>: +2 to +4 buildFactory (based on scoring potential)</li>
     *   <li><b>MostInvestors/Engineers</b>: +2 upgradeResident, +1 settleResident</li>
     *   <li><b>Island/Expedition Objectives</b>: +2 to +3 discover/expedition</li>
     *   <li><b>SingleIslandBonus</b>: -3 discover (avoid multiple islands)</li>
     * </ul>
     * 
     * @param objectiveContext The objective context with active cards
     * @return New StrategyWeights with adjustments applied
     */
    public StrategyWeights adjustForObjectives(ObjectiveContext objectiveContext) {
        double adjBuildFactory = buildFactory;
        double adjBuildShipyard = buildShipyard;
        double adjBuildShips = buildShips;
        double adjFulfillNeeds = fulfillNeeds;
        double adjSettleResident = settleResident;
        double adjUpgradeResident = upgradeResident;
        double adjSwapResidentCards = swapResidentCards;
        double adjDiscoverOld = discoverOldWorldIsland;
        double adjDiscoverNew = discoverNewWorldIsland;
        double adjExpedition = expedition;
        double adjCarneval = carneval;

        // === Penalty Cards: Encourage ending game faster ===
        if (objectiveContext.hasResidentCardPenalty()) {
            adjFulfillNeeds += 3.0;  // Play cards ASAP to avoid penalty
            adjSwapResidentCards -= 1.0;  // Less swapping, more playing
        }

        // === Factory Objectives: Boost buildFactory ===
        int luxuryPotential = objectiveContext.getScoringPotential("LuxuryFactories");
        int newWorldPotential = objectiveContext.getScoringPotential("NewWorldProductFactories");
        int artisanPotential = objectiveContext.getScoringPotential("ArtisanGoodsFactories");
        int engineerPotential = objectiveContext.getScoringPotential("EngineerGoodsFactories");
        int basicPotential = objectiveContext.getScoringPotential("BasicGoodsProducer");
        int prestigePotential = objectiveContext.getScoringPotential("PrestigeFactories");
        
        int totalFactoryPotential = luxuryPotential + newWorldPotential + artisanPotential 
                                   + engineerPotential + basicPotential + prestigePotential;
        
        if (totalFactoryPotential > 50) {
            adjBuildFactory += 4.0;  // Very high factory focus
        } else if (totalFactoryPotential > 25) {
            adjBuildFactory += 2.5;  // Medium factory focus
        } else if (totalFactoryPotential > 0) {
            adjBuildFactory += 1.5;  // Some factory focus
        }

        // === Resident Objectives: Boost upgradeResident / settleResident ===
        if (objectiveContext.hasObjective(ObjectiveCard.MostInvestors.class)) {
            adjUpgradeResident += 2.5;
            adjSettleResident += 1.5;  // Need base residents to upgrade
        }
        
        if (objectiveContext.hasObjective(ObjectiveCard.MostEngineers.class)) {
            adjUpgradeResident += 2.5;
            adjSettleResident += 1.5;
        }
        
        if (objectiveContext.hasObjective(ObjectiveCard.MostResidentsTotal.class)) {
            adjSettleResident += 3.0;  // Mass production
            adjUpgradeResident += 1.0;  // Some upgrades for variety
        }

        // === Exploration Objectives: Boost discovery and expedition ===
        if (objectiveContext.hasObjective(ObjectiveCard.NewWorldExplorer.class)) {
            adjDiscoverNew += 3.0;  // New World islands are priority
            adjBuildShips += 1.5;   // Need ships to explore
        }
        
        int expeditionPotential = objectiveContext.getScoringPotential("ArtifactBonus")
                                + objectiveContext.getScoringPotential("AnimalBonus");
        if (expeditionPotential > 0) {
            adjExpedition += 2.0;
            adjBuildShips += 1.0;  // Need explorer ships
        }
        
        if (objectiveContext.hasObjective(ObjectiveCard.MostExpeditionCards.class)) {
            adjExpedition += 2.5;
            adjBuildShips += 1.5;
        }

        // === SingleIslandBonus: AVOID multiple islands ===
        if (objectiveContext.hasObjective(ObjectiveCard.SingleIslandBonus.class)) {
            adjDiscoverOld -= 3.0;  // Strong penalty for discovering
            adjDiscoverNew -= 3.0;
        }

        // === Trade/Ship Objectives ===
        if (objectiveContext.hasObjective(ObjectiveCard.MostTradeChips.class)) {
            adjBuildShipyard += 1.5;
            adjBuildShips += 2.0;  // Trade ships give chips
        }

        return new StrategyWeights(
                Math.max(0.0, adjBuildFactory),
                Math.max(0.0, adjBuildShipyard),
                Math.max(0.0, adjBuildShips),
                Math.max(0.0, adjFulfillNeeds),
                Math.max(0.0, adjSettleResident),
                Math.max(0.0, adjUpgradeResident),
                Math.max(0.0, adjSwapResidentCards),
                Math.max(0.0, adjDiscoverOld),
                Math.max(0.0, adjDiscoverNew),
                Math.max(0.0, adjExpedition),
                Math.max(0.0, adjCarneval)
        );
    }
}
