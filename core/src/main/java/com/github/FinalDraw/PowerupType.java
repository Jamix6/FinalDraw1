package com.github.FinalDraw;

public enum PowerupType {
    GO_FOR_17("Go for 17"),
    GO_FOR_24("Go for 24"),
    PROTECTION("Protection"),
    REMOVAL("Removal"),
    RESHUFFLE("Reshuffle"),
    DOUBLE_DOWN("DoubleDown"),
    FORESIGHT("Foresight"),
    SWAP("Swap"),
    OVERLOAD("Overload"),
    DISCARD("Discard"),
    TOSS("Toss"),
    CLEAR("Clear");

    private final String label;

    PowerupType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
