package com.criticalrange.core.events;

/**
 * Interface for handling feature events
 */
@FunctionalInterface
public interface FeatureEventHandler {
    /**
     * Handle a feature event
     * @param event The event to handle
     * @return true if the event was handled, false if it should continue propagating
     */
    boolean handle(FeatureEvent event);
}