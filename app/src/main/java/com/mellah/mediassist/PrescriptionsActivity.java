package com.mellah.mediassist;

import android.content.Intent;
import android.content.pm.PackageManager;
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrescriptions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied! Cannot load images.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadPrescriptions() {
        int userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor cursor = dbHelper.getAllPrescriptions(userId);
        if (cursor != null) {
            adapter = new PrescriptionAdapter(this, cursor);
            rvRx.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No prescriptions found", Toast.LENGTH_SHORT).show();
        }
    }
}
