package com.xanderlent.android.mmatc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.Collection;

public class BackendService extends Service {
    private final IBinder backendBinder;
    private Bus backendBus;
    private Backend backend;

    public BackendService() {
        backend = new Backend();
        backendBus = new Bus();
        backendBus.register(this);
        backendBinder = new BackendBinder();
        // ^ Register so as to subscribe to user changed plane event notifications
        // TODO Create Timer/AlarmManager to trigger tick()
    }

    @Override
    public IBinder onBind(Intent intent) {
        return backendBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class BackendBinder extends Binder {
        Bus getBackendBus() {
            return backendBus;
        }
    }

    private void tick() {
        backend.tick();
        backendBus.post(new PlanesChangedEvent(backend.getPlanes()));
    }

    @Subscribe
    public void userChangedAltitude(GameActivity.UserChangedAltitudeEvent event) {
        backend.changeAltitude(event.getPlane(), event.getIncrement());
    }

    @Subscribe
    public void userChangedDirection(GameActivity.UserChangedDirectionEvent event) {
        backend.turnPlane(event.getPlane(), event.getDirection());
    }

    @Produce
    public PlanesChangedEvent producePlanesChangedEvent() {
        return new PlanesChangedEvent(backend.getPlanes());
    }

    public class PlanesChangedEvent {
        private Collection<Plane> planes;

        public PlanesChangedEvent(Collection<Plane> planes) {
            this.planes = planes;
        }

        public Collection<Plane> getPlanes() {
            return planes;
        }
    }
}
