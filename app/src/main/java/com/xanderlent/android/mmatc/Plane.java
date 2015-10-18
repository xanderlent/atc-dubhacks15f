package com.xanderlent.android.mmatc;

public class Plane {
    public static final int DEFAULT_ALTITUDE = 7;

    /**
     * Name of the plane.  Usually a single character.
     */
    private String name;

    /**
     * Altitude of the plane, in thousands.  Range [0, 9].
     */
    private int altitude;

    /**
     * Current position of the plane.
     */
    private Position position;

    /**
     * Current direction of the plane.
     */
    private Direction direction;

    /**
     * Index of the destination exit.
     */
    private int destinationExitNo;

    public Plane(String name, Position position, Direction direction, int destinationExitNo) {
        this.name = name;
        this.altitude = DEFAULT_ALTITUDE;
        this.position = position;
        this.direction = direction;
        this.destinationExitNo = destinationExitNo;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        if(altitude < 0 || altitude > 9) {
            throw new IllegalArgumentException("altitude out of bounds");
        }
        this.altitude = altitude;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        if(position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        this.position = position;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(direction == null) {
            throw new IllegalArgumentException("direction must not be null");
        }
        this.direction = direction;
    }

    public int getDestinationExitNo() {
        return destinationExitNo;
    }
}