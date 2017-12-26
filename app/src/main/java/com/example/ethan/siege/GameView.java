package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {
    private float width, height;
    private Canvas canvas;
    private Context context;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Thread thread;
    public Player player;
    private Map map;
    private ArrayList<Bullet> bullets;
    private ArrayList<Zombie> zombies;
    private boolean running;
    private long prevT, prevUpdateT, totalDeltaTime;
    private Rect cam;
    private float px, py, joyX0, joyY0;
    private int activePtrId = -1;
    public GameView(Context ctx, float w, float h) {
        super(ctx);
        width = w;
        height = h;
        context = ctx;
        surfaceHolder = getHolder();
        paint = new Paint();
        running = true;
        prevT = 0;
        prevUpdateT = 0;
        joyX0 = width - 300;
        joyY0 = height - 500;
        px = joyX0;
        py = joyY0;
        startGame();
    }

    public void resume() {
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Toast.makeText(context, "thread interrupted", Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
        }
    }

    public void update() {
        player.update(map.getTiles());
        for(int i = bullets.size()-1; i >= 0; i--) {
            if(!bullets.get(i).update(map, cam)) bullets.remove(i);
        }
        while(zombies.size() < Zombie.nZombies) {
            float zx, zy;
            Tile[][] tiles = map.getTiles();
            float sc = map.getSc();
            Tile t0 = tiles[0][0];
            outer:
            while (true) {
                float randX = (float) Math.random() * cam.w * 0.5f - cam.w * 0.25f;
                float randY = (float) Math.random() * cam.h * 0.5f - cam.h * 0.25f;
                zx = cam.x + (randX >= 0.0f ? randX : cam.w + randX);
                zy = cam.y + (randY >= 0.0f ? randY : cam.h + randY);
                map.update(cam.x, cam.y);
                int top = (int) ((zy - Zombie.radius - tiles[0][0].y) / sc);
                if(top < 0) top = 0;
                int bottom = (int) ((zy + Zombie.radius - tiles[0][0].y) / sc);
                if(bottom > tiles.length - 1) bottom = tiles.length - 1;
                int left = (int) ((zx - Zombie.radius - tiles[0][0].x) / sc);
                if(left < 0) left = 0;
                int right = (int) ((zx + Zombie.radius - tiles[0][0].x) / sc);
                if(right > tiles[0].length - 1) right = tiles[0].length - 1;
                Circle zd = new Circle(zx, zy, Zombie.radius);
                for (int y = top; y <= bottom; y++) {
                    for (int x = left; x <= right; x++) {
                        if (checkCol(tiles[y][x], zd)) {
                            continue outer;
                        }
                    }
                }
                break;
            }
            zombies.add(new Zombie(zx, zy));
        }
        Circle leaderDims = zombies.get(0).getDims();
        for (int i = zombies.size() - 1; i >= 0; i--) {
            if(!zombies.get(i).update(map.getTiles(), zombies, player, new Point(leaderDims.x, leaderDims.y), cam)) zombies.remove(i);
        }
        long dt = System.currentTimeMillis() - prevUpdateT;
        float a = dt * 0.02f;
        if (a > 1.0f) a = 1.0f;
        if (a < 0.0f) a = 0.0f;
        Circle pd = player.getDims();
        cam.x = cam.x * (1.0f - a) + (pd.x - width / 2) * a;
        cam.y = cam.y * (1.0f - a) + (pd.y - height / 2) * a;
        map.update(cam.x, cam.y);
    }
    public static boolean checkCol(Tile t, Circle c) {
        float left = t.x - c.x;
        float right = c.x - (t.x + t.sc);
        float top = t.y - c.y;
        float bottom = c.y - (t.y + t.sc);
        //edges
        if (t.type == Tile_t.open || left > c.r || right > c.r || top > c.r
                || bottom > c.r) return false;
        //corners
        if ((left >= 0.0f && top >= 0.0f && Math.sqrt(left * left + top * top) > c.r) || (top >= 0.0f && right >= 0.0f && Math.sqrt(top * top + right * right) > c.r) || (right >= 0.0f && bottom >= 0.0f && Math.sqrt(right * right + bottom * bottom) > c.r) || (bottom >= 0.0f && left >= 0.0f && Math.sqrt(bottom * bottom + left * left) > c.r)) return false;

        return true;
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawColor(ContextCompat.getColor(context, R.color.gray));

                    map.draw(canvas, paint, context, cam);

                    for(int i = 0; i < bullets.size(); i++) {
                        bullets.get(i).draw(canvas, paint, context, cam);
                    }
                    for(int i = 0; i < zombies.size(); i++) {
                        zombies.get(i).draw(canvas, paint, context, cam);
                    }

                    Circle playerDims = player.getDims();
                    player.draw(canvas, paint, context, cam);

                    paint.setColor(0x40FFFFFF);
                    canvas.drawCircle(joyX0, joyY0, 240, paint);
                    paint.setColor(ContextCompat.getColor(context, R.color.dark_blue) & 0x70FFFFFF);
                    canvas.drawCircle(px, py, 120, paint);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(50);
                    canvas.drawText("fps: " + Float.toString(1000.0f / totalDeltaTime), 50, 70, paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Toast.makeText(context, "null canvas", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void run() {
        while (running) {
            if (Thread.currentThread().isInterrupted()) {
                Toast.makeText(context, "GameView thread interrupted", Toast.LENGTH_LONG).show();
                break;
            }
            update();
            prevUpdateT = System.currentTimeMillis();
            draw();
            long dt = System.currentTimeMillis() - prevT;
            if (dt < 17) {
                try {
                    Thread.sleep(17 - dt);
                } catch (InterruptedException e) {
                    Toast.makeText(context, "thread interrupted", Toast.LENGTH_LONG).show();
                    Thread.currentThread().interrupt();
                }
            } else {
                //Toast.makeText(context, "frame rate drop", Toast.LENGTH_SHORT).show();
            }
            totalDeltaTime = System.currentTimeMillis() - prevT;
            prevT = System.currentTimeMillis();
        }
    }

    private void startGame() {
        player = new Player();
        Circle pd = player.getDims();
        cam = new Rect(pd.x - width / 2, pd.y - height / 2, width, height);
        map = new Map(width, height, 150.0f, 555);
        bullets = new ArrayList<Bullet>();
        zombies = new ArrayList<Zombie>();
        player.respawn(map, cam);
    }

    public void playerShoot() {
        Circle pd = player.getDims();
        Point dir = player.getDir();
        bullets.add(new Bullet(pd.x, pd.y, 10, dir.x * Bullet.speed, dir.y * Bullet.speed, player.getCharged() ? 2 : 1));
        player.setCharged(false);
        player.setChargeT0(Long.MAX_VALUE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int nPtrs = event.getPointerCount();

        final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int id = event.getPointerId(index);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < nPtrs; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);
                    float dx = x - joyX0;
                    float dy = y - joyY0;

                    float mag = (float) Math.sqrt(dx * dx + dy * dy);
                    if (mag < 500) {
                        px = x;
                        py = y;
                        if (mag > 240) {
                            px = joyX0 + dx * (240 / mag);
                            py = joyY0 + dy * (240 / mag);
                        }
                        if (Math.abs(mag) > 3.0) {
                            player.setXV((dx * Player.speed) / mag);
                            player.setYV((dy * Player.speed) / mag);
                            activePtrId = id;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if(activePtrId == id) {
                    px = joyX0;
                    py = joyY0;
                    player.setXV(0);
                    player.setYV(0);
                }
                break;
        }
        return true;
    }
}
