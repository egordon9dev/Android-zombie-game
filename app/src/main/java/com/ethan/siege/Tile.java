package com.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

public class Tile {
    public float x, y, sc;
    public Tile_t type;
    public Tile(float x, float y, float sc, Tile_t type) {
        this.x = x;
        this.y = y;
        this.sc = sc;
        this.type = type;
    }
    public Tile() {}
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        switch(type) {
            case open:
                paint.setColor(ContextCompat.getColor(context, R.color.gray));
                break;
            case wall:
                paint.setColor(ContextCompat.getColor(context, R.color.dark_gray));
                break;
            case beacon:
                paint.setColor(Color.YELLOW);
                break;
            default:
                paint.setColor(Color.RED);
                break;
        }
        float x0 = x - cam.x;
        float y0 =  y - cam.y;
        canvas.drawRect(x0, y0, x0+sc, y0+sc, paint);
    }
}
