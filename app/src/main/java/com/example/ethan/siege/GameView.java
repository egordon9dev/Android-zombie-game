package com.example.ethan.siege;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
    private boolean running, paused;
    private long prevT, prevUpdateT, totalDeltaTime;
    private Rect cam;
    private float px, py, joyX0, joyY0;
    private int activePtrId = -1;
    private JPS jps;
    private float camResponse = 0.05f;
    //settings
    private static final String ctrlMode_tapAnywhere = "Tap Anywhere";
    private static final String ctrlMode_joystick = "Joystick";
    private static String ctrlMode = ctrlMode_tapAnywhere;
    public boolean getPaused() { return paused; }
    public void setPaused(boolean b) { paused = b; }
    // map scale / node space
    public static final int nsFac = 1;
    public GameView(Context ctx, float w, float h) {
        super(ctx);
        width = w;
        height = h;
        context = ctx;
        surfaceHolder = getHolder();
        paint = new Paint();
        joyX0 = width - 400;
        joyY0 = height - 500;
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
        while(zombies.size() < Zombie.nZombies) {
            float zx, zy;
            Tile[][] tiles = map.getTiles();
            Tile t0 = tiles[0][0];
            outer:
            while (true) {
                zx = (float) Math.random() * cam.w + cam.x;
                zy = (float) Math.random() * cam.h + cam.y;
                map.update(cam.x, cam.y);
                int top = (int) ((zy - Zombie.radius - tiles[0][0].y) / Map.sc);
                if(top < 0) top = 0;
                int bottom = (int) ((zy + Zombie.radius - tiles[0][0].y) / Map.sc);
                if(bottom > tiles.length - 1) bottom = tiles.length - 1;
                int left = (int) ((zx - Zombie.radius - tiles[0][0].x) / Map.sc);
                if(left < 0) left = 0;
                int right = (int) ((zx + Zombie.radius - tiles[0][0].x) / Map.sc);
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
            zombies.add(new Zombie(zx, zy, 0.2f, jps));
        }
        for(int b = bullets.size() - 1; b >= 0; b--) {
            if (!bullets.get(b).update(map, cam)) {
                bullets.remove(b);
                continue;
            }
        }
        for (int z = zombies.size() - 1; z >= 0; z--) {
            Zombie zombie = zombies.get(z);
            if(!zombie.update(map.getTiles(), zombies, player, cam)) {
                zombies.remove(z);
                continue;
            }
            Circle zd = zombie.getDims();
            float dx, dy;
            for(int b = bullets.size() - 1; b >= 0; b--) {
                Circle bd = bullets.get(b).getDims();
                dx = bd.x-zd.x;
                dy = bd.y-zd.y;
                if(Math.sqrt(dx*dx+dy*dy) < bd.r+zd.r) {
                    bullets.remove(b);
                    zombies.remove(z);
                    player.setPts(player.getPts() + player.getPtsPerKill());
                }
            }
        }
        long dt = System.currentTimeMillis() - prevUpdateT;
        if(dt > 1000) dt = 0;
        float a = dt * camResponse;
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
                        zombies.get(i).draw(canvas, paint, context, cam, map);
                    }
                    player.draw(canvas, paint, context, cam);
                    if(ctrlMode.equals(ctrlMode_joystick)) {
                        paint.setColor(0x40FFFFFF);
                        canvas.drawCircle(joyX0, joyY0, 240, paint);
                        paint.setColor(ContextCompat.getColor(context, R.color.dark_blue) & 0x70FFFFFF);
                        canvas.drawCircle(px, py, 120, paint);
                    }

                    paint.setColor(0xBBBB3333);//0xBB3333
                    paint.setTextSize(90);
                    paint.setTypeface(Typeface.create("Arial",Typeface.BOLD));
                    canvas.drawText(context.getString(R.string.score), 50, 90, paint);
                    paint.setTextSize(150);
                    canvas.drawText(Integer.toString(player.getPts()), 50, 250, paint);

/*
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(50);
                    canvas.drawText("fps: " + Integer.toString(totalDeltaTime == 0 ? 999999 : 1000 / (int)totalDeltaTime), 50, 70, paint);
                    */

                    paint.setColor(0x40FFFFFF);
                    int x0 = (int)cam.w/3, y0 = 30;
                    int x1 = (int)(cam.w - cam.w/5), y1 = y0 + 150;
                    canvas.drawRect(x0, y0, x1, y1, paint);
                    float health = player.getHealth(), maxHealth = player.getMaxHealth();
                    if(health > maxHealth/2) paint.setColor(0x7000FF00);
                    else if(health > maxHealth/4) paint.setColor(0x70FFFF00);
                    else paint.setColor(0x70FF0000);
                    if(health > 0) canvas.drawRect(x0, y0, x0 + (x1-x0)*(player.getHealth()/player.getMaxHealth()), y1, paint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } else {
                    Toast.makeText(context, "null canvas", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void dismissWindow() {
        if(Game.window == null) {
            System.out.println("null GameOverWindow");
            return;
        }
        Game.window.dismiss();
    }
    public void goHome() {
        dismissWindow();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
    public void resumeGame() {
        dismissWindow();
        paused = false;
    }
    public void restartGame() {
        dismissWindow();
        startGame();
    }
    private Spinner ctrlOps;
    public void goSettings() {
        dismissWindow();
        showPopup(R.layout.settings);
        ctrlOps = (Spinner)Game.inflatedView.findViewById(R.id.ctrlOpsSpinner);
        if(ctrlOps != null) {
            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ctrlOps, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ctrlOps.setAdapter(adapter);
            ctrlOps.setSelection(adapter.getPosition(ctrlMode));
            ctrlOps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    ctrlMode = (String)ctrlOps.getSelectedItem();
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }
    }
    public void showPopup(int layoutRes) {
        Game.inflatedView = LayoutInflater.from(context).inflate(layoutRes, null);
        Game.window = new PopupWindow(Game.inflatedView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, true);
        Drawable drawable = new ColorDrawable(0x00000000);
        drawable.setBounds(0,0, Game.window.getWidth(), Game.window.getHeight());
        Game.window.setBackgroundDrawable(drawable);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Game.window.showAtLocation(GameView.this, Gravity.NO_GRAVITY, 0, 0);
            }
        };
        this.post(runnable);
    }
    public void run() {
        while (running) {
            if (Thread.currentThread().isInterrupted()) {
                Toast.makeText(context, "GameView thread interrupted", Toast.LENGTH_LONG).show();
                break;
            }
            if(!paused && player.getHealth() <= 0.0f) {
                paused = true;
                showPopup(R.layout.game_over);
            }
            if(!paused) {
                update();
                prevUpdateT = System.currentTimeMillis();
                draw();
            }
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
            if(totalDeltaTime > 10000) totalDeltaTime = 10000;
            prevT = System.currentTimeMillis();
        }
    }

    private void startGame() {
        prevUpdateT = 0;
        running = true;
        paused = false;
        prevT = 0;
        px = joyX0;
        py = joyY0;
        player = new Player(100);
        player.setMoving(true);
        Circle pd = player.getDims();
        cam = new Rect(pd.x - width / 2, pd.y - height / 2, width, height);
        map = new Map(width, height, 555);
        map.update(cam.x, cam.y);
        Tile tiles[][] = map.getTiles();
        jps = new JPS(tiles[0].length*nsFac, tiles.length*nsFac, (int)(Map.sc/nsFac));
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
                    float dx, dy;
                    float dMax;
                    if(ctrlMode.equals(ctrlMode_joystick)) {
                        dx = x - joyX0;
                        dy = y - joyY0;
                        dMax = 500f;
                    } else {
                        Circle pd = player.getDims();
                        dx = x - (pd.x-cam.x);
                        dy = y - (pd.y-cam.y);
                        dMax = 99999f;
                    }

                    float mag = (float) Math.sqrt(dx * dx + dy * dy);
                    if (mag < dMax) {
                        px = x;
                        py = y;
                        if(ctrlMode.equals(ctrlMode_joystick) && mag > 240) {
                            px = joyX0 + dx * (240 / mag);
                            py = joyY0 + dy * (240 / mag);
                        }

                        if (Math.abs(mag) > 0.001f) {
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
                    if(ctrlMode.equals(ctrlMode_joystick)) {
                        px = joyX0;
                        py = joyY0;
                    }
                    player.setXV(0);
                    player.setYV(0);
                }
                break;
        }
        return true;
    }
}
