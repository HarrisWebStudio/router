package com.harriswebstudio.router.event;

public interface Cancellable {

    /**
     * Checks if the event is cancelled.
     *
     * @return true if the event is cancelled, false otherwise.
     */
    boolean isCancelled();

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancelled true to cancel the event, false to uncancel it.
     */
    void setCancelled(boolean cancelled);

}