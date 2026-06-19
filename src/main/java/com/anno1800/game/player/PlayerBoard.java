package com.anno1800.game.player;

import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.NewWorldIsland;
import com.anno1800.game.tiles.OldWorldIsland;
import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.data.gamedata.FactoryData;
import com.anno1800.data.gamedata.Goods;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.player.ProducedGood;
import com.anno1800.game.player.ProducedGood.GoodSource;

import static com.anno1800.data.gamedata.Producers.*;
import static com.anno1800.game.residents.ResidentStatus.*;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ExpeditionCard;
import com.anno1800.game.cards.ObjectiveCard;
import com.anno1800.game.cards.ResidentCard;
import com.anno1800.game.engine.Game;
import com.anno1800.game.rewards.Reward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.anno1800.data.gamedata.ShipType;

public class PlayerBoard {

    int landTiles = 10;
    int coastTiles = 5;
    int seaTiles = 5;
    // Tile occupancy tracking (stores placed tile type or null)
    private String[] landTileTypes;
    private String[] coastTileTypes;
    private String[] seaTileTypes;
    int numShips = 0;
    int numFactories = 0;
    int numFactoriesOnLand = 0;
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int numOldWorldIslands = 0;
    int numNewWorldIslands = 0;
    int gold = 0;
    int fulfillNeedsCount = 0;

    int availableTradeChips = 0;
    int availableExplorerChips = 0;

    /**
     * Tracks whether the Extra Action free action has been used this turn.
     * Rule: "Geht nur 1x pro Zug"
     */
    private boolean usedExtraActionThisTurn = false;
    
    /**
     * Tracks whether the Discard Resident Card free action has been used this turn.
     * Rule: "1x Pro Zug (freie Aktion)"
     */
    private boolean usedDiscardResidentCardThisTurn = false;
    
    /**
     * Tracks whether the Investor Gold free action has been used this turn.
     * Rule: "1x pro Zug"
     */
    private boolean usedInvestorGoldThisTurn = false;

    /**
     * Tracks whether ViewResidentCards free action has been used this turn.
     * This avoids endless free-action loops.
     */
    private boolean usedViewResidentCardsThisTurn = false;

    /**
     * Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
     * Must be cleared at the end of each turn using clearTradedGoodsThisTurn().
     */
    private Set<Goods> tradedGoodsThisTurn = new HashSet<>();

    // 4 NW-Inseln × 3 Plantagen = max. 12 Plantagen pro Spieler.
    // Nur 6 verschiedene Typen existieren, daher hat ein Spieler nie mehr als
    // 6 nützliche Plantagen, kann aber regelkonform bis zu 12 besitzen.
    Plantation[] plantations = new Plantation[12];

    Factory[] factories = new Factory[15]; // listet nur alle Factories eines Spielers auf. Nicht für Logik zu benutzen,
                                           // da die Shipyards nicht berücksichtigt werden. Besser getFreeLandTiles etc.
                                           // benutzen.

    // Default StartFactories that can be overbuilt
    private List<Factory> defaultFactories = new ArrayList<>();
    private List<Factory> overbuildingFactories = new ArrayList<>(); // Factories that overbuilt defaults
    private Map<Factory, Factory> overbuildMap = new HashMap<>(); // Maps new factory -> default factory it overbuilt

    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();

    ArrayList<Resident> residents = new ArrayList<>();

    ArrayList<ResidentCard> residentCards = new ArrayList<>();

    ArrayList<ExpeditionCard> expeditionCards = new ArrayList<>();

    // Owned islands (keep references so we can serialize which specific islands a player discovered)
    private final List<com.anno1800.game.tiles.OldWorldIsland> ownedOldWorldIslands = new ArrayList<>();
    private final List<com.anno1800.game.tiles.NewWorldIsland> ownedNewWorldIslands = new ArrayList<>();

    /**
     * Rewards that have been earned but not yet activated.
     * Rewards are added when a ResidentCard is fulfilled (FulfillNeeds action).
     * Players can activate these rewards later using the ActivateReward action.
     */
    private ArrayList<Reward> pendingRewards = new ArrayList<>();

    /**
     * Stored goods with their sources.
     * Used during planning phase to track how goods would be obtained.
     * Must be cleared after action execution.
     */
    private List<ProducedGood> storedGoods;

    /**
     * Last consumed goods with source information from the most recently executed action.
     * Used for debug/state export.
     */
    private List<ProducedGood> lastConsumedGoods;

    public PlayerBoard() {
        storedGoods = new ArrayList<>();
        lastConsumedGoods = new ArrayList<>();
        initializeDefaultFactories();
        // Default factories occupy land tiles
        numFactoriesOnLand = 10; // 10 start factories (5 GREEN + 5 RED)
        // initialize tile arrays
        landTileTypes = new String[landTiles];
        coastTileTypes = new String[coastTiles];
        seaTileTypes = new String[seaTiles];
        // mark default factories occupying first land tiles
        for (int i = 0; i < defaultFactories.size() && i < landTiles; i++) {
            var df = defaultFactories.get(i);
            landTileTypes[i] = df.getType().name().toLowerCase();
            df.setTileIndex(i);
        }
    }

    // ========== Trade Tracking Methods ==========

    /**
     * Checks if a good has already been traded this turn.
     * Rule: "Pro Spielzug kann dieselbe Ressource nur einmal erhandelt werden."
     * 
     * @param good The good to check
     * @return true if the good has already been traded this turn
     */
    public boolean hasAlreadyTradedThisTurn(Goods good) {
        return tradedGoodsThisTurn.contains(good);
    }

    /**
     * Registers a good as traded this turn.
     * Should be called after a successful trade.
     * 
     * @param good The good that was traded
     */
    public void registerTradedGood(Goods good) {
        tradedGoodsThisTurn.add(good);
    }

    /**
     * Clears the set of traded goods for a new turn.
     * Must be called at the end of each player's turn.
     */
    public void clearTradedGoodsThisTurn() {
        tradedGoodsThisTurn.clear();
    }
    
    /**
     * Clears all "once per turn" free action flags.
     * Must be called at the end of each player's turn.
     */
    public void clearFreeActionFlagsThisTurn() {
        usedExtraActionThisTurn = false;
        usedDiscardResidentCardThisTurn = false;
        usedInvestorGoldThisTurn = false;
        usedViewResidentCardsThisTurn = false;
    }
    
