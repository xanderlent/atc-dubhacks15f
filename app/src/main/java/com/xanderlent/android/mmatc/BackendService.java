package com.xanderlent.android.mmatc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Bus;

public class BackendService extends Service {
    private final IBinder backendBinder;
    private Bus backendBus;
    private Backend backend;

    public BackendService() {
        backendBinder = new BackendBinder();
        backendBus = new Bus();
        backend = new Backend();
        //backendBus.post(/* planesChanged */ null); // TODO Make Published message
        //backendBus.post(/* neighborsChanged*/ null); // TODO Make Published message
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
}
