package com.xanderlent.android.mmatc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

import java.util.Collection;

public class BackendService extends Service {
    private final IBinder backendBinder;
    private Bus backendBus;
    private Backend backend;

    public BackendService() {
        backendBinder = new BackendBinder();
        backendBus = new Bus();
        backendBus.register(this);
        backend = new Backend();
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