    // ========== Free Action Tracking Methods ==========
    
    /**
     * Checks if the Extra Action free action has been used this turn.
     */
    public boolean hasUsedExtraActionThisTurn() {
        return usedExtraActionThisTurn;
    }
    
    /**
     * Marks the Extra Action free action as used this turn.
     */
    public void markExtraActionUsed() {
        usedExtraActionThisTurn = true;
    }
    
    /**
     * Checks if the Discard Resident Card free action has been used this turn.
     */
    public boolean hasUsedDiscardResidentCardThisTurn() {
        return usedDiscardResidentCardThisTurn;
    }
    
    /**
     * Marks the Discard Resident Card free action as used this turn.
     */
    public void markDiscardResidentCardUsed() {
        usedDiscardResidentCardThisTurn = true;
    }
    
    /**
     * Checks if the Investor Gold free action has been used this turn.
     */
    public boolean hasUsedInvestorGoldThisTurn() {
        return usedInvestorGoldThisTurn;
    }
    
    /**
     * Marks the Investor Gold free action as used this turn.
     */
    public void markInvestorGoldUsed() {
        usedInvestorGoldThisTurn = true;
    }

    /**
     * Checks if ViewResidentCards has already been used this turn.
     */
    public boolean hasUsedViewResidentCardsThisTurn() {
        return usedViewResidentCardsThisTurn;
    }

    /**
     * Marks ViewResidentCards as used for this turn.
     */
    public void markViewResidentCardsUsed() {
        usedViewResidentCardsThisTurn = true;
    }

    /**
     * Gets the set of goods that have been traded this turn.
     * Useful for debugging and display purposes.
     * 
     * @return An unmodifiable view of the traded goods
     */
    public Set<Goods> getTradedGoodsThisTurn() {
        return Set.copyOf(tradedGoodsThisTurn);
    }

    // ========== Getter Methods ==========

    public int getLandTiles() {
        return landTiles;
    }

    public int getCoastTiles() {
        return coastTiles;
    }

    public int getSeaTiles() {
        return seaTiles;
    }

    public int getNumShips() {
        return numShips;
    }

    public int getNumFactories() {
        return numFactories;
    }

    public int getNumFactoriesOnLand() {
        return numFactoriesOnLand;
    }

    public int getNumFactoriesOnCoast() {
        return numFactoriesOnCoast;
    }

    public int getNumShipyards() {
        return numShipyards;
    }

    public int getNumOldWorldIslands() {
        return numOldWorldIslands;
    }

    public int getNumNewWorldIslands() {
        return numNewWorldIslands;
    }

    public int getGold() {
        return gold;
    }

    public int getFulfillNeedsCount() {
        return fulfillNeedsCount;
    }

    public void incrementFulfillNeedsCount() {
        fulfillNeedsCount++;
    }

    public int getAvailableTradeChips() {
        return availableTradeChips;
    }

    public int getAvailableExplorerChips() {
        return availableExplorerChips;
    }

    public Factory[] getFactories() {
        return factories;
    }

    public Plantation[] getPlantations() {
        return plantations;
    }

    public ArrayList<Shipyard> getShipyards() {
        return shipyards;
    }

    public ArrayList<TradeShip> getTradeShips() {
        return tradeShips;
    }

    public ArrayList<ExplorerShip> getExplorerShips() {
        return explorerShips;
    }

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public ArrayList<ResidentCard> getResidentCards() {
        return residentCards;
    }

    public ArrayList<ExpeditionCard> getExpeditionCards() {
        return expeditionCards;
    }

    /**
     * Returns the list of rewards that have been earned but not yet activated.
     * 
     * @return List of pending rewards
     */
    public ArrayList<Reward> getPendingRewards() {
        return pendingRewards;
    }

    /**
     * Adds a reward to the player's pending rewards list.
     * Called when a ResidentCard is fulfilled.
     * 
     * @param reward The reward to add
     */
    public void addPendingReward(Reward reward) {
        pendingRewards.add(reward);
    }

    /**
     * Removes a reward from the pending rewards list after it has been activated.
     * 
     * @param reward The reward to remove
     * @return true if the reward was removed, false if it wasn't in the list
     */
    public boolean removePendingReward(Reward reward) {
        return pendingRewards.remove(reward);
    }

    /**
     * Entfernt eine ResidentCard vom Spieler und legt sie auf den passenden Stack des Boards zurück.
     */
    public void discardResidentCard(ResidentCard card, Board board) {
        residentCards.remove(card);
        int level = card.populationLevel();
        if (level <= 2) {
            board.getResidentStack1().push(card);
        } else if (level <= 5) {
            board.getResidentStack2().push(card);
        } else {
            board.getResidentStack3().push(card);
        }
    }   

    public int getFreeLandTiles() {
        return landTiles - numFactoriesOnLand;
    }

    public int getFreeCoastTiles() {
        return coastTiles - shipyards.size() - numFactoriesOnCoast;
    }

    public int getFreeSeaTiles() {
        return seaTiles - explorerShips.size() - tradeShips.size();
    }

    public void initializePlayerBoard(Player player, Board gameBoard) {
        PlayerBoard board = player.getPlayerBoard();

        // Rule: Startspieler = 0 Gold, 2. Spieler = 1, 3. Spieler = 2, 4. Spieler = 3
        // Position is 1-based, so subtract 1
        board.addGold(player.getPosition() - 1, gameBoard);

        // Take farmer from board and add to player board
        for (int i = 0; i < 4; i++) {
            addResident(gameBoard, board, 1);
        }
        for (int i = 0; i < 3; i++) {
            addResident(gameBoard, board, 2);
        }
        for (int i = 0; i < 2; i++) {
            addResident(gameBoard, board, 3);
        }

        board.addShip(1, ShipType.TradeShip, gameBoard);
        board.addShip(1, ShipType.TradeShip, gameBoard);
        board.addShip(1, ShipType.ExplorerShip, gameBoard);

        // Note: Start-Factories are already initialized in defaultFactories by constructor
        // They should NOT be added to factories[] array to avoid double-counting
        
        // Plantations initialisieren (Beispiel, ggf. anpassen)
        // REMOVED: Plantations should only be added when player discovers NewWorldIsland!
        // Plantations will be added through addNewWorldIsland() method when player explores
        // board.addPlantation(FactoryData.getPlantation(CACAO_PLANTATION));
        // board.addPlantation(FactoryData.getPlantation(SUGAR_PLANTATION));
        // board.addPlantation(FactoryData.getPlantation(TOBACCO_PLANTATION));
        // board.addPlantation(FactoryData.getPlantation(COFFEE_PLANTATION));
        // board.addPlantation(FactoryData.getPlantation(COTTON_PLANTATION));
        // board.addPlantation(FactoryData.getPlantation(RUBBER_PLANTATION));

        board.addShipyard(1);
    }

