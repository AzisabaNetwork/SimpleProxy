package net.azisaba.simpleproxy.api.event;

public enum EventPriority {
    /**
     * Event call is of very low importance and should be run first, to allow other plugins to further customise the outcome.
     */
    LOWEST(-30),    // ^ First
    VERY_LOW(-20),  // |
    LOW(-10),       // |
    NORMAL(0),      // |
    HIGH(10),       // |
    VERY_HIGH(20),  // |
    HIGHEST(30),    // |
    /** The event executor should be run last, No modifications to the event should be made under this priority. */
    MONITOR(40),    // v Last
    ;

    private final int slot;

    EventPriority(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
