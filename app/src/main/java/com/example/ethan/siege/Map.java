package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Ethan on 12/23/2017.
 */

public class Map {
    private Tile tiles[][];
    private float sc;
    SimplexNoise noise;
    public Map(float width, float height, float sc, int seed) {
        this.sc = sc;
        tiles = new Tile[(int)(height / sc) + 2][(int)(width / sc) + 2];
        noise = new SimplexNoise(3000, 0.4, seed);
    }
    public Tile[][] getTiles() {
        return tiles;
    }
    public float getSc() { return sc; }
    public void update(float x0, float y0) {
        x0 = x0 >= 0 ? x0 - x0 % sc : x0 - (sc + x0 % sc);
        y0 = y0 >= 0 ? y0 - y0 % sc : y0 - (sc + y0 % sc);
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                Tile t = new Tile();
                t.x = x0 + x * sc;
                t.y = y0 + y * sc;
                t.sc = this.sc;
                t.type = noise.getNoise((int)(t.x), (int)(t.y)) > 0.0 ? Tile_t.open : Tile_t.wall;
                tiles[y][x] = t;
            }
        }
    }
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        for(int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                if(tiles[y][x] != null) {
                    tiles[y][x].draw(canvas, paint, context, cam);
                }
            }
        }
    }
    private double noise(float x, float y) {
        return (Math.sin((double)x) + Math.sin((double)y));
    }
}