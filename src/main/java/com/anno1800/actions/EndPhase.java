package com.anno1800.actions;

/**
 * Placeholder class for EndPhase actions.
 * Actual end phase action is defined as record in Action interface.
 * This class exists to satisfy the sealed interface permit clause.
 */
public final class EndPhase implements Action {
    private EndPhase() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Use Action.EndCurrentPhase instead");
    }
}
