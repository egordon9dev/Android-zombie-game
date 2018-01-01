package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

/**
 * Created by Ethan on 12/22/2017.
 */

public class Player {
    private Circle dims;
    private float xv, yv;
    private long prevT;
    private Point dir;
    private boolean charged;
    private long chargeT0;
    public static final float speed = 0.9f;
    private boolean moving;
    private int pts, ptsPerKill;
    public static final float regenRate = 0.003f;
    private float health, maxHealth;

    public Player(float maxHealth) {
        dims = new Circle(150.0f, 150.0f, 90.0f);
        xv = 0.0f;
        yv = 0.0f;
        dir = new Point(1, 0);
        charged = true;
        chargeT0 = 0;
        moving = false;
        pts = 0;
        ptsPerKill = 1;
        this.maxHealth = maxHealth;
        health = maxHealth;
    }

    public void update(Tile[][] tiles) {
        if(System.currentTimeMillis() - chargeT0 > 100) {
            charged = true;
        }

        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        //dt is too much: we probably just called this function for the first time
        Point vel;
        if (dt > 1000) {
            vel = new Point(0, 0);
        } else {
            health += regenRate * dt;
            if(health > maxHealth) health = maxHealth;
            vel = new Point(xv * dt, yv * dt);
            double mag = Math.sqrt(vel.x * vel.x + vel.y * vel.y);
            if (Math.abs(mag) > 0.001) {
                dir.x = (float) (vel.x / mag);
                dir.y = (float) (vel.y / mag);
            }
        }
        if(!moving) return;
        float sc = tiles[0][0].sc;

        float substep_approx = 3.0f;
        int stepsX = Math.abs((int) (vel.x / substep_approx + 0.5));
        if (stepsX == 0) stepsX = 1;
        float substepX = vel.x / stepsX;
        for (int i = 0; i < stepsX; i++) {
            dims.x += substepX;
            int top = (int) ((dims.y - dims.r - tiles[0][0].y) / sc);
            if(top < 0) top = 0;
            int bottom = (int) ((dims.y + dims.r - tiles[0][0].y) / sc);
            if(bottom > tiles.length - 1) bottom = tiles.length - 1;
            int left = (int) ((dims.x - dims.r - tiles[0][0].x) / sc);
            if(left < 0) left = 0;
            int right = (int) ((dims.x + dims.r - tiles[0][0].x) / sc);
            if(right > tiles[0].length - 1) right = tiles[0].length - 1;
            outer:
            for (int y = top; y <= bottom; y++) {
                for (int x = left; x <= right; x++) {
                    if (GameView.checkCol(tiles[y][x], dims)) {
                        dims.x -= substepX;
                        break outer;
                    }
                }
            }
        }
        int stepsY = Math.abs((int) (vel.y / substep_approx + 0.5));
        if (stepsY == 0) stepsY = 1;
        float substepY = vel.y / stepsY;
        for (int i = 0; i < stepsY; i++) {
            dims.y += substepY;
            int top = (int) ((dims.y - dims.r - tiles[0][0].y) / sc);
            if(top < 0) top = 0;
            int bottom = (int) ((dims.y + dims.r - tiles[0][0].y) / sc);
            if(bottom > tiles.length - 1) bottom = tiles.length - 1;
            int left = (int) ((dims.x - dims.r - tiles[0][0].x) / sc);
            if(left < 0) left = 0;
            int right = (int) ((dims.x + dims.r - tiles[0][0].x) / sc);
            if(right > tiles[0].length - 1) right = tiles[0].length - 1;
            outer:
            for (int y = top; y <= bottom; y++) {
                for (int x = left; x <= right; x++) {
                    if (GameView.checkCol(tiles[y][x], dims)) {
                        dims.y -= substepY;
                        break outer;
                    }
                }
            }
        }
    }
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        float x = dims.x - cam.x;
        float y = dims.y - cam.y;
        paint.setColor((charged ? Color.RED : Color.BLUE) & 0xCCFFFFFF);
        canvas.drawLine(x, y, x + dir.x * 1500, y + dir.y * 1500, paint);
        paint.setColor(ContextCompat.getColor(context, R.color.light_blue));
        canvas.drawCircle(x, y, dims.r, paint);
        paint.setColor(charged ? Color.RED : ContextCompat.getColor(context, R.color.dark_blue));
        canvas.drawCircle(x, y, dims.r - 6, paint);
    }

    public void respawn(Map map, Rect cam) {
        Tile tiles[][] = map.getTiles();
        float sc = map.sc;
        outer:
        while (true) {
            dims.x = (float) Math.random() * cam.w;
            dims.y = (float) Math.random() * cam.h;
            cam.x = dims.x - cam.w / 2;
            cam.y = dims.y - cam.h / 2;
            map.update(cam.x, cam.y);

            int top = (int) ((dims.y - dims.r - tiles[0][0].y) / sc);
            if(top < 0) top = 0;
            int bottom = (int) ((dims.y + dims.r - tiles[0][0].y) / sc);
            if(bottom > tiles.length - 1) bottom = tiles.length - 1;
            int left = (int) ((dims.x - dims.r - tiles[0][0].x) / sc);
            if(left < 0) left = 0;
            int right = (int) ((dims.x + dims.r - tiles[0][0].x) / sc);
            if(right > tiles[0].length - 1) right = tiles[0].length - 1;
            for (int y = top; y <= bottom; y++) {
                for (int x = left; x <= right; x++) {
                    if (GameView.checkCol(tiles[y][x], dims)) {
                        continue outer;
                    }
                }
            }
            break;
        }
    }
    public float getMaxHealth() { return maxHealth; }
    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }
    public int getPtsPerKill() { return ptsPerKill; }
    public void setPts(int pts) { this.pts = pts; }
    public int getPts() { return pts; }
    public void setMoving(boolean b) { moving = b; }
    public boolean getCharged() { return charged; }
    public void setCharged(boolean b) { charged = b; }
    public long getChargeT0() { return chargeT0; }
    public void setChargeT0(long t0) { chargeT0 = t0; }
    public Circle getDims() {
        return dims;
    }
    public Point getDir() { return dir; }
    public void setXV(float xv) {
        this.xv = xv;
    }

    public void setYV(float yv) {
        this.yv = yv;
    }
}
