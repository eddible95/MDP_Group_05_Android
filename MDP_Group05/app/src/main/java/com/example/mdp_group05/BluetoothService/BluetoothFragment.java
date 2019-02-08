package com.example.mdp_group05.BluetoothService;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_group05.R;


public class BluetoothFragment extends Fragment {

    private static final String TAG = "BluetoothFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private Button buttonConnect, buttonSend, buttonDiscoverable, buttonDisconnect;
    private Button buttonForward, buttonReverse, buttonLeft, buttonRight;
    private ListView messageListView;
    private TextView status, robotStatus;
    private EditText writeMsg;

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

        // Checks if bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
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
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    // Setup the remaining view
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        buttonConnect = view.findViewById(R.id.btnConnect);
        buttonSend = view.findViewById(R.id.btnSend);
        buttonDiscoverable = view.findViewById(R.id.btnDiscoverable);
        buttonDisconnect = view.findViewById(R.id.btnDisconnect);
        buttonForward = view.findViewById(R.id.btnForward);
        buttonReverse = view.findViewById(R.id.btnReverse);
        buttonLeft = view.findViewById(R.id.btnLeft);
        buttonRight = view.findViewById(R.id.btnRight);
        status = view.findViewById(R.id.status);
        robotStatus = view.findViewById(R.id.robotStatus);
        writeMsg = view.findViewById(R.id.writemsg);
        messageListView = view.findViewById(R.id.messageListView);
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

        // Upon clicking the connect button, it will initiate a scan for nearby devices
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                //startActivity(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });

        // Upon clicking the discoverable button, the device will be discoverable by nearby devices
        buttonDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

        // Upon clicking the disconnect button, the device will end the bluetooth connection with the device
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothService.stop();
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("f");  //Defaulted at "f" in AMD
                robotStatus.setText("Status: Robot Moving Forward");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
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
                robotStatus.setText("Status: Robot Reversing");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
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
                robotStatus.setText("Status: Robot Rotating Left");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
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
                robotStatus.setText("Status: Robot Rotating Right");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        robotStatus.setText("Status: Robot Ready for Action");
                    }
                }, 1000);
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
                            conversationArrayAdapter.clear();
                            break;
                        case BluetoothCommunicationService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            status.setText("Status: Connecting..");
                            break;
                        case BluetoothCommunicationService.STATE_LISTENING:
                            status.setText("Status: Listening...");
                            break;
                        case BluetoothCommunicationService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            status.setText("Status: No Connected");
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
                    if (readMessage.contains("turning right"))
                        robotStatus.setText("Status Rotating Right");
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
