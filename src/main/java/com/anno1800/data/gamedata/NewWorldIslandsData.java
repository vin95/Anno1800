package com.anno1800.data.gamedata;

import static com.anno1800.data.gamedata.Producers.*;

import java.util.ArrayDeque;
import java.util.Deque;

import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.NewWorldIsland;

public class NewWorldIslandsData {
    public static Deque<NewWorldIsland> createNewWorldIslands() {
        Deque<NewWorldIsland> islands = new ArrayDeque<>();

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(CACAO_PLANTATION),
                    FactoryData.getPlantation(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(COFFEE_PLANTATION),
                    FactoryData.getPlantation(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(SUGAR_PLANTATION),
                    FactoryData.getPlantation(COFFEE_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(CACAO_PLANTATION),
                    FactoryData.getPlantation(SUGAR_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(TOBACCO_PLANTATION),
                    FactoryData.getPlantation(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(COFFEE_PLANTATION),
                    FactoryData.getPlantation(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    FactoryData.getPlantation(COTTON_PLANTATION),
                    FactoryData.getPlantation(RUBBER_PLANTATION),
                    FactoryData.getPlantation(SUGAR_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(CACAO_PLANTATION),
                    (Plantation) FactoryData.getProducer(RUBBER_PLANTATION)
                }
            )
        );

        return islands;
    }
}
