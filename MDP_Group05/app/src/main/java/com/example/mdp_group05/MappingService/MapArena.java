package com.example.mdp_group05.MappingService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mdp_group05.BluetoothService.Constants;
import com.example.mdp_group05.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mdp_group05.BluetoothService.BluetoothFragment.autoUpdate;

public class MapArena extends View {

    public static int gridSize;
    private static String TAG = "MapArena";

    // Member fields
    private LinearLayout mapView;
    private ArrayList<String> obstaclesCoordinates = new ArrayList<>(); // Format: (1,10)

    private MDFDecoder mdfDecoder;

    private Paint black = new Paint(); // Obstacle
    private Paint grey = new Paint(); // Unexplored area
    private Paint white = new Paint(); // Explored area
    private Paint green = new Paint(); // Start Point
    private Paint red = new Paint(); // Waypoint
    private Paint yellow = new Paint(); // End Point
    private Paint blue = new Paint(); // Robot's head
    private Paint orange = new Paint(); // Arrow
    private Paint clear = new Paint(); // Test
    private Paint light_white = new Paint(); // Test

    public MapArena(Context context) {
        super(context);

        black.setColor(getResources().getColor(R.color.black));
        grey.setColor(getResources().getColor(R.color.grey));
        white.setColor(getResources().getColor(R.color.white));
        green.setColor(getResources().getColor(R.color.green));
        red.setColor(getResources().getColor(R.color.red));
        yellow.setColor(getResources().getColor(R.color.yellow));
        blue.setColor(getResources().getColor(R.color.blue));
        orange.setColor(getResources().getColor(R.color.orange));
        clear.setColor(getResources().getColor(R.color.clear));
        light_white.setColor(getResources().getColor(R.color.light_white));
        mdfDecoder = new MDFDecoder();
    }

    public void addObstacles(String coordinates) {
        this.obstaclesCoordinates.add(coordinates);
    }

    public MDFDecoder getMdfDecoder(){
        return this.mdfDecoder;
    }

    // Draw the entire 2D Arena with the robot, obstacles, startpoint and endpoint
    @Override
    public void onDraw(Canvas canvas) {
        // Gets the size for each box
        mapView = getRootView().findViewById(R.id.mapArena);
        int width = mapView.getMeasuredWidth();
        gridSize = width / (Constants.MAP_COLUMN + 1);

        // Draws the 2D grid of the arena
        drawArena(canvas);
        drawGridLines(canvas);
        drawObstacle(canvas);


        if(autoUpdate){
            invalidate();
        }
    }

