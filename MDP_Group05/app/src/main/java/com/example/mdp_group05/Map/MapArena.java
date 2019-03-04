package com.example.mdp_group05.Map;

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
    //private ArrayList<String> obstaclesCoordinates = new ArrayList<>(); // Format: (1,10)

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

        this.mdfDecoder = new MDFDecoder();
    }

    /*
    public void addObstacles(String coordinates) {
        this.obstaclesCoordinates.add(coordinates);
    }*/

    public MDFDecoder getMdfDecoder() {
        return mdfDecoder;
    }

    // Draw the entire 2D Arena with the robot, obstacles, startpoint and endpoint
    @Override
    public void onDraw(Canvas canvas) {
        // Gets the size for each box
        mapView = getRootView().findViewById(R.id.mapArena);
        int width = mapView.getMeasuredWidth();
        gridSize = width / (Constants.MAP_COLUMN + 1);

        drawArena(canvas);
        drawGridLines(canvas);
        //drawObstacle(canvas);

        if(autoUpdate){
            invalidate();
        }
    }

    private void drawArena(Canvas canvas){

        int[][] arenaMap = mdfDecoder.decodeMapDescriptor(); // Gets the 2D array of the arena
        int [] robotPositionArr = getRobotCenter();

        for (int column = 0; column < Constants.MAP_COLUMN ; column++) { // 15
            for (int row = 0; row < Constants.MAP_ROW; row++) { // 20
                float left = (column * gridSize);
                float top = (row * gridSize);
                float right = left + gridSize;
                float btm = top + gridSize;

                MapCell cell = new MapCell(left, top, right, btm);
                if (arenaMap[row][column] == 0){// Unexplored
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 1){// Explored
                    cell.setExplored(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 2){// Obstacles
                    cell.setObstacle(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 3){// Start Point
                    cell.setStartpoint(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 4){// Way Point
                    cell.setWaypoint(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 5){// End Point
                    cell.setEndpoint(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (arenaMap[row][column] == 6){// Arrow
                    // Draw the arrow icon at the specific sqaure in the 2D grid
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
            canvas.drawLine((gridSize * c) , 0, (gridSize * c), (gridSize * Constants.MAP_ROW), black);
        }

        // For row lines
        for (int r = 0; r < Constants.MAP_ROW + 1; r++) {
            canvas.drawLine(0, (gridSize * r), (gridSize * Constants.MAP_COLUMN), (gridSize * r), black);
        }
    }

    // Drawing of the robot
    private void drawRobot(Canvas canvas, int[] robotPositionArr) {
        int [] robotCenter = new int[3];
        int [] robotFront = new int[3];

        // Need to swap x-axis and y-axis
        robotCenter[0] = robotPositionArr[0];
        robotCenter[1] = robotPositionArr[1];
        robotCenter[2] = robotPositionArr[2];
        robotFront[0] = robotCenter[0];
        robotFront[1] = robotCenter[1] - 1;
        robotFront[2] = robotCenter[2];
        //String message = String.format("Robot Center (%d,%d)",robotCenter[0], robotCenter[1]);
        //Log.e(TAG,message);

        float bodyRadius = (gridSize * 3) / 2;
        float bodyRight = (robotCenter[0] * gridSize) + (gridSize / 2); //Use number of columns
        float bodyDown = (robotCenter[1] * gridSize) + (gridSize / 2); //Use number of rows //18
        canvas.drawCircle(bodyRight, bodyDown, bodyRadius, clear);

        switch(robotCenter[2]){
            case 0: // North, value = "N"
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 90: // South,  value = "S"
                robotFront[0] = robotCenter[0] + 1;
                robotFront[1] = robotCenter[1];
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 180: // East, value = "E"
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 270: // West, value = "W"
                robotFront[0] = robotCenter[0] - 1;
                robotFront[1] = robotCenter[1];
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            default:
                break;
        }
    }

    /*
    private void drawObstacle(Canvas canvas) {
        for(String item: obstaclesCoordinates){
            int[] coordinates = stringToCoordinates(item);
            drawCell(coordinates, canvas, 1);
        }
    }*/

    public void setWaypoint(int x, int y){
        mdfDecoder.updateWaypoint(x,y);
    }

    /*
    private void drawCell(int[] coordinates, Canvas canvas, int cellType) {
        float left = (coordinates[0] * gridSize);
        float top = (coordinates[1] * gridSize);
        float right = left + gridSize;
        float btm = top + gridSize;
        MapCell cell = new MapCell(left, top, right, btm);
        switch(cellType){
            case 1:
                cell.setObstacle(true);
                break;
            case 3:
                cell.setStartpoint(true);
                break;
            case 4:
                cell.setWaypoint(true);
                break;
            case 5:
                cell.setEndpoint(true);
                break;
            default:
                break;
        }
        canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
    }*/

    // Get the cell colour base on the cell type
    private Paint getColour(int colourSet){
        switch(colourSet){
            case 0: // Unexplored
                return grey;
            case 1: // Explored
                return white;
            case 2: // Obstacle
                return black;
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

    /*
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
    }*/

    public void updateDemoArenaMap(String obstacleMapDes){
        mdfDecoder.updateDemoMapArray(obstacleMapDes);
    }

    public void updateDemoRobotPos(String robotPositionStr) {
        mdfDecoder.updateDemoRobotPos(robotPositionStr);
    }

    public void updateArenaMap(String obstacleMapDes, String exploredMapDes){
        mdfDecoder.updateMapArray(obstacleMapDes, exploredMapDes);
    }

    public void updateRobotStartPoint(String robotPositionStr) {
        mdfDecoder.updateRobotStartPoint(robotPositionStr);
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

    public void clearArenaMap(){
        mdfDecoder.clearMapArray();
    }

    public void updateArrowCoordinates(int[] coordinates){
        mdfDecoder.updateArrowArr(coordinates[0], coordinates[1]);
    }
}
