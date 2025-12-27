package com.anno1800.actions;

/**
 * Placeholder class for Build actions.
 * Actual build actions are defined as records in Action interface.
 * This class exists to satisfy the sealed interface permit clause.
 */
public final class Build implements Action {
    private Build() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("Use Action.BuildFactory instead");
    }
}
