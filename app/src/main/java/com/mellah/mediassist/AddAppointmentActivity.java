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

public class AddAppointmentActivity extends AppCompatActivity {
    private EditText etTitle, etNotes;
    private TextView tvDate, tvTime;
    private Button btnPickDate, btnPickTime, btnSave;
    private MediAssistDatabaseHelper dbHelper;

    private int apptYear, apptMonth, apptDay;
    private int apptHour, apptMinute;

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

        dbHelper = new MediAssistDatabaseHelper(this);

        // Initialize with current date/time
        Calendar cal = Calendar.getInstance();
        apptYear   = cal.get(Calendar.YEAR);
        apptMonth  = cal.get(Calendar.MONTH);
        apptDay    = cal.get(Calendar.DAY_OF_MONTH);
        apptHour   = cal.get(Calendar.HOUR_OF_DAY);
        apptMinute = cal.get(Calendar.MINUTE);
        updateDateDisplay();
        updateTimeDisplay();

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(
                        AddAppointmentActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                apptYear  = year;
                                apptMonth = month;
                                apptDay   = dayOfMonth;
                                updateDateDisplay();
                            }
                        }, apptYear, apptMonth, apptDay);
                dpd.show();
            }
        });

        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(
                        AddAppointmentActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                apptHour   = hourOfDay;
                                apptMinute = minute;
                                updateTimeDisplay();
                            }
                        }, apptHour, apptMinute, true);
                tpd.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String notes = etNotes.getText().toString().trim();

                if (title.isEmpty()) {
                    Toast.makeText(AddAppointmentActivity.this, "Enter appointment title", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Format date/time
                String date = String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay);
                String time = String.format("%02d:%02d", apptHour, apptMinute);
                int reminderOffset = 60; // default 60 minutes before, adjust or add UI if needed

                long id = dbHelper.addAppointment(title, date, time, reminderOffset, notes);
                if (id > 0) {
                    Toast.makeText(AddAppointmentActivity.this, "Appointment added", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAppointmentActivity.this, "Failed to add appointment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateDateDisplay() {
        tvDate.setText(String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay));
    }

    private void updateTimeDisplay() {
        tvTime.setText(String.format("%02d:%02d", apptHour, apptMinute));
    }
}
