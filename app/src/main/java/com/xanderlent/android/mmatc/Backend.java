package com.xanderlent.android.mmatc;

import java.util.Collection;

public interface Backend {
    Collection<Plane> getPlanes();
    void turnPlane(Plane plane, Direction newDirection);
    void changeAltitude(Plane plane, int newAltitude);
    void createPlane(Position where);
    void tick();
}
