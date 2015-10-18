package com.xanderlent.android.mmatc;

public enum Direction {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST;

    public Direction left() {
        switch(this) {
            case NORTH: return NORTH_WEST;
            case NORTH_EAST: return NORTH;
            case EAST: return NORTH_EAST;
            case SOUTH_EAST: return EAST;
            case SOUTH: return SOUTH_EAST;
            case SOUTH_WEST: return SOUTH;
            case WEST: return SOUTH_WEST;
            case NORTH_WEST: return WEST;
        }
        throw new AssertionError("unhandled case in Direction.left");
    }

    public Direction right() {
        switch(this) {
            case NORTH: return NORTH_EAST;
            case NORTH_EAST: return EAST;
            case EAST: return SOUTH_EAST;
            case SOUTH_EAST: return SOUTH;
            case SOUTH: return SOUTH_WEST;
            case SOUTH_WEST: return WEST;
            case WEST: return NORTH_WEST;
            case NORTH_WEST: return NORTH;
        }
        throw new AssertionError("unhandled case in Direction.right");
    }

    /**
     * @return The change in the X coördinate when going in this direction.
     */
    public int getDeltaX() {
        switch(this) {
            case NORTH: return 0;
            case NORTH_EAST: return 1;
            case EAST: return 1;
            case SOUTH_EAST: return 1;
            case SOUTH: return 0;
            case SOUTH_WEST: return -1;
            case WEST: return -1;
            case NORTH_WEST: return -1;
        }
        throw new AssertionError("unhandled case in Direction.getDeltaX");
    }

    /**
     * @return The change in the Y coördinate when going in this direction.
     */
    public int getDeltaY() {
        switch(this) {
            case NORTH: return -1;
            case NORTH_EAST: return -1;
            case EAST: return 0;
            case SOUTH_EAST: return 1;
            case SOUTH: return 1;
            case SOUTH_WEST: return 1;
            case WEST: return 0;
            case NORTH_WEST: return -1;
        }
        throw new AssertionError("unhandled case in Direction.getDeltaY");
    }
}
