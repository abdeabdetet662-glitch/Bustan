package com.bustan.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0D0D0D"));
        root.setPadding(60, 100, 60, 100);

        TextView logo = new TextView(this);
        logo.setText("🧩");
        logo.setTextSize(120);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        TextView title = new TextView(this);
        title.setText("بُستان");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(60);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 30, 0, 10);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("لعبة المكعبات العربية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(18);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 80);
        root.addView(sub);

        Button play = makeButton("🎮  ابدأ اللعب", "#0B4F2C");
        play.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class)));
        root.addView(play);

        setContentView(root);
    }

    private Button makeButton(String text, String color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        b.setTextColor(Color.WHITE);
        b.setTypeface(null, Typeface.BOLD);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(color));
        bg.setCornerRadius(30);
        b.setBackground(bg);
        b.setPadding(40, 40, 40, 40);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 15, 0, 15);
        b.setLayoutParams(lp);

        return b;
    }
}
