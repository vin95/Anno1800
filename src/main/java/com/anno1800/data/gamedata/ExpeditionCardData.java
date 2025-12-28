package com.anno1800.data.gamedata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.anno1800.game.cards.ExpeditionCard;

public class ExpeditionCardData {
    
    public static Deque<ExpeditionCard> getExpeditionCards() {
        return new ArrayDeque<>(List.of(
            new ExpeditionCard(5, 5, 3, 3),

            new ExpeditionCard(5, 4, 3, 2),
            new ExpeditionCard(5, 4, 3, 2),

            new ExpeditionCard(5, 3, 3, 1),
            new ExpeditionCard(5, 3, 3, 1),

            new ExpeditionCard(4, 5, 2, 3),
            new ExpeditionCard(4, 5, 2, 3),

            new ExpeditionCard(4, 4, 2, 2),
            new ExpeditionCard(4, 4, 2, 2),
            new ExpeditionCard(4, 4, 2, 2),

            new ExpeditionCard(4, 3, 2, 1),
            new ExpeditionCard(4, 3, 2, 1),
            new ExpeditionCard(4, 3, 2, 1),

            new ExpeditionCard(3, 5, 1, 3),
            new ExpeditionCard(3, 5, 1, 3),

            new ExpeditionCard(3, 4, 1, 2),
            new ExpeditionCard(3, 4, 1, 2),
            new ExpeditionCard(3, 4, 1, 2),

            new ExpeditionCard(3, 3, 1, 1),
            new ExpeditionCard(3, 3, 1, 1),
            new ExpeditionCard(3, 3, 1, 1),
            new ExpeditionCard(3, 3, 1, 1)
        ));
    }
}
