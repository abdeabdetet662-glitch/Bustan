package com.bustan.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends Activity {

    private GameView gameView;
    private TextView scoreView;
    private TextView bestView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("bustan", MODE_PRIVATE);

        FirebaseManager.get().signIn(new FirebaseManager.OnDone() {
            @Override public void onSuccess() {}
            @Override public void onError(String msg) {}
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0D0D"));

        // ============ شريط علوي ============
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(20, 40, 20, 10);

        // 🏆 زر المتصدرين
        Button leadBtn = new Button(this);
        leadBtn.setText("🏆");
        leadBtn.setTextSize(24);
        leadBtn.setBackgroundColor(Color.TRANSPARENT);
        leadBtn.setOnClickListener(v ->
            startActivity(new Intent(this, LeaderboardActivity.class)));
        topBar.addView(leadBtn);

        // النقاط
        LinearLayout scoreCol = new LinearLayout(this);
        scoreCol.setOrientation(LinearLayout.VERTICAL);
        scoreCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView sLbl = new TextView(this);
        sLbl.setText("النقاط");
        sLbl.setTextColor(Color.parseColor("#9E9E9E"));
        sLbl.setTextSize(11);
        sLbl.setGravity(Gravity.CENTER);
        scoreCol.addView(sLbl);

        scoreView = new TextView(this);
        scoreView.setText("0");
        scoreView.setTextColor(Color.WHITE);
        scoreView.setTextSize(30);
        scoreView.setTypeface(null, Typeface.BOLD);
        scoreView.setGravity(Gravity.CENTER);
        scoreCol.addView(scoreView);

        topBar.addView(scoreCol);

        // 👤 زر الاسم
        Button nameBtn = new Button(this);
        nameBtn.setText("👤");
        nameBtn.setTextSize(24);
        nameBtn.setBackgroundColor(Color.TRANSPARENT);
        nameBtn.setOnClickListener(v -> editName());
        topBar.addView(nameBtn);

        // الأفضل
        LinearLayout bestCol = new LinearLayout(this);
        bestCol.setOrientation(LinearLayout.VERTICAL);
        bestCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView bLbl = new TextView(this);
        bLbl.setText("الأفضل");
        bLbl.setTextColor(Color.parseColor("#9E9E9E"));
        bLbl.setTextSize(11);
        bLbl.setGravity(Gravity.CENTER);
        bestCol.addView(bLbl);

        bestView = new TextView(this);
        bestView.setText(String.valueOf(prefs.getInt("best", 0)));
        bestView.setTextColor(Color.parseColor("#FFD54F"));
        bestView.setTextSize(30);
        bestView.setTypeface(null, Typeface.BOLD);
        bestView.setGravity(Gravity.CENTER);
        bestCol.addView(bestView);

        topBar.addView(bestCol);

        // 🔄 زر إعادة
        Button reset = new Button(this);
        reset.setText("🔄");
        reset.setTextSize(24);
        reset.setBackgroundColor(Color.TRANSPARENT);
        reset.setOnClickListener(v -> gameView.newGame());
        topBar.addView(reset);

        root.addView(topBar);

        // ============ اللعبة ============
        gameView = new GameView(this);
        gameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        gameView.setListener(new GameView.GameListener() {
            @Override
            public void onScoreChanged(int score) {
                runOnUiThread(() -> {
                    scoreView.setText(String.valueOf(score));
                    ScaleAnimation pulse = new ScaleAnimation(
                            1.3f, 1f, 1.3f, 1f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    pulse.setDuration(200);
                    scoreView.startAnimation(pulse);
                });
            }
            @Override
            public void onGameOver(int score, int best) {
                runOnUiThread(() -> {
                    bestView.setText(String.valueOf(best));
                    showGameOver(score, best);
                });
            }
        });

        root.addView(gameView);
        setContentView(root);
    }

    private void editName() {
        final EditText input = new EditText(this);
        input.setHint("اسمك (سيظهر في المتصدرين)");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(prefs.getString("playerName", ""));
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
            .setTitle("اسم اللاعب")
            .setView(input)
            .setPositiveButton("حفظ", (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) name = "لاعب";
                prefs.edit().putString("playerName", name).apply();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void showGameOver(int score, int best) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(60, 60, 60, 60);

        boolean isRecord = score >= best && score > 0;

        TextView icon = new TextView(this);
        icon.setText(isRecord ? "🏆" : "🎮");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        content.addView(icon);

        TextView title = new TextView(this);
        title.setText(isRecord ? "رقم قياسي جديد!" : "انتهت اللعبة");
        title.setTextColor(Color.parseColor("#FFD54F"));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 20);
        content.addView(title);

        TextView scoreTxt = new TextView(this);
        scoreTxt.setText("نتيجتك: " + score);
        scoreTxt.setTextColor(Color.WHITE);
        scoreTxt.setTextSize(22);
        scoreTxt.setGravity(Gravity.CENTER);
        scoreTxt.setPadding(0, 10, 0, 10);
        content.addView(scoreTxt);

        TextView bestTxt = new TextView(this);
        bestTxt.setText("الأفضل: " + best);
        bestTxt.setTextColor(Color.parseColor("#FFD54F"));
        bestTxt.setTextSize(18);
        bestTxt.setGravity(Gravity.CENTER);
        bestTxt.setPadding(0, 0, 0, 30);
        content.addView(bestTxt);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(content)
                .setCancelable(false)
                .create();

        Button replay = new Button(this);
        replay.setText("🎮  العب مرة أخرى");
        replay.setTextSize(18);
        replay.setTextColor(Color.WHITE);
        replay.setTypeface(null, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#0B4F2C"));
        bg.setCornerRadius(30);
        replay.setBackground(bg);
        replay.setPadding(40, 30, 40, 30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        replay.setLayoutParams(lp);
        replay.setOnClickListener(v -> {
            dialog.dismiss();
            gameView.newGame();
        });
        content.addView(replay);

        Button leaderboard = new Button(this);
        leaderboard.setText("🏆  المتصدرون");
        leaderboard.setTextSize(16);
        leaderboard.setTextColor(Color.WHITE);
        GradientDrawable bg3 = new GradientDrawable();
        bg3.setColor(Color.parseColor("#B8860B"));
        bg3.setCornerRadius(30);
        leaderboard.setBackground(bg3);
        leaderboard.setPadding(40, 25, 40, 25);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp3.setMargins(0, 10, 0, 10);
        leaderboard.setLayoutParams(lp3);
        leaderboard.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, LeaderboardActivity.class));
        });
        content.addView(leaderboard);

        Button exit = new Button(this);
        exit.setText("🚪  خروج");
        exit.setTextSize(16);
        exit.setTextColor(Color.WHITE);
        GradientDrawable bg2 = new GradientDrawable();
        bg2.setColor(Color.parseColor("#424242"));
        bg2.setCornerRadius(30);
        exit.setBackground(bg2);
        exit.setPadding(40, 25, 40, 25);
        exit.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        exit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        content.addView(exit);

        dialog.show();
    }
}
