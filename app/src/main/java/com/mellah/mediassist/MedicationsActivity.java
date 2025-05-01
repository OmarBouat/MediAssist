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

public class MedicationsActivity extends AppCompatActivity
        implements MedicationAdapter.OnMedicationActionListener {
    private RecyclerView rvMeds;
    private MedicationAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medications);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvMeds = findViewById(R.id.rvMedications);
        rvMeds.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAddMedication);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicationActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }

    private void loadMedications() {
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor cursor = dbHelper.getAllMedications(userId);
        if (cursor != null) {
            adapter = new MedicationAdapter(this, cursor, this);
            rvMeds.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No medications found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEdit(int medId, String name, String dosage,
                       String timesJson, String startDate, String endDate, String notes) {
        Intent i = new Intent(this, AddMedicationActivity.class);
        i.putExtra("medId", medId);
        i.putExtra("name", name);
        i.putExtra("dosage", dosage);
        i.putExtra("timesJson", timesJson);
        i.putExtra("startDate", startDate);
        i.putExtra("endDate", endDate);
        i.putExtra("notes", notes);
        startActivity(i);
    }

    @Override
    public void onDelete(int medId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medication?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteMedication(medId);
                    loadMedications();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
