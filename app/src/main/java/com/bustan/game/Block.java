package com.bustan.game;

public class Block {
    public int[][] shape;
    public int color;

    public Block(int[][] shape, int color) {
        this.shape = shape;
        this.color = color;
    }

    public int rows() { return shape.length; }
    public int cols() { return shape[0].length; }
}
