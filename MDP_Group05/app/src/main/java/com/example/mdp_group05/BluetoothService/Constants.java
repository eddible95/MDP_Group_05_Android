package com.example.mdp_group05.BluetoothService;

// Defines all the constants that are common among the BluetoothService classes
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    int MESSAGE_WRITE_BYTE = 6;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // Number of columns and rows for the Arena Map
    int MAP_COLUMN = 15;
    int MAP_ROW = 20;
}
