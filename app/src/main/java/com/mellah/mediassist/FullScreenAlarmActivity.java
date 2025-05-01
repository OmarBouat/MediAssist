package com.mellah.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FullScreenAlarmActivity extends AppCompatActivity {
    private TextView tvAlarmLabel, tvAlarmTime;
    private Button btnDismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Flags to show on lock screen / turn on screen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON    |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
        setContentView(R.layout.activity_fullscreen_alarm);

        tvAlarmLabel = findViewById(R.id.tvAlarmLabel);
        tvAlarmTime  = findViewById(R.id.tvAlarmTime);
        btnDismiss   = findViewById(R.id.btnDismissAlarm);

        Intent intent = getIntent();
        tvAlarmLabel.setText(intent.getStringExtra("label"));
        tvAlarmTime .setText(intent.getStringExtra("time"));

        btnDismiss.setOnClickListener(v -> finish());
    }
}
