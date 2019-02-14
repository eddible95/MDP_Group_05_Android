package com.example.mdp_group05.BluetoothService;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_group05.MapArena;
import com.example.mdp_group05.MapCell;
import com.example.mdp_group05.R;
import com.example.mdp_group05.Robot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mdp_group05.MapArena.gridSize;


public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Strings for reconfigurations
    public static final String MY_PREFERENCE = "MyPref";
    public static final String COMMAND_1 = "cmd1String";
    public static final String COMMAND_2 = "cmd2String";

    // Layout Views
    private ImageButton buttonForward, buttonReverse, buttonLeft, buttonRight;
    private Button buttonSend, buttonCmd1, buttonCmd2, buttonReconfigure, buttonAuto, buttonManual;
    private ListView messageListView;
    private TextView status, robotStatus;
    private EditText writeMsg;

    // Map Layout
    public static MapArena arena;
    public static MapCell[][] mapCell = new MapCell[Constants.MAP_ROW][Constants.MAP_COLUMN];
    private LinearLayout map;
    public static int[] robotFront = {1, 17}; //[Right, Down] coordinates
    public static int[] robotCenter = {1, 18};
    private Robot robot;
    private ProgressDialog pd;

    // SharedPreference to store data
    private SharedPreferences sharedPreferences;

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

    private void addTouchListener(View view) {
        LinearLayout mapArena = view.findViewById(R.id.mapArena);
        mapArena.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                String message = String.format("Coordinate: (%d, %d)",x,y);

                Log.d(TAG,message);
                return false;
            }
        });

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
        pd = new ProgressDialog(getActivity());
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    // Handles clicking of the option menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Setup the remaining view
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        buttonSend = view.findViewById(R.id.btnSend);
        buttonForward = view.findViewById(R.id.btnForward);
        buttonReverse = view.findViewById(R.id.btnReverse);
        buttonLeft = view.findViewById(R.id.btnLeft);
        buttonRight = view.findViewById(R.id.btnRight);
        buttonCmd1 = view.findViewById(R.id.btn_cmdF1);
        buttonCmd2 = view.findViewById(R.id.btn_cmdF2);
        buttonReconfigure = view.findViewById(R.id.btn_reconfigure);
        buttonAuto = view.findViewById(R.id.btnAuto);
        buttonManual = view.findViewById(R.id.btnManual);
        status = view.findViewById(R.id.status);
        robotStatus = view.findViewById(R.id.robotStatus);
        writeMsg = view.findViewById(R.id.writemsg);
        messageListView = view.findViewById(R.id.messageListView);
        addTouchListener(view);
        init(view); // Set up the map view
    }

    // Drawing of the 2D-Grid
    private void init(View view) {
        for (int column = 0; column < Constants.MAP_COLUMN; column++) {
            for (int row = 0; row < Constants.MAP_ROW; row++) {
                mapCell[row][column] = new MapCell();
                mapCell[row][column].setWaypoint(false);
                mapCell[row][column].setObstacles(false);
            }
        }
        arena = new MapArena(getContext());
        robot = new Robot(robotFront,robotCenter,gridSize);
        map = view.findViewById(R.id.mapArena);
        map.addView(arena);
    }

    // Setting up the UI for Bluetooth Communication
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        conversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message_layout);

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
                sendMessage("f");  //Defaulted at "f" in AMD
                pd.setMessage("Moving Forward");
                pd.show();
                robotStatus.setText("Status: Robot Moving Forward");
                robot.moveUp();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        robotStatus.setText("Status: Robot Ready for Action");
                    }
                }, 1000);
            }
        });

        // Upon clicking the "Reverse" button, vehicle should reverse on AMD
        buttonReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("r"); //Defaulted at "r" in AMD
                pd.setMessage("Robot Reversing");
                pd.show();
                robotStatus.setText("Status: Robot Reversing");
                robot.reverse();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        robotStatus.setText("Status: Robot Ready for Action");
                    }
                }, 1000);
            }
        });

        // Upon clicking the "Rotate Left" button, vehicle should rotate left on AMD
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("tl"); //Defaulted at "tl" in AMD
                pd.setMessage("Rotating Left");
                pd.show();
                robotStatus.setText("Status: Robot Rotating Left");
                robot.rotateLeft();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        robotStatus.setText("Status: Robot Ready for Action");
                    }
                }, 1000);
            }
        });

        // Upon clicking the "Rotate Right" button, vehicle should rotate right on AMD
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("tr"); //Defaulted at "tr" in AMD
                pd.setMessage("Rotating Right");
                pd.show();
                robotStatus.setText("Status: Robot Rotating Right");
                robot.rotateRight();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        robotStatus.setText("Status: Robot Ready for Action");
                    }
                }, 1000);
            }
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

    // Method to send a message via Bluetooth connection
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
            bluetoothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
            writeMsg.setText(outStringBuffer);
        }
    }

    // To listen for any return key on the EditText Widget
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    // Update status on the Action Bar
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    // Update status on the Action Bar
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
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
                            setStatus(getString(R.string.title_connected_to, connectedDeviceName));
                            status.setText("Status: Connected");
                            robotStatus.setText("Status: Robot Ready For Action");
                            conversationArrayAdapter.clear();
                            break;
                        case BluetoothCommunicationService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            status.setText("Status: Connecting..");
                            robotStatus.setText("Status: Robot Not Connected");
                            break;
                        case BluetoothCommunicationService.STATE_LISTENING:
                            status.setText("Status: Listening...");
                            robotStatus.setText("Status: Robot Not Connected");
                            break;
                        case BluetoothCommunicationService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            status.setText("Status: No Connected");
                            robotStatus.setText("Status: Robot Not Connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    conversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    conversationArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    updateStatus(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
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

    // Reads the message sent from robot and update it's current status and location on Grid
    public void updateStatus(String readMessage){
        if (readMessage.contains("turning right")) {
            robotStatus.setText("Status: Robot Rotating Right");
            robot.rotateRight();
        }
        else if (readMessage.contains("turning left")) {
            robotStatus.setText("Status: Robot Rotating Left");
            robot.rotateLeft();
        }
        else if (readMessage.contains("moving forward")) {
            robotStatus.setText("Status: Robot Moving Forward");
            robot.moveUp();
        }
        else if (readMessage.contains("reversing")) {
            robotStatus.setText("Status: Robot Reversing");
            robot.reverse();
        }
        else if (readMessage.contains("Startpoint")){
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(readMessage);
            int[] coordinates = {0,0};
            int index = 0;
            while(m.find()) {
                coordinates[index] = Integer.parseInt(m.group());
                index++;
            }
            robotCenter[0] = coordinates[0];
            robotCenter[1] = coordinates[1];
            robotFront[0] = coordinates[0];
            robotFront[1] = coordinates[1]-1;
        }
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
