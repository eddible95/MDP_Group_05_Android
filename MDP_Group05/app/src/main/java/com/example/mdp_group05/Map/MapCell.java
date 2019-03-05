package com.example.mdp_group05.Map;

import android.graphics.RectF;

public class MapCell {

    // Member fields
    private int cellColor = 0; // Default set cell as unexplored
    private RectF cellDimension;

    // Constructor is called to create the cell to draw
    public MapCell(float left, float top, float right, float btm){
        setRect(left, top, right, btm);
    }

    public void setExplored() {
        setCellColor(1);
    }

    public void setObstacle() {
        setCellColor(2);
    }

    public void setStartpoint() {
        setCellColor(3);
    }


    public void setEndpoint() {
        setCellColor(5);
    }

    public void setCellColor(int colourSet) {
        this.cellColor = colourSet;
    }

    public void setRect(float left, float top, float right, float down) {
        this.cellDimension = new RectF(left, top, right, down);
    }

    public int getCellColor() {
        return cellColor;
    }

    public RectF getRect(){
        return this.cellDimension;
    }
}
