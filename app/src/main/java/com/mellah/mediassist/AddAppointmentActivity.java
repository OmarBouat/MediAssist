package com.mellah.mediassist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
                new DatePickerDialog(this, (view,y,m,d) -> {
                    apptYear=y; apptMonth=m; apptDay=d;
                    updateDateDisplay();
                }, apptYear, apptMonth, apptDay).show()
        );
        btnPickTime.setOnClickListener(v ->
                new TimePickerDialog(this, (view,h,min) -> {
                    apptHour=h; apptMinute=min;
                    updateTimeDisplay();
                }, apptHour, apptMinute, true).show()
        );

        btnSave.setOnClickListener(v -> saveAppointment());

        // Edit mode?
        Intent intent = getIntent();
        if (intent.hasExtra("apptId")) {
            apptId = intent.getIntExtra("apptId", -1);
            etTitle.setText(intent.getStringExtra("title"));
            String date = intent.getStringExtra("date");
            String[] dp = date.split("-");
                    apptYear  = Integer.parseInt(dp[0]);
            apptMonth = Integer.parseInt(dp[1]) - 1;
            apptDay   = Integer.parseInt(dp[2]);
            updateDateDisplay();

            String time = intent.getStringExtra("time");
            String[] tp = time.split(":");
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
            Toast.makeText(this, "Enter appointment title", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = etNotes.getText().toString().trim();
        String date  = String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay);
        String time  = String.format("%02d:%02d", apptHour, apptMinute);
        int userId   = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("currentUserId", -1);

        boolean success;
        if (apptId >= 0) {
            success = dbHelper.updateAppointment(apptId, title, date, time, apptOffset, notes);
        } else {
            long id = dbHelper.addAppointment(userId, title, date, time, apptOffset, notes);
            success = id > 0;
        }

        if (success) {
            Toast.makeText(this, "Appointment saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save appointment", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDateDisplay() {
        tvDate.setText(String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay));
    }

    private void updateTimeDisplay() {
        tvTime.setText(String.format("%02d:%02d", apptHour, apptMinute));
    }
}
