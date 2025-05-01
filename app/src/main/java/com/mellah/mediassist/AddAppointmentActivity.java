package com.mellah.mediassist;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddAppointmentActivity extends AppCompatActivity {
    private EditText etTitle, etNotes;
    private TextView tvDate, tvTime;
    private Button btnPickDate, btnPickTime, btnSave;
    private MediAssistDatabaseHelper dbHelper;

    private int apptId = -1;
    private int apptYear, apptMonth, apptDay;
    private int apptHour, apptMinute, apptOffset = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        etTitle     = findViewById(R.id.etApptTitle);
        etNotes     = findViewById(R.id.etApptNotes);
        tvDate      = findViewById(R.id.tvApptDate);
        tvTime      = findViewById(R.id.tvApptTime);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave     = findViewById(R.id.btnSaveAppt);
        dbHelper    = new MediAssistDatabaseHelper(this);

        Calendar cal = Calendar.getInstance();
        apptYear   = cal.get(Calendar.YEAR);
        apptMonth  = cal.get(Calendar.MONTH);
        apptDay    = cal.get(Calendar.DAY_OF_MONTH);
        apptHour   = cal.get(Calendar.HOUR_OF_DAY);
        apptMinute = cal.get(Calendar.MINUTE);
        updateDateDisplay();
        updateTimeDisplay();

        btnPickDate.setOnClickListener(v ->
                new android.app.DatePickerDialog(
                        this,
                        (view,y,m,d) -> {
                            apptYear = y; apptMonth = m; apptDay = d;
                            updateDateDisplay();
                        },
                        apptYear, apptMonth, apptDay
                ).show()
        );
        btnPickTime.setOnClickListener(v ->
                new android.app.TimePickerDialog(
                        this,
                        (view,h,min) -> {
                            apptHour = h; apptMinute = min;
                            updateTimeDisplay();
                        },
                        apptHour, apptMinute,
                        true
                ).show()
        );

        btnSave.setOnClickListener(v -> saveAppointment());

        Intent intent = getIntent();
        if (intent.hasExtra("apptId")) {
            apptId = intent.getIntExtra("apptId", -1);
            etTitle.setText(intent.getStringExtra("title"));
            String[] dp = intent.getStringExtra("date").split("-");
            apptYear  = Integer.parseInt(dp[0]);
            apptMonth = Integer.parseInt(dp[1]) - 1;
            apptDay   = Integer.parseInt(dp[2]);
            updateDateDisplay();
            String[] tp = intent.getStringExtra("time").split(":");
            apptHour   = Integer.parseInt(tp[0]);
            apptMinute = Integer.parseInt(tp[1]);
            updateTimeDisplay();
            apptOffset = intent.getIntExtra("offset", 60);
            etNotes.setText(intent.getStringExtra("notes"));
        }
    }

    private void saveAppointment() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = etNotes.getText().toString().trim();
        String date  = String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay);
        String time  = String.format("%02d:%02d", apptHour, apptMinute);
        int userId   = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getInt("currentUserId", -1);

        int scheduleId;
        boolean success;
        if (apptId >= 0) {
            success = dbHelper.updateAppointment(
                    apptId, title, date, time, apptOffset, notes
            );
            scheduleId = apptId;
        } else {
            long id = dbHelper.addAppointment(
                    userId, title, date, time, apptOffset, notes
            );
            success = id > 0;
            scheduleId = (int) id;
        }

        if (success) {
            Toast.makeText(this, "Appointment saved", Toast.LENGTH_SHORT).show();
            scheduleAppointmentAlarm(
                    scheduleId, date, time, apptOffset, title
            );
            finish();
        } else {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAppointmentAlarm(int scheduleId,
                                          String date,
                                          String time,
                                          int offsetMinutes,
                                          String label) {
        String[] dp = date.split("-");
        String[] tp = time.split(":");
        Calendar cal = Calendar.getInstance();
        cal.set(
                Integer.parseInt(dp[0]),
                Integer.parseInt(dp[1]) - 1,
                Integer.parseInt(dp[2]),
                Integer.parseInt(tp[0]),
                Integer.parseInt(tp[1]),
                0
        );
        cal.add(Calendar.MINUTE, -offsetMinutes);
        long triggerMillis = cal.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Wait for user to approve and come back
            }
        }

        // Fire intent
        Intent fireIntent = new Intent(this, AlarmReceiver.class);
        fireIntent.putExtra("itemId",    scheduleId);
        fireIntent.putExtra("itemType", "APPOINTMENT");
        fireIntent.putExtra("label",      label);
        fireIntent.putExtra("time",       time);
        PendingIntent firePi = PendingIntent.getBroadcast(
                this,
                scheduleId,
                fireIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Show upcoming alarm intent
        Intent showIntent = new Intent(this, ScheduleActivity.class);
        PendingIntent showPi = PendingIntent.getActivity(
                this,
                scheduleId * 1000,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmClockInfo acInfo =
                new AlarmClockInfo(triggerMillis, showPi);
        alarmManager.setAlarmClock(acInfo, firePi);
    }

    private void updateDateDisplay() {
        tvDate.setText(String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay));
    }

    private void updateTimeDisplay() {
        tvTime.setText(String.format("%02d:%02d", apptHour, apptMinute));
    }
}
