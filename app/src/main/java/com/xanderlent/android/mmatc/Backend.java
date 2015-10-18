package com.xanderlent.android.mmatc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class Backend {
    private Random random;
    private Collection<Plane> planes;

    public Backend() {
        random = new Random();
        planes = Collections.synchronizedCollection(new ArrayList<Plane>());
        planes.add(new Plane(new Position(10, 2), Direction.SOUTH_EAST, 3));
        planes.add(new Plane(new Position(4, 4), Direction.SOUTH_EAST, 1));
        planes.add(new Plane(new Position(5, 15), Direction.NORTH, 4));
    }

    public synchronized Collection<Plane> getPlanes() {
        return Collections.unmodifiableCollection(planes);
    }

    public synchronized void turnPlane(Plane plane, Direction newDirection) {
        if(!planes.contains(plane)) {
            return;  // TODO: exception?
        }
        plane.setDirection(newDirection);
    }

    public synchronized void changeAltitude(Plane plane, int newAltitude) {
        if(!planes.contains(plane)) {
            return;  // TODO: exception?
        }
        plane.setTargetAltitude(newAltitude);
    }

    public synchronized void createPlane(Position where) {
        Direction direction = Direction.NORTH_EAST;
        int destIndex = random.nextInt(6);
        Plane plane = new Plane(where, direction, destIndex);
        planes.add(plane);
    }

    public synchronized void tick() {
        for(Plane plane : planes) {
            plane.setPosition(plane.getPosition().towardsDirection(plane.getDirection()));
            plane.setAltitudeImmediately(plane.getAltitude() +
                    Integer.signum(plane.getTargetAltitude() - plane.getAltitude()), false);
        }
    }
}
