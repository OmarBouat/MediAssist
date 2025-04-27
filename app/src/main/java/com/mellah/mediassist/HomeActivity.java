package com.mellah.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {
    private Button btnMedications, btnAppointments, btnPrescriptions, btnContacts, btnSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MediAssist");

        // Initialize buttons
        btnMedications    = findViewById(R.id.btnMedications);
        btnAppointments   = findViewById(R.id.btnAppointments);
        btnPrescriptions  = findViewById(R.id.btnPrescriptions);
        btnContacts       = findViewById(R.id.btnContacts);
        btnSchedule       = findViewById(R.id.btnSchedule);
/*
        // Set listeners
        btnMedications.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MedicationsActivity.class));
        });
        btnAppointments.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AppointmentsActivity.class));
        });
        btnPrescriptions.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, PrescriptionsActivity.class));
        });
        btnContacts.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, EmergencyContactsActivity.class));
        });
        btnSchedule.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ScheduleActivity.class));
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {/*
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;*/
        }
        return super.onOptionsItemSelected(item);
    }
}