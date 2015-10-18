package com.xanderlent.android.mmatc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collection;
import java.util.Collections;

public class PlaneView extends View {
    public static final int GRID_WIDTH = 20;
    public static final int GRID_HEIGHT = 20;

    public interface SelectionChangeCallback {
        void onSelectionChanged(PlaneView planeView, Plane selectedPlane);
    }

    private Collection<Plane> planes;
    private Plane selectedPlane;
    private SelectionChangeCallback selectionChangeCallback;

    public PlaneView(Context context) {
        super(context);
        init(null, 0);
    }

    public PlaneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PlaneView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PlaneView, defStyle, 0);

        a.recycle();

        planes = Collections.emptyList();
        selectedPlane = null;
        selectionChangeCallback = null;
    }

    public void setPlanes(Collection<Plane> planes) {
        this.planes = planes;
        invalidate();
    }

    public void setSelectionChangeCallback(SelectionChangeCallback callback) {
        selectionChangeCallback = callback;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);
        strokePaint.setColor(Color.BLACK);
        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        for(Plane plane : planes) {
            Position position = plane.getPosition();
            float left = (float)position.getX() * getWidth() / GRID_WIDTH;
            float top = (float)position.getY() * getHeight() / GRID_HEIGHT;
            float right = left + (float)getWidth() / GRID_WIDTH;
            float bottom = top + (float)getHeight() / GRID_HEIGHT;
            if(plane == selectedPlane) {
                fillPaint.setColor(Color.GREEN);
            }else{
                fillPaint.setColor(Color.YELLOW);
            }
            canvas.drawRect(left, top, right, bottom, fillPaint);
            canvas.drawRect(left, top, right, bottom, strokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
            int gridX = (int)(touchX / getWidth() * GRID_WIDTH);
            int gridY = (int)(touchY / getHeight() * GRID_HEIGHT);
            Position position = new Position(gridX, gridY);
            Plane selectedPlane = null;
            for(Plane plane : planes) {
                if(plane.getPosition().equals(position)) {
                    selectedPlane = plane;
                }
            }
            setSelectedPlane(selectedPlane);
            if(selectionChangeCallback != null) {
                selectionChangeCallback.onSelectionChanged(this, selectedPlane);
            }
            return true;
        }
        return false;
    }

    public Plane getSelectedPlane() {
        return selectedPlane;
    }

    public void setSelectedPlane(Plane selectedPlane) {
        this.selectedPlane = selectedPlane;
        invalidate();
    }
}
