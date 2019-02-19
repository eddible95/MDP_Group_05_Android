package com.example.mdp_group05;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mdp_group05.BluetoothService.Constants;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mdp_group05.BluetoothService.BluetoothFragment.autoUpdate;
import static com.example.mdp_group05.BluetoothService.BluetoothFragment.robotCenter;
import static com.example.mdp_group05.BluetoothService.BluetoothFragment.robotFront;

public class MapArena extends View {

    public static int gridSize;
    private static int cellWidth, cellHeight;

    // Member fields
    private LinearLayout mapView;
    private ArrayList<String> obstaclesCoordinates = new ArrayList<>(); // Format (1,10)
    private ArrayList<String> imageCoordinates = new ArrayList<>(); // Format (1,10)
    private String wayPoint; // Format(1,10)
    private MDFDecoder mapDecoder;

    // Direction the robot is facing
    private boolean faceNorth;
    private boolean faceEast;
    private boolean faceSouth;
    private boolean faceWest;

    // Colour used to draw the 2D-Grid
    private Paint black = new Paint(); // Obstacle
    private Paint grey = new Paint(); // Unexplored area
    private Paint white = new Paint(); // Explored area
    private Paint green = new Paint(); // Start Point
    private Paint red = new Paint(); // Waypoint
    private Paint yellow = new Paint(); // End Point
    private Paint blue = new Paint(); // Robot body color
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

