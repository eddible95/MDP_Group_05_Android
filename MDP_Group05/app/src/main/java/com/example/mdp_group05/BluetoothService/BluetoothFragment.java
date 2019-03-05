package com.example.mdp_group05.BluetoothService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_group05.MDFViewActivity;
import com.example.mdp_group05.Map.MapArena;
import com.example.mdp_group05.R;
import com.example.mdp_group05.Map.Robot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mdp_group05.MainActivity.xTilt;
import static com.example.mdp_group05.MainActivity.yTilt;
import static com.example.mdp_group05.Map.MapArena.gridSize;

public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Flags controlling edit mode and showing of message
    private boolean isSetStartPointMode = false;
    //private boolean isSetObstacleMode = false;
    private boolean isSetWayPointMode = false;
    private boolean showMessage = false;
    private boolean mdfStringSet = false;

    // MapArena Auto Update
    public static boolean autoUpdate = true; // True = Auto, False = Manual
    public static boolean listenForUpdate = false; // True = Manual, False = Auto

    // Boolean for Exploration Mode & Fastest Path Mode
    private boolean stillExplore = false;
    private boolean stillFast = false;

    // For Exploration Mode & Fastest Path Mode Timings
    private long startExploreTime = 0;
    private long startFastTime =0;

    // Strings for reconfigurations
    public static final String MY_PREFERENCE = "MyPref";
    public static final String COMMAND_1 = "cmd1String";
    public static final String COMMAND_2 = "cmd2String";

    // Layout Views
    private ImageButton buttonForward, buttonLeft, buttonRight;
    private Button buttonSend, buttonCmd1, buttonCmd2, buttonReconfigure, buttonAuto, buttonManual, buttonMotion;
    private Button buttonFastestPath, buttonExploration;
    private ListView messageListView;
    private TextView status, robotStatus;
    private TextView fastestPathTimer, explorationTimer;
    private EditText writeMsg;
    private ProgressDialog progressDialog;

    // Member Fields to create the 2D Map View
    private MapArena mapArena;
    private LinearLayout mapArenaLayout;
    private Robot robot;

    // Image Recognition String
    private String imageString="";
    private String mdfString1;
    private String mdfString2;

    // SharedPreference to store data
    private SharedPreferences sharedPreferences;
    private Intent mdfStringViewIntent;

    private String connectedDeviceName = null;  // Connected device
    private ArrayAdapter<String> conversationArrayAdapter; // Array adapter for the conversation thread
    private StringBuffer outStringBuffer; // String buffer for outgoing messages
    private BluetoothAdapter bluetoothAdapter; // Local copy of the bluetooth adapter
    private BluetoothCommunicationService bluetoothService; // Handles all bluetooth connections

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Gets data stored on SharedPreference
        sharedPreferences = getActivity().getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        // Checks if bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    // Create the Option Menu containing bluetooth conenctions
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.option_menu, menu);
    }

    // Enable the bluetooth at the start of the fragment
    @Override
    public void onStart() {
        super.onStart();
        // If bluetooth is not on a intent will be created to request it to be on
        // setupChat() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // If bluetooth is on, proceed to setup the bluetooth communication session
        } else if (bluetoothService == null) {
            setupChat();
        }
    }

    // Stops the BluetoothCommunicationService when leaving the fragment
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Performing this check in onResume() covers the case in which bluetooth was
           not enabled during onStart(), and proceed to enable the bluetooth and
           onResume() will be called when ACTION_REQUEST_ENABLE activity returns */
        if (bluetoothService != null) {
            // If state is STATE_NONE, proceeds to start the bluetooth communication service
            if (bluetoothService.getState() == BluetoothCommunicationService.STATE_NONE) {
                // Starts the Bluetooth communication service
                bluetoothService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(getActivity());
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    // Setup the remaining view
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        buttonSend = view.findViewById(R.id.btnSend);
        buttonForward = view.findViewById(R.id.btnForward);
        buttonLeft = view.findViewById(R.id.btnLeft);
        buttonRight = view.findViewById(R.id.btnRight);
        buttonMotion = view.findViewById(R.id.btnMotionControl);
        buttonCmd1 = view.findViewById(R.id.btn_cmdF1);
        buttonCmd2 = view.findViewById(R.id.btn_cmdF2);
        buttonReconfigure = view.findViewById(R.id.btn_reconfigure);
        buttonAuto = view.findViewById(R.id.btnAuto);
        buttonManual = view.findViewById(R.id.btnManual);
        buttonFastestPath = view.findViewById(R.id.btnFastestPath);
        buttonExploration = view.findViewById(R.id.btnExploration);
        status = view.findViewById(R.id.status);
        robotStatus = view.findViewById(R.id.robotStatus);
        writeMsg = view.findViewById(R.id.writemsg);
        messageListView = view.findViewById(R.id.messageListView);
        fastestPathTimer = view.findViewById(R.id.fastestPathTimer);
        explorationTimer = view.findViewById(R.id.explorationTimer);

        // Disable buttons when not connected
        disableButtons();
        createMapView(view); // Set up the mapArenaLayout view
    }

    // Setting up the UI for Bluetooth Communication
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        conversationArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.message_layout);

        messageListView.setAdapter(conversationArrayAdapter);

        // Initialize the send button with a listener listening for click events
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    String message = String.valueOf(writeMsg.getText());
                    sendMessage(message);
                    writeMsg.setText("");
                }
            }
        });

        // Upon clicking the "Forward" button, vehicle should move forward on AMD
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveForward();
            }
        });

        // Upon clicking the "Rotate Left" button, vehicle should rotate left on AMD
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateLeft();
            }
        });

        // Upon clicking the "Rotate Right" button, vehicle should rotate right on AMD
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateRight();
            }
        });

        buttonMotion.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 1000);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override public void run() {
                    controlByMotionSensor();
                    mHandler.postDelayed(this, 1000);
                }
            };
        });

        // Sends the configured message to the robot
        buttonCmd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getCommand1String = sharedPreferences.getString(COMMAND_1,"NULL");
                sendMessage(getCommand1String);
            }
        });

        // Sends the configured message to the robot
        buttonCmd2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getCommand2String = sharedPreferences.getString(COMMAND_2,"NULL");
                sendMessage(getCommand2String);
            }
        });

        // Opens up the configuration window to reconfigure the 2 commands
        buttonReconfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        // Sets to manual update of the 2D Grid Map
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autoUpdate == false){ // If Manual update is already enabled, click again to update mapArenaLayout when you want to
                    autoUpdate = true;
                    listenForUpdate = true;
                    buttonAuto.setEnabled(true);
                    //sendMessage("sendArena");
                    Toast.makeText(getActivity(), "Manual-Updating", Toast.LENGTH_SHORT).show();
                }
                else{ // Set Manual updating of Arena Map
                    autoUpdate = false;
                    listenForUpdate = true;
                    buttonAuto.setEnabled(true);
                    sendMessage("sendArena");
                    Toast.makeText(getActivity(), "Manual-Update Enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sets to auto update of the 2D Grid Map
        buttonAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Auto updating of Arena Map
                autoUpdate = true;
                buttonAuto.setEnabled(false);
                Toast.makeText(getActivity(), "Auto-Update Enabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Starts Exploration Mode
        buttonExploration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageString="";
                disableDirections();
                byte[] msgBuffer = new byte[3];
                msgBuffer[0] = (byte) 2; // Destination
                msgBuffer[1] = (byte) 1; // Sender
                msgBuffer[2] = (byte) 5; // Payload
                sendByteArr(msgBuffer);
                Toast.makeText(getActivity(), "Start Exploration Mode", Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        robotStatus.setText("Status: Robot starting Exploration Mode");
                    }
                },1000);
                stillExplore=true;
                startExploreTime = System.currentTimeMillis();
                timerHandler1.postDelayed(timerRunnable1, 0);
            }
        });

        // Starts Fastest Path Mode
        buttonFastestPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableDirections();
                byte[] msgBuffer = new byte[3];
                msgBuffer[0] = (byte) 2; // Destination
                msgBuffer[1] = (byte) 1; // Sender
                msgBuffer[2] = (byte) 6; // Payload
                sendByteArr(msgBuffer);
                Toast.makeText(getActivity(), "Start Fastest Path Mode", Toast.LENGTH_SHORT).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        robotStatus.setText("Status: Robot starting Fastest Path Mode");
                    }
                },1000);
                stillFast=true;
                startFastTime = System.currentTimeMillis();
                timerHandler2.postDelayed(timerRunnable2,0);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        bluetoothService = new BluetoothCommunicationService(mHandler);

        // Initialize the buffer for outgoing messages
        outStringBuffer = new StringBuffer();
    }

    // Makes the device discoverable by bluetooth for 300 seconds
    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // Method to send a String message via Bluetooth connection
    private void sendMessage(String message) {
        // Check that the device is connected before trying anything
        if (bluetoothService.getState() != BluetoothCommunicationService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there is actually content to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.writeByteArray(send);

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
            writeMsg.setText(outStringBuffer);
        }
    }

    // Send a single byte to PC or Arduino for different commands
    private void sendByte(byte byteOut){
        // Check that the device is connected before trying anything
        if (bluetoothService.getState() != BluetoothCommunicationService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothService.writeByte(byteOut);
    }

    // Send bytes array of data to PC
    private void sendByteArr(byte[] byteOut){
        // Check that the device is connected before trying anything
        if (bluetoothService.getState() != BluetoothCommunicationService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothService.writeByteArray(byteOut);
    }

    // Disable all buttons when not connected to a robot
    private void disableButtons() {
        buttonMotion.setEnabled(false);
        buttonManual.setEnabled(false);
        buttonAuto.setEnabled(false);
        buttonCmd1.setEnabled(false);
        buttonCmd2.setEnabled(false);
        buttonForward.setEnabled(false);
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
        buttonSend.setEnabled(false);
        buttonFastestPath.setEnabled(false);
        buttonExploration.setEnabled(false);
    }

    // Disable all direction buttons during exploration and fastest path mode
    private void disableDirections(){
        buttonForward.setEnabled(false);
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
        buttonManual.setEnabled(false);
        buttonAuto.setEnabled(false);
    }

    // Enable all buttons when connection with the robot is established
    private void enableButtons(){
        buttonMotion.setEnabled(true);
        buttonManual.setEnabled(true);
        buttonCmd1.setEnabled(true);
        buttonCmd2.setEnabled(true);
        buttonForward.setEnabled(true);
        buttonLeft.setEnabled(true);
        buttonRight.setEnabled(true);
        buttonSend.setEnabled(true);
        buttonFastestPath.setEnabled(true);
        buttonExploration.setEnabled(true);
        buttonManual.setEnabled(true);
        buttonAuto.setEnabled(true);
    }
    // Handles clicking of the option menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connectSecure:
                Intent connectSecureIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(connectSecureIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.connectInsecure:
                Intent connectInsecureIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(connectInsecureIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.disconnect:
                bluetoothService.stop();
                return true;
            case R.id.makeDiscoverable:
                ensureDiscoverable();
                return true;
            case R.id.showMessages:
                if (!showMessage){
                    messageListView.setVisibility(View.VISIBLE);
                    mapArena.setVisibility(View.INVISIBLE);
                    buttonFastestPath.setVisibility(View.INVISIBLE);
                    buttonExploration.setVisibility(View.INVISIBLE);
                    fastestPathTimer.setVisibility(View.INVISIBLE);
                    explorationTimer.setVisibility(View.INVISIBLE);
                    showMessage = true;
                    isSetStartPointMode = false;
                    //isSetObstacleMode = false;
                    isSetWayPointMode = false;
                } else{
                    messageListView.setVisibility(View.INVISIBLE);
                    mapArena.setVisibility(View.VISIBLE);
                    buttonFastestPath.setVisibility(View.VISIBLE);
                    buttonExploration.setVisibility(View.VISIBLE);
                    fastestPathTimer.setVisibility(View.VISIBLE);
                    explorationTimer.setVisibility(View.VISIBLE);
                    showMessage = false;
                    isSetStartPointMode = false;
                    //isSetObstacleMode = false;
                    isSetWayPointMode = false;
                }
                return true;
            case R.id.setStartPoint:
                if (isSetStartPointMode) {
                    removeTouchListener(getView());
                    isSetStartPointMode = false;
                    //isSetObstacleMode = false;
                    isSetWayPointMode = false;
                    Toast.makeText(getActivity(), "Exiting Edit Mode", Toast.LENGTH_SHORT).show();
                } else {
                    addTouchListener(getView());
                    isSetStartPointMode = true; // Prevent both setting of startpoint and obstacles at the same time
                    //isSetObstacleMode = false;
                    isSetWayPointMode = false;
                    Toast.makeText(getActivity(), "Entering Edit Mode", Toast.LENGTH_SHORT).show();
                }
                return true;
            /*case R.id.setObstacles:
                if (isSetObstacleMode) {
                    removeTouchListener(getView());
                    isSetObstacleMode = false;
                    isSetStartPointMode = false;
                    isSetWayPointMode = false;
                    Toast.makeText(getActivity(), "Exiting Edit Mode", Toast.LENGTH_SHORT).show();
                } else {
                    addTouchListener(getView());
                    isSetObstacleMode = true;
                    isSetStartPointMode = false;
                    isSetWayPointMode = false;
                    Toast.makeText(getActivity(), "Entering Edit Mode", Toast.LENGTH_SHORT).show();
                }
                return true;*/
            case R.id.setWayPoint:
                if (isSetWayPointMode) {
                    removeTouchListener(getView());
                    //isSetObstacleMode = false;
                    isSetStartPointMode = false;
                    isSetWayPointMode = false;
                    Toast.makeText(getActivity(), "Exiting Edit Mode", Toast.LENGTH_SHORT).show();
                } else {
                    addTouchListener(getView());
                    isSetWayPointMode = true;
                    //isSetObstacleMode = false;
                    isSetStartPointMode = false;
                    Toast.makeText(getActivity(), "Entering Edit Mode", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.clearMap:
                mapArena.clearArenaMap();
                return true;
            case R.id.showMDFStrings:
                if(mdfStringSet) {
                    startActivity(mdfStringViewIntent);
                    return true;
                } else{
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void controlByMotionSensor() {
        if (xTilt > 2.5){ //Left Tilt
            rotateLeft();
        }
        else if (xTilt < -2.5){ //Right Tilt
            rotateRight();
        }
        if (yTilt > 2.5) { // Forward Tilt
            moveForward();
        }
        else if (yTilt < -2.5){ //Back Tilt
            //No command for now
        }
    }

    // Received coordinate on the screen when user touch on the 2D-Grid
    private void addTouchListener(View view) {
        LinearLayout arenaLayout = view.findViewById(R.id.mapArena);
        arenaLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (isSetStartPointMode) {
                    int mapX = x/gridSize;
                    int mapY = 19-(y/gridSize);
                    if (mapX == 0 || mapX >= 14 || mapY >=19 ||mapY == 0){ // Accounts for parameter of 2D-Grid
                        Toast.makeText(getActivity(),"Cannot place startpoint here" , Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String robotPositionStr = String.format("%d,%d,%d",mapY,mapX,0);
                        mapArena.updateRobotStartPoint(robotPositionStr);
                        String message = String.format("Start Coordinate (%d,%d)",mapX, mapY);
                        sendMessage(message);
                    }
                }/*
                if (isSetObstacleMode){
                    int mapX = x/gridSize;
                    int mapY = y/gridSize;
                    if (mapX > 14 || mapY > 19) { // Handles within grid only
                        Toast.makeText(getActivity(), "Cannot place obstacles there", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mapArena.addObstacles(String.format("(%d,%d)", mapX, mapY)); // Add obstacles coordinates to the arena
                        //String message = String.format("Coordinate: (%d, %d), GridSize %d",mapX,mapY,gridSize);
                        //Toast.makeText(getActivity(),message , Toast.LENGTH_SHORT).show();
                    }
                }*/

                if (isSetWayPointMode){
                    int mapX = x/gridSize;
                    int mapY = 19-(y/gridSize);
                    if (mapX > 14 || mapY > 19) { // Handles within grid only
                        Toast.makeText(getActivity(),"Cannot place waypoint there" , Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mapArena.setWaypoint(mapX, mapY);  // Update waypoint coordinates to the arena
                        Log.e(TAG,String.format("X:%d, Y:%d",mapX, mapY));

                        byte[] msgBuffer = new byte[5];
                        msgBuffer[0] = (byte) 2; // Sender_Addr
                        msgBuffer[1] = (byte) 1; // Receiver Addr
                        msgBuffer[2] = (byte) 7; // Command
                        msgBuffer[3] = (byte) mapX;
                        msgBuffer[4] = (byte) mapY;
                        sendByteArr(msgBuffer);
                    }
                }
                return false;
            }
        });
    }

    // Preventing setting of StartPoint
    private void removeTouchListener(View view) {
        LinearLayout mapArena = view.findViewById(R.id.mapArena);
        mapArena.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    // Drawing of the 2D-Grid
    private void createMapView(View view) {
        mapArena = new MapArena(getContext());
        int [] robotCenterPosition = mapArena.getRobotCenter();
        int [] robotFrontPosition = new int[3];
        robotFrontPosition[0] = robotCenterPosition[0];
        robotFrontPosition[1] = robotCenterPosition[1] - 1;
        robotFrontPosition[2] = robotCenterPosition[2];
        robot = new Robot(robotFrontPosition,robotCenterPosition);
        mapArenaLayout = view.findViewById(R.id.mapArena);
        mapArenaLayout.addView(mapArena);
    }

    // Handler that listens and handle different information from the BluetoothCommunicationService
    private final Handler mHandler = new Handler() {
        @SuppressLint("StringFormatInvalid")
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommunicationService.STATE_CONNECTED:
                            status.setText("Status: Connected");
                            robotStatus.setText("Status: Robot Ready For Action");
                            conversationArrayAdapter.clear();
                            enableButtons();
                            break;
                        case BluetoothCommunicationService.STATE_CONNECTING:
                            status.setText("Status: Connecting..");
                            robotStatus.setText("Status: Robot Not Connected");
                            break;
                        case BluetoothCommunicationService.STATE_LISTENING:
                            status.setText("Status: Listening...");
                            robotStatus.setText("Status: Robot Not Connected");
                            break;
                        case BluetoothCommunicationService.STATE_NONE:
                            status.setText("Status: No Connected");
                            robotStatus.setText("Status: Robot Not Connected");
                            disableButtons();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // Construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    conversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_WRITE_BYTE:
                    byte writeByte = (byte) msg.obj;
                    // Construct a string from the buffer
                    String binaryString ="";
                    binaryString = binaryString + String.format("%8s", Integer.toBinaryString(writeByte & 0xFF)).replace(' ', '0');
                    Log.e(TAG,binaryString);
                    conversationArrayAdapter.add("Me:  " + binaryString);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct a string from the buffer
                    binaryString ="";
                    Log.e(TAG,new String(readBuf));
                    // Binary String
                    for (int i = 0; i<readBuf.length; i++) {
                        binaryString = binaryString + String.format("%8s", Integer.toBinaryString(readBuf[i] & 0xFF)).replace(' ', '0');
                    }
                    Log.e(TAG,String.format("Binary String: %s", binaryString));
                    conversationArrayAdapter.add(connectedDeviceName + ":  " + binaryString);

                    // Exploration Mode
                    if(readBuf[1] == 0b00001000) { // Value 8
                        updateMap(readBuf);
                    }

                    // Fastest Path Mode
                    if(readBuf[1] == 0b00001101) { // Value 13
                        updateRobotPos(readBuf);
                    }

                    // Arrow Image Recognition String
                    if(readBuf[1] == 0b00001110) { // Value 14
                        updateImageString(readBuf);
                    }

                    // Update and display the resulting MDF Strings
                    if(readBuf[1] == 0b00001111){ // Value 15
                        updateMDFString(new String(readBuf));
                        Log.e(TAG, String.format("Reading in MDF Strings"));
                    }

                    // End of Fastest Path Mode
                    if(readBuf[1] == 0b00010000){ // Value 16
                        stillFast = false; // Ends the timer
                        robotStatus.setText("Status: Fastest Path Mode Ended");
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                robotStatus.setText("Status: Robot Ready for Action");
                            }
                        },2000);
                    }
                    updateStatus(new String(readBuf));
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // Save the connected device's name
                    connectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    // Handler for exploration timer function
    Handler timerHandler1 = new Handler();
    Runnable timerRunnable1 = new Runnable() {
        @Override
        public void run() {
            if(!stillExplore){
                return;
            }
            long millis = System.currentTimeMillis() - startExploreTime;
            int seconds = (int) (millis / 1000.0);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            explorationTimer.setText("Exploration Time : " + String.format("%d:%d", ((int) minutes), ((int) seconds)));

            timerHandler1.postDelayed(this, 0);
        }
    };

    // Handler for fastest path timer function
    Handler timerHandler2 = new Handler();
    Runnable timerRunnable2 = new Runnable() {
        @Override
        public void run() {
            if(!stillFast){
                return;
            }
            long millis = System.currentTimeMillis() - startFastTime;
            int seconds = (int) (millis / 1000.0);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            fastestPathTimer.setText("Fastest Path Time : "+String.format("%d:%d", ((int)minutes), ((int)seconds)));
            timerHandler2.postDelayed(this, 0);
        }
    };

    // Reads the message sent from robot and update it's current status and location on Grid
    public void updateStatus(String readMessage){
        if (readMessage.contains("turning right")) {
            robot.rotateRight();
            String status = String.format("Status: Robot Rotating Right at (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
            robotStatus.setText(status);
        }
        else if (readMessage.contains("turning left")) {
            robot.rotateLeft();
            String status = String.format("Status: Robot Rotating Left at (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
            robotStatus.setText(status);
        }
        else if (readMessage.contains("moving forward")) {
            robot.moveForward();
            String status = String.format("Status: Robot Moving Forward from (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
            robotStatus.setText(status);
        }

        // Simulate the receiving of an arrow information from AMDTool
        else if (readMessage.contains("Arrow")){
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(readMessage);
            int [] coordinatesArr = new int[2];
            int index = 0;
            while(m.find()) {
                coordinatesArr[index] = Integer.parseInt(m.group());
                index++;
            }
            mapArena.updateArrowCoordinates(coordinatesArr);
        }
        // Updates the arena information from AMDTool
        else if(readMessage.contains("grid")) {
            try {
                Log.e(TAG,"MDF: "+readMessage);
                JSONObject obj = new JSONObject(readMessage);
                String demoObstacleMDF  = obj.getString("grid");
                mdfStringViewIntent = new Intent(getContext(), MDFViewActivity.class);
                mdfStringViewIntent.putExtra("MDFString1", demoObstacleMDF);
                mdfStringSet = true;

                if(autoUpdate==true||listenForUpdate==true) {
                    // Updates obstacle mapArenaLayout
                    mapArena.updateDemoArenaMap(demoObstacleMDF);
                    mapArena.invalidate();
                    listenForUpdate=false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Updates the position of the robot from AMDTool
        else if(readMessage.contains("robotPosition")){
            if(autoUpdate==true) {
                //update robot position
                mapArena.updateDemoRobotPos(readMessage);
                mapArena.invalidate();
            }
        }
    }

    private void moveForward(){
        sendMessage("f");  //Defaulted at "f" in AMD
        byte byteOut = 0b00001001; // Value 9
        sendByte(byteOut);
        progressDialog.setMessage("Moving Forward");
        progressDialog.show();
        robot.moveForward();
        String status = String.format("Status: Robot Moving Forward from (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
        robotStatus.setText(status);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                robotStatus.setText("Status: Robot Ready for Action");
            }
        }, 1000);
    }

    private void rotateLeft(){
        sendMessage("tl"); //Defaulted at "tl" in AMD
        byte byteOut = 0b00001010; // Value 10
        sendByte(byteOut);
        progressDialog.setMessage("Rotating Left");
        progressDialog.show();
        robot.rotateLeft();
        String status = String.format("Status: Robot Rotating Left at (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
        robotStatus.setText(status);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                robotStatus.setText("Status: Robot Ready for Action");
            }
        },1000);
    }

    private void rotateRight(){
        sendMessage("tr"); //Defaulted at "tr" in AMD
        byte byteOut = 0b00001011; // Value 11
        sendByte(byteOut);
        progressDialog.setMessage("Rotating Right");
        progressDialog.show();
        robot.rotateRight();
        String status = String.format("Status: Robot Rotating Right at (%d,%d)",mapArena.getRobotCenter()[0],mapArena.getRobotCenter()[1]);
        robotStatus.setText(status);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                robotStatus.setText("Status: Robot Ready for Action");
            }
        }, 1000);
    }

    // Reading the position of the robot during Fastest Path Mode
    public void updateRobotPos(byte[] buffRead){
        // Convert from byte to string the robot's direction
        String robotDirection="";
        switch(buffRead[4]){
            case 0b00000000: // "N"
                robotDirection = "0";
                break;
            case 0b00000001: // "E"
                robotDirection = "90";
                break;
            case 0b00000010: // "S"
                robotDirection = "180";
                break;
            case 0b00000011: // "W"
                robotDirection = "270";
                break;
            default:
                break;
        }
        String robotPostStr = String.format("%d,%d,%s",buffRead[2], buffRead[3],robotDirection);
        Log.e(TAG,robotPostStr);
        mapArena.updateRobotPos(robotPostStr);
    }

    // Reading and decoding of Exploration Map and Obstacles Map from Algo during Exploration Mode
    public void updateMap(byte[] buffRead){
        String robotPostStr = "";
        String exploredMapStr ="";
        String tmpObstaclesMapStr="";
        String obstaclesStr = "";
        int bytePosition;

        // Convert from byte to string the robot's direction
        String robotDirection="";
        switch(buffRead[4]){
            case 0b00000000: // "N"
                robotDirection = "0";
                break;
            case 0b00000001: // "E"
                robotDirection = "90";
                break;
            case 0b00000010: // "S"
                robotDirection = "180";
                break;
            case 0b00000011: // "W"
                robotDirection = "270";
                break;
            default:
                break;
        }
        robotPostStr = robotPostStr + String.format("%d,%d,%s",buffRead[2], buffRead[3],robotDirection);
        mapArena.updateRobotPos(robotPostStr); // Format (1,1,90)

        // Extract out Explored Map String
        for (bytePosition = 5; bytePosition < 43; bytePosition++){
            exploredMapStr = exploredMapStr + String.format("%8s", Integer.toBinaryString(buffRead[bytePosition] & 0xFF)).replace(' ', '0');
        }

        // Remove padding of 1's in Explored Map String resulting in a 300-bits binary string
        exploredMapStr = exploredMapStr.substring(2, exploredMapStr.length()-2);

        // Extract out Obstacles Map String
        //for(int i = 44; i <= bytePosition+byteCounter; i++){
        for(bytePosition = 43; bytePosition < 81; bytePosition++){
            tmpObstaclesMapStr = tmpObstaclesMapStr + String.format("%8s", Integer.toBinaryString(buffRead[bytePosition] & 0xFF)).replace(' ', '0');
        }

        //1st string: 1111011, Count number of 1s
        //2nd string: 1100X00, When the 1st string is 0, skip reading second string for that bit
        //3rd string: 2211011 where 2 is obstacle, 1 is explored, 0 is unexplored, this will be the obstacle map

        int counter1 = 0;
        int counter2 = 0;

        for (;counter1 < 300; counter1++){
            if(exploredMapStr.charAt(counter1) == '1'){
                if(tmpObstaclesMapStr.charAt(counter2) == '1'){
                    obstaclesStr = obstaclesStr + "2"; // Explored with obstacles
                } else if (tmpObstaclesMapStr.charAt(counter2) == '0') {
                    obstaclesStr = obstaclesStr + "1"; // Explored with no obstacles
                }
                counter2++;
            }else{
                obstaclesStr = obstaclesStr + "0"; // Unexplored
            }
        }
        mapArena.updateArenaMap(obstaclesStr, exploredMapStr);
        Log.e(TAG, String.format("Explored Map String: %s",exploredMapStr));
        Log.e(TAG,String.format("%d", exploredMapStr.length()));
        Log.e(TAG, String.format("Tmp Obstacles String: %s",tmpObstaclesMapStr));
        Log.e(TAG,String.format("%d", tmpObstaclesMapStr.length()));
        Log.e(TAG, String.format("Obstacles String: %s",obstaclesStr));
        Log.e(TAG,String.format("%d", obstaclesStr.length()));
    }

    // Updates arrow on the arena whenever it is detected and update the Image Recognition String in MDFViewActivity
    public void updateImageString(byte[] buffRead){
        int xCoordinates = (int) buffRead[2];
        int yCoordinates = 19 - (int) buffRead[3];
        int[] arrowCoordinates = new int[2];
        arrowCoordinates[0] = xCoordinates;
        arrowCoordinates[1] = yCoordinates;
        // Convert from byte to string the robot's direction
        String robotDirection="";
        switch(buffRead[4]){
            case 0b00000000: // "N"
                robotDirection = "U";
                break;
            case 0b00000001: // "E"
                robotDirection = "R";
                break;
            case 0b00000010: // "S"
                robotDirection = "D";
                break;
            case 0b00000011: // "W"
                robotDirection = "L";
                break;
            default:
                break;
        }
        mapArena.updateArrowCoordinates(arrowCoordinates);
        if(imageString == ""){
            imageString = imageString + String.format("(%d,%d,%s)",xCoordinates,yCoordinates,robotDirection);
        }else{
            imageString = imageString + String.format(", (%d,%d,%s)",xCoordinates,yCoordinates,robotDirection);
        }
        mdfStringViewIntent = new Intent(getContext(), MDFViewActivity.class);
        mdfStringViewIntent.putExtra("MDFString1", mdfString1);
        mdfStringViewIntent.putExtra("MDFString2", mdfString2);
        mdfStringViewIntent.putExtra("ImageString", imageString);
        mdfStringSet = true;
    }

    // Reads the String MDF String from Algo and display it in MDFViewActivity
    public void updateMDFString(String buffRead){
        stillExplore=false; // End timer
        String mdfStringArr[];
        mdfStringArr = buffRead.split(":");
        mdfString1 = mdfStringArr[1];
        mdfString2 = mdfStringArr[2];
        mdfStringViewIntent = new Intent(getContext(), MDFViewActivity.class);
        mdfStringViewIntent.putExtra("MDFString1", mdfString1);
        mdfStringViewIntent.putExtra("MDFString2", mdfString2);
        mdfStringViewIntent.putExtra("ImageString", imageString);
        mdfStringSet = true;
        robotStatus.setText("Status: Exploration Mode Ended");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                robotStatus.setText("Status: Robot Ready for Action");
            }
        },2000);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    // Called upon to establish connection with a bluetooth device
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothService.connect(device, secure);
    }
}
