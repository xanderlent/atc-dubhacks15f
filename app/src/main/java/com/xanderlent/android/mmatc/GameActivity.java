package com.xanderlent.android.mmatc;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.Arrays;

public class GameActivity extends AppCompatActivity {
    private PlaneView planeView;

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
    }

}
