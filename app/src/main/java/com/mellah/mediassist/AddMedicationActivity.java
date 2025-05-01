package com.mellah.mediassist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private EditText etName, etDosage, etFrequency, etNotes;
    private TextView tvStartDate, tvEndDate;
    private Button btnPickStartDate, btnPickEndDate, btnSave;
    private NumberPicker npTimesPerDay;
    private LinearLayout llTimePickers;
    private MediAssistDatabaseHelper dbHelper;
    private LayoutInflater inflater;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // Initialize views
        etName       = findViewById(R.id.etMedName);
        etDosage     = findViewById(R.id.etMedDosage);
        etFrequency  = findViewById(R.id.etMedFrequency);
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
        calendar     = Calendar.getInstance();

        // Setup Date Pickers
        final int year  = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day   = calendar.get(Calendar.DAY_OF_MONTH);
        updateDateText(tvStartDate, year, month, day);
        updateDateText(tvEndDate, year, month, day);

        btnPickStartDate.setOnClickListener(v -> new DatePickerDialog(
                AddMedicationActivity.this,
                (view, y, m, d) -> updateDateText(tvStartDate, y, m, d),
                year, month, day).show());

        btnPickEndDate.setOnClickListener(v -> new DatePickerDialog(
                AddMedicationActivity.this,
                (view, y, m, d) -> updateDateText(tvEndDate, y, m, d),
                year, month, day).show());

        // Setup NumberPicker
        npTimesPerDay.setMinValue(1);
        npTimesPerDay.setMaxValue(10);
        npTimesPerDay.setValue(1);
        generateTimePickers(1);

        npTimesPerDay.setOnValueChangedListener((picker, oldVal, newVal) ->
                generateTimePickers(newVal)
        );

        // Save button
        btnSave.setOnClickListener(v -> saveMedication());
    }

    /**
     * Dynamically generates time-picker rows based on count.
     */
    private void generateTimePickers(int count) {
        llTimePickers.removeAllViews();
        for (int i = 0; i < count; i++) {
            View timeView = inflater.inflate(R.layout.time_picker_item, llTimePickers, false);
            TextView tvTimeLabel = timeView.findViewById(R.id.tvTimeLabel);
            Button btnPickTime  = timeView.findViewById(R.id.btnPickTime);

            btnPickTime.setOnClickListener(v -> {
                int h = calendar.get(Calendar.HOUR_OF_DAY);
                int m = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(
                        AddMedicationActivity.this,
                        (TimePicker view, int hourOfDay, int minute) ->
                                tvTimeLabel.setText(String.format("%02d:%02d", hourOfDay, minute)),
                        h, m, true
                ).show();
            });

            llTimePickers.addView(timeView);
        }
    }

    /**
     * Reads inputs, validates, serializes times, and saves to database.
     */
    private void saveMedication() {
        String name      = etName.getText().toString().trim();
        String dosage    = etDosage.getText().toString().trim();
        String freq      = etFrequency.getText().toString().trim();
        String startDate = tvStartDate.getText().toString();
        String endDate   = tvEndDate.getText().toString();
        String notes     = etNotes.getText().toString().trim();

        if (name.isEmpty() || dosage.isEmpty() || freq.isEmpty()) {
            Toast.makeText(this, "Name, dosage, and frequency are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect times
        List<String> timesList = new ArrayList<>();
        for (int i = 0; i < llTimePickers.getChildCount(); i++) {
            View row = llTimePickers.getChildAt(i);
            TextView tv = row.findViewById(R.id.tvTimeLabel);
            String t = tv.getText().toString();
            if ("--:--".equals(t)) {
                Toast.makeText(this, "Please pick all reminder times", Toast.LENGTH_SHORT).show();
                return;
            }
            timesList.add(t);
        }

        // Serialize to JSON
        String timesJson = new Gson().toJson(timesList);

        // Retrieve current user
        SharedPreferences prefs = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("currentUserId", -1);
        if (userId < 0) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.addMedication(
                userId, name, dosage, freq, timesJson,
                startDate, endDate, notes
        );
        if (id > 0) {
            Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper to set TextView date text.
     */
    private void updateDateText(TextView tv, int y, int m, int d) {
        tv.setText(String.format("%04d-%02d-%02d", y, m + 1, d));
    }
}
