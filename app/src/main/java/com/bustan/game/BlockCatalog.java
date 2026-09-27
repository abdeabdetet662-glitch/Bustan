package com.bustan.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockCatalog {

    public static final int COLOR_BLUE = 0xFF1565C0;
    public static final int COLOR_RED = 0xFFC62828;
    public static final int COLOR_GREEN = 0xFF2E7D32;
    public static final int COLOR_YELLOW = 0xFFF9A825;
    public static final int COLOR_PURPLE = 0xFF6A1B9A;
    public static final int COLOR_ORANGE = 0xFFEF6C00;
    public static final int COLOR_PINK = 0xFFAD1457;
    public static final int COLOR_CYAN = 0xFF00838F;

    public static List<Block> getAllShapes() {
        List<Block> list = new ArrayList<>();

        // مربع 1×1
        list.add(new Block(new int[][]{{1}}, COLOR_BLUE));

        // خطوط أفقية
        list.add(new Block(new int[][]{{1, 1}}, COLOR_RED));
        list.add(new Block(new int[][]{{1, 1, 1}}, COLOR_GREEN));
        list.add(new Block(new int[][]{{1, 1, 1, 1}}, COLOR_YELLOW));
        list.add(new Block(new int[][]{{1, 1, 1, 1, 1}}, COLOR_PURPLE));

        // خطوط عمودية
        list.add(new Block(new int[][]{{1}, {1}}, COLOR_ORANGE));
        list.add(new Block(new int[][]{{1}, {1}, {1}}, COLOR_PINK));
        list.add(new Block(new int[][]{{1}, {1}, {1}, {1}}, COLOR_CYAN));
        list.add(new Block(new int[][]{{1}, {1}, {1}, {1}, {1}}, COLOR_BLUE));

        // مربعات
        list.add(new Block(new int[][]{{1, 1}, {1, 1}}, COLOR_RED));
        list.add(new Block(new int[][]{{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, COLOR_GREEN));

        // L-Shapes
        list.add(new Block(new int[][]{{1, 0}, {1, 0}, {1, 1}}, COLOR_YELLOW));
        list.add(new Block(new int[][]{{0, 1}, {0, 1}, {1, 1}}, COLOR_PURPLE));
        list.add(new Block(new int[][]{{1, 1, 1}, {1, 0, 0}}, COLOR_ORANGE));
        list.add(new Block(new int[][]{{1, 1, 1}, {0, 0, 1}}, COLOR_PINK));
        list.add(new Block(new int[][]{{1, 1}, {1, 0}, {1, 0}}, COLOR_CYAN));
        list.add(new Block(new int[][]{{1, 1}, {0, 1}, {0, 1}}, COLOR_BLUE));

        // T-Shapes
        list.add(new Block(new int[][]{{1, 1, 1}, {0, 1, 0}}, COLOR_RED));
        list.add(new Block(new int[][]{{0, 1, 0}, {1, 1, 1}}, COLOR_GREEN));
        list.add(new Block(new int[][]{{1, 0}, {1, 1}, {1, 0}}, COLOR_YELLOW));
        list.add(new Block(new int[][]{{0, 1}, {1, 1}, {0, 1}}, COLOR_PURPLE));

        // S/Z Shapes
        list.add(new Block(new int[][]{{1, 1, 0}, {0, 1, 1}}, COLOR_ORANGE));
        list.add(new Block(new int[][]{{0, 1, 1}, {1, 1, 0}}, COLOR_PINK));
        list.add(new Block(new int[][]{{1, 0}, {1, 1}, {0, 1}}, COLOR_CYAN));
        list.add(new Block(new int[][]{{0, 1}, {1, 1}, {1, 0}}, COLOR_BLUE));

        return list;
    }

    public static Block getRandom() {
        List<Block> all = getAllShapes();
        return all.get(new Random().nextInt(all.size()));
    }
}
