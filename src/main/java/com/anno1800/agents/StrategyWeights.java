package com.anno1800.agents;

/**
 * Defines per-action-type base weight multipliers for a strategy.
 * Higher values make the agent prefer that action category.
 *
 * These weights are the starting point; {@link WeightedScoringAgent} adds
 * context-sensitive bonuses on top (e.g. VP value of a card, free land tiles).
 *
 * Use the static factory methods for predefined strategies:
 * <ul>
 *   <li>{@link #vpRush()}        – Karten so schnell wie möglich ausspielen</li>
 *   <li>{@link #productionFocus()} – Wirtschaftliche Engine aus Fabriken aufbauen</li>
 *   <li>{@link #expansionFocus()} – Möglichst viele Inseln entdecken</li>
 *   <li>{@link #balanced()}      – Kein klarer Fokus, rein kontextbasiert</li>
 * </ul>
 */
public record StrategyWeights(
        // === Main actions ===
        double fulfillNeeds,
        double settleResident,
        double upgradeResident,
        double buildFactory,
        double overbuildFactory,
        double demolishFactory,
        double buildShipyard,
        double buildShips,
        double discoverOldWorldIsland,
        double discoverNewWorldIsland,
        double expedition,
        double carneval,
        double swapResidentCards,

        // === Production / resource actions ===
        double doOvertime,
        double produceGoods,
        double tradeGoods,
        double importGood,

        // === Card / reward actions ===
        double drawResidentCard,
        double activateReward,

        // === Objective card free actions ===
        double useExtraAction,
        double discardResidentCard,
        double investorGoldAction,

        // === Worker assignment ===
        double assignWorker,
        double exhaustWorker,
        double chooseGoods,

        // === Info-only actions ===
        double viewResidentCards
) {

    // =========================================================================
    // Predefined Strategies
    // =========================================================================

    /**
     * VP Rush: Möglichst schnell alle Bewohnerkarten ausspielen.
     * Ziel: Spiel als erster beenden → 7 Feuerwerks-Bonuspunkte kassieren.
     */
    public static StrategyWeights vpRush() {
        return new StrategyWeights(
                10.0,  // fulfillNeeds          ← SEHR HOCH: Karten ausspielen = VP + Spielende
                5.0,   // settleResident         ← Hoch: neue Karten aufnehmen
                3.0,   // upgradeResident        ← Mittel: höhere Karten = mehr VP
                2.0,   // buildFactory
                2.0,   // overbuildFactory
                0.5,   // demolishFactory
                1.0,   // buildShipyard
                1.0,   // buildShips
                1.5,   // discoverOldWorldIsland
                1.5,   // discoverNewWorldIsland
                3.0,   // expedition             ← Expeditionskarten = VP am Spielende
                2.0,   // carneval
                6.0,   // swapResidentCards      ← Hoch: schlechte Karten loswerden
                2.0,   // doOvertime
                2.0,   // produceGoods
                2.0,   // tradeGoods
                2.0,   // importGood
                4.0,   // drawResidentCard       ← Hoch: neue Karten für VP
                3.0,   // activateReward
                4.0,   // useExtraAction         ← Extra-Aktion für mehr Karten ausspielen
                3.0,   // discardResidentCard    ← Unspielbare Karten wegwerfen
                2.0,   // investorGoldAction
                1.0,   // assignWorker
                1.0,   // exhaustWorker
                2.0,   // chooseGoods
                0.1    // viewResidentCards      ← Keine Auswirkung
        );
    }

    /**
     * Production Focus: Eine starke Produktionsbasis aus Fabriken aufbauen.
     * Ziel: Immer genug Waren haben um jede Aktion bezahlen zu können.
     */
    public static StrategyWeights productionFocus() {
        return new StrategyWeights(
                5.0,   // fulfillNeeds
                4.0,   // settleResident         ← Hoch: Arbeitskräfte für Fabriken
                6.0,   // upgradeResident        ← SEHR HOCH: höhere Stufe = bessere Fabriken
                9.0,   // buildFactory           ← SEHR HOCH: Kern der Strategie
                8.0,   // overbuildFactory       ← Hoch: Default-Fabriken verbessern
                1.0,   // demolishFactory
                4.0,   // buildShipyard
                3.0,   // buildShips
                5.0,   // discoverOldWorldIsland ← Hoch: mehr Bauplätze
                2.0,   // discoverNewWorldIsland
                2.0,   // expedition
                2.0,   // carneval
                3.0,   // swapResidentCards
                5.0,   // doOvertime             ← Hoch: sofort Waren produzieren
                7.0,   // produceGoods           ← SEHR HOCH
                5.0,   // tradeGoods             ← Hoch
                3.0,   // importGood
                3.0,   // drawResidentCard
                4.0,   // activateReward
                3.0,   // useExtraAction
                2.0,   // discardResidentCard
                3.0,   // investorGoldAction
                4.0,   // assignWorker           ← Hoch: Fabriken besetzt halten
                3.0,   // exhaustWorker
                3.0,   // chooseGoods
                0.1    // viewResidentCards
        );
    }

    /**
     * Expansion Focus: Möglichst viele Inseln entdecken.
     * Ziel: Maximale Bauplätze + Neue-Welt-Produktion als Vorteil nutzen.
     */
    public static StrategyWeights expansionFocus() {
        return new StrategyWeights(
                4.0,   // fulfillNeeds
                3.0,   // settleResident
                3.0,   // upgradeResident
                4.0,   // buildFactory
                3.0,   // overbuildFactory
                0.5,   // demolishFactory
                6.0,   // buildShipyard          ← Hoch: Schiffe brauchen Werften
                8.0,   // buildShips             ← SEHR HOCH: Schiffe für Entdeckungen
                9.0,   // discoverOldWorldIsland ← SEHR HOCH
                9.0,   // discoverNewWorldIsland ← SEHR HOCH
                7.0,   // expedition             ← Hoch: Expeditionskarten = VP
                3.0,   // carneval
                2.0,   // swapResidentCards
                3.0,   // doOvertime
                3.0,   // produceGoods
                4.0,   // tradeGoods
                6.0,   // importGood             ← Hoch: Neue Welt ausnutzen
                3.0,   // drawResidentCard
                3.0,   // activateReward
                3.0,   // useExtraAction
                2.0,   // discardResidentCard
                2.0,   // investorGoldAction
                2.0,   // assignWorker
                2.0,   // exhaustWorker
                2.0,   // chooseGoods
                0.1    // viewResidentCards
        );
    }

    /**
     * Balanced: Kein klarer Fokus – gleiche Basisgewichte für alle Aktionstypen.
     * Die Entscheidung ergibt sich fast ausschließlich aus den Kontext-Boni.
     */
    public static StrategyWeights balanced() {
        return new StrategyWeights(
                5.0,   // fulfillNeeds
                4.0,   // settleResident
                4.0,   // upgradeResident
                4.0,   // buildFactory
                3.0,   // overbuildFactory
                0.5,   // demolishFactory
                3.0,   // buildShipyard
                3.0,   // buildShips
                4.0,   // discoverOldWorldIsland
                4.0,   // discoverNewWorldIsland
                4.0,   // expedition
                3.0,   // carneval
                3.0,   // swapResidentCards
                3.0,   // doOvertime
                3.0,   // produceGoods
                3.0,   // tradeGoods
                3.0,   // importGood
                3.0,   // drawResidentCard
                3.0,   // activateReward
                3.0,   // useExtraAction
                2.0,   // discardResidentCard
                2.0,   // investorGoldAction
                2.0,   // assignWorker
                2.0,   // exhaustWorker
                2.0,   // chooseGoods
                0.1    // viewResidentCards
        );
    }
}
