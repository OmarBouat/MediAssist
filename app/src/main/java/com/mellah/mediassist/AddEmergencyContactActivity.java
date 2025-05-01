package com.mellah.mediassist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddEmergencyContactActivity extends AppCompatActivity {
    private EditText etName, etPhone, etRelation;
    private Button btnSave;
    private MediAssistDatabaseHelper dbHelper;

    private int ecId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emergency_contact);

        etName     = findViewById(R.id.etECName);
        etPhone    = findViewById(R.id.etECPhone);
        etRelation = findViewById(R.id.etECRelation);
        btnSave    = findViewById(R.id.btnSaveEC);

        dbHelper = new MediAssistDatabaseHelper(this);

        // Edit mode?
        Intent intent = getIntent();
        if (intent.hasExtra("ecId")) {
            ecId = intent.getIntExtra("ecId", -1);
            etName.setText(intent.getStringExtra("name"));
            etPhone.setText(intent.getStringExtra("phone"));
            etRelation.setText(intent.getStringExtra("relation"));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String rawPhone = etPhone.getText().toString().trim();
            String relation = etRelation.getText().toString().trim();
            if (name.isEmpty() || rawPhone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prefix country code from settings if needed
            SharedPreferences prefs = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE);
            String countryCode = prefs.getString("country_code", "+216");
            String phone;
            if (rawPhone.startsWith("+")) {
                phone = rawPhone;           // already includes code
            } else {
                phone = countryCode + rawPhone;
            }

            int userId = prefs.getInt("currentUserId", -1);
            boolean success;
            if (ecId >= 0) {
                success = dbHelper.updateEmergencyContact(ecId, name, phone, relation);
            } else {
                long id = dbHelper.addEmergencyContact(userId, name, phone, relation);
                success = id > 0;
            }

            if (success) {
                Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
