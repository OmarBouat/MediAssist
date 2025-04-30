package com.mellah.mediassist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

public class AppointmentsActivity extends AppCompatActivity {
    private RecyclerView rvAppts;
    private AppointmentAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvAppts = findViewById(R.id.rvAppointments);
        rvAppts.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAddAppointment);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddAppointmentActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();
    }

    private void loadAppointments() {
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor cursor = dbHelper.getAllAppointments(userId);
        if (cursor != null) {
            adapter = new AppointmentAdapter(this, cursor);
            rvAppts.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No appointments found", Toast.LENGTH_SHORT).show();
        }
    }
}

