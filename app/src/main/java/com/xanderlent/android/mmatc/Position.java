package com.xanderlent.android.mmatc;

/**
 * Represents a position.
 */
public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position towardsDirection(Direction direction) {
        return new Position(x + direction.getDeltaX(), y + direction.getDeltaY());
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Position)) {
            return false;
        }
        Position other = (Position)o;
        return x == other.x && y == other.y;
    }
}
