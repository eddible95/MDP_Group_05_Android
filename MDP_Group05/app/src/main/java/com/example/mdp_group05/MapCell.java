package com.example.mdp_group05;

import android.graphics.Color;

public class MapCell {
    private Color cellColor;
    private boolean cellIsWaypoint;
    private boolean cellIsObstacle;


    public Color takeColor() {
        return cellColor;
    }

    public void setColor(Color color) {
        this.cellColor = color;
    }

    public boolean isWaypoint() {
        return cellIsWaypoint;
    }

    public void setWaypoint(boolean waypoint) {
        this.cellIsWaypoint = waypoint;
    }

    public boolean isObstacles() {
        return cellIsObstacle;
    }

    public void setObstacles(boolean obstacle) {
        this.cellIsObstacle = obstacle;
    }


}
