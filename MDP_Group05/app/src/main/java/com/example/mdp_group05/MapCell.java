package com.example.mdp_group05;

import android.graphics.RectF;

public class MapCell {
    private int cellColor = 0;
    private boolean cellIsWaypoint;
    private boolean cellIsObstacle;
    private boolean cellIsStartpoint;
    private boolean cellIsEndpoint;
    private boolean cellIsExplored;
    private RectF cell;

    public MapCell(float left, float top, float right, float down){
        setRect(left, top, right,down);
    }

    public void setCellIsStartpoint(boolean cellIsStartpoint) {
        setColor(3);
        this.cellIsStartpoint = cellIsStartpoint;
    }

    public void setCellIsEndpoint(boolean cellIsEndpoint) {
        setColor(5);
        this.cellIsEndpoint = cellIsEndpoint;
    }

    public void setCellIsExplored(boolean cellIsExplored) {
        setColor(1);
        this.cellIsExplored = cellIsExplored;
    }

    public int takeColor() {
        return cellColor;
    }

    public void setColor(int colourSet) {
       this.cellColor = colourSet;
    }

    public boolean isWaypoint() {
        return cellIsWaypoint;
    }

    public void setWaypoint(boolean waypoint) {
        setColor(4);
        this.cellIsWaypoint = waypoint;
    }

    public boolean isObstacles() {
        return cellIsObstacle;
    }

    public void setObstacles(boolean obstacle) {
        setColor(2);
        this.cellIsObstacle = obstacle;
    }

    public void setRect(float left, float top, float right, float down) {
        this.cell = new RectF(left, top, right, down);
    }

    public RectF getRect(){
        return this.cell;
    }
}
