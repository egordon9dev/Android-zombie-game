package com.example.ethan.siege;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GameView extends SurfaceView implements Runnable{
    private int width, height;
    private Canvas canvas;
    private Context context;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Thread thread;
    private Player player;
    private boolean running;
    private long prevT;
    private float playerSpeed = 0.1f;
    public GameView(Context ctx, Point dims) {
        super(ctx);
        width = dims.x;
        height = dims.y;
        context = ctx;
        surfaceHolder = getHolder();
        paint = new Paint();
        running = true;
        prevT = 0;
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
        } catch(InterruptedException e) {
            Toast.makeText(context, "thread interrupted", Toast.LENGTH_LONG).show();
            Thread.currentThread().interrupt();
        }
    }
    public void update() {
        player.update();
    }
    public void draw() {
        if(surfaceHolder.getSurface().isValid()) {
            synchronized(surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();
                if(canvas != null) {
                    Circle playerDims =  player.getDims();
                    canvas.drawColor(ContextCompat.getColor(context, R.color.gray));
                    paint.setColor(ContextCompat.getColor(context, R.color.light_blue));
                    canvas.drawCircle(playerDims.x, playerDims.y, playerDims.r, paint);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(50);
                    canvas.drawText("( " + Float.toString(playerDims.x) + ", " + Float.toString(playerDims.y) + " )", 100, 200, paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Toast.makeText(context, "null canvas", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    public void run() {
        while(running) {
            if(Thread.currentThread().isInterrupted()) {
                Toast.makeText(context, "GameView thread interrupted", Toast.LENGTH_LONG).show();
                break;
            }
            long dt = System.currentTimeMillis() - prevT;
            if(dt < 17) {
                try {
                    Thread.sleep(17 - dt);
                } catch(InterruptedException e) {
                    Toast.makeText(context, "thread interrupted", Toast.LENGTH_LONG).show();
                    Thread.currentThread().interrupt();
                }
            } else {
                //Toast.makeText(context, "frame rate drop", Toast.LENGTH_SHORT).show();
            }
            prevT = System.currentTimeMillis();
            update();
            draw();
        }
    }
    private void startGame() {
        player = new Player();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        int dx = x - (int)player.getDims().x;
        int dy = y - (int)player.getDims().y;
        float mag = (float)Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        if(Math.abs(mag) > 5.0) {
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
