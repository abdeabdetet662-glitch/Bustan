package com.bustan.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class GameActivity extends Activity {

    private static final int GRID_SIZE = 8;
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];
    private GridLayout gridView;
    private TextView scoreView;
    private int score = 0;
    private Random random = new Random();

    // الألوان العربية
    private static final String[] COLORS = {
            "#E6C88C", // رمل
            "#D4AF37", // ذهبي
            "#0B4F2C", // أخضر النخيل
            "#FF6D00", // برتقالي الغروب
            "#8B4513", // بني
            "#1565C0", // أزرق
            "#7B1FA2", // بنفسجي
            "#C2185B"  // وردي
    };

    private static final String[] EMOJIS = {
            "🌴", "🏺", "⭐", "🌙", "🐪", "🏵️", "🌵", "🔮"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.parseColor("#0A0A0A"));
        root.setPadding(20, 40, 20, 40);

        // شريط النقاط
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(10, 10, 10, 20);

        scoreView = new TextView(this);
        scoreView.setText("النقاط: 0");
        scoreView.setTextColor(Color.parseColor("#D4AF37"));
        scoreView.setTextSize(22);
        scoreView.setTypeface(null, Typeface.BOLD);
        scoreView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(scoreView);

        Button resetBtn = new Button(this);
        resetBtn.setText("🔄");
        resetBtn.setTextSize(20);
        resetBtn.setOnClickListener(v -> resetGame());
        topBar.addView(resetBtn);

        root.addView(topBar);

        // شبكة 8×8
        gridView = new GridLayout(this);
        gridView.setColumnCount(GRID_SIZE);
        gridView.setRowCount(GRID_SIZE);
        gridView.setPadding(10, 10, 10, 10);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A1A1A"));
        bg.setCornerRadius(20);
        gridView.setBackground(bg);

        int cellSize = getResources().getDisplayMetrics().widthPixels / GRID_SIZE - 8;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                final int row = r, col = c;
                TextView cell = new TextView(this);
                cell.setTextSize(24);
                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(Color.WHITE);

                GradientDrawable cellBg = new GradientDrawable();
                cellBg.setColor(Color.parseColor("#2A2A2A"));
                cellBg.setCornerRadius(10);
                cell.setBackground(cellBg);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = cellSize;
                lp.height = cellSize;
                lp.setMargins(4, 4, 4, 4);
                cell.setLayoutParams(lp);

                // عند الضغط → نضيف قطعة
                cell.setOnClickListener(v -> {
                    if (grid[row][col] == 0) {
                        placeRandomBlock(row, col);
                    }
                });

                gridView.addView(cell);
            }
        }

        root.addView(gridView);

        // عداد + مستوى
        TextView infoView = new TextView(this);
        infoView.setText("اضغط على المربعات لوضع القطع العربية");
        infoView.setTextColor(Color.parseColor("#9E9E9E"));
        infoView.setTextSize(14);
        infoView.setGravity(Gravity.CENTER);
        infoView.setPadding(0, 30, 0, 0);
        root.addView(infoView);

        setContentView(root);
    }

    private void placeRandomBlock(int row, int col) {
        int colorIndex = random.nextInt(COLORS.length);
        grid[row][col] = colorIndex + 1;
        updateGrid();
        checkLines();
    }

    private void updateGrid() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                TextView cell = (TextView) gridView.getChildAt(r * GRID_SIZE + c);
                if (grid[r][c] == 0) {
                    cell.setText("");
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor("#2A2A2A"));
                    bg.setCornerRadius(10);
                    cell.setBackground(bg);
                } else {
                    int idx = grid[r][c] - 1;
                    cell.setText(EMOJIS[idx]);
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor(COLORS[idx]));
                    bg.setCornerRadius(10);
                    cell.setBackground(bg);
                }
            }
        }
    }

    private void checkLines() {
        int cleared = 0;

        // افحص الصفوف
        for (int r = 0; r < GRID_SIZE; r++) {
            boolean full = true;
            for (int c = 0; c < GRID_SIZE; c++) {
                if (grid[r][c] == 0) { full = false; break; }
            }
            if (full) {
                for (int c = 0; c < GRID_SIZE; c++) grid[r][c] = 0;
                cleared++;
            }
        }

        // افحص الأعمدة
        for (int c = 0; c < GRID_SIZE; c++) {
            boolean full = true;
            for (int r = 0; r < GRID_SIZE; r++) {
                if (grid[r][c] == 0) { full = false; break; }
            }
            if (full) {
                for (int r = 0; r < GRID_SIZE; r++) grid[r][c] = 0;
                cleared++;
            }
        }

        if (cleared > 0) {
            score += cleared * 10;
            scoreView.setText("النقاط: " + score);
            Toast.makeText(this, "🎉 +" + (cleared * 10) + " نقطة!", Toast.LENGTH_SHORT).show();
            updateGrid();
        }
    }

    private void resetGame() {
        grid = new int[GRID_SIZE][GRID_SIZE];
        score = 0;
        scoreView.setText("النقاط: 0");
        updateGrid();
        Toast.makeText(this, "لعبة جديدة!", Toast.LENGTH_SHORT).show();
    }
}
