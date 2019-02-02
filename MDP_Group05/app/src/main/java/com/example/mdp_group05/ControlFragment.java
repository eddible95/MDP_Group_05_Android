package com.example.mdp_group05;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mdp_group05.BluetoothService.BluetoothFragment;

public class ControlFragment extends Fragment {

    private static final String TAG = "ControlFragment";

    // Layout Views
    private Button buttonForward, buttonReverse, buttonRotateLeft, buttonRotateRight;
    private BluetoothFragment bluetoothFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    // Setup the remaining view
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        buttonForward = view.findViewById(R.id.button_forward);
        buttonReverse = view.findViewById(R.id.button_reverse);
        buttonRotateLeft = view.findViewById(R.id.button_r_left);
        buttonRotateRight = view.findViewById(R.id.button_r_right);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupControl();

    }

    private void setupControl() {
        Log.d(TAG, "setupControl()");
        // Upon clicking the "Forward" button, vehicle should move forward on AMD
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothFragment = new BluetoothFragment();
                bluetoothFragment.sendMessage("f"); //Defaulted at "f" in AMD
            }
        });

        // Upon clicking the "Reverse" button, vehicle should reverse on AMD
        buttonReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothFragment = new BluetoothFragment();
                bluetoothFragment.sendMessage("r"); //Defaulted at "r" in AMD
            }
        });

        // Upon clicking the "Rotate Left" button, vehicle should rotate left on AMD
        buttonRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothFragment = new BluetoothFragment();
                bluetoothFragment.sendMessage("tl"); //Defaulted at "tl" in AMD
            }
        });

        // Upon clicking the "Rotate Right" button, vehicle should rotate right on AMD
        buttonRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothFragment = new BluetoothFragment();
                bluetoothFragment.sendMessage("tr"); //Defaulted at "tr" in AMD
            }
        });

    }
}
