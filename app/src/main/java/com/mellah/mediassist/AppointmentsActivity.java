package com.mellah.mediassist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;

public class AppointmentsActivity extends AppCompatActivity
        implements AppointmentAdapter.OnAppointmentActionListener {
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
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddAppointmentActivity.class))
        );
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
        adapter = new AppointmentAdapter(this, cursor, this);
        rvAppts.setAdapter(adapter);
    }

    @Override
    public void onEdit(int apptId, String title, String date, String time, int offset, String notes) {
        Intent i = new Intent(this, AddAppointmentActivity.class);
        i.putExtra("apptId", apptId);
        i.putExtra("title",  title);
        i.putExtra("date",   date);
        i.putExtra("time",   time);
        i.putExtra("offset", offset);
        i.putExtra("notes",  notes);
        startActivity(i);
    }

    @Override
    public void onDelete(int apptId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Appointment?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d,w) -> {
                    dbHelper.deleteAppointment(apptId);
                    loadAppointments();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
