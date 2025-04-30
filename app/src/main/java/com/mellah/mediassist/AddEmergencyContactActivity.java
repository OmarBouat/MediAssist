package com.mellah.mediassist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEmergencyContactActivity extends AppCompatActivity {
    private EditText etName, etPhone, etRelation;
    private Button btnSave;
    private MediAssistDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        etName     = findViewById(R.id.etECName);
        etPhone    = findViewById(R.id.etECPhone);
        etRelation = findViewById(R.id.etECRelation);
        btnSave    = findViewById(R.id.btnSaveEC);

        dbHelper = new MediAssistDatabaseHelper(this);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relation = etRelation.getText().toString().trim();
            if (name.isEmpty()||phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                    .getInt("currentUserId", -1);

            long id = dbHelper.addEmergencyContact(userId, name, phone, relation);
            if (id>0) {
                Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
