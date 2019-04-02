package com.example.mdp_group05.BluetoothService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

public class BluetoothCommunicationService {

    private static final String TAG = "BluetoothCommService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothCommunicationSecure";
    private static final String NAME_INSECURE = "BluetoothCommunicationInsecure";

    // Unique UUID for bluetooth connection for the application
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTENING = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Constructor to initialise a new instance of the BluetoothCommunicationService
    public BluetoothCommunicationService(Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    // Gets the current bluetooth connection state
    public synchronized int getState() {
        return mState;
    }

    // Starts the bluetooth communication service by listening for any connection through the AcceptThread
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancels any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancels any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Starts the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }

        // Update UI title
        updateUserInterfaceTitle();
    }

    // Creates a ConnectThread to initiate a connection to a remote device
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    // Creates a ConnectedThread to manage a Bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

            // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }

    // Stops all threads
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    // Write to the ConnectedThread in an unsynchronized manner
    public void writeByteArray(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeByteArray(out);
    }

    // Called upon when bluetooth connection with a device fails and notifies the UI Activity
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the Bluetooth communication service over to restart listening mode
        BluetoothCommunicationService.this.start();
    }

    // Called upon when bluetooth connection with a device is lost and notifies the UI Activity
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothCommunicationService.this.start();
    }

    /* A thread that runs while listening for incoming bluetooth connections and runs until a
       connection is accepted or is cancelled */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket serverSocket;
        private String mSocketType;

        // Constructor to initialize all the fields required for the thread to listen for connections
        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure){
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                } else {
                    tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            serverSocket = tmp;
            mState = STATE_LISTENING;
        }

        // Start running the thread
        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if the device is not connected
            while (mState != STATE_CONNECTED) {
                try {
                    /* This is a blocking call and will only return on a
                       successful connection or an exception */
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothCommunicationService.this) {
                        switch (mState) {
                            case STATE_LISTENING:
                            case STATE_CONNECTING:
                                // Situation normal and starts the connected thread
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected hence the new socket is terminated
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        // Closes the server socket listening for incoming bluetooth connections
        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }

    // A thread that runs while attempting to make an outgoing connection with a device
    private class ConnectThread extends Thread {
        private final BluetoothSocket clientSocket;
        private final BluetoothDevice bluetoothDevice;
        private String mSocketType;

        // Constructor to initialize all the fields required for the thread to listen for connections
        public ConnectThread(BluetoothDevice device, boolean secure) {
            bluetoothDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            clientSocket = tmp;
            mState = STATE_CONNECTING;
        }

        // Starts running the thread
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                clientSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    clientSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothCommunicationService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(clientSocket, bluetoothDevice, mSocketType);
        }

        // Closes the client socket connecting to bluetooth devices
        public void cancel() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /* A thread that runs during a connection with a remote device and handles all incoming and outgoing
       transactions */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private ListenThread listenThread;
        private ProcessThread processThread;
        private ArrayBlockingQueue <byte[]> queue = new ArrayBlockingQueue<>(1024);

        // Constructor to initialize all the fields required for the thread to listen for connections
        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        // Starts running the thread
        public void run() {

            // Starts the thread that listen for incoming messages
            listenThread = new ListenThread(inputStream, queue, mState);
            listenThread.start();

            // Starts the thread that process the incoming messages
            processThread = new ProcessThread(queue);
            processThread.start();
        }

        // Send the message the Bluetooth device
        public void writeByteArray(byte[] buffer) {
            try {
                outputStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        // Closes the bluetooth socket that is connected to the bluetooth device
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        // Thread to listen for messages sent and add to the queue
        private class ListenThread extends Thread {
            private final InputStream inputStream;
            private ArrayBlockingQueue <byte[]> queue;
            private int mState;

            // Constructor to initialise all the fields required for the thread to listen for incoming messages
            public ListenThread(InputStream inputStream, ArrayBlockingQueue<byte[]> queue, int mState){
                this.inputStream = inputStream;
                this.queue = queue;
                this.mState = mState;
            }

            // Starts the thread
            public void run(){
                Log.e(TAG, "BEGIN mListenThread");
                byte[] buffer = new byte[1024];

                // Keep listening to the InputStream while connected and adds to the queue
                while (mState == STATE_CONNECTED) {
                    try {
                        // Reads from the InputStream
                        inputStream.read(buffer);
                        byte[] tmpBuffer = Arrays.copyOf(buffer, 1024);

                        // Adds the buffer to the queue
                        queue.add(tmpBuffer);
                        Log.e(TAG, String.format("Adding to the queue"));
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        connectionLost();
                        break;
                    }
                }
            }
        }

        // Thread to process data in the received queue
        private class ProcessThread extends Thread{
            private ArrayBlockingQueue<byte[]> queue;
            private byte[] bufferRead = new byte[1024];

            // Constructor to initialise all the fields required for the thread to process incoming messages
            public ProcessThread(ArrayBlockingQueue<byte[]> queue){
                this.queue = queue;
            }

            // Reads from the queue and sends to the UI
            public void run(){
                Log.e(TAG, "BEGIN mProcessThread");
                while(true){
                    // Only remove an item from the queue when it is not empty
                    if(!queue.isEmpty()){
                        bufferRead = queue.remove();
                        mHandler.obtainMessage(Constants.MESSAGE_READ, -1, -1, bufferRead).sendToTarget();
                        Log.e(TAG, String.format("Removing from the queue"));
                        // For fastest path mode where instructions are received
                        /*if(new String(bufferRead).contains("!")){
                            String commandMessageStream = new String(bufferRead);
                            String commandArr[];
                            commandArr = commandMessageStream.split(":"); //
                            Message format !:fffllrb
                            String commandStr = commandArr[1];
                            for (int i = 0; i <commandStr.length(); i++){
                                // Send the obtained bytes to the UI Activity
                                if (commandStr.charAt(i) == 'f' || commandStr.charAt(i) == 'r' || commandStr.charAt(i) == 'l' || commandStr.charAt(i) == 'b'){
                                    mHandler.obtainMessage(Constants.FASTEST_PATH, -1, -1, commandStr.charAt(i)).sendToTarget();
                                    try {
                                        sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    Log.e(TAG, String.format("Removing from the queue"));
                                    Log.e(TAG, String.format("Fastest Path Command: %c",commandStr.charAt(i)));
                                } else{
                                    break;
                                }
                            }
                        }

                        // For all other messages
                        else {
                            // Send the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ, -1, -1, bufferRead).sendToTarget();
                            Log.e(TAG, String.format("Removing from the queue"));
                        }*/
                    }
                }
            }
        }
    }
}
