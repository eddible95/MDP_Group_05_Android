package com.example.mdp_group05;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_group05.BluetoothService.BluetoothFragment;

import static com.example.mdp_group05.BluetoothService.BluetoothFragment.EXPLORATION_TIME;
import static com.example.mdp_group05.BluetoothService.BluetoothFragment.FASTEST_TIME;
import static com.example.mdp_group05.BluetoothService.BluetoothFragment.MY_PREFERENCE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private static final String TAG = "MainActivity";

    // Layout views
    private DrawerLayout drawer;
    private TextView fastestPathTimer, explorationPathTimer;
    private SharedPreferences sharedPreferences;

    // For motion sensor movement controls
    private SensorManager sensorManager;
    private Sensor accelerometer;
    public static float xTilt, yTilt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Home Page");
        Log.d(TAG, "onCreate: Started");

        // Gets data stored on SharedPreference
        sharedPreferences = getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        // Toolbar must be widget v7
        // This is to set to tool bar since we remove it
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting up of the drawer menu
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Log.d(TAG, String.format("Drawer menu successfully created"));


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setting up of motion sensor manager to detect the tilting of the tablet
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, String.format("Motion Sensor successfully setup"));

        // Setting the UI for timings
        explorationPathTimer = findViewById(R.id.exploration_time);
        fastestPathTimer = findViewById(R.id.fastestpath_time);
        explorationPathTimer.setText(sharedPreferences.getString(EXPLORATION_TIME,"NULL"));
        fastestPathTimer.setText(sharedPreferences.getString(FASTEST_TIME,"NULL"));

        // Setting the views
        findViewById(R.id.welcome_title_1).setVisibility(View.VISIBLE);
        findViewById(R.id.welcome_title_2).setVisibility(View.VISIBLE);
        findViewById(R.id.timer_title).setVisibility(View.VISIBLE);
        findViewById(R.id.exploration_timer_title).setVisibility(View.VISIBLE);
        findViewById(R.id.fastestpath_timer_title).setVisibility(View.VISIBLE);
        findViewById(R.id.exploration_time).setVisibility(View.VISIBLE);
        findViewById(R.id.fastestpath_time).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_Bluetooth:
                Toast.makeText(getApplicationContext(), "Going to Bluetooth Fragment", Toast.LENGTH_SHORT).show();
                FragmentManager fManager = getSupportFragmentManager();
                FragmentTransaction fTransaction = fManager.beginTransaction();
                BluetoothFragment btFrag = (BluetoothFragment) fManager.findFragmentByTag("bluetoothFrag");

                // If fragment does not exist yet, create one
                if (btFrag == null) {
                    fTransaction.add(R.id.fragment_container, new BluetoothFragment(), "bluetoothFrag");
                    fTransaction.commit();
                }

                // Re-use the old fragment if it exists
                else {
                    fTransaction.replace(R.id.fragment_container, btFrag, "bluetoothFrag");
                    fTransaction.commit();
                }
                break;
            case R.id.nav_Home: {
                fManager = getSupportFragmentManager();
                fTransaction = fManager.beginTransaction();
                btFrag = (BluetoothFragment) fManager.findFragmentByTag("bluetoothFrag");
                Toast.makeText(getApplicationContext(), "Going to Main Activity", Toast.LENGTH_SHORT).show();

                // If an existing BluetoothFragment exists, remove it to prevent multiple instances of the fragments
                if (btFrag != null){
                    fTransaction.remove(btFrag);
                    fTransaction.commit();
                }
                // Ends a current instance of the MainActivity to prevent multiple instances of the same activity
                MainActivity.this.finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Detect how much and which side the tablet is being tilted towards for motion control
    @Override
    public void onSensorChanged(SensorEvent event) {
        xTilt = event.values[0];
        yTilt = -event.values[1];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
