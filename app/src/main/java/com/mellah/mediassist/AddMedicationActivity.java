package com.mellah.mediassist;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMedicationActivity extends AppCompatActivity {
    private EditText etName, etDosage, etNotes;
    private TextView tvStartDate, tvEndDate;
    private Button btnPickStartDate, btnPickEndDate, btnSave;
    private NumberPicker npTimesPerDay;
    private LinearLayout llTimePickers;
    private MediAssistDatabaseHelper dbHelper;
    private LayoutInflater inflater;

    private boolean isEditMode = false;
    private int medId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etName       = findViewById(R.id.etMedName);
        etDosage     = findViewById(R.id.etMedDosage);
        etNotes      = findViewById(R.id.etMedNotes);
        tvStartDate  = findViewById(R.id.tvStartDate);
        tvEndDate    = findViewById(R.id.tvEndDate);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate   = findViewById(R.id.btnPickEndDate);
        npTimesPerDay = findViewById(R.id.npTimesPerDay);
        llTimePickers = findViewById(R.id.llTimePickers);
        btnSave      = findViewById(R.id.btnSaveMed);

        dbHelper     = new MediAssistDatabaseHelper(this);
        inflater     = LayoutInflater.from(this);

        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day   = cal.get(Calendar.DAY_OF_MONTH);
        updateDateText(tvStartDate, year, month, day);
        updateDateText(tvEndDate,   year, month, day);

        btnPickStartDate.setOnClickListener(v ->
                new android.app.DatePickerDialog(
                        this,
                        (view, y, m, d) -> updateDateText(tvStartDate, y, m, d),
                        year, month, day
                ).show()
        );
        btnPickEndDate.setOnClickListener(v ->
                new android.app.DatePickerDialog(
                        this,
                        (view, y, m, d) -> updateDateText(tvEndDate, y, m, d),
                        year, month, day
                ).show()
        );

        npTimesPerDay.setMinValue(1);
        npTimesPerDay.setMaxValue(10);
        npTimesPerDay.setValue(1);
        generateTimePickers(1);
        npTimesPerDay.setOnValueChangedListener((picker, oldVal, newVal) ->
                generateTimePickers(newVal)
        );

        btnSave.setOnClickListener(v -> saveMedication());

        // Check for edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("medId")) {
            isEditMode = true;
            medId = intent.getIntExtra("medId", -1);
            etName.setText(intent.getStringExtra("name"));
            etDosage.setText(intent.getStringExtra("dosage"));
            tvStartDate.setText(intent.getStringExtra("startDate"));
            tvEndDate.setText(intent.getStringExtra("endDate"));
            etNotes.setText(intent.getStringExtra("notes"));

            List<String> timesList = new Gson()
                    .fromJson(intent.getStringExtra("timesJson"), List.class);
            npTimesPerDay.setValue(timesList.size());
            generateTimePickers(timesList.size());
            for (int i = 0; i < timesList.size(); i++) {
                View row = llTimePickers.getChildAt(i);
                TextView tv = row.findViewById(R.id.tvTimeLabel);
                tv.setText(timesList.get(i));
            }
        }
    }

    private void generateTimePickers(int count) {
        llTimePickers.removeAllViews();
        for (int i = 0; i < count; i++) {
            View timeView = inflater.inflate(R.layout.time_picker_item, llTimePickers, false);
            TextView tvTimeLabel = timeView.findViewById(R.id.tvTimeLabel);
            Button btnPickTime   = timeView.findViewById(R.id.btnPickTime);
            btnPickTime.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();
                new TimePickerDialog(
                        this,
                        (view, h, m) -> tvTimeLabel.setText(String.format("%02d:%02d", h, m)),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                ).show();
            });
            llTimePickers.addView(timeView);
        }
    }

    private void saveMedication() {
        String name      = etName.getText().toString().trim();
        String dosage    = etDosage.getText().toString().trim();
        String startDate = tvStartDate.getText().toString();
        String endDate   = tvEndDate.getText().toString();
        String notes     = etNotes.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Name and dosage are required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> timesList = new ArrayList<>();
        for (int i = 0; i < llTimePickers.getChildCount(); i++) {
            View row = llTimePickers.getChildAt(i);
            TextView tv = row.findViewById(R.id.tvTimeLabel);
            String t = tv.getText().toString();
            if ("--:--".equals(t)) {
                Toast.makeText(this, "Please pick all times", Toast.LENGTH_SHORT).show();
                return;
            }
            timesList.add(t);
        }
        String timesJson = new Gson().toJson(timesList);

        SharedPreferences prefs =
                getSharedPreferences("user_session", Context.MODE_PRIVATE);
        int userId = prefs.getInt("currentUserId", -1);
        if (userId < 0) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        int scheduleId;
        boolean success;
        if (isEditMode) {
            success = dbHelper.updateMedication(
                    medId, name, dosage, "", timesJson, startDate, endDate, notes
            );
            scheduleId = medId;
        } else {
            long id = dbHelper.addMedication(
                    userId, name, dosage, "", timesJson, startDate, endDate, notes
            );
            success = id > 0;
            scheduleId = (int) id;
        }

        if (success) {
            Toast.makeText(this, "Medication saved", Toast.LENGTH_SHORT).show();
            scheduleMedicationAlarms(scheduleId, name, timesList);
            finish();
        } else {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleMedicationAlarms(int scheduleId,
                                          String label,
                                          List<String> timesList) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Wait for user to approve and come back
            }
        }

        for (int i = 0; i < timesList.size(); i++) {
            String t = timesList.get(i);
            String[] parts = t.split(":");
            int hour   = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            long triggerMillis = cal.getTimeInMillis();

            // Fire intent
            Intent fireIntent = new Intent(this, AlarmReceiver.class);
            fireIntent.putExtra("itemId",    scheduleId);
            fireIntent.putExtra("itemType", "MEDICATION");
            fireIntent.putExtra("label",      label);
            fireIntent.putExtra("time",       t);
            PendingIntent firePi = PendingIntent.getBroadcast(
                    this,
                    scheduleId * 100 + i,
                    fireIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Show-upcoming intent
            Intent showIntent = new Intent(this, ScheduleActivity.class);
            PendingIntent showPi = PendingIntent.getActivity(
                    this,
                    scheduleId,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmClockInfo acInfo =
                    new AlarmClockInfo(triggerMillis, showPi);
            alarmManager.setAlarmClock(acInfo, firePi);
        }
    }

    private void updateDateText(TextView tv, int y, int m, int d) {
        tv.setText(String.format("%04d-%02d-%02d", y, m + 1, d));
    }
}
