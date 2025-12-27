package com.anno1800.actions;

/**
 * Placeholder class for FulfillNeed actions.
 * Actual fulfill actions are defined as records in Action interface.
 * This class exists to satisfy the sealed interface permit clause.
 */
public final class FullfillNeed implements Action {
    private FullfillNeed() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Use Action.FulfillNeed instead");
    }
}