        this.wayPoint ="{,}";
        this.faceNorth = true;
        this.faceEast = false;
        this.faceSouth = false;
        this.faceWest = false;
        this.mapDecoder = new MDFDecoder();
    }

    public void addObstacles(String coordinates) {
        this.obstaclesCoordinates.add(coordinates);
    }

    public void addImage(String coordinates) {
        this.imageCoordinates.add(coordinates);
    }

    public void setWayPoint(String wayPoint) {
        this.wayPoint = wayPoint;
    }

    public boolean isFaceNorth() {
        return faceNorth;
    }

    public boolean isFaceEast() {
        return faceEast;
    }

    public boolean isFaceSouth() {
        return faceSouth;
    }

    public boolean isFaceWest() {
        return faceWest;
    }

    public void setFaceNorth(boolean faceNorth) {
        this.faceNorth = faceNorth;
    }

    public void setFaceEast(boolean faceEast) {
        this.faceEast = faceEast;
    }

    public void setFaceSouth(boolean faceSouth) {
        this.faceSouth = faceSouth;
    }

    public void setFaceWest(boolean faceWest) {
        this.faceWest = faceWest;
    }

    // Draw the cells out
    @Override
    public void onDraw(Canvas canvas) {
        // Get the size for each box
        mapView = getRootView().findViewById(R.id.mapArena);
        int width = mapView.getMeasuredWidth();
        //int height = mapView.getMeasuredHeight();
        gridSize = width / (Constants.MAP_COLUMN + 1);

        //colorGrid(canvas);
        draw2DGrid(canvas);
        colorObstacles(canvas);
        colorWayPoint(canvas);
        displayArrowImage(canvas);
        drawGridLines(canvas);
        drawRobot(canvas);
    }

    private void draw2DGrid(Canvas canvas) {

        int[][] testMap = mapDecoder.decodeMapDescriptor();

        for (int row = 0; row < Constants.MAP_ROW; row++) {
            for (int column = 0; column < Constants.MAP_COLUMN; column++) {
                float left = (column * gridSize);
                float top = (row * gridSize);
                float right = left + gridSize;
                float btm = top + gridSize;

                MapCell cell = new MapCell(left, top, right, btm);
                if (testMap[row][column] == 0){// Unexplored
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (testMap[row][column] == 1){// Explored
                    cell.setCellIsExplored(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
                if (testMap[row][column] == 2){// Obstacles
                    cell.setCellIsObstacle(true);
                    canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                }
            }
        }
    }

    // Drawing lines for the arena grids
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
    private void drawRobot(Canvas canvas) {
        float bodyRadius = (gridSize * 3) / 2;
        float bodyRight = (robotCenter[0] * gridSize) + (gridSize / 2); //Use number of columns
        float bodyDown = (robotCenter[1] * gridSize) + (gridSize / 2); //Use number of rows //18
        canvas.drawCircle(bodyRight, bodyDown, bodyRadius, clear);

        float headRight = (robotFront[0] * gridSize) + (gridSize / 2);
        float headDown = (robotFront[1] * gridSize) + (gridSize / 2); //17
        // Canvas, Color of shape, x-axis to the right, y-axis downwards, width of triangle

        if (faceNorth){
            drawUpTriangle(canvas, blue, headRight, headDown, gridSize);
        }
        if (faceEast){
            drawRightTriangle(canvas, blue, headRight, headDown, gridSize);
        }
        if (faceSouth){
            drawDownTriangle(canvas, blue, headRight, headDown, gridSize);
        }
        if (faceWest){
            drawLeftTriangle(canvas, blue, headRight, headDown, gridSize);
        }
        /*
        if(autoUpdate){
            invalidate();
        }*/
    }

    // Drawing of triangle to indicate head of robot or representing arrow pointing up
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

    // Color selected cell
    private void colorGrid(Canvas canvas) {
        for (int row = 0; row < Constants.MAP_ROW; row++) {
            for (int column = 0; column < Constants.MAP_COLUMN; column++) {
                String coordinates = row + "," + column;
                float left = (column * gridSize);
                float top = (row * gridSize);
                float right = left + gridSize;
                float btm = top + gridSize;
                MapCell cell = new MapCell(left, top, right, btm);
                switch (coordinates) {
                    // Start Point
                    case "17,0":
                    case "17,1":
                    case "17,2":
                    case "18,0":
                    case "18,1":
                    case "18,2":
                    case "19,0":
                    case "19,1":
                    case "19,2":
                        cell.setCellIsStartpoint(true);
                        canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                        break;
                    // End Point
                    case "0,12":
                    case "0,13":
                    case "0,14":
                    case "1,12":
                    case "1,13":
                    case "1,14":
                    case "2,12":
                    case "2,13":
                    case "2,14":
                        cell.setCellIsEndpoint(true);
                        canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));
                        break;
                    default:
                        canvas.drawRoundRect(cell.getRect(), 5, 5, getColour(cell.getCellColor()));

                }
            }
        }
    }

    private void colorObstacles(Canvas canvas) {
        for(String item: obstaclesCoordinates){
            int[] coordinates = stringToCoordinates(item);
            drawCell(coordinates, canvas, 1);
        }
    }

    private void displayArrowImage(Canvas canvas) {
        for(String item: imageCoordinates){
            int[] coordinates = stringToCoordinates(item);
            float triangleX = (coordinates[0] * gridSize) + (gridSize / 2);
            float triangleY = (coordinates[1] * gridSize) + (gridSize / 2);
            drawUpTriangle(canvas,orange, triangleX, triangleY, gridSize);
        }
    }

    private void colorWayPoint(Canvas canvas) {
        int[] coordinates = stringToCoordinates(wayPoint);
        drawCell(coordinates, canvas, 4);
    }

    private void drawCell(int[] coordinates, Canvas canvas, int cellType) {
        float left = (coordinates[0] * gridSize);
        float top = (coordinates[1] * gridSize);
        float right = left + gridSize;
        float btm = top + gridSize;
        MapCell cell = new MapCell(left, top, right, btm);
        switch(cellType){
            case 1:
                cell.setCellIsObstacle(true);
                break;
            case 3:
                cell.setCellIsStartpoint(true);
                break;
            case 4:
                cell.setCellIsWaypoint(true);
                break;
            case 5:
                cell.setCellIsEndpoint(true);
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
                return blue;
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
        int[] coordinates = {999,999,999};
        int index = 0;
        while(m.find()) {
            coordinates[index] = Integer.parseInt(m.group());
            index++;
        }
        return coordinates;
    }
    public void updateDemoArenaMap(String obstacleMapDes){
        mapDecoder.updateDemoMapArray(obstacleMapDes);
    }

    public void updateDemoRobotPos(String robotPos){
        mapDecoder.updateDemoRobotPos(robotPos);
        int[] coordinates = stringToCoordinates(mapDecoder.getRobotPositionStr());
        robotCenter[0] = coordinates[0];
        robotCenter[1] = coordinates[1];
        robotFront[0] = coordinates[0];
        robotFront[1] = coordinates[1]-1;
        switch (coordinates[2]){
            case 0:
                faceNorth = true;
                faceEast = false;
                faceSouth = false;
                faceWest = false;
                break;
            case 1:
                faceNorth = false;
                faceEast = true;
                faceSouth = false;
                faceWest = false;
                break;
            case 2:
                faceNorth = false;
                faceEast = false;
                faceSouth = true;
                faceWest = false;
                break;
            case 3:
                faceNorth = false;
                faceEast = false;
                faceSouth = false;
                faceWest = true;
                break;
            default:
                break;
        }
    }
}
