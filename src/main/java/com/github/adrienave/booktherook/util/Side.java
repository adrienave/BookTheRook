package com.github.adrienave.booktherook.util;

public enum Side {
    BLACK,
    WHITE;

    public Side reverseSide() {
        return this == BLACK ? WHITE : BLACK;
    }
}
