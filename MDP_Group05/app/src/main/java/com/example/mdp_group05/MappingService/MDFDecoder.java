package com.example.mdp_group05.MappingService;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MDFDecoder {

    private final static String TAG ="MDFDecoder";

    // Strings to store the various map descriptor format
    private String robotPositionStr;     // (y-axis, x-axis, orientation)
    private String exploredMapStr;  // Part 1 of MDF string storing unexplored and explored squares
    private String obstaclesStr;    // Part 2 of MDF string storing obstacles and free squares
    private int[] waypoint = new int[2];
    private boolean isWaypointSet = false;
    private int[][] arrowArr = new int[20][15];
    private int numOfArrow = 0;

    // By default all squares are unexplored
    private static int[] mapArray = new int[300]; // Stores the cell type information for the entire grid

    public MDFDecoder(){
        super();
        clearMapArray();
    }

    // Reset the map arena back to default
    public void clearMapArray(){
        mapArray = new int[300];
        robotPositionStr = "1,1,0";
        exploredMapStr = "0000000000000000000000000000000000000000000000000000000000000000000000000000";
        obstaclesStr = "0";
        waypoint = new int[2];
        isWaypointSet = false;
        arrowArr = new int[20][15];
        numOfArrow = 0;
    }

    // Reset Arrow Array
    public void clearArrowArray(){
        arrowArr = new int[20][15];
        numOfArrow = 0;
    }

    // Updates the map using AMDTool
    public void updateDemoMapArray(String obstacleMap){
        mapArray = new int[300];
        // Set all to explored with additional padding infront and back
        exploredMapStr = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        obstaclesStr = obstacleMap;
    }

    // Updates the actual map during exploration mode
    public void updateMapArray(String obstacleMap, String exploredMapStr){
        mapArray = new int[300];
        this.exploredMapStr = exploredMapStr;
        this.obstaclesStr = obstacleMap;
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

    // Update robot position during fastest path & exploration
    public void updateRobotPos(String robotPositionStr){
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
        robotCoordinates[0] = Integer.parseInt(robotCoordinatesArr[0].trim()); // y-axis
        robotCoordinates[1] = Integer.parseInt(robotCoordinatesArr[1].trim()); // x-axis
        robotCoordinates[2] = Integer.parseInt(robotCoordinatesArr[2].trim()); // Orientation
        return robotCoordinates;
    }

    // Converts binary string of the map arena explored into an int array that can be displayed by MapArena
    private int[] decodeExploredMap(){

        int[] binaryArray = new int[300];
        for(int arrPos = 0; arrPos < exploredMapStr.length(); arrPos++){
            if(exploredMapStr.charAt(arrPos) == '1'){
                binaryArray[arrPos] = 1; // Explored
            }else{
                binaryArray[arrPos] = 0; // Unexplored
            }
        }
        return binaryArray;
    }

    // Converts information about obstacles in the arena into an int array that can be displayed by MapArena
    private int[] decodeMapObject(){

        int[] binaryArray = new int[300];
        for(int arrPos = 0; arrPos < obstaclesStr.length(); arrPos++){
            if(obstaclesStr.charAt(arrPos) == '2'){
                binaryArray[arrPos] = 2; // Obstacles
            }else if(obstaclesStr.charAt(arrPos) == '1'){
                binaryArray[arrPos] = 1; // Explored with no obstacles
            }else{
                binaryArray[arrPos] = 0; // Unexplored
            }
        }
        return binaryArray;
    }

    /* Converts the information from both array into a 2D int array that indicates explored, unexplored,
       free and obstacles squares in the arena */
    private int[][] updateMap(int[] exploredMapArr, int[] obstaclesArr){

        int mapArrayPt = 0;
        int[] obstacleMap = obstaclesArr;

        // 0 - Unexplored
        // 1 - Explored with no obstacle
        // 2 - Obstacle
        // 3 - Start Point
        // 4 - Way Point
        // 5 - End Point
        // 6 - Arrow Images

        // Loops through the map arena array to check if the explored square is empty or has an obstacle
        for (int i =0; i< 300; i++){
            // Explored Squares
            if (exploredMapArr[i] == 1) {

                // Obstacles
                if (obstacleMap[i] == 2) {
                    mapArray[i] = 2;
                }

                // Explored with no obstacle
                else {
                    mapArray[i] = 1;
                }
            }
        }

        // Converts to 2D array
        int[][] mapArray2D = new int[20][15];
        for (int i = 0; i < 20; i ++){
            for (int j = 0; j < 15; j++){
                mapArray2D[19-i][j] = mapArray[mapArrayPt++];
            }
        }

        // Way Point
        if(isWaypointSet)
            mapArray2D[waypoint[0]][waypoint[1]] = 4;

        // Set arrow images recognised by the robot
        if(numOfArrow > 0){
            for(int i = 0; i < 20; i++){
                for(int j = 0; j < 15; j++){
                    // If arrow is detected at the square
                    if(arrowArr[i][j] == 1){
                        mapArray2D[i][j] = 6;
                    }
                }
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

    // Updates the waypoint upon touch on the square
    public void updateWaypoint(int x, int y){
        waypoint[0]=19-y;
        waypoint[1]=x;
        isWaypointSet = true;
    }

    // Updates the coordinates of arrow images
    public void updateArrowArr(int x, int y){
        arrowArr[y][x] = 1; // 20 by 15
        numOfArrow++;
        Log.e(TAG, String.format("Number of arrows: %d",numOfArrow));
    }
}
