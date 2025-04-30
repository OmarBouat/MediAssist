package com.mellah.mediassist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
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
    private MediAssistDatabaseHelper dbHelper;
    private LinearLayout timePickersLayout;
    private NumberPicker frequencyPicker;

    private int startYear, startMonth, startDay;
    private int endYear, endMonth, endDay;
    private List<Pair<Integer, Integer>> medicationTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // Initialize UI elements
        etName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etMedDosage);
        etNotes = findViewById(R.id.etMedNotes);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnSave = findViewById(R.id.btnSaveMed);
        timePickersLayout = findViewById(R.id.timePickersLayout);
        frequencyPicker = findViewById(R.id.frequencyPicker);

        // Initialize member variables
        medicationTimes = new ArrayList<>();

        // Set up the NumberPicker for frequency
        frequencyPicker.setMinValue(1);
        frequencyPicker.setMaxValue(10);

        // Initialize the database helper
        dbHelper = new MediAssistDatabaseHelper(this);

        // Initialize current date values
        Calendar cal = Calendar.getInstance();
        startYear = endYear = cal.get(Calendar.YEAR);
        startMonth = endMonth = cal.get(Calendar.MONTH);
        startDay = endDay = cal.get(Calendar.DAY_OF_MONTH);
        updateStartDateDisplay();
        updateEndDateDisplay();

        // Set up DatePicker dialogs
        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    AddMedicationActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        startYear = year;
                        startMonth = month;
                        startDay = dayOfMonth;
                        updateStartDateDisplay();
                    }, startYear, startMonth, startDay);
            dpd.show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    AddMedicationActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        endYear = year;
                        endMonth = month;
                        endDay = dayOfMonth;
                        updateEndDateDisplay();
                    }, endYear, endMonth, endDay);
            dpd.show();
        });

        // Initialize time pickers based on the initial frequency
        updateTimePickers();

        // Set listener for frequency changes
        frequencyPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateTimePickers());

        // Set up the "Save" button
        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void updateTimePickers() {
        int frequency = frequencyPicker.getValue();

        // Remove existing time pickers
        timePickersLayout.removeAllViews();
        medicationTimes.clear();

        // Add new time pickers based on the frequency
        for (int i = 0; i < frequency; i++) {
            addTimePicker(i);
        }
    }

    // Method to add a time picker dynamically
    private void addTimePicker(int index) {
        final TextView timeView = new TextView(this);

        // Calculate default time based on index
        int defaultHour = 8 + (index * 4); // 8:00, 12:00, 16:00, etc.
        if (defaultHour >= 24) {
            defaultHour -= 24;
        }
        final int finalDefaultHour = defaultHour;
        int defaultMinute = 0;

        timeView.setText(String.format("%02d:%02d", finalDefaultHour, defaultMinute));
        timeView.setTextSize(18);
        timeView.setPadding(8, 8, 8, 8);
        timeView.setClickable(true);
        medicationTimes.add(new Pair<>(finalDefaultHour, defaultMinute));
        timeView.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(
                    AddMedicationActivity.this,
                    (view, hourOfDay, minute) -> {
                        timeView.setText(String.format("%02d:%02d", hourOfDay, minute));
                        medicationTimes.set(index, new Pair<>(hourOfDay, minute));
                    }, finalDefaultHour, defaultMinute, true);
            tpd.show();
        });
        timePickersLayout.addView(timeView);
    }

    // Method to handle saving the medication
    private void saveMedication() {
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Basic input validation
        if (name.isEmpty() || dosage.isEmpty()) {
            Toast.makeText(this, "Fill name, dosage and frequency", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the selected frequency from the NumberPicker
        int frequency = frequencyPicker.getValue();

        // Validate if the number of time is equal to the frequency
        if (frequency != medicationTimes.size()) {
            Toast.makeText(this, "Add all the time please", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format start and end dates
        String startDate = String.format("%04d-%02d-%02d", startYear, startMonth + 1, startDay);
        String endDate = String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay);

        // Get the user ID from SharedPreferences
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);

        // Convert the times from Pair<Integer, Integer> to List<String>
        List<String> times = new ArrayList<>();
        for (Pair<Integer, Integer> time : medicationTimes) {
            times.add(String.format("%02d:%02d", time.first, time.second));
        }

        // Save the medication to the database
        Gson gson = new Gson();
        String timesJson = gson.toJson(times);

        // Save the medication to the database
        long id = dbHelper.addMedication(userId, name, dosage, String.valueOf(frequency), timesJson, startDate, endDate, notes);
        if (id > 0) {
            Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper methods to update date displays
    private void updateStartDateDisplay() {
        tvStartDate.setText(String.format("%04d-%02d-%02d", startYear, startMonth + 1, startDay));
    }

    private void updateEndDateDisplay() {
        tvEndDate.setText(String.format("%04d-%02d-%02d", endYear, endMonth + 1, endDay));
    }
}