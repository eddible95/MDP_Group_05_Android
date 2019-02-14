package com.example.mdp_group05.BluetoothService;

// Defines all the constants that are common among the BluetoothService classes
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Number of columns and rows for the Arena Map
    public static final int MAP_COLUMN = 15;
    public static final int MAP_ROW = 20;
}
