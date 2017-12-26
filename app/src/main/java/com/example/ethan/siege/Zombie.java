package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class Zombie {
    private Circle dims;
    private float xv, yv;
    private long prevT;
    public static final float speed = 0.5f;
    public static final int nZombies = 10;
    public static final float radius = 40.0f;
    private static int idCount = 0;
    public final int id = idCount;
    private Zombie(){}
    public Zombie(float x, float y) {
        dims = new Circle(x, y, Zombie.radius);
        this.xv = 0.0f;
        this.yv = 0.0f;
        prevT = 0;
        idCount++;
    }
    public boolean update(Tile[][] tiles, ArrayList<Zombie> zombies, Player player, Point leader, Rect cam) {
        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        if(dims.x < cam.x || dims.y < cam.y || dims.x > cam.x + cam.w || dims.y > cam.y + cam.h) return false;
        if(dt > 1000) return true;
        Point vel;
        float dx, dy, mag;
        if(Math.abs(leader.x - dims.x) < 0.001 && Math.abs(leader.x - dims.x) < 0.001) {
            //INSERT A*
            vel = new Point(0,0);
        } else {
            //follow the leader
            dx = leader.x - dims.x;
            dy = leader.y - dims.y;
            float scale = (speed / (float)Math.sqrt(dx*dx + dy*dy)) * dt;
            vel = new Point(dx * scale, dy * scale);
        }
        float sc = tiles[0][0].sc;
        dims.x += vel.x;
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
                    dims.x -= vel.x;
                    break outer;
                }
            }
        }
        for(int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            Circle zd = z.getDims();
            if(z.id == this.id) continue;
            dx = zd.x - dims.x;
            dy = zd.y - dims.y;
            mag = (float)Math.sqrt(dx*dx + dy*dy);
            if(mag <= zd.r + dims.r) {
                dims.x -= vel.x;
                break;
            }
        }
        Circle pd = player.getDims();
        dx = pd.x - dims.x;
        dy = pd.y - dims.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag <= pd.r + dims.r) {
            dims.x -= vel.x;
        }
        dims.y += vel.y;
        top = (int) ((dims.y - dims.r - tiles[0][0].y) / sc);
        if(top < 0) top = 0;
        bottom = (int) ((dims.y + dims.r - tiles[0][0].y) / sc);
        if(bottom > tiles.length - 1) bottom = tiles.length - 1;
        left = (int) ((dims.x - dims.r - tiles[0][0].x) / sc);
        if(left < 0) left = 0;
        right = (int) ((dims.x + dims.r - tiles[0][0].x) / sc);
        if(right > tiles[0].length - 1) right = tiles[0].length - 1;
        outer:
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if (GameView.checkCol(tiles[y][x], dims)) {
                    dims.y -= vel.y;
                    break outer;
                }
            }
        }
        for(int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            Circle zd = z.getDims();
            if(z.id == this.id) continue;
            dx = zd.x - dims.x;
            dy = zd.y - dims.y;
            mag = (float)Math.sqrt(dx*dx + dy*dy);
            if(mag <= zd.r + dims.r) {
                dims.y -= vel.y;
                break;
            }
        }
        dx = pd.x - dims.x;
        dy = pd.y - dims.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag <= pd.r + dims.r) {
            dims.y -= vel.y;
        }
        /*  BUGGY: zombies can be pushed into walls
        Circle pd = player.getDims();
        dx = dims.x - pd.x;
        dy = dims.y - pd.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(dx == 0.0f && dy == 0.0f) dx = 1.0f;
        if(mag <= pd.r + dims.r) {
            float scale = (pd.r + dims.r) / mag;
            dims.x += -dx + dx * scale;
            dims.y += -dy + dy * scale;
        }
         */
        return true;
    }
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam) {
        float x = dims.x - cam.x;
        float y = dims.y - cam.y;
        paint.setColor((Color.GREEN));
        canvas.drawCircle(x, y, dims.r, paint);
        paint.setColor(ContextCompat.getColor(context, R.color.dark_green));
        canvas.drawCircle(x, y, dims.r - 6, paint);
    }
    public Circle getDims() { return dims; }
}