    // Draws out the 2D arena with the robot
    private void drawArena(Canvas canvas){

        // Gets the 2D array of the arena by decoding the MDF Strings received
        int[][] arenaMap = mdfDecoder.decodeMapDescriptor();
        // Gets the robot position
        int [] robotPositionArr = getRobotCenter();

        // Iterates through the 2D array to draw the obstacles, startpoint, endpoint, waypoint, robot and arrow
        for (int column = 0; column < Constants.MAP_COLUMN ; column++) { // 15
            for (int row = 0; row < Constants.MAP_ROW; row++) { // 20
                float left = (column * gridSize);
                float top = (row * gridSize);
                float right = left + gridSize;
                float btm = top + gridSize;

                MapCell cell = new MapCell(left, top, right, btm);

                // Unexplored
                if (arenaMap[row][column] == 0){
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }

                // Explored
                if (arenaMap[row][column] == 1){
                    cell.setExplored();
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                    //Drawable myDrawable =  getResources().getDrawable(R.drawable.concrete);
                    //Bitmap grassBM = ((BitmapDrawable) myDrawable).getBitmap();
                    //canvas.drawBitmap(grassBM, null, cell.getRect(),white);
                }

                // Obstacles
                if (arenaMap[row][column] == 2){
                    //cell.setObstacle();
                    //canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                    // Draw an obstacles in the 2D grid
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.monster );
                    Bitmap concreteBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(concreteBM, null, cell.getRect(),white);
                }

                // Start Point
                if (arenaMap[row][column] == 3){
                    //cell.setStartpoint();
                    //canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.grass);
                    Bitmap brickBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(brickBM, null, cell.getRect(),white);
                }

                // Way Point
                if (arenaMap[row][column] == 4){
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.waypointicon);
                    Bitmap waypointBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(waypointBM, null, cell.getRect(),white);
                }

                // End Point
                if (arenaMap[row][column] == 5){
                    //cell.setEndpoint();
                    //canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.brick);
                    Bitmap brickBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(brickBM, null, cell.getRect(),white);
                }

                // Arrow
                if (arenaMap[row][column] == 6){
                    // Draw the arrow icon at the specific square in the 2D grid
                    Drawable myDrawable =  getResources().getDrawable(R.drawable.ic_arrow_obstacle);
                    Bitmap arrowBM = ((BitmapDrawable) myDrawable).getBitmap();
                    canvas.drawBitmap(arrowBM, null, cell.getRect(),white);
                }
            }
        }
        drawRobot(canvas, robotPositionArr);
    }

    // Drawing lines for the mapArena grids
    private void drawGridLines(Canvas canvas) {
        black.setStrokeWidth(2);

        // For column lines
        for (int c = 0; c < Constants.MAP_COLUMN + 1; c++) {
            canvas.drawLine((gridSize * c) , 0, (gridSize * c), (gridSize * Constants.MAP_ROW), clear);
        }

        // For row lines
        for (int r = 0; r < Constants.MAP_ROW + 1; r++) {
            canvas.drawLine(0, (gridSize * r), (gridSize * Constants.MAP_COLUMN), (gridSize * r), clear);
        }
    }

    // Drawing of the robot
    private void drawRobot(Canvas canvas, int[] robotPositionArr) {
        int [] robotCenter = new int[3];
        int [] robotFront = new int[3];

        robotCenter[0] = robotPositionArr[0];
        robotCenter[1] = robotPositionArr[1];
        robotCenter[2] = robotPositionArr[2];
        robotFront[0] = robotCenter[0];
        robotFront[1] = robotCenter[1] - 1;
        robotFront[2] = robotCenter[2];

//        MapCell cell = new MapCell((robotCenter[0]*gridSize)+(gridSize/2)-gridSize, (robotCenter[1]*gridSize)+(gridSize/2)-gridSize, (robotCenter[0]*gridSize)+(gridSize/2)+(gridSize*2)-gridSize, (robotCenter[1]*gridSize)+(gridSize/2)+(gridSize*2)-gridSize);
//        Drawable myDrawable =  getResources().getDrawable(R.drawable.thomas_grass);
//        Bitmap brickBM = ((BitmapDrawable) myDrawable).getBitmap();
//        canvas.drawBitmap(brickBM,null,cell.getRect(),white);


        float bodyRadius = (gridSize * 9) / 7;
        float bodyRight = (robotCenter[0] * gridSize) + (gridSize / 2); //Use number of columns
        float bodyDown = (robotCenter[1] * gridSize) + (gridSize / 2); //Use number of rows
        canvas.drawCircle(bodyRight, bodyDown, bodyRadius, orange);

        // Set the robot's head to the correct orientation
        switch(robotCenter[2]){
            case 0: // North, value = "N"
                break;
            case 90: // South,  value = "S"
                robotFront[0] = robotCenter[0] + 1;
                robotFront[1] = robotCenter[1];
                break;
            case 180: // East, value = "E"
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                break;
            case 270: // West, value = "W"
                robotFront[0] = robotCenter[0] - 1;
                robotFront[1] = robotCenter[1];
                break;
            default:
                break;
        }
        canvas.drawRect((robotFront[0] * gridSize)+(gridSize/4), (robotFront[1] * gridSize)+(gridSize/4), ((robotFront[0] + 1) * gridSize)-(gridSize/4), ((robotFront[1] + 1) * gridSize)-(gridSize/4), blue);
    }

    // Draw obstacles manually selected by user
    private void drawObstacle(Canvas canvas) {
        for(String item: obstaclesCoordinates){
            int[] coordinates = stringToCoordinates(item);
            float left = (coordinates[0] * gridSize);
            float top = (coordinates[1] * gridSize);
            float right = left + gridSize;
            float btm = top + gridSize;
            MapCell cell = new MapCell(left, top, right, btm);
            Drawable myDrawable =  getResources().getDrawable(R.drawable.monster);
            Bitmap monsterBM = ((BitmapDrawable) myDrawable).getBitmap();
            canvas.drawBitmap(monsterBM, null, cell.getRect(),white);
        }
    }

    // Get the cell colour base on the cell type
    private Paint getColour(int colourSet){
        switch(colourSet){
            case 0: // Unexplored
                return black;
            case 1: // Explored
                return white;
            case 2: // Obstacle
                return grey;
            case 3: // Start Point
                return green;
            case 4: // Way Point
                return red;
            case 5: // End Point
                return yellow;
            default: // Use value 6
                return clear;
        }
    }

    // Using regular expression to retrieve the coordinates
    private int[] stringToCoordinates(String item) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(item);
        int[] coordinates = new int[3];
        int index = 0;
        while(m.find()) {
            coordinates[index] = Integer.parseInt(m.group());
            index++;
        }
        return coordinates;
    }

    public void updateDemoArenaMap(String obstacleMapDes){
        mdfDecoder.updateDemoMapArray(obstacleMapDes);
    }

    public void updateDemoRobotPos(String robotPositionStr) {
        mdfDecoder.updateDemoRobotPos(robotPositionStr);
    }

    public void updateArenaMap(String obstacleMapDes, String exploredMapDes){
        mdfDecoder.updateMapArray(obstacleMapDes, exploredMapDes);
    }

    public void updateRobotPos(String robotPositionStr) {
        mdfDecoder.updateRobotPos(robotPositionStr);
    }

    // Gets the robot center position in the form of (x-axis, y-axis, orientation)
    public int[] getRobotCenter(){
        int [] temp = mdfDecoder.decodeRobotPos();
        int [] robotCenter = new int[3];
        robotCenter[0] = temp[1]; // x-axis (1)
        robotCenter[1] = 19-temp[0]; // y-axis (1)
        robotCenter[2] = temp[2]; // orientation (90)
        return robotCenter;
    }

    // Sets the arena map back to default
    public void clearArenaMap(){
        obstaclesCoordinates = new ArrayList<>();
        mdfDecoder.clearMapArray();
    }

    // Clears arrow
    public void clearArrowArray(){
        mdfDecoder.clearArrowArray();
    }

    // Updates the coordinates of the newly set waypoint
    public void setWaypoint(int x, int y){
        mdfDecoder.updateWaypoint(x,y);
    }

    // Updates the coordinates of the arrow images detected
    public void updateArrowCoordinates(int[] coordinates){
        mdfDecoder.updateArrowArr(coordinates[0], coordinates[1]);
    }
}
