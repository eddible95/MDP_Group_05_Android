package com.example.mdp_group05;

public class Robot {

    // Member fields
    private float bodyRadius, bodyRight, bodyDown, headRight, headDown;
    private int gridSize;
    private int[] robotFront; //[Right, Down] coordinates
    private int[] robotCenter; //[Right, Down] coordinates

    public void setBodyRadius(float bodyRadius) {
        this.bodyRadius = bodyRadius;
    }

    public void setBodyRight(float bodyRight) {
        this.bodyRight = bodyRight;
    }

    public void setBodyDown(float bodyDown) {
        this.bodyDown = bodyDown;
    }

    public void setHeadRight(float headRight) {
        this.headRight = headRight;
    }

    public void setHeadDown(float headDown) {
        this.headDown = headDown;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setRobotFront(int[] robotFront) {
        this.robotFront = robotFront;
    }

    public void setRobotCenter(int[] robotCenter) {
        this.robotCenter = robotCenter;
    }

    public float getBodyRadius() {
        return bodyRadius;
    }

    public float getBodyRight() {
        return bodyRight;
    }

    public float getBodyDown() {
        return bodyDown;
    }

    public float getHeadRight() {
        return headRight;
    }

    public float getHeadDown() {
        return headDown;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int[] getRobotFront() {
        return robotFront;
    }

    public int[] getRobotCenter() {
        return robotCenter;
    }

    public Robot(int [] robotFront, int [] robotCenter, int gridSize) {

        this.robotFront= robotFront;
        this.robotCenter = robotCenter;
        this.robotCenter[1] = robotCenter[1];
        this.gridSize = gridSize;
        this.bodyRadius = (gridSize * 3) / 2;
        this.bodyRight = (this.robotCenter[0] * gridSize) + (gridSize / 2) + 100; //Use number of columns
        this.bodyDown = (this.robotCenter[1] * gridSize) + (gridSize / 2) + 100; //Use number of rows //18
        this.headRight = (this.robotFront[0] * gridSize) + (gridSize / 2) + 100;
        this.headDown = (this.robotFront[1] * gridSize) + (gridSize / 2) + 100; //17
    }

    public void moveUp() { //[Right, Down] coordinates

        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){//Facing Up
            if(robotFront[1] == 0 && robotCenter[1] == 1){// Reached top of arena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
            }
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){//Facing Right
            if(robotFront[0] == 14 && robotCenter[0] == 13){// Reached right of arena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
            }
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            if(robotFront[1] == 19 && robotCenter[1] == 18){// Reached bottom of arena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
            }
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            if(robotFront[0] == 0 && robotCenter[0] == 1){// Reached left of arena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
            }
        }
    }

    public void rotateRight() { //[Right, Down] coordinates
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){ //Facing Up
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Right
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1 ;
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            robotFront[0] = robotCenter[0] -1;
            robotFront[1] = robotCenter[1] ;
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1;
        }
    }

    public void rotateLeft(){
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){ //Facing Up
            robotFront[0] = robotCenter[0] - 1;
            robotFront[1] = robotCenter[1] ;
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Right
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1 ;
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1;
        }
    }

    public void reverse(){
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){//Facing Up
            if(robotFront[1] == 0 && robotCenter[1] == 1){// Reached top of arena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
            }
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){//Facing Right
            if(robotFront[0] == 14 && robotCenter[0] == 13){// Reached right of arena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
            }
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            if(robotFront[1] == 19 && robotCenter[1] == 18){// Reached bottom of arena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
            }
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            if(robotFront[0] == 0 && robotCenter[0] == 1){// Reached left of arena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
            }
        }
    }
}
