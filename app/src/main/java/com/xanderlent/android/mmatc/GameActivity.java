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
import android.util.Log;

import com.squareup.otto.Bus;

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

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        {
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
       // getActionBar().setDisplayHomeAsUpEnabled(true);
        planeView = (PlaneView)findViewById(R.id.planeView);
        planeView.setPlanes(Arrays.asList(
                new Plane("Ali", new Position(10, 2), Direction.SOUTH_EAST, 3),
                new Plane("Eco", new Position(4, 4), Direction.SOUTH_EAST, 1),
                new Plane("Dia", new Position(5, 15), Direction.NORTH, 4)
        ));
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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection backendConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to BackendService, cast the IBinder
            // and get the bus from the BackendService instance.
            BackendService.BackendBinder binder = (BackendService.BackendBinder) service;
            backendBus = binder.getBackendBus();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}
