package com.xanderlent.android.mmatc;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class GameActivity extends AppCompatActivity {
    private PlaneView planeView;
    private Bus backendBus;
    private boolean isBound;
    private Plane selectedPlane;
    private TextView altStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        selectedPlane = null; // Generally, we don't select a plane.
        planeView = (PlaneView)findViewById(R.id.planeView);
        planeView.setSelectionChangeCallback(new PlaneView.SelectionChangeCallback() {
            @Override
            public void onSelectionChanged(PlaneView planeView, Plane selectedPlane) {
                GameActivity.this.selectedPlane = selectedPlane;
                updatePlaneStatusTexts();

            }
        });
        altStatusText = (TextView)findViewById(R.id.alt_status);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to BackendService
        Intent intent = new Intent(this, BackendService.class);
        bindService(intent, backendConnection, Context.BIND_AUTO_CREATE);
    }

    // TODO: Implement onResume & onDestroy b/c Android says we have to!

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            unbindService(backendConnection);
            isBound = false;
        }
    }

    /* Handle the various cases for the four actions' buttons. */
    public void upClicked(View view) {
        if (selectedPlane != null) {
            BackendService.UserChangedAltitudeEvent event;
            int newAltitude = selectedPlane.getTargetAltitude() + 1;
            event = new BackendService.UserChangedAltitudeEvent(selectedPlane, newAltitude);
            backendBus.post(event);
        }
        // NOP
    }
    public void downClicked(View view) {
        if (selectedPlane != null) {
            BackendService.UserChangedAltitudeEvent event;
            int newAltitude = selectedPlane.getTargetAltitude() - 1;
            event = new BackendService.UserChangedAltitudeEvent(selectedPlane, newAltitude);
            backendBus.post(event);
        }
        // NOP
    }
    public void rightClicked(View view) {
        if (selectedPlane != null) {
            BackendService.UserChangedDirectionEvent event;
            Direction newDirection = selectedPlane.getDirection().right();
            event = new BackendService.UserChangedDirectionEvent(selectedPlane, newDirection);
            backendBus.post(event);
        }
        // NOP
    }
    public void leftClicked(View view) {
        if (selectedPlane != null) {
            BackendService.UserChangedDirectionEvent event;
            Direction newDirection = selectedPlane.getDirection().left();
            event = new BackendService.UserChangedDirectionEvent(selectedPlane, newDirection);
            backendBus.post(event);
        }
        // NOP
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection backendConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to BackendService, cast the IBinder
            // and get the bus from the BackendService instance.
            BackendService.Binder binder = (BackendService.Binder) service;
            backendBus = binder.getBus();
            backendBus.register(GameActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            backendBus.unregister(GameActivity.this);
            isBound = false;
        }
    };

    /* Get notified about events due to the model changing its Planes, and update accordingly. */
    @Subscribe
    public void onPlanesChanged(BackendService.PlanesChangedEvent event) {
        planeView.setPlanes(event.getPlanes());
        updatePlaneStatusTexts();
    }

    /* Get notified about events due to the model changing its Planes, and update accordingly. */
    @Subscribe
    public void onPlanesCrashed(BackendService.PlanesCrashedEvent event) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_over_title))
                .setMessage(getString(R.string.game_over_message))
                .create()
                .show();
    }

    @Subscribe
    public void onPeersChanged(BluetoothService.PeersChangedEvent event) {
        planeView.setNeighborNames(event.getNeighborNames());
    }

    private void updatePlaneStatusTexts() {
        if(selectedPlane == null) {
            altStatusText.setText("");
        }else {
            altStatusText.setText(getString(R.string.alt_status_text, selectedPlane.getDestinationExitNo()));
        }
    }
}
