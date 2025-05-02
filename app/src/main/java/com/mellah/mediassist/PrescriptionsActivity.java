package com.mellah.mediassist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;

public class PrescriptionsActivity
        extends AppCompatActivity
        implements PrescriptionAdapter.OnPrescriptionActionListener {

    private RecyclerView rvRx;
    private PrescriptionAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescriptions);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvRx     = findViewById(R.id.rvPrescriptions);
        rvRx.setLayoutManager(new GridLayoutManager(this, 1));

        fabAdd = findViewById(R.id.fabAddPrescription);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddPrescriptionActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        int userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor cursor = dbHelper.getAllPrescriptions(userId);
        adapter = new PrescriptionAdapter(this, cursor, this);
        rvRx.setAdapter(adapter);
    }

    @Override
    public void onEdit(int rxId, String imagePath, String description) {
        Intent i = new Intent(this, AddPrescriptionActivity.class);
        i.putExtra("rxId",        rxId);
        i.putExtra("imagePath", imagePath != null ? imagePath : "");
        i.putExtra("description", description);
        startActivity(i);
    }

    @Override
    public void onDelete(int rxId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Prescription?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.deletePrescription(rxId);
                    loadPrescriptions();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
