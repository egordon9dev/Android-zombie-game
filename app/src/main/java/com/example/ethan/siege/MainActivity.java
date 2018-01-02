package com.example.ethan.siege;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    public void startGame(View v) {
        Intent myIntent = new Intent(this, Game.class);
        this.startActivity(myIntent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = this.getSharedPreferences("com.example.ethan.siege", Context.MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        TextView tvHighScore = (TextView)this.findViewById(R.id.highScore);
        tvHighScore.setText(Integer.toString(highScore));
        int highestRound = prefs.getInt("highestRound", 0);
        TextView tvHighRd = (TextView)this.findViewById(R.id.highestRound);
        tvHighRd.setText(Integer.toString(highestRound));
    }
}
