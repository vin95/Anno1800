package com.anno1800.data;

import java.util.ArrayDeque;
import java.util.Deque;

import com.anno1800.Boardtiles.Factory;
import com.anno1800.Boardtiles.NewWorldIsland;
import static com.anno1800.data.Factories.*;

public class NewWorldIslandsData {
    public static Deque<NewWorldIsland> createNewWorldIslands() {
        Deque<NewWorldIsland> islands = new ArrayDeque<>();

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(CACAO_PLANTATION),
                    FactoryData.getFactory(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(COFFEE_PLANTATION),
                    FactoryData.getFactory(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(SUGAR_PLANTATION),
                    FactoryData.getFactory(COFFEE_PLANTATION),
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(CACAO_PLANTATION),
                    FactoryData.getFactory(SUGAR_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(TOBACCO_PLANTATION),
                    FactoryData.getFactory(RUBBER_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(COFFEE_PLANTATION),
                    FactoryData.getFactory(TOBACCO_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(RUBBER_PLANTATION),
                    FactoryData.getFactory(SUGAR_PLANTATION)
                }
            )
        );

        islands.add(
            new NewWorldIsland(
                new Factory[]{
                    FactoryData.getFactory(COTTON_PLANTATION),
                    FactoryData.getFactory(CACAO_PLANTATION),
                    FactoryData.getFactory(RUBBER_PLANTATION)
                }
            )
        );

        return islands;
    }
}
