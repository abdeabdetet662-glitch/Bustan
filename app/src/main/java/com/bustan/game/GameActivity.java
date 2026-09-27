package com.bustan.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends Activity {

    private GameView gameView;
    private TextView scoreView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0D0D"));

        // شريط علوي
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(30, 40, 30, 20);

        TextView title = new TextView(this);
        title.setText("🌴 بُستان");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(title);

        scoreView = new TextView(this);
        scoreView.setText("0");
        scoreView.setTextColor(Color.WHITE);
        scoreView.setTextSize(28);
        scoreView.setTypeface(null, Typeface.BOLD);
        scoreView.setGravity(Gravity.CENTER);
        topBar.addView(scoreView);

        Button reset = new Button(this);
        reset.setText("🔄");
        reset.setTextSize(20);
        reset.setBackgroundColor(Color.TRANSPARENT);
        reset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("لعبة جديدة")
                .setMessage("هل تريد البدء من جديد؟")
                .setPositiveButton("نعم", (d, w) -> {
                    gameView.newGame();
                })
                .setNegativeButton("إلغاء", null)
                .show();
        });
        topBar.addView(reset);

        root.addView(topBar);

        // منطقة اللعب
        gameView = new GameView(this);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1));
        gameView.setListener(new GameView.GameListener() {
            @Override
            public void onScoreChanged(int score) {
                runOnUiThread(() -> scoreView.setText(String.valueOf(score)));
            }
            @Override
            public void onGameOver(int score) {
                runOnUiThread(() -> showGameOver(score));
            }
        });

        root.addView(gameView);
        setContentView(root);
    }

    private void showGameOver(int score) {
        new AlertDialog.Builder(this)
            .setTitle("🎮 انتهت اللعبة!")
            .setMessage("نتيجتك: " + score + " نقطة\n\nهل تريد اللعب مرة أخرى؟")
            .setCancelable(false)
            .setPositiveButton("لعبة جديدة", (d, w) -> gameView.newGame())
            .setNegativeButton("خروج", (d, w) -> finish())
            .show();
    }
}