    // ========== Initialization Methods (used only during setup) ==========

    private void addGold(int gold, Board gameBoard) {
        this.gold = this.gold + gameBoard.takeGold(gold);
    }


    private void addFactory(Factory factory) {
        ensureFactoryCapacity();
        factories[numFactories] = factory;
        // assign a land tile if available, otherwise coast
        String typeName = factory.getType().name().toLowerCase();
        if (getFreeLandTiles() > 0) {
            int idx = claimFreeLandTile(typeName);
            factory.setTileIndex(idx);
            numFactoriesOnLand++;
        } else {
            int idx = claimFreeCoastTile(typeName);
            factory.setTileIndex(landTiles + idx);
            numFactoriesOnCoast++;
        }
        numFactories++;
    }

    private int numPlantations = 0;
    private void addPlantation(Plantation plantation) {
        ensurePlantationCapacity();
        plantations[numPlantations] = plantation;
        numPlantations++;
    }

    /**
     * Ensures there is room for at least one more factory entry.
     */
    private void ensureFactoryCapacity() {
        if (numFactories >= factories.length) {
            int newSize = Math.max(factories.length * 2, factories.length + 1);
            factories = Arrays.copyOf(factories, newSize);
        }
    }

    /**
     * Ensures there is room for at least one more plantation entry.
     */
    private void ensurePlantationCapacity() {
        if (numPlantations >= plantations.length) {
            int newSize = Math.max(plantations.length * 2, plantations.length + 1);
            plantations = Arrays.copyOf(plantations, newSize);
        }
    }

    // ===== Tile helpers =====
    public String[] getLandTileTypes() { return landTileTypes; }
    public String[] getCoastTileTypes() { return coastTileTypes; }
    public String[] getSeaTileTypes() { return seaTileTypes; }

    private int claimFreeLandTile(String tileType) {
        for (int i = 0; i < landTileTypes.length; i++) {
            if (landTileTypes[i] == null) {
                landTileTypes[i] = tileType;
                return i;
            }
        }
        throw new IllegalStateException("No free land tiles");
    }

    private int claimFreeCoastTile(String tileType) {
        for (int i = 0; i < coastTileTypes.length; i++) {
            if (coastTileTypes[i] == null) {
                coastTileTypes[i] = tileType;
                return i;
            }
        }
        throw new IllegalStateException("No free coast tiles");
    }

    private int claimFreeSeaTile(String tileType) {
        for (int i = 0; i < seaTileTypes.length; i++) {
            if (seaTileTypes[i] == null) {
                seaTileTypes[i] = tileType;
                return i;
            }
        }
        throw new IllegalStateException("No free sea tiles");
    }

    private void clearTileByGlobalIndex(int globalIndex) {
        if (globalIndex < 0) return;
        if (globalIndex < landTiles) {
            landTileTypes[globalIndex] = null;
        } else if (globalIndex < landTiles + coastTiles) {
            coastTileTypes[globalIndex - landTiles] = null;
        } else {
            seaTileTypes[globalIndex - landTiles - coastTiles] = null;
        }
    }

    private void addShipyard(int level) {
        Shipyard newShipyard = new Shipyard(level);
        int coastIdx = claimFreeCoastTile("shipyard_lv" + level);
        newShipyard.setTileIndex(landTiles + coastIdx);
        shipyards.add(newShipyard);
        numShipyards++;
    }

    /**
     * Adds a ship to the player board during initialization.
     * Also adds the corresponding chips to the player's available chips.
     * 
     * @param level The ship level (1-3)
     * @param type The type of ship (ExplorerShip or TradeShip)
     * @param gameBoard The game board to take chips from
     */
    private void addShip(int level, ShipType type, Board gameBoard) {
        switch (type) {
            case ExplorerShip -> {
                ExplorerShip ship = new ExplorerShip(level);
                int seaIdx = claimFreeSeaTile("explorerShip_lv" + level);
                ship.setTileIndex(landTiles + coastTiles + seaIdx);
                explorerShips.add(ship);
                availableExplorerChips += gameBoard.takeExplorerChip(level);
            }
            case TradeShip -> {
                TradeShip ship = new TradeShip(level);
                int seaIdx = claimFreeSeaTile("tradeShip_lv" + level);
                ship.setTileIndex(landTiles + coastTiles + seaIdx);
                tradeShips.add(ship);
                availableTradeChips += gameBoard.takeTradeChip(level);
            }
        }
    }

