package com.bustan.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {

    public interface GameListener {
        void onScoreChanged(int score);
        void onGameOver(int score);
    }

    private static final int GRID = 8;
    private int[][] grid = new int[GRID][GRID];
    private int score = 0;
    private GameListener listener;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // أبعاد
    private float boardLeft, boardTop, cellSize, boardSize;
    private float trayTop, trayCellSize;

    // القطع الحالية (3)
    private final Block[] tray = new Block[3];
    private final boolean[] trayUsed = new boolean[3];

    // سحب
    private int draggingIndex = -1;
    private float dragX = 0, dragY = 0;
    private int previewRow = -1, previewCol = -1;
    private boolean previewValid = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public GameView(Context context) {
        super(context);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        newGame();
    }

    public void setListener(GameListener l) { this.listener = l; }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float padding = w * 0.05f;
        boardSize = w - padding * 2;
        cellSize = boardSize / GRID;
        boardLeft = padding;
        boardTop = h * 0.08f;

        trayTop = boardTop + boardSize + h * 0.06f;
        trayCellSize = cellSize * 0.85f;
    }

    public void newGame() {
        grid = new int[GRID][GRID];
        score = 0;
        if (listener != null) listener.onScoreChanged(score);
        refillTray();
        invalidate();
    }

    private void refillTray() {
        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null) {
                tray[i] = BlockCatalog.getRandom();
                trayUsed[i] = false;
            }
        }
    }

    private void refreshTrayIfEmpty() {
        boolean allUsed = true;
        for (int i = 0; i < 3; i++) {
            if (!trayUsed[i]) { allUsed = false; break; }
        }
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
        canvas.drawColor(0xFF0D0D0D);

        // ارسم اللوحة
        paint.setColor(0xFF1A1A1A);
        RectF board = new RectF(boardLeft, boardTop, boardLeft + boardSize, boardTop + boardSize);
        canvas.drawRoundRect(board, 24, 24, paint);

        // ارسم شبكة فارغة
        paint.setColor(0xFF2A2A2A);
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                float x = boardLeft + c * cellSize + 3;
                float y = boardTop + r * cellSize + 3;
                RectF cell = new RectF(x, y, x + cellSize - 6, y + cellSize - 6);
                canvas.drawRoundRect(cell, 8, 8, paint);
            }
        }

        // ارسم القطع الموضوعة على اللوحة
        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                if (grid[r][c] != 0) {
                    drawBlock(canvas, r, c, grid[r][c], 1f);
                }
            }
        }

        // ارسم preview السحب
        if (draggingIndex != -1 && previewValid) {
            Block b = tray[draggingIndex];
            if (b != null) {
                paint.setColor(b.color);
                paint.setAlpha(120);
                for (int r = 0; r < b.rows(); r++) {
                    for (int c = 0; c < b.cols(); c++) {
                        if (b.shape[r][c] == 1) {
                            int gr = previewRow + r;
                            int gc = previewCol + c;
                            if (gr >= 0 && gr < GRID && gc >= 0 && gc < GRID) {
                                float x = boardLeft + gc * cellSize + 3;
                                float y = boardTop + gr * cellSize + 3;
                                RectF cell = new RectF(x, y, x + cellSize - 6, y + cellSize - 6);
                                canvas.drawRoundRect(cell, 8, 8, paint);
                            }
                        }
                    }
                }
                paint.setAlpha(255);
            }
        }

        // ارسم الـ tray (3 قطع أسفل)
        drawTray(canvas);

        // ارسم القطعة أثناء السحب
        if (draggingIndex != -1 && tray[draggingIndex] != null) {
            drawDraggingBlock(canvas);
        }
    }

    private void drawBlock(Canvas canvas, int r, int c, int color, float scale) {
        float x = boardLeft + c * cellSize + 3;
        float y = boardTop + r * cellSize + 3;
        float size = (cellSize - 6) * scale;
        paint.setColor(color);
        RectF cell = new RectF(x, y, x + size, y + size);
        canvas.drawRoundRect(cell, 8, 8, paint);

        // لمعة داخلية
        paint.setColor(0x30FFFFFF);
        RectF shine = new RectF(x + 4, y + 4, x + size - 4, y + size / 3);
        canvas.drawRoundRect(shine, 6, 6, paint);
    }

    private void drawTray(Canvas canvas) {
        float trayWidth = getWidth() - boardLeft * 2;
        float slotWidth = trayWidth / 3;

        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null) continue;

            Block b = tray[i];
            float slotCenterX = boardLeft + slotWidth * i + slotWidth / 2;
            float slotCenterY = trayTop + trayCellSize * 2f;

            float blockW = b.cols() * trayCellSize;
            float blockH = b.rows() * trayCellSize;
            float startX = slotCenterX - blockW / 2;
            float startY = slotCenterY - blockH / 2;

            // إذا كانت هذه القطعة تحت السحب، لا ترسمها في مكانها
            if (draggingIndex == i) continue;

            paint.setColor(b.color);
            for (int r = 0; r < b.rows(); r++) {
                for (int c = 0; c < b.cols(); c++) {
                    if (b.shape[r][c] == 1) {
                        float x = startX + c * trayCellSize + 2;
                        float y = startY + r * trayCellSize + 2;
                        RectF cell = new RectF(x, y, x + trayCellSize - 4, y + trayCellSize - 4);
                        canvas.drawRoundRect(cell, 6, 6, paint);
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

        paint.setColor(b.color);
        paint.setAlpha(200);

        for (int r = 0; r < b.rows(); r++) {
            for (int c = 0; c < b.cols(); c++) {
                if (b.shape[r][c] == 1) {
                    float x = startX + c * cellSize;
                    float y = startY + r * cellSize;
                    RectF cell = new RectF(x + 3, y + 3, x + cellSize - 3, y + cellSize - 3);
                    canvas.drawRoundRect(cell, 8, 8, paint);
                }
            }
        }
        paint.setAlpha(255);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // تحقق إذا ضغط على قطعة في الـ tray
                int hit = hitTestTray(x, y);
                if (hit != -1) {
                    draggingIndex = hit;
                    dragX = x;
                    dragY = y;
                    updatePreview();
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (draggingIndex != -1) {
                    dragX = x;
                    dragY = y;
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
                    previewRow = -1;
                    previewCol = -1;
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

            if (x >= slotCenterX - blockW / 2 - 10 && x <= slotCenterX + blockW / 2 + 10
                    && y >= slotCenterY - blockH / 2 - 10 && y <= slotCenterY + blockH / 2 + 10) {
                return i;
            }
        }
        return -1;
    }

    private void updatePreview() {
        if (draggingIndex == -1 || tray[draggingIndex] == null) {
            previewValid = false;
            return;
        }

        Block b = tray[draggingIndex];
        float blockW = b.cols() * cellSize;
        float blockH = b.rows() * cellSize;
        float startX = dragX - blockW / 2;
        float startY = dragY - blockH / 2;

        int col = Math.round((startX - boardLeft) / cellSize);
        int row = Math.round((startY - boardTop) / cellSize);

        previewRow = row;
        previewCol = col;

        previewValid = canPlace(b, row, col);
    }

    private boolean canPlace(Block b, int row, int col) {
        if (b == null) return false;
        for (int r = 0; r < b.rows(); r++) {
            for (int c = 0; c < b.cols(); c++) {
                if (b.shape[r][c] == 1) {
                    int gr = row + r;
                    int gc = col + c;
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
        int cleared = clearLines();

        if (cleared > 0) {
            score += cleared * 10 + (cleared - 1) * 5;
            if (listener != null) listener.onScoreChanged(score);
        }

        refreshTrayIfEmpty();

        if (!hasAnyMove()) {
            if (listener != null) listener.onGameOver(score);
        }
    }

    private int clearLines() {
        int cleared = 0;
        List<Integer> fullRows = new ArrayList<>();
        List<Integer> fullCols = new ArrayList<>();

        for (int r = 0; r < GRID; r++) {
            boolean full = true;
            for (int c = 0; c < GRID; c++) {
                if (grid[r][c] == 0) { full = false; break; }
            }
            if (full) fullRows.add(r);
        }

        for (int c = 0; c < GRID; c++) {
            boolean full = true;
            for (int r = 0; r < GRID; r++) {
                if (grid[r][c] == 0) { full = false; break; }
            }
            if (full) fullCols.add(c);
        }

        for (int r : fullRows) {
            for (int c = 0; c < GRID; c++) grid[r][c] = 0;
            cleared++;
        }
        for (int c : fullCols) {
            for (int r = 0; r < GRID; r++) grid[r][c] = 0;
            cleared++;
        }

        return cleared;
    }

    private boolean hasAnyMove() {
        // تحقق إذا كان أي من القطع الحالية يمكن وضعها في مكان ما
        for (int i = 0; i < 3; i++) {
            if (trayUsed[i] || tray[i] == null) continue;
            Block b = tray[i];
            for (int r = 0; r < GRID; r++) {
                for (int c = 0; c < GRID; c++) {
                    if (canPlace(b, r, c)) return true;
                }
            }
        }
        return false;
    }
}
