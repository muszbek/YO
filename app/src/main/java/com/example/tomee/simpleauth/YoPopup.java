package com.example.tomee.simpleauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class YoPopup extends AppCompatActivity {

    private TextView nameView;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yo_popup);

        String name = getIntent().getStringExtra("SENDER");
        nameView = (TextView) findViewById(R.id.popupNameTextView);
        nameView.setText(name);

        layout = (LinearLayout) findViewById(R.id.popupLayout);
        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
