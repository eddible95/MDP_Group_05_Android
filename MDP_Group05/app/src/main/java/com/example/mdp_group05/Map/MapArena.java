package com.example.mdp_group05.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
    private ArrayList<String> imageCoordinates = new ArrayList<>(); // Format (1,10)
    private String wayPoint; // Format(1,10)

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

        this.wayPoint ="{-999,-999}";
        this.mdfDecoder = new MDFDecoder();
    }

    public void addObstacles(String coordinates) {
        this.obstaclesCoordinates.add(coordinates);
    }

    public void addImage(String coordinates) {
        this.imageCoordinates.add(coordinates);
    }

    public MDFDecoder getMdfDecoder() {
        return mdfDecoder;
    }

    // Draw the entire 2D Arena with the robot, obstacles, startpoint and endpoint
    @Override
    public void onDraw(Canvas canvas) {
        //get the size for each box
        mapView = getRootView().findViewById(R.id.mapArena);
        int width = mapView.getMeasuredWidth();
        gridSize = width / (Constants.MAP_COLUMN + 1);

        drawArena(canvas);
        drawGridLines(canvas);
        //drawObstacle(canvas);
        //drawWaypoint(canvas);
        //drawArrowImage(canvas);

        if(autoUpdate){
            invalidate();
        }
    }

    private void drawArena(Canvas canvas){

        int[][] arenaMap = mdfDecoder.decodeMapDescriptor(); // Gets the 2D array of the arena
        int [] robotPositionArr = getRobotCenter();

        for (int column = 0; column < Constants.MAP_COLUMN ; column++) {
            for (int row = 0; row < Constants.MAP_ROW; row++) {
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
                    float triangleX = (column * gridSize) + (gridSize / 2);
                    float triangleY = (row * gridSize) + (gridSize / 2);
                    drawUpTriangle(canvas,orange, triangleX, triangleY, gridSize);
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

        float bodyradius = (gridSize * 3) / 2;
        float bodyright = (robotCenter[0] * gridSize) + (gridSize / 2); //Use number of columns
        float bodydown = (robotCenter[1] * gridSize) + (gridSize / 2); //Use number of rows //18
        canvas.drawCircle(bodyright, bodydown, bodyradius, clear);

        switch(robotCenter[2]){
            case 0: // North
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 90: // South
                robotFront[0] = robotCenter[0] + 1;
                robotFront[1] = robotCenter[1];
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 180: // East
                robotFront[0] = robotCenter[0];
                robotFront[1] = robotCenter[1] + 1;
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            case 270: // West
                robotFront[0] = robotCenter[0] - 1;
                robotFront[1] = robotCenter[1];
                canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
                break;
            default:
                break;
        }
        //canvas.drawRect((robotFront[0] * gridSize), robotFront[1] * gridSize, (robotFront[0] + 1) * gridSize, (robotFront[1] + 1) * gridSize, blue);
    }

    // Drawing of triangle to represent arrow pointing up
    private void drawUpTriangle(Canvas canvas, Paint paint, float x, float y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x, y - halfWidth); // Top
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom right
        path.lineTo(x, y - halfWidth); // Back to Top
        path.close();

        canvas.drawPath(path, paint);
    }

    // Drawing of down triangle to represent arrow pointing down
    private void drawDownTriangle(Canvas canvas, Paint paint, float x, float y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x, y + halfWidth); // Bottom
        path.lineTo(x - halfWidth, y - halfWidth); // Top Left
        path.lineTo(x + halfWidth, y - halfWidth); // Top Right
        path.lineTo(x, y + halfWidth); // Back to Bottom
        path.close();

        canvas.drawPath(path, paint);
    }

    // Drawing of left triangle to represent arrow pointing left
    private void drawLeftTriangle(Canvas canvas, Paint paint, float x, float y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x - halfWidth, y); // Left
        path.lineTo(x + halfWidth, y - halfWidth); // Top Right
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom Right
        path.lineTo(x - halfWidth, y); // Back to Left
        path.close();

        canvas.drawPath(path, paint);
    }

    // Drawing of right triangle to represent arrow pointing right
    private void drawRightTriangle(Canvas canvas, Paint paint, float x, float y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x + halfWidth, y); // Right
        path.lineTo(x - halfWidth, y - halfWidth); // Top Left
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom Left
        path.lineTo(x + halfWidth, y); // Back to Bottom
        path.close();

        canvas.drawPath(path, paint);
    }

    private void drawObstacle(Canvas canvas) {
        for(String item: obstaclesCoordinates){
            int[] coordinates = stringToCoordinates(item);
            drawCell(coordinates, canvas, 1);
        }
    }

    private void drawArrowImage(Canvas canvas) {
        for(String item: imageCoordinates){
            int[] coordinates = stringToCoordinates(item);
            float triangleX = (coordinates[0] * gridSize) + (gridSize / 2);
            float triangleY = (coordinates[1] * gridSize) + (gridSize / 2);
            drawUpTriangle(canvas,orange, triangleX, triangleY, gridSize);
        }
    }

    private void drawWaypoint(Canvas canvas) {
        int[] coordinates = stringToCoordinates(wayPoint);
        drawCell(coordinates, canvas, 4);
    }

    public void setWaypoint(int x, int y){
        mdfDecoder.updateWaypoint(x,y);
    }

    public void setArrowImage(){
        int index = 0;
        for(String item: imageCoordinates){
            int[] coordinates = stringToCoordinates(item);
            mdfDecoder.updateArrowImages(coordinates[0],coordinates[1], index);
            index++;
        }
    }

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
    }

    // Get the cell colour base on the cell type
    private Paint getColour(int colourSet){
        switch(colourSet){
            case 0: // Explored
                return white;
            case 1: // Obstacle
                return black;
            case 2: // Unexplored
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

    public void updateRobotStartPoint(String robotPositionStr) {
        mdfDecoder.updateRobotStartPoint(robotPositionStr);
    }

    // Gets the robot center position in the form of (x-axis, y-axis, orientation)
    public int[] getRobotCenter(){
        int [] temp = mdfDecoder.decodeRobotPos();
        int [] robotCenter = new int[3];
        robotCenter[0] = temp[1]; // x-axis
        robotCenter[1] = temp[0]; // y-axis
        robotCenter[2] = temp[2]; // orientation
        return robotCenter;
    }

    public void clearArenaMap(){
        mdfDecoder.clearMapArray();
        imageCoordinates = new ArrayList<>();
    }
}
