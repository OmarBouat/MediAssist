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

public class MedicationsActivity extends AppCompatActivity {
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

        fabAdd = findViewById(R.id.fabAddMedication);/*
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMedicationActivity.class));
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }

    private void loadMedications() {
        Cursor cursor = dbHelper.getAllMedications();
        if (cursor != null) {
            adapter = new MedicationAdapter(this, cursor);
            rvMeds.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No medications found", Toast.LENGTH_SHORT).show();
        }
    }
}
