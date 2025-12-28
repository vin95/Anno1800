package com.anno1800.data.gamedata;

import java.util.ArrayDeque;
import java.util.Deque;

import com.anno1800.game.tiles.ExplorerShip;
import com.anno1800.game.tiles.Factory;
import com.anno1800.game.tiles.OldWorldIsland;
import com.anno1800.game.tiles.Shipyard;
import com.anno1800.game.tiles.TradeShip;
import com.anno1800.game.rewards.Reward;
import static com.anno1800.data.gamedata.Factories.*;

public class OldWorldIslandData {
    @SuppressWarnings("deprecation")
    public static Deque<OldWorldIsland> createOldWorldIslands() {
        Deque<OldWorldIsland> islands = new ArrayDeque<>();

        // Example islands, you can customize the parameters as needed
        islands.add(
            new OldWorldIsland(
                2,
                2,
                2,
                new Reward.NewResidents(2, 1),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );
        
        islands.add(
            new OldWorldIsland(
                2,
                2,
                2,
                new Reward.NewResidents(2, 2),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );
        
        islands.add(
            new OldWorldIsland(
                2,
                2,
                2,
                new Reward.NewResidents(1, 3),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );
        
        islands.add(
            new OldWorldIsland(
                1,
                2,
                2,
                new Reward.Gold(0),
                new Factory[]{FactoryData.getFactory(WAREHOUSE_BLUE)},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                1,
                2,
                2,
                new Reward.Gold(0),
                new Factory[]{FactoryData.getFactory(BRICK_FACTORY_BLUE)},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                1,
                2,
                2,
                new Reward.Gold(0),
                new Factory[]{FactoryData.getFactory(COAL_MINE_BLUE)},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                1,
                2,
                2,
                new Reward.Gold(0),
                new Factory[]{FactoryData.getFactory(STEEL_WORKS_BLUE)},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                1,
                2,
                2,
                new Reward.Gold(0),
                new Factory[]{FactoryData.getFactory(SAILMAKERS_BLUE)},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                2,
                2,
                1,
                new Reward.Gold(0),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{new TradeShip(1)},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                2,
                2,
                1,
                new Reward.Gold(0),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{new ExplorerShip(1)}
            )
        );

        islands.add(
            new OldWorldIsland(
                2,
                1,
                2,
                new Reward.Gold(0),
                new Factory[]{},
                new Shipyard[]{new Shipyard(1)},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        islands.add(
            new OldWorldIsland(
                2,
                2,
                2,
                new Reward.ExpeditionCards(),
                new Factory[]{},
                new Shipyard[]{},
                new TradeShip[]{},
                new ExplorerShip[]{}
            )
        );

        return islands;
    }
}
