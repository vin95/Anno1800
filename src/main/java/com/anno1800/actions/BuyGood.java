package com.anno1800.actions;

/**
 * Placeholder class for BuyGood actions.
 * Actual buy actions are defined as records in Action interface.
 * This class exists to satisfy the sealed interface permit clause.
 */
public final class BuyGood implements Action {
    private BuyGood() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Use Action.BuyFromMarket instead");
    }
}
