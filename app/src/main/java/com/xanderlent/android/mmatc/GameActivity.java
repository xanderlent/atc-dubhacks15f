package com.xanderlent.android.mmatc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
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
        // Bind to LocalService
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
            UserChangedAltitudeEvent event;
            int newAltitude = selectedPlane.getAltitude() + 1;
            event = new UserChangedAltitudeEvent(selectedPlane, newAltitude);
            backendBus.post(event);
        }
        // NOP
    }
    public void downClicked(View view) {
        if (selectedPlane != null) {
            UserChangedAltitudeEvent event;
            int newAltitude = selectedPlane.getAltitude() - 1;
            event = new UserChangedAltitudeEvent(selectedPlane, newAltitude);
            backendBus.post(event);
        }
        // NOP
    }
    public void rightClicked(View view) {
        if (selectedPlane != null) {
            UserChangedDirectionEvent event;
            Direction newDirection = selectedPlane.getDirection().right();
            event = new UserChangedDirectionEvent(selectedPlane, newDirection);
            backendBus.post(event);
        }
        // NOP
    }
    public void leftClicked(View view) {
        if (selectedPlane != null) {
            UserChangedDirectionEvent event;
            Direction newDirection = selectedPlane.getDirection().left();
            event = new UserChangedDirectionEvent(selectedPlane, newDirection);
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
            BackendService.BackendBinder binder = (BackendService.BackendBinder) service;
            backendBus = binder.getBackendBus();
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

    private void updatePlaneStatusTexts() {
        if(selectedPlane == null) {
            altStatusText.setText("");
        }else {
            altStatusText.setText(getString(R.string.alt_status_text, selectedPlane.getDestinationExitNo()));
        }
    }

    /* An event that communicates the user's desires about altitude when things are changed. */
    public class UserChangedAltitudeEvent {
        private Plane plane;
        private int increment;

        public UserChangedAltitudeEvent(Plane plane, int increment) {
            this.plane = plane;
            this.increment = increment;
        }

        public Plane getPlane() {
            return plane;
        }

        public int getIncrement() {
            return increment;
        }
    }

    /* An event that communicates the user's desires about direction when things are changed. */
    public class UserChangedDirectionEvent {
        private Plane plane;
        private Direction direction;

        public UserChangedDirectionEvent(Plane plane, Direction newDirection) {
            this.plane = plane;
            this.direction = newDirection;
        }

        public Plane getPlane() {
            return plane;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}
