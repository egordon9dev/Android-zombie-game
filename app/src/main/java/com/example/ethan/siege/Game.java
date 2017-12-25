package com.example.ethan.siege;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class Game extends Activity {
    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        Point dims = new Point();
        display.getSize(dims);
        gameView = new GameView(this, dims.x, dims.y);
        setContentView(gameView);
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
