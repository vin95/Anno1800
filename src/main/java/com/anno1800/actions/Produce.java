package com.anno1800.actions;

/**
 * Placeholder class for Produce actions.
 * Actual produce actions are defined as records in Action interface.
 * This class exists to satisfy the sealed interface permit clause.
 */
public final class Produce implements Action {
    private Produce() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Use Action.ProduceGoods instead");
    }
}
