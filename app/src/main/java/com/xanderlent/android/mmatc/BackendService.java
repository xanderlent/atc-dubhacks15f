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
    private static Bus backendBus; // The Bus is a singleton, according to Otto docs...
    private Backend backend;

    /* Initialize the BackendService */
    public BackendService() {
        backend = new Backend();
        backendBus = new Bus();
        backendBus.register(this);
        backendBinder = new BackendBinder();
        // ^ Register so as to subscribe to user changed plane event notifications
        // TODO Create Timer/AlarmManager to trigger tick()
    }

    /* Give the component binding this service a reference to the BackendBinder. */
    @Override
    public IBinder onBind(Intent intent) {
        return backendBinder;
    }

    /**
     * The Binder that the client will use to bind to this service. "Because we know this service
     * always runs in the same process as its clients, we don't need to deal with IPC."
     * (In quotes: verbatim from Android example code.)
     */
    public class BackendBinder extends Binder {
        Bus getBackendBus() {
            return backendBus;
        }
    }

    /* Process one tick (step) of the model/backend. */
    private void tick() {
        backend.tick(); // Step the backend
        backendBus.post(new PlanesChangedEvent(backend.getPlanes()));
        // ^ Create a message notifying clients that the Planes have changed.
    }

    /* Get notified about events due to the user changing altitude, and update accordingly. */
    @Subscribe
    public void userChangedAltitude(GameActivity.UserChangedAltitudeEvent event) {
        backend.changeAltitude(event.getPlane(), event.getIncrement());
    }

    /* Get notified about events due to the user changing direction, and update accordingly. */
    @Subscribe
    public void userChangedDirection(GameActivity.UserChangedDirectionEvent event) {
        backend.turnPlane(event.getPlane(), event.getDirection());
    }

    /* Automatically create a PlanesChangedEvent when said event is subscribed to. */
    @Produce
    public PlanesChangedEvent producePlanesChangedEvent() {
        return new PlanesChangedEvent(backend.getPlanes());
    }

    /* An event that communicates the model's/backend's Planes when things change. */
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
