package com.mellah.mediassist;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FullScreenAlarmActivity extends AppCompatActivity {
    private static Ringtone ringtone;  // i changed this to static so only one rington ever exists
    private Vibrator   vibrator;
    private TextView   tvAlarmLabel, tvAlarmTime;
    private Button     btnDismiss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // show over lock screen
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

        // Stop any previously playing ringtone before starting a new one
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        // 1) Play default alarm ringtone
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) ringtone.play();

        // 2) Vibrate
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE)
            );
        }

        // Dismiss stops both
        btnDismiss.setOnClickListener(v -> {
            stopAlarm();
            finish();
        });
    }

    private void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
