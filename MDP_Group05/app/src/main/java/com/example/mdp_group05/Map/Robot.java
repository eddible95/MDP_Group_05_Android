package com.example.mdp_group05.Map;

import android.util.Log;

public class Robot {

    private static String TAG = "Robot";

    // Member fields
    private int[] robotFront; //[Right, Down] coordinates
    private int[] robotCenter; //[Right, Down] coordinates
    private MDFDecoder mdfDecoder;

    public Robot(int [] robotFront, int [] robotCenter, MDFDecoder mdfDecoder) {

        this.robotFront = robotFront;
        this.robotCenter = robotCenter;
        this.mdfDecoder = mdfDecoder;
    }

    public void moveForward() { //[Right, Down] coordinates

        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){//Facing Up
            if(robotFront[1] == 0 && robotCenter[1] == 1){// Reached top of mapArena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
                //mdfDecoder.updateRobotStartPoint(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){//Facing Right
            if(robotFront[0] == 14 && robotCenter[0] == 13){// Reached right of mapArena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
                //mdfDecoder.updateRobotStartPoint(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            if(robotFront[1] == 19 && robotCenter[1] == 18){// Reached bottom of mapArena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
                //mdfDecoder.updateRobotStartPoint(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            if(robotFront[0] == 0 && robotCenter[0] == 1){// Reached left of mapArena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
                //mdfDecoder.updateRobotStartPoint(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
    }

    public void rotateRight() { //[Right, Down] coordinates
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){ //Facing Up
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Right
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1 ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            robotFront[0] = robotCenter[0] -1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
    }

    public void rotateLeft(){
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){ //Facing Up
            robotFront[0] = robotCenter[0] - 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],270);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Right
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1 ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],0);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],90);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],180);
            //mdfDecoder.updateRobotStartPoint(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
    }

    public void reverse(){
        if(robotCenter[0] == robotFront[0] && robotCenter[1] > robotFront[1]){//Facing Up
            if(robotFront[1] == 0 && robotCenter[1] == 1){// Reached top of mapArena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
            }
        }
        else if(robotCenter[0] < robotFront[0] && robotCenter[1] == robotFront[1]){//Facing Right
            if(robotFront[0] == 14 && robotCenter[0] == 13){// Reached right of mapArena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
            }
        }
        else if(robotCenter[0] == robotFront[0] && robotCenter[1] < robotFront[1]){ //Facing Down
            if(robotFront[1] == 19 && robotCenter[1] == 18){// Reached bottom of mapArena
                // No action
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
            }
        }
        else if(robotCenter[0] > robotFront[0] && robotCenter[1] == robotFront[1]){ //Facing Left
            if(robotFront[0] == 0 && robotCenter[0] == 1){// Reached left of mapArena
                // No action
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
            }
        }
    }
}
