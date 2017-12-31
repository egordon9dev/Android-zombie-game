package com.example.ethan.siege;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class Game extends Activity {
    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point dims = new Point();
        display.getSize(dims);
        FrameLayout frameLayout = new FrameLayout(this);
        gameView = new GameView(this, dims.x, dims.y);
        Button butShoot = new Button(this);
        butShoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
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
        butShoot.setPadding(30, 30, 30, 30);
        butShoot.setTextSize(35);
        butShoot.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_blue) & 0xAAFFFFFF);
        butShoot.setTextColor(ContextCompat.getColor(this, R.color.light_blue) & 0xAAFFFFFF);
        butShoot.setId(1001);
        gameView.player.setMoving(true);
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
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setPadding(100, 100, 100, 100);
        RelativeLayout.LayoutParams lpButShoot = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpButShoot.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        lpButShoot.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);/*
        RelativeLayout.LayoutParams lpButMove = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpButMove.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lpButMove.addRule(RelativeLayout.ABOVE, 1001);
        lpButMove.setMargins(0, 0, 0, 10);*/
        RelativeLayout.LayoutParams lpLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(butShoot, lpButShoot);
        //layout.addView(butMove, lpButMove);
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
}
