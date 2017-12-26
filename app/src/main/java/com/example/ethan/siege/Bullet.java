package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

/**
 * Created by Ethan on 12/25/2017.
 */

public class Bullet {
    private Circle dims;
    private float xv, yv;
    private long prevT;
    private int power;
    public static final float speed = 5.0f;
    public Bullet(float x, float y, float r, float xv, float yv, int power) {
        dims = new Circle(x, y, r);
        this.xv = xv;
        this.yv = yv;
        prevT = 0;
        this.power = power;
    }
    public boolean update(Map map, Rect cam) {
        Tile[][] tiles = map.getTiles();
        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        //dt is too much: we probably just called this function for the first time
        if (dt > 1000) return true;
        dims.x += xv * dt;
        dims.y += yv * dt;
        float sc = tiles[0][0].sc;
        if(dims.x < cam.x || dims.y < cam.y || dims.x > cam.x + cam.w || dims.y > cam.y + cam.h) {
            return false;
        }

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
                Tile t = tiles[y][x];
                if (GameView.checkCol(t, dims)) {
                    if(power > 1) {
                        map.addBrokenWall(new Point(t.x, t.y));
                    }
                    return false;
                }
            }
        }
        return true;
    }
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        paint.setColor(power > 1 ? Color.RED : ContextCompat.getColor(context, R.color.light_gray));
        canvas.drawCircle(dims.x - cam.x, dims.y - cam.y, dims.r, paint);
        paint.setColor(ContextCompat.getColor(context, R.color.dark_blue));
        canvas.drawCircle(dims.x - cam.x, dims.y - cam.y, dims.r - 2, paint);
    }
    public Circle getDims() { return dims; }
}
