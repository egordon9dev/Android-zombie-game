package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

/**
 * Created by Ethan on 12/22/2017.
 */

public class Player {
    private Circle dims;
    private float xv, yv;
    private long prevT;

    public Player() {
        dims = new Circle(150.0f, 150.0f, 90.0f);
        xv = 0.0f;
        yv = 0.0f;
    }

    public void update() {
        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        //dt is too much: we probably just called this function for the first time
        if (dt > 1000) return;
        dims.x += xv * dt;
        dims.y += yv * dt;
    }
    public Point getVel() {
        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        //dt is too much: we probably just called this function for the first time
        if (dt > 1000) return new Point(0, 0);
        return new Point(xv * dt, yv * dt);
    }

    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        paint.setColor(ContextCompat.getColor(context, R.color.light_blue));
        canvas.drawCircle(dims.x - cam.x, dims.y - cam.y, dims.r, paint);
        paint.setColor(ContextCompat.getColor(context, R.color.dark_blue));
        canvas.drawCircle(dims.x - cam.x, dims.y - cam.y, dims.r - 6, paint);

    }
    public Circle getDims() {
        return dims;
    }

    public void setXV(float xv) {
        this.xv = xv;
    }

    public void setYV(float yv) {
        this.yv = yv;
    }
}
