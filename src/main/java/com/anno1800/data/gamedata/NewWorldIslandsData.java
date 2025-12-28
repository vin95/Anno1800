package com.anno1800.data.gamedata;

import java.util.ArrayDeque;
import java.util.Deque;

import com.anno1800.game.tiles.Plantation;
import com.anno1800.game.tiles.NewWorldIsland;
import static com.anno1800.game.tiles.Producers.*;

public class NewWorldIslandsData {
    public static Deque<NewWorldIsland> createNewWorldIslands() {
        Deque<NewWorldIsland> islands = new ArrayDeque<>();

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(CACAO_PLANTATION),
                    (Plantation) FactoryData.getProducer(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(COFFEE_PLANTATION),
                    (Plantation) FactoryData.getProducer(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(SUGAR_PLANTATION),
                    (Plantation) FactoryData.getProducer(COFFEE_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(CACAO_PLANTATION),
                    (Plantation) FactoryData.getProducer(SUGAR_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(TOBACCO_PLANTATION),
                    (Plantation) FactoryData.getProducer(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(COFFEE_PLANTATION),
                    (Plantation) FactoryData.getProducer(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Plantation[]{
                    (Plantation) FactoryData.getProducer(COTTON_PLANTATION),
                    (Plantation) FactoryData.getProducer(RUBBER_PLANTATION),
                    (Plantation) FactoryData.getProducer(SUGAR_PLANTATION)
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
