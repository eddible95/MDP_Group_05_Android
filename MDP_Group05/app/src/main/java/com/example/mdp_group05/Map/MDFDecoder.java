package com.example.mdp_group05.Map;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MDFDecoder {

    // Tag for Logging purpose
    private final static String TAG ="MDFDecoder";

    // Strings to store the various map descriptor format
    private String robotPositionStr;     // (y-axis, x-axis, orientation)
    private String exploredMapStr;  // Part 1 of MDF string storing unexplored and explored squares
    private String obstaclesStr;    // Part 2 of MDF string storing obstacles and free squares
    private int[] waypoint = new int[2];
    private int[] imageCoordinates = new int[300];
    private int numOfImage;
    private boolean isWaypointSet = false;
    private boolean isImageFound = false;

    // By default all squares are unexplored
    private static int[] mapArray = new int[300]; // Stores the cell type information for the entire grid

    public MDFDecoder(){
        super();
        clearMapArray();
    }

    public void clearMapArray(){
        mapArray = new int[300];
        robotPositionStr = "18,1,0";
        exploredMapStr = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
        obstaclesStr = "0";
        waypoint = new int[2];
        isWaypointSet = false;
        isImageFound = false;
        this.numOfImage = 0;
    }

    // Updates the map
    public void updateDemoMapArray(String obstacleMap){
        mapArray = new int[300];
        // Set all to explored with additional padding infront and back
        exploredMapStr = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        obstaclesStr = obstacleMap;
    }

    // Updates robot position with AMDTool
    public void updateDemoRobotPos(String robotPos) {
        // AMDTool reference the top left cell as reference
        JSONObject receive;
        try {
            receive = new JSONObject(robotPos);
            JSONArray positionCoordinates = receive.getJSONArray("robotPosition");
            int x = positionCoordinates.getInt(0);
            int y = positionCoordinates.getInt(1);
            int direction = positionCoordinates.getInt(2);

            // There is a need to add 1 to both x-axis and y-axis to get the center of the robot
            String tempStr = String.format("%s,%s,%s",y+1,x+1,direction);
            Log.e(TAG,tempStr);
            this.robotPositionStr = tempStr;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Update robot position manually by clicking on the squares
    public void updateRobotStartPoint(String robotPositionStr){
        this.robotPositionStr = robotPositionStr;
    }

    /* Merge information from two different MDFs and output the arena in a format that can be displayed
       by MapArena */
    public int[][] decodeMapDescriptor(){

        int[] exploredMapArr;
        int[] obstaclesArr;
        int[][] currentMap;

        exploredMapArr = decodeExploredMap();
        obstaclesArr = decodeMapObject();
        currentMap = updateMap(exploredMapArr,obstaclesArr);
        return currentMap;
    }

    // Converts the robot position from a String into an int array that can be displayed by MapArena
    public int[] decodeRobotPos(){

        int [] robotCoordinates = new int[3];
        String[] robotCoordinatesArr = robotPositionStr.split(",");
        robotCoordinates[0] = Integer.parseInt(robotCoordinatesArr[0].trim());
        robotCoordinates[1] = Integer.parseInt(robotCoordinatesArr[1].trim());
        robotCoordinates[2] = Integer.parseInt(robotCoordinatesArr[2].trim());
        String message = String.format("Robot Position: (%d,%d,%d)",robotCoordinates[0], robotCoordinates[1], robotCoordinates[2]);
        //Log.e(TAG,message);

        return robotCoordinates;
    }

    // Converts information about the arena explored into an int array that can be displayed by MapArena
    private int[] decodeExploredMap(){

        int[] binaryArray = new int[300];
        int arrPos = 0;
        String binaryString;

        //hexString should be 76 characters
        for (int i= 0; i < exploredMapStr.length(); i++){
            binaryString = hexToBinaryConverter(String.valueOf(exploredMapStr.charAt(i)));
            if (i == 0){
                //ignore the first 2 padding bits
                binaryArray[arrPos++] = binaryString.charAt(2) - '0';
                binaryArray[arrPos++] = binaryString.charAt(3) - '0';
            }
            else if (i == exploredMapStr.length()-1){
                //ignore the last 2 padding bits
                binaryArray[arrPos++] = binaryString.charAt(0) - '0';
                binaryArray[arrPos++] = binaryString.charAt(1) - '0';
            }else{
                binaryArray[arrPos++] = binaryString.charAt(0) - '0';
                binaryArray[arrPos++] = binaryString.charAt(1) - '0';
                binaryArray[arrPos++] = binaryString.charAt(2) - '0';
                binaryArray[arrPos++] = binaryString.charAt(3) - '0';
            }
        }
        return binaryArray;
    }

    // Converts information about obstacles in the arena into an int array that can be displayed by MapArena
    private int[] decodeMapObject(){

        int[] binaryArray = new int[obstaclesStr.length()*4];
        int arrPos = 0;
        String binaryString;

        for (int i= 0; i < obstaclesStr.length(); i++){
            // There may b padding at the end depending on the length of the MDF
            binaryString = hexToBinaryConverter(String.valueOf(obstaclesStr.charAt(i)));
            binaryArray[arrPos++] = binaryString.charAt(0) - '0';
            binaryArray[arrPos++] = binaryString.charAt(1) - '0';
            binaryArray[arrPos++] = binaryString.charAt(2) - '0';
            binaryArray[arrPos++] = binaryString.charAt(3) - '0';
        }
        return binaryArray;
    }

    /* Converts the information from both array into a 2D int array that indicates explored, unexplored,
       free and obstacles squares in the arena */
    private int[][] updateMap(int[] exploredMapArr, int[] obstaclesArr){

        int mapArrayPt = 0;
        int[] obstacleMap = checkForExploredSquares(exploredMapArr, obstaclesArr);

        // 0 - Unexplored
        // 1 - Explored with no obstacle
        // 2 - Obstacle
        // 3 - Start Point
        // 4 - Way Point
        // 5 - End Point
        // 6 - Arrow

        // Loop through entire mapArray that is updated upon changes
        for (int i =0; i< 300; i++){
            if (mapArray[i] == 0){
                // Check if previous unexplored square is now explored
                if (exploredMapArr[i] == 1){
                    // Check if the explored square is empty or has an obstacle
                    if (obstacleMap[i] == 0){
                        mapArray[i] = 1;
                    }else{
                        mapArray[i] = 2;
                    }
                }
            }
        }

        // Converts to 2D array
        int[][] mapArray2D = new int[20][15];
        for (int i = 0; i < 20; i ++){
            for (int j = 0; j < 15; j++){
                mapArray2D[i][j] = mapArray[mapArrayPt++];
            }
        }

        // Way Point
        if(isWaypointSet)
            mapArray2D[waypoint[0]][waypoint[1]] = 4;

        if(isImageFound && numOfImage > 0){
            int index;
            for(index = 0; index < numOfImage; index++) {
                mapArray2D[imageCoordinates[index]][imageCoordinates[index + 1]] = 6;
            }
        }

        // Start point
        mapArray2D[17][0] = 3;
        mapArray2D[17][1] = 3;
        mapArray2D[17][2] = 3;
        mapArray2D[18][0] = 3;
        mapArray2D[18][1] = 3;
        mapArray2D[18][2] = 3;
        mapArray2D[19][0] = 3;
        mapArray2D[19][1] = 3;
        mapArray2D[19][2] = 3;

        // End Point
        mapArray2D[0][12] = 5;
        mapArray2D[0][13] = 5;
        mapArray2D[0][14] = 5;
        mapArray2D[1][12] = 5;
        mapArray2D[1][13] = 5;
        mapArray2D[1][14] = 5;
        mapArray2D[2][12] = 5;
        mapArray2D[2][13] = 5;
        mapArray2D[2][14] = 5;

        return mapArray2D;
    }

    private int[] checkForExploredSquares(int[] exploredMapArr, int[] obstaclesArr){
        int[] obstacleMap = new int[300];
        int obstaclesArrPt = 0;
        for (int i =0; i<300; i++){
            if (exploredMapArr[i] == 1){
                obstacleMap[i] = obstaclesArr[obstaclesArrPt++]; // Check for obstacles only if square is explored
            }
        }
        return obstacleMap;
    }

    // Updates the waypoint upon touch on the square
    public void updateWaypoint(int x, int y){
        waypoint[0]=y;
        waypoint[1]=x;
        isWaypointSet = true;
    }

    // Update arrow coordinates
    public void updateArrowImages(int x, int y, int index){
        int arrayIndex;
        numOfImage = index + 1;
        for(arrayIndex =0;arrayIndex < index; arrayIndex++){
            imageCoordinates[index] = y;
            imageCoordinates[index+1] = x;
        }
        isImageFound = true;
    }

    // Converts a hexadecimal string into a binary string
    private String hexToBinaryConverter(String hexadecimal){
        int decimalValue = Integer.parseInt(hexadecimal, 16);
        String binaryString = Integer.toBinaryString(decimalValue);
        switch (binaryString.length()){
            case 1:
                binaryString = "000"+binaryString; // Padding if the binary is 0 to 1
                break;
            case 2:
                binaryString = "00"+binaryString; // Padding if the binary is 10 to 11
                break;
            case 3:
                binaryString = "0"+binaryString; // Padding if the binary is 100 to 111
                break;
            default:
                break;
        }
        return binaryString;
    }

}
