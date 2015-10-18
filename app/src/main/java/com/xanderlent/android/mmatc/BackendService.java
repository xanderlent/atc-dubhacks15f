package com.xanderlent.android.mmatc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.Collection;

public class BackendService extends Service {
    private static final int TICK_RATE = 3000;

    private final IBinder backendBinder;
    private static Bus backendBus; // The Bus is a singleton, according to Otto docs...
    private Backend backend;
    private Handler handler;
    private boolean alive = true;

    /* Initialize the BackendService */
    public BackendService() {
        backend = new Backend();
        backendBus = new Bus();
        backendBus.register(this);
        backendBinder = new BackendBinder();
        // ^ Register so as to subscribe to user changed plane event notifications
        handler = new Handler();
        handler.postDelayed(tickRunnable, TICK_RATE);
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
        backend.tick();
        firePlanesChanged();
    }

    /* Get notified about events due to the user changing altitude, and update accordingly. */
    @Subscribe
    public void userChangedAltitude(GameActivity.UserChangedAltitudeEvent event) {
        backend.changeAltitude(event.getPlane(), event.getNewAltitude());
    }

    /* Get notified about events due to the user changing direction, and update accordingly. */
    @Subscribe
    public void userChangedDirection(GameActivity.UserChangedDirectionEvent event) {
        backend.turnPlane(event.getPlane(), event.getDirection());
        firePlanesChanged();
    }

    private void firePlanesChanged() {
        backendBus.post(producePlanesChangedEvent());
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

    private Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if(alive) {
                tick();
                handler.postDelayed(tickRunnable, TICK_RATE);
            }
        }
    };

    @Override
    public void onDestroy() {
        alive = false;
        super.onDestroy();
    }
}
