package com.mellah.mediassist;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

public class ScheduleActivity extends AppCompatActivity {
    private RecyclerView rvSchedule;
    private ScheduleAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvSchedule = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedule();
    }

    private void loadSchedule() {
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        // Combine meds and appts cursor in adapter
        Cursor meds = dbHelper.getAllMedications(userId);
        Cursor appts = dbHelper.getAllAppointments(userId);
        if (meds != null || appts != null) {
            adapter = new ScheduleAdapter(this, meds, appts);
            rvSchedule.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No schedule items found", Toast.LENGTH_SHORT).show();
        }
    }
}
