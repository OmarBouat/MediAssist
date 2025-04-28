package com.mellah.mediassist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

public class PrescriptionsActivity extends AppCompatActivity {
    private RecyclerView rvRx;
    private PrescriptionAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptions);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvRx = findViewById(R.id.rvPrescriptions);
        rvRx.setLayoutManager(new GridLayoutManager(this, 2));

        fabAdd = findViewById(R.id.fabAddPrescription);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPrescriptionActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        Cursor cursor = dbHelper.getAllPrescriptions();
        if (cursor != null) {
            adapter = new PrescriptionAdapter(this, cursor);
            rvRx.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No prescriptions found", Toast.LENGTH_SHORT).show();
        }
    }
}
