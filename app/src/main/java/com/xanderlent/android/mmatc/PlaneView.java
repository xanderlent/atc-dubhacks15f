package com.xanderlent.android.mmatc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
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

    private Paint fillPaint;
    private Paint strokePaint;
    private TextPaint textPaint;
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

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

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

        strokePaint.setStrokeWidth(2);
        strokePaint.setColor(Color.BLACK);
        fillPaint.setColor(Color.DKGRAY);

        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), fillPaint);

        canvas.save();
        float minimalDimension = Math.min(getWidth(), getHeight());
        canvas.translate((getWidth() - minimalDimension) / 2, (getHeight() - minimalDimension) / 2);
        fillPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, minimalDimension, minimalDimension, fillPaint);
        canvas.drawRect(0, 0, minimalDimension, minimalDimension, strokePaint);

        textPaint.setTextSize(minimalDimension / GRID_HEIGHT * 0.7f);

        Path path = new Path();
        {
            float bigness = minimalDimension / GRID_WIDTH / 2 * 1.3f;
            path.moveTo(0, -bigness);
            path.lineTo(bigness, bigness);
            path.lineTo(0, bigness / 2);
            path.lineTo(-bigness, bigness);
            path.close();
        }

        for(Plane plane : planes) {
            Position position = plane.getPosition();
            float left = position.getX() * minimalDimension / GRID_WIDTH;
            float top = position.getY() * minimalDimension / GRID_HEIGHT;
            float right = left + minimalDimension / GRID_WIDTH;
            float bottom = top + minimalDimension / GRID_HEIGHT;
            if(plane == selectedPlane) {
                fillPaint.setColor(Color.GREEN);
            }else{
                fillPaint.setColor(Color.YELLOW);
            }
            canvas.save();
            canvas.translate((left + right) / 2, (top + bottom) / 2);
            switch(plane.getDirection()) {
                case NORTH: canvas.rotate(0); break;
                case NORTH_EAST: canvas.rotate(45); break;
                case EAST: canvas.rotate(90); break;
                case SOUTH_EAST: canvas.rotate(135); break;
                case SOUTH: canvas.rotate(180); break;
                case SOUTH_WEST: canvas.rotate(225); break;
                case WEST: canvas.rotate(270); break;
                case NORTH_WEST: canvas.rotate(315); break;
            }
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
            //canvas.drawRect(left, top, right, bottom, fillPaint);
            //canvas.drawRect(left, top, right, bottom, strokePaint);
            canvas.restore();
            canvas.drawText(Integer.toString(plane.getAltitude()), (left + right) / 2, (top + bottom * 3) / 4, textPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            float minimalDimension = Math.min(getWidth(), getHeight());
            float touchX = event.getX();
            float touchY = event.getY();
            touchX -= (getWidth() - minimalDimension) / 2;
            touchY -= (getHeight() - minimalDimension) / 2;
            Plane nearestPlane = null;
            float nearestDistanceSq = Float.POSITIVE_INFINITY;
            float stepSize = minimalDimension / GRID_WIDTH;
            for(Plane plane : planes) {
                Position position = plane.getPosition();
                float planeVPX = plane.getPosition().getX() * minimalDimension / GRID_WIDTH;
                float planeVPY = plane.getPosition().getY() * minimalDimension / GRID_HEIGHT;
                float deltaX = planeVPX - touchX;
                float deltaY = planeVPY - touchY;
                float distanceSq = deltaX * deltaX + deltaY * deltaY;
                if(distanceSq < nearestDistanceSq) {
                    nearestPlane = plane;
                    nearestDistanceSq = distanceSq;
                }
            }
            if(nearestDistanceSq > stepSize * stepSize * 6) {
                nearestPlane = null;
            }
            setSelectedPlane(nearestPlane);
            if(selectionChangeCallback != null) {
                selectionChangeCallback.onSelectionChanged(this, nearestPlane);
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