    private void addResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        // Use the new unified takeResident method
        Resident resident = gameBoard.takeResident(populationLevel);
        resident.setStatus(FIT);
        this.residents.add(resident);
        ResidentCard residentCard = gameBoard.drawResidentCard(resident.getPopulationLevel());
        this.residentCards.add(residentCard);
    }

    // ========== Build Methods (used by ActionHandler during gameplay) ==========

    /**
     * Builds a factory on the player board.
     * Used during gameplay when a player builds a factory.
     * Prefers land tiles over coast tiles.
     * 
     * @param factory The factory instance to build
     * @throws IllegalStateException if no free land or coast tiles available
     */
    public void buildFactory(Factory factory) {
        if (getFreeLandTiles() <= 0 && getFreeCoastTiles() <= 0) {
            throw new IllegalStateException("No free tiles available to place factory");
        }

        ensureFactoryCapacity();
        factories[numFactories] = factory;

        // Assign tile index and set tile type
        String typeName = factory.getType().name().toLowerCase();
        if (getFreeLandTiles() > 0) {
            int idx = claimFreeLandTile(typeName);
            factory.setTileIndex(idx);
            numFactoriesOnLand++;
        } else {
            int idx = claimFreeCoastTile(typeName);
            factory.setTileIndex(landTiles + idx);
            numFactoriesOnCoast++;
        }

        numFactories++;
    }

    /**
     * Builds a shipyard on the player board.
     * Used during gameplay when a player builds a shipyard.
     * Shipyards can only be built on coast tiles.
     * 
     * @param shipyard The shipyard instance to build
     * @throws IllegalStateException if no free coast tiles available
     */
    public void buildShipyard(Shipyard shipyard) {
        if (getFreeCoastTiles() <= 0) {
            throw new IllegalStateException("No free coast tiles available to place shipyard");
        }

        int coastIdx = claimFreeCoastTile("shipyard_lv" + shipyard.getLevel());
        shipyard.setTileIndex(landTiles + coastIdx);
        shipyards.add(shipyard);
        numShipyards++;
    }

    /**
     * Builds a ship on the player board.
     * Used during gameplay when a player builds a ship.
     * Also adds the corresponding chips to the player's available chips.
     * 
     * @param ship     The ship instance to build (ExplorerShip or TradeShip)
     * @param shipType The type of ship
     * @param level    The level of the ship (determines chip amount)
     * @throws IllegalStateException if no free sea tiles available
     */
    public void buildShip(Object ship, ShipType shipType, int level) {
        if (getFreeSeaTiles() <= 0) {
            throw new IllegalStateException("No free sea tiles available to place ship");
        }

        switch (shipType) {
            case ExplorerShip -> {
                if (ship instanceof com.anno1800.game.tiles.ExplorerShip explorerShip) {
                    int seaIdx = claimFreeSeaTile("explorerShip_lv" + level);
                    explorerShip.setTileIndex(landTiles + coastTiles + seaIdx);
                    explorerShips.add(explorerShip);
                    availableExplorerChips += level;
                } else {
                    throw new IllegalArgumentException(
                            "Expected ExplorerShip but got " + ship.getClass().getSimpleName());
                }
            }
            case TradeShip -> {
                if (ship instanceof com.anno1800.game.tiles.TradeShip tradeShip) {
                    int seaIdx = claimFreeSeaTile("tradeShip_lv" + level);
                    tradeShip.setTileIndex(landTiles + coastTiles + seaIdx);
                    tradeShips.add(tradeShip);
                    availableTradeChips += level;
                } else {
                    throw new IllegalArgumentException("Expected TradeShip but got " + ship.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Creates a new Factory instance based on the template from FactoryData.
     * Each player gets their own Factory instances.
     * @param type The Producers enum value for the factory
     * @return a new Factory instance
     */
    // private static Factory copyFactory(Producers type) {
    //     Factory template = FactoryData.getFactory(type);
    //     return new Factory(
    //             template.getType(),
    //             template.costs(),
    //             template.produces(),
    //             template.populationLevel(),
    //             template.getTradeCosts() // Always read from FactoryData
    //     );
    // }

    public void earnGold(int amount) {
        this.gold += amount;
    }

    public void earnExpeditionCard(int amount, Board gameBoard) {
        if (amount <= 0) {
            return;
        }

        for (int i = 0; i < amount; i++) {
            if (gameBoard.getExpeditionStack().isEmpty()) {
                System.out.println("No expedition cards left in stack; stopping reward draw.");
                break;
            }
            this.expeditionCards.add(gameBoard.drawExpeditionCard());
        }
    }

    public void spendGold(int amount) {
        if (amount > gold) {
            throw new IllegalArgumentException("Not enough gold to spend");
        }
        this.gold -= amount;
    }

    public void reduceAvailableTradeChips(int amount) {
        if (amount > availableTradeChips) {
            throw new IllegalArgumentException("Cannot reduce trade chips below zero");
        }
        availableTradeChips -= amount;
    }

    public void reduceAvailableExplorerChips(int amount) {
        if (amount > availableExplorerChips) {
            throw new IllegalArgumentException("Cannot reduce explorer chips below zero");
        }
        availableExplorerChips -= amount;
    }

    public void increaseAvailableTradeChips(int amount) {
        if( amount < 0) {
            throw new IllegalArgumentException("Cannot increase trade chips by negative amount");
        }
        availableTradeChips += amount;
    }

    public void increaseAvailableExplorerChips(int amount) {
        if( amount < 0) {
            throw new IllegalArgumentException("Cannot increase explorer chips by negative amount");
        }
        availableExplorerChips += amount;
    }

    public void resetAvailableTradeChips() {
        this.availableTradeChips = 0;
    }

    public void resetAvailableExplorerChips() {
        this.availableExplorerChips = 0;
    }

    public int getPlayersTradeChips() {
        int ownedTradeChips = 0;
        for (TradeShip ship : this.tradeShips) {
            ownedTradeChips += ship.getLevel();
        }
        return ownedTradeChips;
    }

    public int getPlayersExplorerChips() {
        int ownedExplorerChips = 0;
        for (ExplorerShip ship : this.explorerShips) {
            ownedExplorerChips += ship.getLevel();
        }
        return ownedExplorerChips;
    }

    private void buildResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        addResident(gameBoard, playerBoard, populationLevel);
    }

    public void buildFactoryAsReward(Factory factory) {
        addFactory(factory);
    }

    /**
     * Adds a good to stored goods (used when reward gives goods).
     * @deprecated Use addProducedGood() instead to track source
     */
    @Deprecated
    public void addGoodToStoredGoods(Goods good) {
        storedGoods.add(new ProducedGood(good, new GoodSource.FromReward()));
    }

    /**
     * Reduce the player's gold by the specified amount.
     * 
     * @param amount The amount of gold to deduct
     */
    public void reduceGold(int amount) {
        this.gold -= amount;
        if (this.gold < 0) {
            this.gold = 0;
        }
    }
    
    /**
     * Add gold to the player's total (without taking from board).
     * Used by ObjectiveCard free actions.
     * 
     * @param amount The amount of gold to add
     */
    public void gainGold(int amount) {
        this.gold += amount;
    }

    // ========== Island Management Methods ==========

    /**
     * Adds an Old World Island to the player board.
     * Increases island count and updates tile counts based on the island's attributes.
     * 
     * @param island The Old World Island to add
     */
    public void addOldWorldIsland(OldWorldIsland island) {
        // Increment island count
        numOldWorldIslands++;
        
        // Add tiles from the island to the player's available tiles
        landTiles += island.getFreeLandTiles();
        coastTiles += island.getFreeCoastTiles();
        seaTiles += island.getFreeSeaTiles();
        
        // Add buildings from the island to the player board
        addIslandFactories(island.getFactories());
        addIslandShipyards(island.getShipyards());
        addIslandTradeShips(island.getTradeShips());
        addIslandExplorerShips(island.getExplorerShips());

        // remember the actual island object for serialization/debugging
        ownedOldWorldIslands.add(island);
    }

    /**
     * Adds a New World Island to the player board.
     * Increases island count and adds the island's plantations.
     * 
     * @param island The New World Island to add
     */
    public void addNewWorldIsland(NewWorldIsland island) {
        // Increment island count
        numNewWorldIslands++;
        
        // Add plantations from the island to the player board
        for (Plantation plantation : island.getPlantations()) {
            addPlantation(plantation);
        }

        // remember the actual island object for serialization/debugging
        ownedNewWorldIslands.add(island);
    }

    /**
     * Returns the list of Old World Islands owned by this player (in discovery order).
     */
    public List<com.anno1800.game.tiles.OldWorldIsland> getOwnedOldWorldIslands() {
        return java.util.Collections.unmodifiableList(ownedOldWorldIslands);
    }

    /**
     * Returns the list of New World Islands owned by this player (in discovery order).
     */
    public List<com.anno1800.game.tiles.NewWorldIsland> getOwnedNewWorldIslands() {
        return java.util.Collections.unmodifiableList(ownedNewWorldIslands);
    }

    /**
     * Helper method to add factories from an island to the player board.
     */
    private void addIslandFactories(Factory[] islandFactories) {
        for (Factory factory : islandFactories) {
            addFactory(factory);
        }
    }

    /**
     * Helper method to add shipyards from an island to the player board.
     */
    private void addIslandShipyards(Shipyard[] islandShipyards) {
        for (Shipyard shipyard : islandShipyards) {
            int coastIdx = claimFreeCoastTile("shipyard_lv" + shipyard.getLevel());
            shipyard.setTileIndex(landTiles + coastIdx);
            shipyards.add(shipyard);
            numShipyards++;
        }
    }

    /**
     * Helper method to add trade ships from an island to the player board.
     */
    private void addIslandTradeShips(TradeShip[] islandTradeShips) {
        for (TradeShip tradeShip : islandTradeShips) {
            int seaIdx = claimFreeSeaTile("tradeShip_lv" + tradeShip.getLevel());
            tradeShip.setTileIndex(landTiles + coastTiles + seaIdx);
            tradeShips.add(tradeShip);
        }
    }

    /**
     * Helper method to add explorer ships from an island to the player board.
     */
    private void addIslandExplorerShips(ExplorerShip[] islandExplorerShips) {
        for (ExplorerShip explorerShip : islandExplorerShips) {
            int seaIdx = claimFreeSeaTile("explorerShip_lv" + explorerShip.getLevel());
            explorerShip.setTileIndex(landTiles + coastTiles + seaIdx);
            explorerShips.add(explorerShip);
        }
    }

    // ========== Default Factory Management ==========

    /**
     * Initializes default StartFactories on the PlayerBoard.
     * These can be overbuilt by other factories.
     */
    private void initializeDefaultFactories() {
        // GREEN StartFactories
        defaultFactories.add(FactoryData.getFactory(SAWMILL_GREEN));
        defaultFactories.add(FactoryData.getFactory(GRAIN_FARM_GREEN));
        defaultFactories.add(FactoryData.getFactory(POTATO_FARM_GREEN));
        defaultFactories.add(FactoryData.getFactory(PIG_FARM_GREEN));
        defaultFactories.add(FactoryData.getFactory(SHEEP_FARM_GREEN));
        
        // RED StartFactories  
        defaultFactories.add(FactoryData.getFactory(COAL_MINE_RED));
        defaultFactories.add(FactoryData.getFactory(BRICK_FACTORY_RED));
        defaultFactories.add(FactoryData.getFactory(WAREHOUSE_RED));
        defaultFactories.add(FactoryData.getFactory(STEEL_WORKS_RED));
        defaultFactories.add(FactoryData.getFactory(SAILMAKERS_RED));
    }

    /**
     * Overbuilds a default factory with a new factory.
     * Workers in the default factory are exhausted, slots are cleared,
     * and the default factory becomes passive (not usable until new factory is demolished).
     * 
     * @param defaultFactory The default factory to overbuild
     * @param newFactory The new factory that overbuilds the default one
     * @return true if overbuilding was successful
     */
    public boolean overbuildDefaultFactory(Factory defaultFactory, Factory newFactory) {
        if (defaultFactories.contains(defaultFactory)) {
            // Exhaust all residents working in the default factory
            com.anno1800.game.residents.Resident slot1 = defaultFactory.getSlot1();
            com.anno1800.game.residents.Resident slot2 = defaultFactory.getSlot2();
            
            if (slot1 != null) {
                slot1.setStatus(com.anno1800.game.residents.ResidentStatus.EXHAUSTED);
            }
            if (slot2 != null) {
                slot2.setStatus(com.anno1800.game.residents.ResidentStatus.EXHAUSTED);
            }
            
            // Clear the default factory's work slots
            defaultFactory.freeSlots();
            
            // Track which factory overbuilt which default
            overbuildMap.put(newFactory, defaultFactory);
            
            // Remove from active defaults and add new factory to overbuilding list
            defaultFactories.remove(defaultFactory);
            overbuildingFactories.add(newFactory);
            
            // Add the new factory to the normal factory array
            buildFactoryAsReward(newFactory);
            return true;
        }
        return false;
    }

    /**
     * Demolishes a factory. If it was overbuilding a default factory, 
     * the default factory becomes available again.
     * 
     * @param factory The factory to demolish
     * @return true if demolition was successful
     */
    public boolean demolishFactory(Factory factory) {
        // Check if this factory was overbuilding a default factory
        if (overbuildingFactories.contains(factory)) {
            overbuildingFactories.remove(factory);
            
            // Restore the corresponding default factory
            restoreDefaultFactory(factory);
            
            // Remove from normal factory array
            removeFactoryFromArray(factory);
            return true;
        }
        
        // Regular factory demolition (not overbuilding)
        removeFactoryFromArray(factory);
        return true;
    }

    /**
     * Restores a default factory when its overbuilding factory is demolished.
     * 
     * @param overbuildingFactory The factory that was overbuilding
     */
    private void restoreDefaultFactory(Factory overbuildingFactory) {
        // Find the corresponding default factory based on the type
        // This is a simplified approach - you might need more sophisticated logic
        for (com.anno1800.data.gamedata.Producers producer : com.anno1800.data.gamedata.Producers.values()) {
            if (isStartFactory(producer)) {
                try {
                    Factory defaultFactory = FactoryData.getFactory(producer);
                    // Add back the default factory (simplified logic)
                    defaultFactories.add(defaultFactory);
                    break;
                } catch (IllegalArgumentException e) {
                    // Not a factory
                }
            }
        }
    }

    /**
     * Removes a factory from the factory array.
     * 
     * @param factory The factory to remove
     */
    private void removeFactoryFromArray(Factory factory) {
        for (int i = 0; i < factories.length; i++) {
            if (factories[i] == factory) {
                int idx = factory.getTileIndex();
                // free occupied tile
                clearTileByGlobalIndex(idx);
                if (idx >= 0) {
                    if (idx < landTiles) numFactoriesOnLand = Math.max(0, numFactoriesOnLand - 1);
                    else if (idx < landTiles + coastTiles) numFactoriesOnCoast = Math.max(0, numFactoriesOnCoast - 1);
                }
                factories[i] = null;
                numFactories--;
                break;
            }
        }
    }

    /**
     * Gets all currently active factories including default factories.
     * 
     * @return List of all active factories
     */
    public List<Factory> getAllActiveFactories() {
        List<Factory> allFactories = new ArrayList<>();
        
        // Add default factories that are not overbuilt
        allFactories.addAll(defaultFactories);
        
        // Add regular factories from the array
        for (Factory factory : factories) {
            if (factory != null) {
                allFactories.add(factory);
            }
        }
        
        return allFactories;
    }

    /**
     * Gets the default factories that are currently active (not overbuilt).
     * 
     * @return List of active default factories
     */
    public List<Factory> getDefaultFactories() {
        return new ArrayList<>(defaultFactories);
    }

    /**
     * Gets the factories that are currently overbuilding default factories.
     * 
     * @return List of overbuilding factories
     */
    public List<Factory> getOverbuildingFactories() {
        return new ArrayList<>(overbuildingFactories);
    }

    /**
     * Gets the stored goods on the player board.
     * 
     * @return List of stored goods with their sources
     */
    public List<ProducedGood> getStoredGoods() {
        return storedGoods;
    }

    /**
     * Gets consumed goods (including source) of the last executed action.
     *
     * @return Snapshot list of last consumed goods
     */
    public List<ProducedGood> getLastConsumedGoods() {
        return new ArrayList<>(lastConsumedGoods);
    }

    /**
     * Clears tracked consumed goods of the last action.
     */
    public void clearLastConsumedGoods() {
        lastConsumedGoods.clear();
    }

    /**
     * Replaces the tracked consumed goods snapshot for the last action.
     * Useful for actions that execute multiple internal consume steps.
     */
    public void replaceLastConsumedGoods(List<ProducedGood> consumedGoods) {
        lastConsumedGoods.clear();
        if (consumedGoods != null && !consumedGoods.isEmpty()) {
            lastConsumedGoods.addAll(consumedGoods);
        }
    }

    /**
     * Helper method to determine if a producer is a StartFactory.
     * 
     * @param producer The producer to check
     * @return true if it's a StartFactory
     */
    private static boolean isStartFactory(com.anno1800.data.gamedata.Producers producer) {
        return switch (producer) {
            // GREEN StartFactories
            case SAWMILL_GREEN, GRAIN_FARM_GREEN, POTATO_FARM_GREEN, 
                 PIG_FARM_GREEN, SHEEP_FARM_GREEN,
            // RED StartFactories
                 COAL_MINE_RED, BRICK_FACTORY_RED, WAREHOUSE_RED, 
                 STEEL_WORKS_RED, SAILMAKERS_RED -> true;
            default -> false;
        };
    }

    // ========== GOODS PLANNING AND CONSUMPTION SYSTEM ==========
    
    /**
     * PLANNING PHASE: Simulates production of goods to check if action is feasible.
     * Does NOT actually produce goods or exhaust residents.
     * Adds goods to storedGoods for planning purposes.
     * 
     * @param required Array of goods required
     * @return true if all goods can be produced/traded/imported
     */
    public boolean canObtainGoods(Goods[] required) {
        return canObtainGoods(required, null);
    }
    
    /**
     * Checks if all required goods can be obtained through production, trading, or import.
     * PLANNING PHASE: Determines strategy for obtaining goods without modifying game state.
     * 
     * @param required Array of required goods
     * @param game Optional game context for checking objective cards (e.g., ExplorerTrader)
     * @return true if all goods can be produced/traded/imported
     */
    public boolean canObtainGoods(Goods[] required, Game game) {
        if (required == null || required.length == 0) {
            return true;
        }
        
        // Try to obtain each required good
        for (Goods good : required) {
            if (!tryObtainGood(good, game)) {
                // Can't obtain this good - rollback and return false
                clearStoredGoods();
                return false;
            }
        }
        
        // All goods can be obtained
        return true;
    }
    
    /**
     * Tries to obtain a single good through production, trading, or import.
     * Adds to storedGoods if successful.
     * 
     * @param good The good to obtain
     * @param game Optional game context for checking objective cards
     * @return true if the good can be obtained
     */
    private boolean tryObtainGood(Goods good, Game game) {
        // Special case: WORKFORCE_3 means "Artisan resident" - check if we have one available
        if (good == Goods.WORKFORCE_3) {
            Resident artisan = findFitResident(3); // Level 3 = Artisan
            if (artisan != null) {
                storedGoods.add(new ProducedGood(good, new GoodSource.FromReward()));
                return true;
            }
            return false;
        }
        
        // Special case: WORKFORCE_4 means "Engineer resident"
        if (good == Goods.WORKFORCE_4) {
            Resident engineer = findFitResident(4); // Level 4 = Engineer
            if (engineer != null) {
                storedGoods.add(new ProducedGood(good, new GoodSource.FromReward()));
                return true;
            }
            return false;
        }
        
        // Special case: WORKFORCE_5 means "Investor resident"
        if (good == Goods.WORKFORCE_5) {
            Resident investor = findFitResident(5); // Level 5 = Investor
            if (investor != null) {
                storedGoods.add(new ProducedGood(good, new GoodSource.FromReward()));
                return true;
            }
            return false;
        }
        
        // Try 1a: Check Default Factories first (these are always available at game start)
        for (Factory factory : defaultFactories) {
            if (factory.produces().equals(good)) {
                // Check if default factory is not overbuilt
                boolean isOverbuilt = overbuildMap.values().stream()
                    .anyMatch(df -> df == factory);
                
                if (!isOverbuilt && (factory.getSlot1() == null || factory.getSlot2() == null)) {
                    // Check if we have a FIT resident of correct level
                    Resident resident = findFitResident(factory.populationLevel());
                    if (resident != null) {
                        // Success! Add to storedGoods
                        storedGoods.add(new ProducedGood(good, new GoodSource.Produced(factory, resident)));
                        return true;
                    }
                }
            }
        }
        
        // Try 1b: Production in regular factories (find factory that produces this good and has free slot + FIT resident)
        for (int i = 0; i < numFactories; i++) {
            Factory factory = factories[i];
            if (factory != null && factory.produces().equals(good)) {
                // Check if factory has a free slot
                if (factory.getSlot1() == null || factory.getSlot2() == null) {
                    // Check if we have a FIT resident of correct level
                    Resident resident = findFitResident(factory.populationLevel());
                    if (resident != null) {
                        // Success! Add to storedGoods
                        storedGoods.add(new ProducedGood(good, new GoodSource.Produced(factory, resident)));
                        return true;
                    }
                }
            }
        }
        
        // Check if ExplorerTrader objective card is active
        boolean explorerTraderActive = false;
        if (game != null) {
            explorerTraderActive = game.getBoard().getActiveObjectiveCards().stream()
                .anyMatch(card -> card instanceof ObjectiveCard.ExplorerTrader);
        }
        
        // Try 2: Trading from another player (cost depends on traded factory color via tradeCosts)
        int plannedTradeChips = storedGoods.stream()
                .map(ProducedGood::source)
                .mapToInt(source -> {
                    if (source instanceof GoodSource.Traded traded) {
                        return traded.chipCost();
                    }
                    return 0;
                })
                .sum();

        int plannedExplorerChipsForTrade = storedGoods.stream()
                .map(ProducedGood::source)
                .mapToInt(source -> {
                    if (source instanceof GoodSource.TradedWithExplorer tradedWithExplorer) {
                        return tradedWithExplorer.explorerChipCost();
                    }
                    return 0;
                })
                .sum();

        int cheapestTradeCosts = Integer.MAX_VALUE;
        int tradingPlayerIndex = -1;
        int lowestTradingPartnerGold = Integer.MAX_VALUE;
        if (game != null) {
            Player[] players = game.getPlayers();
            for (int i = 0; i < players.length; i++) {
                Player otherPlayer = players[i];
                if (otherPlayer.getPlayerBoard() == this) {
                    continue;
                }

                int partnerGold = otherPlayer.getPlayerBoard().getGold();

                for (Factory factory : otherPlayer.getPlayerBoard().getAllActiveFactories()) {
                    if (factory.produces().equals(good)) {
                        int tradeCosts = factory.getTradeCosts();
                        if (tradeCosts < cheapestTradeCosts
                                || (tradeCosts == cheapestTradeCosts && partnerGold < lowestTradingPartnerGold)) {
                            cheapestTradeCosts = tradeCosts;
                            tradingPlayerIndex = i;
                            lowestTradingPartnerGold = partnerGold;
                        }
                    }
                }
            }
        }

        boolean foundTradeSource = tradingPlayerIndex >= 0 && cheapestTradeCosts != Integer.MAX_VALUE;
        if (foundTradeSource && (availableTradeChips - plannedTradeChips) >= cheapestTradeCosts) {
            storedGoods.add(new ProducedGood(good, new GoodSource.Traded(tradingPlayerIndex, cheapestTradeCosts)));
            return true;
        }

        // Try 2b: ExplorerTrader can replace missing trade chips with explorer chips (2:1)
        if (foundTradeSource && explorerTraderActive) {
            int explorerChipCost = cheapestTradeCosts * 2;
            if ((availableExplorerChips - plannedExplorerChipsForTrade) >= explorerChipCost) {
                storedGoods.add(new ProducedGood(
                    good,
                    new GoodSource.TradedWithExplorer(tradingPlayerIndex, cheapestTradeCosts, explorerChipCost)
                ));
                return true;
            }
        }
        
        // Try 3: Import from new world (check if we have a plantation that produces this good)
        // Count chips already planned in storedGoods to avoid over-committing
        int plannedExplorerChips = storedGoods.stream()
                .map(ProducedGood::source)
                .mapToInt(source -> {
                    if (source instanceof GoodSource.Imported imported) {
                        return imported.explorerChip();
                    }
                    if (source instanceof GoodSource.TradedWithExplorer tradedWithExplorer) {
                        return tradedWithExplorer.explorerChipCost();
                    }
                    return 0;
                })
                .sum();
        if (isNewWorldGood(good) && (availableExplorerChips - plannedExplorerChips) > 0) {
            // Check if player has a plantation that produces this good
            boolean hasPlantation = false;
            for (Plantation plantation : plantations) {
                if (plantation != null && plantation.produces().equals(good)) {
                    hasPlantation = true;
                    break;
                }
            }
            
            if (hasPlantation) {
                storedGoods.add(new ProducedGood(good, new GoodSource.Imported(1)));
                return true;
            }
        }
        
        // Can't obtain this good
        return false;
    }
    
    /**
     * Finds a FIT resident of the specified population level.
     * Used during planning phase to check if production is possible.
     * Excludes residents that are already planned for use in storedGoods.
     * 
     * @param populationLevel The required population level
     * @return A FIT resident, or null if none available
     */
    private Resident findFitResident(int populationLevel) {
        // Collect all residents already planned for use
        java.util.Set<Resident> plannedResidents = new java.util.HashSet<>();
        for (ProducedGood pg : storedGoods) {
            if (pg.source() instanceof GoodSource.Produced produced) {
                plannedResidents.add(produced.resident());
            }
        }
        
        // Find a FIT resident that's not already planned
        for (Resident resident : residents) {
            if (resident.getPopulationLevel() == populationLevel && 
                resident.getStatus() == com.anno1800.game.residents.ResidentStatus.FIT &&
                !plannedResidents.contains(resident)) {
                return resident;
            }
        }
        return null;
    }
    
    /**
     * Checks if a good is from the new world.
     */
    private boolean isNewWorldGood(Goods good) {
        return switch (good) {
            case CACAO, SUGARCANE, TOBACCO, COFFEE_BEANS, COTTON, RUBBER -> true;
            default -> false;
        };
    }
    
    /**
     * EXECUTION PHASE: Consumes goods from storedGoods and performs actual production/trade.
     * This is called when an action is actually executed.
     * After consumption, storedGoods is cleared.
     * 
     * @param required Array of goods to consume
     */
    public void consumeGoods(Goods[] required) {
        consumeGoods(required, null);
    }

    public void consumeGoods(Goods[] required, Game game) {
        lastConsumedGoods.clear();

        if (required == null || required.length == 0) {
            return;
        }
        
        for (Goods good : required) {
            // Find and remove from storedGoods
            ProducedGood producedGood = null;
            for (ProducedGood pg : storedGoods) {
                if (pg.good().equals(good)) {
                    producedGood = pg;
                    break;
                }
            }
            
            if (producedGood == null) {
                throw new IllegalStateException("Tried to consume " + good + " but it's not in storedGoods. " +
                    "Ensure canObtainGoods() was called first!");
            }
            
            // Execute the actual action based on source
            executeGoodSource(producedGood, game);

            // Track for debug/export output
            lastConsumedGoods.add(producedGood);
            
            // Remove from storedGoods
            storedGoods.remove(producedGood);
        }
        
        // Clear remaining goods
        clearStoredGoods();
    }
    
    /**
     * Executes the actual production/trade/import based on the good's source.
     * This performs the real game state changes (exhaust residents, use chips, etc.)
     */
    private void executeGoodSource(ProducedGood producedGood, Game game) {
        switch (producedGood.source()) {
            case GoodSource.Produced(Factory factory, Resident resident) -> {
                // Actually assign resident to factory
                if (factory.getSlot1() == null) {
                    factory.setSlot1(resident);
                } else {
                    factory.setSlot2(resident);
                }
                resident.setStatus(com.anno1800.game.residents.ResidentStatus.AT_WORK);
                System.out.println("  -> Produced " + producedGood.good() + " in " + factory.getType());
            }
            case GoodSource.Traded(int fromPlayer, int chipCost) -> {
                if (chipCost <= 0 || availableTradeChips < chipCost) {
                    throw new IllegalStateException("Invalid chip cost for trading: " + chipCost);
                }
                availableTradeChips -= chipCost;
                if (game != null && fromPlayer >= 0 && fromPlayer < game.getPlayers().length) {
                    game.getPlayers()[fromPlayer].getPlayerBoard().earnGold(chipCost);
                }
                System.out.println("  -> Traded " + producedGood.good() + " from Player " + (fromPlayer + 1)
                        + " (cost " + chipCost + " TradeChip" + (chipCost == 1 ? "" : "s") + ")");
            }
            case GoodSource.TradedWithExplorer(int fromPlayer, int tradeChipCost, int explorerChipCost) -> {
                if (explorerChipCost <= 0 || availableExplorerChips < explorerChipCost) {
                    throw new IllegalStateException("Invalid explorer chip cost for trading: " + explorerChipCost);
                }
                availableExplorerChips -= explorerChipCost;
                if (game != null && fromPlayer >= 0 && fromPlayer < game.getPlayers().length) {
                    game.getPlayers()[fromPlayer].getPlayerBoard().earnGold(tradeChipCost);
                }
                System.out.println("  -> Traded " + producedGood.good() + " from Player " + (fromPlayer + 1)
                        + " (ExplorerTrader, cost " + explorerChipCost + " ExplorerChips ~= "
                        + tradeChipCost + " TradeChips)");
            }
            case GoodSource.Imported(int chip) -> {
                // Use explorer chip
                availableExplorerChips--;
                System.out.println("  -> Imported " + producedGood.good() + " from New World");
            }
            case GoodSource.FromReward() -> {
                // No action needed - already obtained
                System.out.println("  -> Used " + producedGood.good() + " from reward");
            }
            case GoodSource.Other(String desc) -> {
                System.out.println("  -> Used " + producedGood.good() + " (" + desc + ")");
            }
        }
    }
    
    /**
     * Removes a factory from the player board.
     * Used when a factory is demolished.
     * If the factory was overbuilding a default factory, the default factory becomes active again.
     * Updates the factory counts accordingly.
     * 
     * @param factory The factory to remove
     * @return true if the factory was removed, false if not found
     */
    public boolean removeFactory(Factory factory) {
        boolean found = false;
        
        // Check if this factory was overbuilding a default factory
        if (overbuildMap.containsKey(factory)) {
            Factory defaultFactory = overbuildMap.get(factory);
            
            // Restore the default factory to active list
            defaultFactories.add(defaultFactory);
            
            // Remove from overbuilding tracking
            overbuildMap.remove(factory);
            overbuildingFactories.remove(factory);
        }
        
        // Find and remove factory from array
        for (int i = 0; i < numFactories; i++) {
            if (factories[i] == factory) {
                // Shift remaining factories
                for (int j = i; j < numFactories - 1; j++) {
                    factories[j] = factories[j + 1];
                }
                factories[numFactories - 1] = null;
                numFactories--;
                found = true;
                break;
            }
        }
        
        if (found) {
            // Decrease the appropriate tile count
            // We need to determine if it was on land or coast
            // For simplicity, prefer reducing land first (matching buildFactory logic)
            if (numFactoriesOnLand > 0) {
                numFactoriesOnLand--;
            } else if (numFactoriesOnCoast > 0) {
                numFactoriesOnCoast--;
            }
        }
        
        return found;
    }

    /**
     * Clears all stored goods. Called after action execution or when planning fails.
     * This is the cleanup/rollback mechanism.
     */
    public void clearStoredGoods() {
        storedGoods.clear();
    }
    
    /**
     * Adds a produced good to storedGoods (used during planning).
     * 
     * @param producedGood The good with its source
     */
    public void addProducedGood(ProducedGood producedGood) {
        storedGoods.add(producedGood);
    }
}
