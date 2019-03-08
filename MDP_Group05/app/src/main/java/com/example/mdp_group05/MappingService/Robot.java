package com.example.mdp_group05.MappingService;

import android.util.Log;

public class Robot {

    private static String TAG = "Robot";

    // Member fields
    private int[] robotFront; //[Right, Down] coordinates
    private int[] robotCenter; //[Right, Down] coordinates

    public Robot(int [] robotFront, int [] robotCenter) {

        this.robotFront = robotFront;
        this.robotCenter = robotCenter;
    }

    public void moveForward() { //[Right, Down] coordinates

        // Facing Up
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){

            // Reached top of mapArena
            if(robotFront[1] == 0 && robotCenter[1] == 1){
                // No action
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Right
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){

            // Reached right of mapArena
            if(robotFront[0] == 14 && robotCenter[0] == 13){
                // No action
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Down
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){

            // Reached bottom of mapArena
            if(robotFront[1] == 19 && robotCenter[1] == 18){
                // No action
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Left
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){

            // Reached left of mapArena
            if(robotFront[0] == 0 && robotCenter[0] == 1){
                // No action
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
    }

    public void rotateRight() { //[Right, Down] coordinates

        // Facing Up
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Right
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1 ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Down
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){
            robotFront[0] = robotCenter[0] -1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Left
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
    }

    public void rotateLeft(){

        // Facing Up
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){
            robotFront[0] = robotCenter[0] - 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Right
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1 ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Down
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Left
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
    }
}
