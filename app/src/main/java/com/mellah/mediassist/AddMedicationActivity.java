package com.mellah.mediassist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddMedicationActivity extends AppCompatActivity {
    private EditText etName, etDosage, etFrequency, etNotes;
    private TextView tvTime, tvStartDate, tvEndDate;
    private Button btnPickTime, btnPickStartDate, btnPickEndDate, btnSave;
    private MediAssistDatabaseHelper dbHelper;

    private int hour, minute;
    private int startYear, startMonth, startDay;
    private int endYear, endMonth, endDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etName      = findViewById(R.id.etMedName);
        etDosage    = findViewById(R.id.etMedDosage);
        etFrequency = findViewById(R.id.etMedFrequency);
        etNotes     = findViewById(R.id.etMedNotes);
        tvTime      = findViewById(R.id.tvMedTime);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate   = findViewById(R.id.tvEndDate);
        btnPickTime      = findViewById(R.id.btnPickTime);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate   = findViewById(R.id.btnPickEndDate);
        btnSave     = findViewById(R.id.btnSaveMed);

        dbHelper = new MediAssistDatabaseHelper(this);

        // init current date/time
        Calendar cal = Calendar.getInstance();
        hour   = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        startYear = endYear = cal.get(Calendar.YEAR);
        startMonth = endMonth = cal.get(Calendar.MONTH);
        startDay = endDay = cal.get(Calendar.DAY_OF_MONTH);
        updateTimeDisplay();
        updateStartDateDisplay();
        updateEndDateDisplay();

        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(
                    AddMedicationActivity.this,
                    (view, h, m) -> {
                        hour = h; minute = m;
                        updateTimeDisplay();
                    }, hour, minute, true);
            tpd.show();
        });

        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    AddMedicationActivity.this,
                    (view, y, m, d) -> {
                        startYear=y; startMonth=m; startDay=d;
                        updateStartDateDisplay();
                    }, startYear, startMonth, startDay);
            dpd.show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    AddMedicationActivity.this,
                    (view, y, m, d) -> {
                        endYear=y; endMonth=m; endDay=d;
                        updateEndDateDisplay();
                    }, endYear, endMonth, endDay);
            dpd.show();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            if (name.isEmpty()||dosage.isEmpty()||frequency.isEmpty()) {
                Toast.makeText(this, "Fill name, dosage and frequency", Toast.LENGTH_SHORT).show();
                return;
            }
            String time = String.format("%02d:%02d", hour, minute);
            String startDate = String.format("%04d-%02d-%02d", startYear, startMonth+1, startDay);
            String endDate = String.format("%04d-%02d-%02d", endYear, endMonth+1, endDay);

            long id = dbHelper.addMedication(name, dosage, frequency, time, startDate, endDate, notes);
            if (id>0) {
                Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTimeDisplay() {
        tvTime.setText(String.format("%02d:%02d", hour, minute));
    }
    private void updateStartDateDisplay() {
        tvStartDate.setText(String.format("%04d-%02d-%02d", startYear, startMonth+1, startDay));
    }
    private void updateEndDateDisplay() {
        tvEndDate.setText(String.format("%04d-%02d-%02d", endYear, endMonth+1, endDay));
    }
}
