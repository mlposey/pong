package com.marcusposey;

/** A game object which is visible on a 2d plane */
public abstract class Entity {
    private int xCoord;
    private int yCoord;

    private int width;
    private int height;

    public Entity(int xCoord, int yCoord, int width, int height) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.width = width;
        this.height = height;
    }

    /** Returns the entity's x coordinate */
    public int getX() { return xCoord; }

    /** Returns the entity's y coordinate */
    public int getY() { return yCoord; }

    /** Returns the entity's width */
    public int getWidth() { return width; }

    /** Returns the entity's height */
    public int getHeight() { return height; }

    /** Sets the entity's x coordinate */
    public void setX(final int x) { xCoord = x; }

    /** Sets the entity's y coordinate */
    public void setY(final int y) { yCoord = y; }

    /** Sets the entity's width */
    public void setWidth(int width) { this.width = width; }

    /** Set's the entity's height */
    public void setHeight(int height) { this.height = height; }
}
