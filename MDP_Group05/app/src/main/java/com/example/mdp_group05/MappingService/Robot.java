package com.example.mdp_group05.MappingService;

import android.util.Log;

public class Robot {

    private static String TAG = "Robot";

    // Member fields
    private int[] robotFront; //[Right, Down] coordinates
    private int[] robotCenter; //[Right, Down] coordinates
    private final MDFDecoder mdfDecoder;

    public Robot(int[] robotFront, int[] robotCenter, MDFDecoder mdfDecoder) {

        this.robotFront = robotFront;
        robotFront[1] = 19-robotFront[1];
        this.robotCenter = robotCenter;
        robotCenter[1] = 19-robotCenter[1];
        this.mdfDecoder = mdfDecoder;
    }

    public void setRobotPos(int[] robotCenter, int[] robotFront){
        this.robotFront = robotFront;
        robotFront[1] = 19-robotFront[1];
        this.robotCenter = robotCenter;
        robotCenter[1] = 19-robotCenter[1];
    }

    public void moveForward() { //[Right, Down] coordinates

        // Facing Up
        if(robotCenter[2] == 0){

            // Reached top of mapArena
            if(robotFront[1] == 19 && robotCenter[1] == 18){
                Log.e(TAG, String.format("End of Arena"));
            }
            else{
                robotFront[1] = robotFront[1] + 1;
                robotCenter[1] = robotCenter[1] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
                mdfDecoder.updateRobotPos(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Right
        else if(robotCenter[2] == 90){

            // Reached right of mapArena
            if(robotFront[0] == 14 && robotCenter[0] == 13){
                Log.e(TAG, String.format("End of Arena"));
            }
            else{
                robotFront[0] = robotFront[0] + 1;
                robotCenter[0] = robotCenter[0] + 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
                mdfDecoder.updateRobotPos(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Down
        else if(robotCenter[2] == 180){

            // Reached bottom of mapArena
            if(robotFront[1] == 0 && robotCenter[1] == 1){
                Log.e(TAG, String.format("End of Arena"));
            }
            else{
                robotFront[1] = robotFront[1] - 1;
                robotCenter[1] = robotCenter[1] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
                mdfDecoder.updateRobotPos(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }

        // Facing Left
        else if(robotCenter[2] == 270){

            // Reached left of mapArena
            if(robotFront[0] == 0 && robotCenter[0] == 1){
                Log.e(TAG, String.format("End of Arena"));
            }
            else{
                robotFront[0] = robotFront[0] - 1;
                robotCenter[0] = robotCenter[0] - 1;
                String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
                mdfDecoder.updateRobotPos(robotPositionStr);
                Log.e(TAG,"After moving forward coordinates:" +robotPositionStr);
            }
        }
    }

    public void rotateRight() { //[Right, Down] coordinates

        // Facing Up
        if(robotCenter[2] == 0){
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            robotCenter[2] = 90;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Right
        else if(robotCenter[2] == 90){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1 ;
            robotCenter[2] = 180;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Down
        else if(robotCenter[2] == 180){
            robotFront[0] = robotCenter[0] -1;
            robotFront[1] = robotCenter[1] ;
            robotCenter[2] = 270;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }

        // Facing Left
        else if(robotCenter[2] == 270){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1;
            robotCenter[2] = 0;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating right coordinates:" +robotPositionStr);
        }
    }

    public void rotateLeft(){

        // Facing Up
        if(robotCenter[2] == 0){
            robotFront[0] = robotCenter[0] - 1;
            robotFront[1] = robotCenter[1] ;
            robotCenter[2] = 270;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Right
        else if(robotCenter[2] == 90){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] - 1 ;
            robotCenter[2] = 0;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Down
        else if(robotCenter[2] == 180){
            robotFront[0] = robotCenter[0] + 1;
            robotFront[1] = robotCenter[1] ;
            robotCenter[2] = 90;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }

        // Facing Left
        else if(robotCenter[2] == 270){
            robotFront[0] = robotCenter[0] ;
            robotFront[1] = robotCenter[1] + 1;
            robotCenter[2] = 180;
            String robotPositionStr = String.format("%d,%d,%d",robotCenter[1],robotCenter[0],robotCenter[2]);
            mdfDecoder.updateRobotPos(robotPositionStr);
            Log.e(TAG,"After rotating left coordinates:" +robotPositionStr);
        }
    }
}
