package com.bustan.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    public interface GameListener {
        void onScoreChanged(int score);
        void onGameOver(int score, int best);
    }

    private static final int GRID = 8;
    private int[][] grid = new int[GRID][GRID];
    private int score = 0;
    private int best = 0;
    private GameListener listener;
    private Random random = new Random();

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float boardLeft, boardTop, cellSize, boardSize;
    private float trayTop, trayCellSize;

    private final Block[] tray = new Block[3];
    private final boolean[] trayUsed = new boolean[3];

    private int draggingIndex = -1;
    private float dragX = 0, dragY = 0;
    private int previewRow = -1, previewCol = -1;
    private boolean previewValid = false;

    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final List<ClearingCell> clearingCells = new ArrayList<>();

    private float scorePulse = 1f;

    private ToneGenerator tone;

    public GameView(Context context) {
        super(context);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        try {
            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        } catch (Exception e) { tone = null; }

        best = context.getSharedPreferences("bustan", Context.MODE_PRIVATE)
                .getInt("best", 0);
        newGame();
    }

    public void setListener(GameListener l) { this.listener = l; }
    public int getBest() { return best; }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = w * 0.05f;
        boardSize = w - padding * 2;
        cellSize = boardSize / GRID;
        boardLeft = padding;
        boardTop = h * 0.10f;
        trayTop = boardTop + boardSize + h * 0.05f;
        trayCellSize = cellSize * 0.85f;
    }

    public void newGame() {
        grid = new int[GRID][GRID];
        score = 0;
        particles.clear();
        floatingTexts.clear();
        clearingCells.clear();
        if (listener != null) listener.onScoreChanged(score);
        for (int i = 0; i < 3; i++) {
            tray[i] = BlockCatalog.getRandom();
            trayUsed[i] = false;
        }
        invalidate();
    }

    private void refillTrayIfEmpty() {
        boolean allUsed = true;
        for (int i = 0; i < 3; i++) if (!trayUsed[i]) { allUsed = false; break; }
        if (allUsed) {
            for (int i = 0; i < 3; i++) {
                tray[i] = BlockCatalog.getRandom();
                trayUsed[i] = false;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // خلفية متدرجة
        LinearGradient bg = new LinearGradient(0, 0, 0, getHeight(),
                0xFF1A237E, 0xFF0D0D0D, Shader.TileMode.CLAMP);
        paint.setShader(bg);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);

        // ارسم اللوحة
        paint.setColor(0x33FFFFFF);
        RectF board = new RectF(boardLeft - 6, boardTop - 6,
                boardLeft + boardSize + 6, boardTop + boardSize + 6);
        canvas.drawRoundRect(board, 28, 28, paint);

        paint.setColor(0x88000000);
        RectF inner = new RectF(boardLeft, boardTop,
                boardLeft + boardSize, boardTop + boardSize);
        canvas.drawRoundRect(inner, 22, 22, paint);

        // شبكة فارغة
        paint.setColor(0x22FFFFFF);
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                if (grid[r][c] == 0) {
                    float x = boardLeft + c * cellSize + 4;
                    float y = boardTop + r * cellSize + 4;
                    RectF cell = new RectF(x, y, x + cellSize - 8, y + cellSize - 8);
                    canvas.drawRoundRect(cell, 10, 10, paint);
                }
            }
        }

        // ارسم القطع الموضوعة
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                if (grid[r][c] != 0) {
                    drawBlock3D(canvas, boardLeft + c * cellSize + 4,
                            boardTop + r * cellSize + 4,
                            cellSize - 8, grid[r][c], 1f, 1f);
                }
            }
        }

        // ارسم القطع المتحللة
        Iterator<ClearingCell> ci = clearingCells.iterator();
        while (ci.hasNext()) {
            ClearingCell cc = ci.next();
            cc.progress += 0.06f;
            if (cc.progress >= 1f) { ci.remove(); continue; }
            float scale = 1f - cc.progress;
            float alpha = 1f - cc.progress;
            drawBlock3D(canvas, boardLeft + cc.c * cellSize + 4,
                    boardTop + cc.r * cellSize + 4,
                    cellSize - 8, cc.color, scale, alpha);
        }

        // preview السحب
        if (draggingIndex != -1 && previewValid) {
            Block b = tray[draggingIndex];
            if (b != null) {
                paint.setColor(b.color);
                paint.setAlpha(100);
                for (int r = 0; r < b.rows(); r++) {
                    for (int c = 0; c < b.cols(); c++) {
                        if (b.shape[r][c] == 1) {
                            int gr = previewRow + r, gc = previewCol + c;
                            if (gr >= 0 && gr < GRID && gc >= 0 && gc < GRID) {
                                float x = boardLeft + gc * cellSize + 4;
                                float y = boardTop + gr * cellSize + 4;
                                RectF cell = new RectF(x, y, x + cellSize - 8, y + cellSize - 8);
                                canvas.drawRoundRect(cell, 10, 10, paint);
                            }
                        }
                    }
                }
                paint.setAlpha(255);
            }
        }

        // الـ tray
        drawTray(canvas);

        // ارسم القطعة أثناء السحب
        if (draggingIndex != -1 && tray[draggingIndex] != null) {
            drawDraggingBlock(canvas);
        }

        // ارسم الشرارات
        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) {
            Particle p = pi.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.5f;
            p.life -= 0.02f;
            if (p.life <= 0) { pi.remove(); continue; }
            paint.setColor(p.color);
            paint.setAlpha((int) (255 * p.life));
            canvas.drawCircle(p.x, p.y, p.size * p.life, paint);
        }
        paint.setAlpha(255);

        // ارسم النصوص الطائرة
        Iterator<FloatingText> fi = floatingTexts.iterator();
        while (fi.hasNext()) {
            FloatingText ft = fi.next();
            ft.y -= 3f;
            ft.life -= 0.025f;
            if (ft.life <= 0) { fi.remove(); continue; }
            textPaint.setTextSize(ft.size);
            textPaint.setColor(ft.color);
            textPaint.setAlpha((int) (255 * ft.life));
            canvas.drawText(ft.text, ft.x, ft.y, textPaint);
        }
        textPaint.setAlpha(255);

        // حلقة الاستمرار
        if (!particles.isEmpty() || !floatingTexts.isEmpty() || !clearingCells.isEmpty()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawBlock3D(Canvas canvas, float x, float y, float size,
                              int color, float scale, float alpha) {
        if (scale <= 0.01f) return;

        float scaled = size * scale;
        float cx = x + size / 2;
        float cy = y + size / 2;
        float left = cx - scaled / 2;
        float top = cy - scaled / 2;

        int a = (int) (alpha * 255);
        int lighter = lighten(color, 1.4f);
        int darker = darken(color, 0.6f);

        // ظل
        paint.setColor(0x44000000);
        paint.setAlpha((int) (alpha * 100));
        RectF shadow = new RectF(left + 3, top + 5, left + scaled + 3, top + scaled + 5);
        canvas.drawRoundRect(shadow, 12, 12, paint);

        // قاعدة المكعب
        paint.setColor(darker);
        paint.setAlpha(a);
        RectF base = new RectF(left, top, left + scaled, top + scaled);
        canvas.drawRoundRect(base, 12, 12, paint);

        // تدرج اللمعة (أعلى)
        LinearGradient shine = new LinearGradient(left, top, left, top + scaled,
                lighter, color, Shader.TileMode.CLAMP);
        paint.setShader(shine);
        paint.setAlpha(a);
        RectF topFace = new RectF(left + scaled * 0.06f, top + scaled * 0.06f,
                left + scaled * 0.94f, top + scaled * 0.94f);
        canvas.drawRoundRect(topFace, 10, 10, paint);
        paint.setShader(null);

        // لمعة بيضاء علوية
        paint.setColor(0x99FFFFFF);
        paint.setAlpha((int) (alpha * 150));
        RectF glow = new RectF(left + scaled * 0.15f, top + scaled * 0.12f,
                left + scaled * 0.85f, top + scaled * 0.32f);
        canvas.drawRoundRect(glow, 8, 8, paint);

        paint.setAlpha(255);
    }

    private int lighten(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.rgb(r, g, b);
    }

    private int darken(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }

    private void drawTray(Canvas canvas) {
        float trayWidth = getWidth() - boardLeft * 2;
        float slotWidth = trayWidth / 3;

        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null || draggingIndex == i) continue;

            Block b = tray[i];
            float slotCenterX = boardLeft + slotWidth * i + slotWidth / 2;
            float slotCenterY = trayTop + trayCellSize * 2f;

            float blockW = b.cols() * trayCellSize;
            float blockH = b.rows() * trayCellSize;
            float startX = slotCenterX - blockW / 2;
            float startY = slotCenterY - blockH / 2;

            for (int r = 0; r < b.rows(); r++) {
                for (int c = 0; c < b.cols(); c++) {
                    if (b.shape[r][c] == 1) {
                        drawBlock3D(canvas,
                                startX + c * trayCellSize,
                                startY + r * trayCellSize,
                                trayCellSize, b.color, 1f, 1f);
                    }
                }
            }
        }
    }

    private void drawDraggingBlock(Canvas canvas) {
        Block b = tray[draggingIndex];
        if (b == null) return;

        float blockW = b.cols() * cellSize;
        float blockH = b.rows() * cellSize;
        float startX = dragX - blockW / 2;
        float startY = dragY - blockH / 2;

        for (int r = 0; r < b.rows(); r++) {
            for (int c = 0; c < b.cols(); c++) {
                if (b.shape[r][c] == 1) {
                    drawBlock3D(canvas,
                            startX + c * cellSize,
                            startY + r * cellSize,
                            cellSize, b.color, 0.85f, 0.95f);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int hit = hitTestTray(x, y);
                if (hit != -1) {
                    draggingIndex = hit;
                    dragX = x; dragY = y;
                    updatePreview();
                    playTone(ToneGenerator.TONE_PROP_BEEP, 30);
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggingIndex != -1) {
                    dragX = x; dragY = y;
                    updatePreview();
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (draggingIndex != -1) {
                    if (previewValid && previewRow >= 0 && previewCol >= 0) {
                        placeBlock();
                    }
                    draggingIndex = -1;
                    previewValid = false;
                    previewRow = -1; previewCol = -1;
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private int hitTestTray(float x, float y) {
        float trayWidth = getWidth() - boardLeft * 2;
        float slotWidth = trayWidth / 3;

        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null) continue;
            Block b = tray[i];
            float slotCenterX = boardLeft + slotWidth * i + slotWidth / 2;
            float slotCenterY = trayTop + trayCellSize * 2f;
            float blockW = b.cols() * trayCellSize;
            float blockH = b.rows() * trayCellSize;

            if (x >= slotCenterX - blockW / 2 - 15 && x <= slotCenterX + blockW / 2 + 15
                    && y >= slotCenterY - blockH / 2 - 15 && y <= slotCenterY + blockH / 2 + 15) {
                return i;
            }
        }
        return -1;
    }

    private void updatePreview() {
        if (draggingIndex == -1 || tray[draggingIndex] == null) {
            previewValid = false; return;
        }
        Block b = tray[draggingIndex];
        float blockW = b.cols() * cellSize;
        float blockH = b.rows() * cellSize;
        float startX = dragX - blockW / 2;
        float startY = dragY - blockH / 2;

        int col = Math.round((startX - boardLeft) / cellSize);
        int row = Math.round((startY - boardTop) / cellSize);

        previewRow = row; previewCol = col;
        previewValid = canPlace(b, row, col);
    }

    private boolean canPlace(Block b, int row, int col) {
        if (b == null) return false;
        for (int r = 0; r < b.rows(); r++) {
            for (int c = 0; c < b.cols(); c++) {
                if (b.shape[r][c] == 1) {
                    int gr = row + r, gc = col + c;
                    if (gr < 0 || gr >= GRID || gc < 0 || gc >= GRID) return false;
                    if (grid[gr][gc] != 0) return false;
                }
            }
        }
        return true;
    }

    private void placeBlock() {
        Block b = tray[draggingIndex];
        if (b == null) return;

        for (int r = 0; r < b.rows(); r++) {
            for (int c = 0; c < b.cols(); c++) {
                if (b.shape[r][c] == 1) {
                    grid[previewRow + r][previewCol + c] = b.color;
                }
            }
        }

        trayUsed[draggingIndex] = true;
        playTone(ToneGenerator.TONE_PROP_ACK, 50);

        int cleared = clearLines();
        if (cleared > 0) {
            int points = cleared * 10 + (cleared - 1) * 10;
            score += points;
            if (listener != null) listener.onScoreChanged(score);
            spawnFloatingText("+" + points, getWidth() / 2, boardTop + boardSize / 2);
            playTone(ToneGenerator.TONE_PROP_BEEP2, 100);
            scorePulse = 1.4f;
        }

        refreshTrayIfEmpty();
        if (!hasAnyMove()) {
            saveBest();
            if (listener != null) listener.onGameOver(score, best);
        }
    }

    private int clearLines() {
        int cleared = 0;
        List<Integer> fullRows = new ArrayList<>();
        List<Integer> fullCols = new ArrayList<>();

        for (int r = 0; r < GRID; r++) {
            boolean full = true;
            for (int c = 0; c < GRID; c++) if (grid[r][c] == 0) { full = false; break; }
            if (full) fullRows.add(r);
        }
        for (int c = 0; c < GRID; c++) {
            boolean full = true;
            for (int r = 0; r < GRID; r++) if (grid[r][c] == 0) { full = false; break; }
            if (full) fullCols.add(c);
        }

        for (int r : fullRows) {
            for (int c = 0; c < GRID; c++) {
                addClearing(r, c, grid[r][c]);
                grid[r][c] = 0;
                spawnParticles(boardLeft + c * cellSize + cellSize / 2,
                        boardTop + r * cellSize + cellSize / 2, 5);
            }
            cleared++;
        }
        for (int c : fullCols) {
            for (int r = 0; r < GRID; r++) {
                if (grid[r][c] != 0) {
                    addClearing(r, c, grid[r][c]);
                    grid[r][c] = 0;
                }
                spawnParticles(boardLeft + c * cellSize + cellSize / 2,
                        boardTop + r * cellSize + cellSize / 2, 5);
            }
            cleared++;
        }
        return cleared;
    }

    private void addClearing(int r, int c, int color) {
        if (color == 0) return;
        ClearingCell cc = new ClearingCell();
        cc.r = r; cc.c = c; cc.color = color; cc.progress = 0f;
        clearingCells.add(cc);
    }

    private void spawnParticles(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = x; p.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            float speed = 3f + random.nextFloat() * 6f;
            p.vx = (float) Math.cos(angle) * speed;
            p.vy = (float) Math.sin(angle) * speed - 4;
            p.life = 1f;
            p.size = 4 + random.nextFloat() * 6;
            int[] colors = {0xFFFFD54F, 0xFF4FC3F7, 0xFF81C784, 0xFFFF8A65, 0xFFFFFFFF};
            p.color = colors[random.nextInt(colors.length)];
            particles.add(p);
        }
    }

    private void spawnFloatingText(String text, float x, float y) {
        FloatingText ft = new FloatingText();
        ft.text = text;
        ft.x = x; ft.y = y;
        ft.color = 0xFFFFD54F;
        ft.life = 1.5f;
        ft.size = 70;
        floatingTexts.add(ft);
    }

    private void refreshTrayIfEmpty() {
        boolean allUsed = true;
        for (int i = 0; i < 3; i++) if (!trayUsed[i]) { allUsed = false; break; }
        if (allUsed) {
            for (int i = 0; i < 3; i++) {
                tray[i] = BlockCatalog.getRandom();
                trayUsed[i] = false;
            }
        }
    }

    private boolean hasAnyMove() {
        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null) continue;
            Block b = tray[i];
            for (int r = 0; r < GRID; r++)
                for (int c = 0; c < GRID; c++)
                    if (canPlace(b, r, c)) return true;
        }
        return false;
    }

    private void saveBest() {
        if (score > best) {
            best = score;
            getContext().getSharedPreferences("bustan", Context.MODE_PRIVATE)
                    .edit().putInt("best", best).apply();
        }
    }

    private void playTone(int toneType, int durationMs) {
        if (tone == null) return;
        try { tone.startTone(toneType, durationMs); } catch (Exception e) {}
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (tone != null) { try { tone.release(); } catch (Exception e) {} }
    }

    private static class Particle {
        float x, y, vx, vy, life, size;
        int color;
    }
    private static class FloatingText {
        String text;
        float x, y, life, size;
        int color;
    }
    private static class ClearingCell {
        int r, c, color;
        float progress;
    }
}
