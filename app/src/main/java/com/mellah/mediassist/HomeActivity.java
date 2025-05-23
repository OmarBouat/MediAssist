package com.mellah.mediassist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {
    private ImageButton btnMedications, btnAppointments, btnPrescriptions, btnContacts, btnSchedule, btnSettings, btnProfile, btnAiDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("MediAssist");

        // Initialize image buttons
        btnMedications    = findViewById(R.id.btnMedications);
        btnAppointments   = findViewById(R.id.btnAppointments);
        btnPrescriptions  = findViewById(R.id.btnPrescriptions);
        btnContacts       = findViewById(R.id.btnContacts);
        btnSchedule       = findViewById(R.id.btnSchedule);
        btnSettings       = findViewById(R.id.btnSettings);
        btnProfile        = findViewById(R.id.btnProfile);
        btnAiDoctor      = findViewById(R.id.btnAiDoctor);

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
        });
        btnAiDoctor.setOnClickListener(v ->
                startActivity(new Intent(this, AiDoctorActivity.class))
        );
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
