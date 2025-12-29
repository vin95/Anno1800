package com.anno1800.game.player;

import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.Producer;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.data.gamedata.FactoryData;
import com.anno1800.game.residents.Resident;
import com.anno1800.game.tiles.Producers;
import static com.anno1800.game.tiles.Producers.*;
import static com.anno1800.game.residents.ResidentStatus.*;
import com.anno1800.game.board.Board;
import com.anno1800.game.cards.ResidentCard;
import java.util.ArrayList;

public class PlayerBoard {

    public enum ShipType {
        ExplorerShip,
        TradeShip
    }

    int landTiles = 10;
    int coastTiles = 5;
    int seaTiles = 5;
    int numShips = 0;
    int numFactories = 0;
    int numFactoriesOnLand = 0;
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int numOldWorldIslands = 0;
    int numNewWorldIslands = 0;
    int gold = 0;

    int availableTradeChips = 0;
    int availableExplorerChips = 0;

    Producer[] factories = new Producer[15]; // listet nur alle Factories eines Spielers auf. Nicht für Logik zu benutzen,
                                           // da die SHipyards nicht berücksichtigt werden. Besser getFreeLandTiles etc.
                                           // benutzen.

    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();

    ArrayList<Resident> residents = new ArrayList<>();

    ArrayList<ResidentCard> residentCards = new ArrayList<>();

    public PlayerBoard() {
    }

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

    public int getAvailableTradeChips() {
        return availableTradeChips;
    }

    public int getAvailableExplorerChips() {
        return availableExplorerChips;
    }

    public Producer[] getFactories() {
        return factories;
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

        board.addGold(player.getPosition(), gameBoard);

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

        board.addFactory(SAWMILL_GREEN);
        board.addFactory(GRAIN_FARM_GREEN);
        board.addFactory(POTATO_FARM_GREEN);
        board.addFactory(PIG_FARM_GREEN);
        board.addFactory(SHEEP_FARM_GREEN);
        board.addFactory(COAL_MINE_RED);
        board.addFactory(BRICK_FACTORY_RED);
        board.addFactory(WAREHOUSE_RED);
        board.addFactory(STEEL_WORKS_RED);
        board.addFactory(SAILMAKERS_RED);

        board.addShipyard(1);
    }

    // ========== Initialization Methods (used only during setup) ==========

    private void addGold(int gold, Board gameBoard) {
        this.gold = this.gold + gameBoard.takeGold(gold);
    }

    private void addFactory(Producers type) {
        Factory newFactory = copyFactory(type);
        factories[numFactories] = newFactory;
        numFactoriesOnLand++;
        numFactories++;
    }

    private void addShipyard(int level) {
        Shipyard newShipyard = new Shipyard(level);
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
                explorerShips.add(ship);
                availableExplorerChips += gameBoard.takeExplorerChip(level);
            }
            case TradeShip -> {
                TradeShip ship = new TradeShip(level);
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

        factories[numFactories] = factory;

        // Prefer land tiles over coast tiles
        if (getFreeLandTiles() > 0) {
            numFactoriesOnLand++;
        } else {
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
                    explorerShips.add(explorerShip);
                    availableExplorerChips += level;
                } else {
                    throw new IllegalArgumentException(
                            "Expected ExplorerShip but got " + ship.getClass().getSimpleName());
                }
            }
            case TradeShip -> {
                if (ship instanceof com.anno1800.game.tiles.TradeShip tradeShip) {
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
     */
    private static Factory copyFactory(Producers type) {
        Factory template = (Factory) FactoryData.getProducer(type);
        return new Factory(
                template.getType(),
                template.costs(),
                template.produces(),
                template.populationLevel(),
                template.getTradeCosts() // Always read from FactoryData
        );
    }

    public void earnGold(int amount) {
        this.gold += amount;
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

    private void buildResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        addResident(gameBoard, playerBoard, populationLevel);
    }
}