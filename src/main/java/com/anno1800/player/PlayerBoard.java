package com.anno1800.player;

import com.anno1800.Boardtiles.ExplorerShip;
import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.Shipyard;
import com.anno1800.Boardtiles.TradeShip;
import com.anno1800.data.FactoryData;
import com.anno1800.residents.Resident;
import com.anno1800.Boardtiles.Producers;
import static com.anno1800.Boardtiles.Producers.*;
import com.anno1800.board.Board;
import com.anno1800.cards.ResidentCard;

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
    int numFactoriesOnCoast = 0;
    int numShipyards = 0;
    int gold = 0;

    int availableTradeChips = 0;
    int availableExplorerChips = 0;
    
    Factory[] factories = new Factory[15]; // listet nur alle Factories eines Spielers auf. Nicht für Logik zu benutzen, da die SHipyards nicht berücksichtigt werden. Besser getFreeLandTiles etc. benutzen.
    
    ArrayList<Shipyard> shipyards = new ArrayList<>();

    ArrayList<TradeShip> tradeShips = new ArrayList<>();

    ArrayList<ExplorerShip> explorerShips = new ArrayList<>();
    
    ArrayList<Resident> residents = new ArrayList<>();

    ArrayList<ResidentCard> residentCards = new ArrayList<>();
    
    public PlayerBoard() {}

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    public ArrayList<ResidentCard> getResidentCards() {
        return residentCards;
    }

    public int getFreeLandTiles() {
        return landTiles - factories.length - numFactoriesOnCoast;
    }

    public int getFreeCoastTiles() {
        return coastTiles - shipyards.size() - numFactoriesOnCoast;
    }

    public int getFreeSeaTiles() {
        return seaTiles - explorerShips.size() - tradeShips.size();
    }

    public Factory[] getFactories() {
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
    
    public int getGold() {
        return gold;
    }
    
    public void initializePlayerBoard(Player player, Board gameBoard) {
        PlayerBoard board = player.getPlayerBoard();
        
        board.adGold(player.getPosition());

        // Take farmer from board and add to player board
        gameBoard.takeResident(1);
        
        for (int i = 0; i < 4; i++) {
            buildResident(gameBoard, board, 1);
        }
        
        for (int i = 0; i < 3; i++) {
            buildResident(gameBoard, board, 2);
        }

        for (int i = 0; i < 2; i++) {
            buildResident(gameBoard, board, 3);
        }

        board.adShip(1, ShipType.ExplorerShip, gameBoard);
        board.adShip(1, ShipType.ExplorerShip, gameBoard);
        board.adShip(1, ShipType.TradeShip, gameBoard);

        board.adFactory(SAWMILL_GREEN);
        board.adFactory(GRAIN_FARM_GREEN);
        board.adFactory(POTATO_FARM_GREEN);
        board.adFactory(PIG_FARM_GREEN);
        board.adFactory(SHEEP_FARM_GREEN);
        board.adFactory(COAL_MINE_RED);
        board.adFactory(BRICK_FACTORY_RED);
        board.adFactory(WAREHOUSE_RED);
        board.adFactory(STEEL_WORKS_RED);
        board.adFactory(SAILMAKERS_RED);

        board.adShipyard(1);


    }
    
    private void adGold(int gold) {
        this.gold = this.gold + gold;
    }

    private void adResident(Resident resident) {
        this.residents.add(resident);
    }

    /**
     * Adds a ship to the player board if available on the game board.
     * Also adds the corresponding chips to the player's available chips.
     * Uses Rules.canBuildShip() for validation.
     * @throws IllegalStateException if ship cannot be built (rules violation)
     */
    private void adShip(int level, ShipType type, Board gameBoard) {
        // Validation is now centralized in Rules class
        // Note: In a full implementation, this would be called from GameEngine
        // which would check Rules before executing the action
        if (!gameBoard.canTakeShip(type, level)) {
            throw new IllegalStateException("Cannot add ship: not enough ships or chips available on board");
        }
        
        switch (type) {
            case ExplorerShip -> {
                ExplorerShip ship = (ExplorerShip) gameBoard.takeShip(type, level);
                explorerShips.add(ship);
                availableExplorerChips += level;
            }
            case TradeShip -> {
                TradeShip ship = (TradeShip) gameBoard.takeShip(type, level);
                tradeShips.add(ship);
                availableTradeChips += level;
            }
        }
    }

    private void adFactory(Producers type) {
        Factory newFactory = copyFactory(type);
        factories[numFactories] = newFactory;
        numFactories++;
    }

    private void adShipyard(int level) {
        Shipyard newShipyard = new Shipyard(level);
        shipyards.add(newShipyard);
        numShipyards++;
    }

    private void buildResident(Board gameBoard, PlayerBoard playerBoard, int populationLevel) {
        // Use the new unified takeResident method
        Resident resident = gameBoard.takeResident(populationLevel);
        playerBoard.adResident(resident);
        ResidentCard residentCard = gameBoard.drawResidentCard(resident.getPopulationLevel());
        playerBoard.getResidentCards().add(residentCard);
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
            template.populationLevel()
        );
    }
}