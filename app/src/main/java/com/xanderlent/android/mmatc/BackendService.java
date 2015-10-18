package com.xanderlent.android.mmatc;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.Collection;
import java.util.Random;

public class BackendService extends Service {
    private static final int TICK_RATE = 3000;

    private final IBinder backendBinder;
    private static Bus backendBus; // The Bus is a singleton, according to Otto docs...
    private Backend backend;
    private Handler handler;
    private boolean alive = true;
    private boolean isBoundToBluetooth = false;
    private Bus bluetoothBus;
    private Random random;

    /* Initialize the BackendService */
    public BackendService() {
        random = new Random();
        backend = new Backend();
        backendBus = new Bus();
        backendBus.register(this);
        backendBinder = new Binder();
        // ^ Register so as to subscribe to user changed plane event notifications
        handler = new Handler();
        handler.postDelayed(tickRunnable, TICK_RATE);
        // Bind to BluetoothService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, bluetoothConnection, Context.BIND_AUTO_CREATE);
    }

    /* Give the component binding this service a reference to the Binder. */
    @Override
    public IBinder onBind(Intent intent) {
        return backendBinder;
    }

    /**
     * The Binder that the client will use to bind to this service. "Because we know this service
     * always runs in the same process as its clients, we don't need to deal with IPC."
     * (In quotes: verbatim from Android example code.)
     */
    public class Binder extends android.os.Binder {
        Bus getBus() {
            return backendBus;
        }
    }

    /* Process one tick (step) of the model/backend. */
    private void tick() {
        backend.tick();
        if (backend.checkIfCrashed()) {
            firePlanesCrashed();
            alive = false;
            return;
        }
        // fireOutgoingPlane();
        firePlanesChanged();
    }

    public void firePlanesChanged(Edge edge) {
        bluetoothBus.post(new BluetoothService.OutgoingPlaneEvent(edge));
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

    private void firePlanesCrashed() {
        backendBus.post(new PlanesCrashedEvent());
    }

    /* Automatically create a PlanesChangedEvent when said event is subscribed to. */
    @Produce
    public PlanesChangedEvent producePlanesChangedEvent() {
        return new PlanesChangedEvent(backend.getPlanes());
    }

    /* An event that communicates the model's/backend's Planes when things change. */
    public static class PlanesChangedEvent {
        private Collection<Plane> planes;

        public PlanesChangedEvent(Collection<Plane> planes) {
            this.planes = planes;
        }

        public Collection<Plane> getPlanes() {
            return planes;
        }
    }

    /* Get notified about events due to the user changing direction, and update accordingly. */
    @Subscribe
    public void onIncomingPlane(BluetoothService.IncomingPlaneEvent event) {
        Edge inEdge = event.getEdge();
        Position newPosition = getRandomPositionAlongEdge(inEdge);
        backend.createPlane(newPosition);
        firePlanesChanged();
    }

    private Position getRandomPositionAlongEdge(Edge edge) {
        switch (edge) {
            case NORTH:
                break;
            case EAST:
                break;
            case SOUTH:
                break;
            case WEST:
                break;
        }
        return new Position(0,0);
        /* TODO Get way to give random position along that edge */
    }

    /* Get notified about events due to the user changing direction, and update accordingly. */
    @Subscribe
    public void onPeersChanged(BluetoothService.PeersChangedEvent event) {
        /* TODO Need to do something about peers? */
        firePlanesChanged();
    }

    /* An event that communicates is things have crashed.
    *  TODO: Which planes crashed? */
    public static class PlanesCrashedEvent {}

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
        if (isBoundToBluetooth) {
            unbindService(bluetoothConnection);
            isBoundToBluetooth = false;
        }
        super.onDestroy();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection bluetoothConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to BackendService, cast the IBinder
            // and get the bus from the BackendService instance.
            BluetoothService.Binder binder = (BluetoothService.Binder) service;
            bluetoothBus = binder.getBus();
            bluetoothBus.register(BackendService.this);
            isBoundToBluetooth = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bluetoothBus.unregister(BackendService.this);
            isBoundToBluetooth = false;
        }
    };
}
