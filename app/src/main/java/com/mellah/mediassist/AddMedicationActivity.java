package com.mellah.mediassist;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.app.TimePickerDialog;
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

    // For edit vs add
    private boolean isEditMode = false;
    private int medId = -1;

    // For permission flow
    private boolean waitingForExactAlarmPermission = false;
    private int pendingScheduleId;
    private String pendingLabel;
    private List<String> pendingTimesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etMedDosage);
        etNotes = findViewById(R.id.etMedNotes);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        npTimesPerDay = findViewById(R.id.npTimesPerDay);
        llTimePickers = findViewById(R.id.llTimePickers);
        btnSave = findViewById(R.id.btnSaveMed);

        dbHelper = new MediAssistDatabaseHelper(this);
        inflater = LayoutInflater.from(this);

        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR), m = cal.get(Calendar.MONTH), d = cal.get(Calendar.DAY_OF_MONTH);
        updateDateText(tvStartDate, y, m, d);
        updateDateText(tvEndDate, y, m, d);

        btnPickStartDate.setOnClickListener(v ->
                new DatePickerDialog(this, (view, yy, mm, dd) ->
                        updateDateText(tvStartDate, yy, mm, dd),
                        y, m, d).show()
        );
        btnPickEndDate.setOnClickListener(v ->
                new DatePickerDialog(this, (view, yy, mm, dd) ->
                        updateDateText(tvEndDate, yy, mm, dd),
                        y, m, d).show()
        );

        npTimesPerDay.setMinValue(1);
        npTimesPerDay.setMaxValue(10);
        npTimesPerDay.setValue(1);
        generateTimePickers(1);
        npTimesPerDay.setOnValueChangedListener((picker, oldVal, newVal) ->
                generateTimePickers(newVal)
        );

        btnSave.setOnClickListener(v -> saveMedication());

        // Edit mode?
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
                TextView tv = llTimePickers.getChildAt(i).findViewById(R.id.tvTimeLabel);
                tv.setText(timesList.get(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingForExactAlarmPermission) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    waitingForExactAlarmPermission = false;
                    scheduleMedicationAlarms(pendingScheduleId, pendingLabel, pendingTimesList);
                    finish();
                }
            }
        }
    }

    private void generateTimePickers(int count) {
        llTimePickers.removeAllViews();
        for (int i = 0; i < count; i++) {
            View row = inflater.inflate(R.layout.time_picker_item, llTimePickers, false);
            TextView tv = row.findViewById(R.id.tvTimeLabel);
            Button btn = row.findViewById(R.id.btnPickTime);
            btn.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();
                new TimePickerDialog(
                        AddMedicationActivity.this,
                        (tp, h, mi) -> tv.setText(String.format("%02d:%02d", h, mi)),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                ).show();
            });
            llTimePickers.addView(row);
        }
    }

    private void saveMedication() {
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();
        String notes = etNotes.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Name and dosage required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> timesList = new ArrayList<>();
        for (int i = 0; i < llTimePickers.getChildCount(); i++) {
            TextView tv = llTimePickers.getChildAt(i).findViewById(R.id.tvTimeLabel);
            String t = tv.getText().toString();
            if ("--:--".equals(t)) {
                Toast.makeText(this, "Pick all times", Toast.LENGTH_SHORT).show();
                return;
            }
            timesList.add(t);
        }
        String timesJson = new Gson().toJson(timesList);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userId = prefs.getInt("currentUserId", -1);
        if (userId < 0) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        int scheduleId;
        boolean ok;
        if (isEditMode) {
            ok = dbHelper.updateMedication(
                    medId, name, dosage, "", timesJson, startDate, endDate, notes
            );
            scheduleId = medId;
        } else {
            long id = dbHelper.addMedication(
                    userId, name, dosage, "", timesJson, startDate, endDate, notes
            );
            ok = id > 0;
            scheduleId = (int) id;
        }

        if (!ok) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Permission check & schedule
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingScheduleId = scheduleId;
        pendingLabel = name;
        pendingTimesList = timesList;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!am.canScheduleExactAlarms()) {
                waitingForExactAlarmPermission = true;
                Toast.makeText(this, "Enable exact alarms in settings", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            } else {
                scheduleMedicationAlarms(scheduleId, name, timesList);
                finish();
            }
        }
    }

    private void scheduleMedicationAlarms(int scheduleId,
                                          String label,
                                          List<String> timesList) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < timesList.size(); i++) {
            String t = timesList.get(i);
            String[] p = t.split(":");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(p[0]));
            c.set(Calendar.MINUTE, Integer.parseInt(p[1]));
            c.set(Calendar.SECOND, 0);
            if (c.getTimeInMillis() < System.currentTimeMillis()) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            long when = c.getTimeInMillis();

            Intent fire = new Intent(this, AlarmReceiver.class);
            fire.putExtra("itemId", scheduleId);
            fire.putExtra("itemType", "MEDICATION");
            fire.putExtra("label", label);
            fire.putExtra("time", t);
            PendingIntent piFire = PendingIntent.getBroadcast(
                    this,
                    scheduleId * 100 + i,
                    fire,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent show = new Intent(this, ScheduleActivity.class);
            PendingIntent piShow = PendingIntent.getActivity(
                    this,
                    scheduleId,
                    show,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmClockInfo info = new AlarmClockInfo(when, piShow);
            am.setAlarmClock(info, piFire);
        }
    }

    private void updateDateText(TextView tv, int y, int m, int d) {
        tv.setText(String.format("%04d-%02d-%02d", y, m + 1, d));
    }
}
