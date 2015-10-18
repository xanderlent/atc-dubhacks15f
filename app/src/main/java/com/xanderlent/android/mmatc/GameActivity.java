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
import android.widget.Button;

import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.Arrays;

public class GameActivity extends AppCompatActivity {
    private PlaneView planeView;
    private Bus backendBus;
    boolean isBound = false;

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
       // getActionBar().setDisplayHomeAsUpEnabled(true);
        planeView = (PlaneView)findViewById(R.id.planeView);
        planeView.setSelectionChangeCallback(new PlaneView.SelectionChangeCallback() {
            @Override
            public void onSelectionChanged(PlaneView planeView, Plane selectedPlane) {
                //Toast.makeText(getApplicationContext(), selectedPlane == null ? "you deselected that plane, sad days" : "you clicked my best friend " + selectedPlane.getName(), Toast.LENGTH_SHORT).show();
                Snackbar.make(planeView, selectedPlane == null ? "you deselected that plane, sad days" : "you clicked my best friend " + selectedPlane.getName(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BackendService.class);
        bindService(intent, backendConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            unbindService(backendConnection);
            isBound = false;
        }
    }

    //TODO: BUTTONS
    public void upClicked(View view) {
        Snackbar.make(planeView, "UP Clicked!", Snackbar.LENGTH_SHORT).show();
        // NOP
    }
    public void downClicked(View view) {
        Snackbar.make(planeView, "DOWN Clicked!", Snackbar.LENGTH_SHORT).show();
        // NOP
    }
    public void rightClicked(View view) {
        Snackbar.make(planeView, "RIGHT Clicked!", Snackbar.LENGTH_SHORT).show();
        // NOP
    }
    public void leftClicked(View view) {
        Snackbar.make(planeView, "LEFT Clicked!", Snackbar.LENGTH_SHORT).show();
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

    @Subscribe
    public void onPlanesChanged(BackendService.PlanesChangedEvent event) {
        planeView.setPlanes(event.getPlanes());
    }

    public class UserChangedDirectionEvent {
        private Plane plane;
        private Direction direction;

        public UserChangedDirectionEvent(Plane plane, Direction direction) {
            this.plane = plane;
            this.direction = direction;
        }

        public Plane getPlane() {
            return plane;
        }

        public Direction getDirection() {
            return direction;
        }
    }

    public class UserChangedAltitudeEvent {
        private Plane plane;
        private int targetAltitude;

        public UserChangedAltitudeEvent(Plane plane, int targetAltitude) {
            this.plane = plane;
            this.targetAltitude = targetAltitude;
        }

        public Plane getPlane() {
            return plane;
        }

        public int getTargetAltitude() {
            return targetAltitude;
        }
    }
}
