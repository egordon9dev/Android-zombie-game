package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Zombie {
    private Circle dims;
    private float xv, yv;
    private long prevT;
    public final float speed;
    JPS jps;
    private int startX, startY, endX, endY;
    ArrayList<Node> path;
    private SimplexNoise noise;
    private float dmg;
    public float getDmg() { return dmg; }
    private static long idCount = 0;
    public final long id = idCount;

    private static final boolean debugPath = false;
    public static final float avgSpeed = 0.3f;
    public static final float radius = 40.0f;
    private static ArrayDeque<Long> killTimes = new ArrayDeque<Long>();
    private static long killWait = 3000;
    private static int nZombies = 10;
    public static int getnZombies() { return nZombies; }
    public static void setnZombie(int n) { nZombies = n; }
    public static ArrayDeque<Long> getKillTimes() { return killTimes; }
    public static long getKillWait() { return killWait; }
    public static void setKillWait(long k) { killWait = k; }
    public static void resetStatics() {
        killWait = 3000;
        killTimes = new ArrayDeque<Long>();
        nZombies = 10;
    }
    public Zombie(float x, float y, float dmg, JPS jps) {
        dims = new Circle(x, y, Zombie.radius);
        speed = avgSpeed * (float)(0.5 + Math.random());
        this.xv = 0.0f;
        this.yv = 0.0f;
        this.dmg = dmg;
        this.jps = jps;
        prevT = 0;
        idCount++;
        path = null;
        noise = new SimplexNoise(3000, 0.4, (int)System.currentTimeMillis());
    }
    private int clamp(int n, int min, int max) {
        if(n < min) return min;
        if(n > max) return max;
        return n;
    }
    public boolean update(Tile[][] tiles, ArrayList<Zombie> zombies, Player player, Rect cam) {
        long dt = System.currentTimeMillis() - prevT;
        prevT = System.currentTimeMillis();
        if(dims.x < cam.x || dims.y < cam.y || dims.x > cam.x + cam.w || dims.y > cam.y + cam.h) return false;
        if(dt > 1000) return true;
        Node nodes[][] = jps.getNodes();
        for(int y = 0; y < nodes.length; y++) {
            for(int x = 0; x < nodes[0].length; x++) {
                if(tiles[y/GameView.nsFac][x/GameView.nsFac].type == Tile_t.wall) nodes[y][x].walkable = false;
                else nodes[y][x].walkable = true;
            }
        }
        int nodeSpace = jps.getNodeSpace();
        Circle pd = player.getDims();
        Tile t00 = tiles[0][0];
        startX = clamp((int)((dims.x-t00.x+0.5f)/nodeSpace), 0, nodes[0].length);
        startY = clamp((int)((dims.y-t00.y+0.5f)/nodeSpace), 0, nodes.length);
        endX = clamp((int)((pd.x-t00.x+0.5f)/nodeSpace + 0.5f), 0, nodes[0].length);
        endY = clamp((int)((pd.y-t00.y+0.5f)/nodeSpace + 0.5f), 0, nodes.length);
        int j = 0;
        outer:
        while(!nodes[startY][startX].walkable) {
            switch(j) {
                case 0:
                    startX--;
                    break;
                case 1:
                    startY--;
                    break;
                case 2:
                    startX++;
                    break;
                case 3:
                    startX++;
                    break;
                case 4:
                    startY++;
                    break;
                case 5:
                    startY++;
                    break;
                case 6:
                    startX--;
                    break;
                case 7:
                    startX--;
                    break;
                default:
                    break outer;
            }
            startY = clamp(startY, 0, nodes.length);
            startX = clamp(startX, 0, nodes[0].length);
            j++;
        }
        j = 0;
        outer:
        while(!nodes[endY][endX].walkable) {
            switch(j) {
                case 0:
                    endX--;
                    break;
                case 1:
                    endY--;
                    break;
                case 2:
                    endX++;
                    break;
                case 3:
                    endX++;
                    break;
                case 4:
                    endY++;
                    break;
                case 5:
                    endY++;
                    break;
                case 6:
                    endX--;
                    break;
                case 7:
                    endX--;
                    break;
                default:
                    break outer;
            }
            endY = clamp(endY, 0, nodes.length);
            endX = clamp(endX, 0, nodes[0].length);
            j++;
        }
        path = jps.findPath(startX, startY, endX, endY);
        Point vel;
        float dx, dy, mag;
        float noiseX = (float)noise.getNoise(0, (int)System.currentTimeMillis()*10);
        float noiseY = (float)noise.getNoise(99999, (int)System.currentTimeMillis()*10);
        dx = (pd.x - dims.x) * (noiseX/2.0f + 1.0f) + noiseX*400;
        dy = (pd.y - dims.y) * (noiseY/2.0f + 1.0f) + noiseY*400;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag <= pd.r + dims.r + Map.sc/2) {
            float scale = (speed/mag) * dt;
            vel = new Point(dx * scale, dy * scale);
        } else if(path != null && path.size() > 1) {
            Node aim1 = path.get(path.size()-1); // source
            Node aim2 = path.get(path.size()-2); // destination
            int ns = jps.getNodeSpace();
            dx = (float)(aim2.x + ns/2 + t00.x - dims.x) * (noiseX/2.0f + 1.0f) + noiseX*400;
            dy = (float)(aim2.y + ns/2 + t00.y - dims.y) * (noiseY/2.0f + 1.0f) + noiseY*400;
            float scale = (speed / (float)Math.sqrt(dx*dx + dy*dy)) * dt;
            vel = new Point(dx*scale, dy*scale);
        } else {
            vel = new Point(0.0f, 0.0f);
        }

        float sc = Map.sc;
        dims.x += vel.x;
        int top = (int) ((dims.y - dims.r - t00.y) / sc);
        if(top < 0) top = 0;
        int bottom = (int) ((dims.y + dims.r - t00.y) / sc);
        if(bottom > tiles.length - 1) bottom = tiles.length - 1;
        int left = (int) ((dims.x - dims.r - t00.x) / sc);
        if(left < 0) left = 0;
        int right = (int) ((dims.x + dims.r - t00.x) / sc);
        if(right > tiles[0].length - 1) right = tiles[0].length - 1;
        outer:
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if (GameView.checkCol(tiles[y][x], dims)) {
                    dims.x -= vel.x;
                    break outer;
                }
            }
        }/*
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
        }*/
        dx = pd.x - dims.x;
        dy = pd.y - dims.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag <= pd.r + dims.r) {
            dims.x -= vel.x;
        }
        dims.y += vel.y;
        top = (int) ((dims.y - dims.r - t00.y) / sc);
        if(top < 0) top = 0;
        bottom = (int) ((dims.y + dims.r - t00.y) / sc);
        if(bottom > tiles.length - 1) bottom = tiles.length - 1;
        left = (int) ((dims.x - dims.r - t00.x) / sc);
        if(left < 0) left = 0;
        right = (int) ((dims.x + dims.r - t00.x) / sc);
        if(right > tiles[0].length - 1) right = tiles[0].length - 1;
        outer:
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if (GameView.checkCol(tiles[y][x], dims)) {
                    dims.y -= vel.y;
                    break outer;
                }
            }
        }/*
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
        }*/
        dx = pd.x - dims.x;
        dy = pd.y - dims.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag <= pd.r + dims.r) {
            dims.y -= vel.y;
        }
        dx = pd.x - dims.x;
        dy = pd.y - dims.y;
        mag = (float)Math.sqrt(dx*dx + dy*dy);
        if(mag < pd.r+dims.r + Map.sc/10) {
            player.setHealth(player.getHealth() - (int)(dmg*dt));
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
    public void draw(Canvas canvas, Paint paint, Context context, Rect cam, Map map) {
        float x = dims.x - cam.x;
        float y = dims.y - cam.y;
        paint.setColor((Color.GREEN) & 0x38FFFFFF);
        canvas.drawCircle(x, y, dims.r, paint);
        paint.setColor(ContextCompat.getColor(context, R.color.dark_green) & 0x38FFFFFF);
        canvas.drawCircle(x, y, dims.r - 6, paint);
        paint.setColor(0xBB0000FF);
        if(path != null && debugPath) {
            Node[][] nodes = jps.getNodes();
            Tile t00 = map.getTiles()[0][0];
            int ns = jps.getNodeSpace();
            for(int i = 0; i < path.size(); i++) {
                Node n = path.get(i);
                int x0 = (int)(n.x + t00.x - cam.x), y0 = (int)(n.y + t00.y - cam.y);
                canvas.drawRect(x0, y0, x0+ns, y0+ns, paint);
            }
            for(int i = 0; i < nodes.length; i++) {
                for(int j = 0; j < nodes[0].length; j++) {
                    Node n = nodes[i][j];
                    int x0 = (int)(n.x + t00.x - cam.x), y0 = (int)(n.y + t00.y - cam.y);
                    paint.setColor(n.walkable ? 0x8800FFFF : 0x88FF00FF);
                    canvas.drawRect(x0, y0, x0+ns, y0+ns, paint);
                }
            }
        }
    }
    public Circle getDims() { return dims; }
}
