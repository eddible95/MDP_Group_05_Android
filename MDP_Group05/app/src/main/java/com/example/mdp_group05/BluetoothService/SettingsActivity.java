package com.example.mdp_group05.BluetoothService;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mdp_group05.R;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingActivity";

    public static final String MY_PREFERENCE = "MyPref";
    public static final String COMMAND_1 = "cmd1String";
    public static final String COMMAND_2 = "cmd2String";

    // Widgets
    private EditText command1, command2;
    private Button btnSave, btnCancel;

    // Saving of configurations
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        command1 = findViewById(R.id.inputCommand1);
        command2 = findViewById(R.id.inputCommand2);
        btnSave = findViewById(R.id.btnSaveSettings);
        btnCancel = findViewById(R.id.btnCancel);

        command1.setText(sharedPreferences.getString(COMMAND_1,"Command 1 not set"));
        command2.setText(sharedPreferences.getString(COMMAND_2,"Command 2 not set"));

        // Upon clicking on save, the 2 functions are reconfigured
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(COMMAND_1, command1.getText().toString());
                editor.putString(COMMAND_2, command2.getText().toString());
                editor.commit();
                SettingsActivity.this.finish();
            }
        });

        // Close the activity
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
    }
}
