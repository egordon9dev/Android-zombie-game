package com.example.ethan.siege;

/**
 * Created by Ethan on 12/22/2017.
 */

public class Player {
    private Circle dims;
    private float xv, yv;
    private long prevT;

    public Player() {
        dims = new Circle(150.0f, 150.0f, 20.0f);
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
