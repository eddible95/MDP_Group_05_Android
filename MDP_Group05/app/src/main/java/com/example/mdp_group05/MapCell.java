package com.example.mdp_group05;

import android.graphics.RectF;

public class MapCell {

    // Member fields
    private int cellColor = 0;
    private boolean cellIsExplored;
    private boolean cellIsObstacle;
    private boolean cellIsStartpoint;
    private boolean cellIsWaypoint;
    private boolean cellIsEndpoint;
    private RectF cell;

    // Constructor is called to create the cell to draw
    public MapCell(float left, float top, float right, float down){
        setRect(left, top, right, down);
    }

    public void setCellIsExplored(boolean cellIsExplored) {
        setCellColor(0);
        this.cellIsExplored = cellIsExplored;
    }

    public void setCellIsObstacle(boolean obstacle) {
        setCellColor(1);
        this.cellIsObstacle = obstacle;
    }

    public void setCellIsStartpoint(boolean cellIsStartpoint) {
        setCellColor(3);
        this.cellIsStartpoint = cellIsStartpoint;
    }

    public void setCellIsWaypoint(boolean waypoint) {
        setCellColor(4);
        this.cellIsWaypoint = waypoint;
    }

    public void setCellIsEndpoint(boolean cellIsEndpoint) {
        setCellColor(5);
        this.cellIsEndpoint = cellIsEndpoint;
    }

    public void setCellColor(int colourSet) {
       this.cellColor = colourSet;
    }

    public void setRect(float left, float top, float right, float down) {
        this.cell = new RectF(left, top, right, down);
    }

    public int getCellColor() {
        return cellColor;
    }

    public boolean isWaypoint() {
        return cellIsWaypoint;
    }


    public boolean isObstacles() {
        return cellIsObstacle;
    }

    public RectF getRect(){
        return this.cell;
    }
}
