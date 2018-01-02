package com.example.ethan.siege;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Game extends Activity {
    private GameView gameView;
    private Point dims;
    public static PopupWindow window;
    public static View inflatedView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        dims = new Point();
        display.getSize(dims);
        FrameLayout frameLayout = new FrameLayout(this);
        gameView = new GameView(this, dims.x, dims.y);
        Button butShoot = new Button(this);
        butShoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        gameView.player.setChargeT0(System.currentTimeMillis());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        gameView.playerShoot();
                        break;
                }
                return true;
            }
        });
        butShoot.setText("Shoot");
        butShoot.setPadding(100, 100, 100, 100);
        butShoot.setTextSize(35);
        int transparentBlue = ContextCompat.getColor(this, R.color.dark_blue) & 0xAAFFFFFF;
        butShoot.setBackgroundColor(transparentBlue);
        butShoot.setTextColor(ContextCompat.getColor(this, R.color.light_blue) & 0xAAFFFFFF);
        butShoot.setId(1001);
        /*
        Button butMove = new Button(this);
        butMove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        gameView.player.setMoving(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        gameView.player.setMoving(false);
                        break;
                }
                return true;

            }
        });
        butMove.setText("Move");
        butMove.setPadding(30, 30, 30, 30);
        butMove.setTextSize(35);
        butMove.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue) & 0xAAFFFFFF);
        butMove.setTextColor(ContextCompat.getColor(this, R.color.light_blue) & 0xAAFFFFFF);*/
        ImageButton butPause = new ImageButton(this);
        butPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goPaused(view);
            }
        });
        butPause.setPadding(0, 0, 0, 0);
        butPause.setBackgroundColor(transparentBlue);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.pause);
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmapResized = bitmap.createScaledBitmap(bitmap, 160, 160, false);
        butPause.setImageDrawable(new BitmapDrawable(this.getResources(), bitmapResized));

        RelativeLayout layout = new RelativeLayout(this);
        layout.setPadding(30, 30, 30, 30);
        RelativeLayout.LayoutParams lpButShoot = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpButShoot.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lpButShoot.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams lpButPause = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpButPause.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lpButPause.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams lpLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(butShoot, lpButShoot);
        layout.addView(butPause, lpButPause);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        frameLayout.addView(gameView);
        frameLayout.addView(layout, lpLayout);
        setContentView(frameLayout);
    }
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
    public void restartGame(View view) {
        gameView.restartGame(view);
    }
    public void resumeGame(View view) { gameView.resumeGame(view); }
    public void goHome(View view) {
        gameView.goHome(view);
    }
    public void goSettings(View view) { gameView.goSettings(view); }
    public void goPaused(View view) {
        gameView.goPaused(view);
    }
}
