package com.example.mdp_group05;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mdp_group05.BluetoothService.Constants;

import static com.example.mdp_group05.BluetoothService.BluetoothFragment.robotCenter;
import static com.example.mdp_group05.BluetoothService.BluetoothFragment.robotFront;

public class MapArena extends View {
    private LinearLayout mapView;
    public static int gridSize;

    private Paint black = new Paint(); // Obstacle
    private Paint grey = new Paint(); // Unexplored area
    private Paint white = new Paint(); // Explored area
    private Paint green = new Paint(); // Start Point
    private Paint red = new Paint(); // Waypoint
    private Paint yellow = new Paint(); // End Point
    private Paint blue = new Paint(); // Robot body color
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
        clear.setColor(getResources().getColor(R.color.clear));
    }

    // Draw the cells out
    @Override
    public void onDraw(Canvas canvas) {
        //get the size for each box
        mapView = getRootView().findViewById(R.id.mapArena);
        int width = mapView.getMeasuredWidth();
        int height = mapView.getMeasuredHeight();
        gridSize = (width - 350) / (Constants.MAP_COLUMN + 1);

        colorGrid(canvas);
        drawGridLines(canvas);
        drawRobot(canvas);
    }

    // Drawing lines for the arena grids
    public void drawGridLines(Canvas canvas) {
        black.setStrokeWidth(2);

        //For column lines
        for (int c = 0; c < Constants.MAP_COLUMN + 1; c++) {
            canvas.drawLine((gridSize * c) + 100, 100, (gridSize * c) + 100, (gridSize * Constants.MAP_ROW) + 100, black);
        }

        //For row lines
        for (int r = 0; r < Constants.MAP_ROW + 1; r++) {
            canvas.drawLine(100, (gridSize * r) + 100, (gridSize * Constants.MAP_COLUMN) + 100, (gridSize * r) + 100, black);
        }
    }

    // ROBOT IS DRAWN HERE
    public void drawRobot(Canvas canvas) {
        float bodyradius = (gridSize * 3) / 2;
        float bodyright = (robotCenter[0] * gridSize) + (gridSize / 2) + 100; //Use number of columns
        float bodydown = (robotCenter[1] * gridSize) + (gridSize / 2) + 100; //Use number of rows //18
        canvas.drawCircle(bodyright, bodydown, bodyradius, clear);

        float headright = (robotFront[0] * gridSize) + (gridSize / 2) + 100;
        float headdown = (robotFront[1] * gridSize) + (gridSize / 2) + 100; //17

        //Canvas, Color of shape, x-axis to the right, y-axis downwards, width of triangle
        drawTriangle(canvas, blue, headright, headdown, gridSize);
        invalidate();
    }

    public void drawTriangle(Canvas canvas, Paint paint, float x, float y, int width) {
        int halfWidth = width / 2;

        Path path = new Path();
        path.moveTo(x, y - halfWidth); // Top
        path.lineTo(x - halfWidth, y + halfWidth); // Bottom left
        path.lineTo(x + halfWidth, y + halfWidth); // Bottom right
        path.lineTo(x, y - halfWidth); // Back to Top
        path.close();

        canvas.drawPath(path, paint);
    }

    // color selected cell
    public void colorGrid(Canvas canvas) {

        for (int row = 0; row < Constants.MAP_ROW; row++) {
            for (int column = 0; column < Constants.MAP_COLUMN; column++) {
                String coordinates = row + "," + column;
                float left = (column * gridSize) + 100;
                float top = (row * gridSize) + 100;
                float right = left + gridSize;
                float btm = top + gridSize;
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
                        canvas.drawRoundRect(new RectF(left, top, right, btm), 5, 5, green);
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
                        canvas.drawRoundRect(new RectF(left, top, right, btm), 5, 5, yellow);
                        break;
                    default:
                        canvas.drawRoundRect(new RectF(left, top, right, btm), 5, 5, white);

                }
            }
        }
    }
}
