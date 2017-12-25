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

public class GameView extends SurfaceView implements Runnable {
    private float width, height;
    private Canvas canvas;
    private Context context;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Thread thread;
    private Player player;
    private Map map;
    private boolean running;
    private long prevT, prevUpdateT, totalDeltaTime;
    private float playerSpeed = 2.0f;
    private Rect cam;
    float px, py;

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
        px = 0;
        py = 0;
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
        //player.update();
        updatePlayer();
        long dt = System.currentTimeMillis() - prevUpdateT;
        float a = dt * 0.02f;
        if (a > 1.0f) a = 1.0f;
        if (a < 0.0f) a = 0.0f;
        Circle pd = player.getDims();
        cam.x = cam.x * (1.0f - a) + (pd.x - width / 2) * a;
        cam.y = cam.y * (1.0f - a) + (pd.y - height / 2) * a;
        map.update(cam.x, cam.y);
    }

    public void updatePlayer() {
        Point vel = player.getVel();
        Circle pd = player.getDims();
        Tile tiles[][] = map.getTiles();
        float sc = map.getSc();

        float substep_approx = 3.0f;
        int stepsX = Math.abs((int) (vel.x / substep_approx + 0.5));
        if (stepsX == 0) stepsX = 1;
        float substepX = vel.x / stepsX;
        for (int i = 0; i < stepsX; i++) {
            pd.x += substepX;
            outer1:
            for (int y = (int) ((pd.y - pd.r - tiles[0][0].y) / sc); y <= (int) ((pd.y + pd.r - tiles[0][0].y) / sc); y++) {
                for (int x = (int) ((pd.x - pd.r - tiles[0][0].x) / sc); x <= (int) ((pd.x + pd.r - tiles[0][0].x) / sc); x++) {
                    if (checkCol(tiles[y][x], pd)) {
                        pd.x -= substepX;
                        break outer1;
                    }
                }
            }
        }
        int stepsY = Math.abs((int) (vel.y / substep_approx + 0.5));
        if (stepsY == 0) stepsY = 1;
        float substepY = vel.y / stepsY;
        for (int i = 0; i < stepsY; i++) {
            pd.y += substepY;
            outer2:
            for (int y = (int) ((pd.y - pd.r - tiles[0][0].y) / sc); y <= (int) ((pd.y + pd.r - tiles[0][0].y) / sc); y++) {
                for (int x = (int) ((pd.x - pd.r - tiles[0][0].x) / sc); x <= (int) ((pd.x + pd.r - tiles[0][0].x) / sc); x++) {
                    if (checkCol(tiles[y][x], pd)) {
                        pd.y -= substepY;
                        break outer2;
                    }
                }
            }
        }
    }

    private boolean checkCol(Tile t, Circle c) {
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

                    Circle playerDims = player.getDims();
                    paint.setColor(Color.argb(150, 255, 0, 0));
                    canvas.drawLine(playerDims.x - cam.x, playerDims.y - cam.y, px, py, paint);
                    player.draw(canvas, paint, context, cam);

                    paint.setColor(Color.WHITE);
                    canvas.drawLine(0, 0, width, 0, paint);
                    canvas.drawLine(width, 0, width, height, paint);
                    canvas.drawLine(width, height, 0, height, paint);
                    canvas.drawLine(0, height, 0, 0, paint);

                    paint.setTextSize(50);
                    canvas.drawText("player: ( " + Float.toString(playerDims.x) + ", " + Float.toString(playerDims.y) + " )", 50, 70, paint);
                    canvas.drawText("fps: " + Double.toString(1000.0 / totalDeltaTime), 50, 140, paint);
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

    private void spawnPlayer() {
        Circle pd = player.getDims();
        Tile tiles[][] = map.getTiles();
        float sc = map.getSc();
        outer:
        while (true) {
            pd.x = (float) Math.random() * width;
            pd.y = (float) Math.random() * height;
            cam.x = pd.x - width / 2;
            cam.y = pd.y - height / 2;
            map.update(cam.x, cam.y);
            for (int y = (int) ((pd.y - pd.r - tiles[0][0].y) / sc); y <= (int) ((pd.y + pd.r - tiles[0][0].y) / sc); y++) {
                for (int x = (int) ((pd.x - pd.r - tiles[0][0].x) / sc); x <= (int) ((pd.x + pd.r - tiles[0][0].x) / sc); x++) {
                    if (checkCol(tiles[y][x], pd)) {
                        continue outer;
                    }
                }
            }
            break;
        }
    }

    private void startGame() {
        player = new Player();
        Circle pd = player.getDims();
        cam = new Rect(pd.x - width / 2, pd.y - height / 2, width, height);
        map = new Map(width, height, 150.0f, 555);
        //map.update(cam.x, cam.y);
        spawnPlayer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        px = x;
        py = y;
        float dx = px + cam.x - player.getDims().x;
        float dy = py + cam.y - player.getDims().y;
        float mag = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        if (Math.abs(mag) > 5.0) {
            player.setXV((dx * playerSpeed) / mag);
            player.setYV((dy * playerSpeed) / mag);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                player.setXV(0);
                player.setYV(0);
                break;
        }
        return true;
    }
}
