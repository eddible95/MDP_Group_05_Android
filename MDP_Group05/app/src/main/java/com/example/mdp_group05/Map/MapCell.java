package com.example.mdp_group05.Map;

import android.graphics.RectF;

public class MapCell {

    // Member fields
    private int cellColor = 2; // Default set cell as unexplored
    private boolean isExplored;
    private boolean isObstacle;
    private boolean isStartpoint;
    private boolean isWaypoint;
    private boolean isEndpoint;
    private RectF cellDimension;

    // Constructor is called to create the cell to draw
    public MapCell(float left, float top, float right, float btm){
        setRect(left, top, right, btm);
    }

    public void setExplored(boolean explored) {
        setCellColor(0);
        this.isExplored = explored;
    }

    public void setObstacle(boolean obstacle) {
        setCellColor(1);
        this.isObstacle = obstacle;
    }

    public void setStartpoint(boolean startpoint) {
        setCellColor(3);
        this.isStartpoint = startpoint;
    }

    public void setWaypoint(boolean waypoint) {
        setCellColor(4);
        this.isWaypoint = waypoint;
    }

    public void setEndpoint(boolean endpoint) {
        setCellColor(5);
        this.isEndpoint = endpoint;
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

    public boolean isWaypoint() {
        return isWaypoint;
    }


    public boolean isObstacles() {
        return isObstacle;
    }

    public RectF getRect(){
        return this.cellDimension;
    }
}
