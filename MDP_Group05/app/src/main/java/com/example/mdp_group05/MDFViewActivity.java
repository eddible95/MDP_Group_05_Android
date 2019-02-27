package com.example.mdp_group05;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MDFViewActivity extends AppCompatActivity {

    private TextView mdf1View;
    private TextView mdf2View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdfview);

        mdf1View = findViewById(R.id.MDFString1);
        mdf2View = findViewById(R.id.MDFString2);

        String mdfString1 = getIntent().getStringExtra("MDFString1");
        String mdfString2 = getIntent().getStringExtra("MDFString2");


        mdf1View.setText(mdfString1);
        mdf2View.setText(mdfString2);
    }
}
