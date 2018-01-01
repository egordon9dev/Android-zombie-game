package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Ethan on 12/23/2017.
 */

public class Map {
    private final Tile tiles[][];
    public static final float sc = 150.0f;
    SimplexNoise noise;
    private ArrayList<Point> brokenWalls;
    private int tilesH, tilesW;
    public int getTilesH() { return tilesH; }
    public int getTilesW() { return tilesW; }
    public Map(float width, float height, int seed) {
        tilesH = (int)(height / sc) + 2;
        tilesW = (int)(width / sc) + 2;
        tiles = new Tile[tilesH][tilesW];
        noise = new SimplexNoise(3000, 0.4, seed);
        brokenWalls = new ArrayList<Point>();
    }
    public Tile[][] getTiles() {
        return tiles;
    }
    public void addBrokenWall(Point p) { brokenWalls.add(p); }
    public void update(float x0, float y0) {
        x0 = x0 >= 0 ? x0 - x0 % sc : x0 - (sc + x0 % sc);
        y0 = y0 >= 0 ? y0 - y0 % sc : y0 - (sc + y0 % sc);
        for(int y = 0; y < tiles.length; y++) {
            for(int x = 0; x < tiles[0].length; x++) {
                Tile t = new Tile();
                t.x = x0 + x * sc;
                t.y = y0 + y * sc;
                t.sc = this.sc;
                t.type = noise.getNoise((int)(t.x), (int)(t.y)) < 0.2 ? Tile_t.open : Tile_t.wall;
                for(int i = 0; i < brokenWalls.size(); i++) {
                    Point p = brokenWalls.get(i);
                    if(Math.abs(p.x - t.x) < 0.001 && Math.abs(p.y - t.y) < 0.001) {
                        t.type = Tile_t.open;
                        break;
                    }
                }
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
}
