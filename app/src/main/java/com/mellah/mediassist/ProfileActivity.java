package com.mellah.mediassist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private EditText etUsername, etBloodType, etAge, etWeight, etAllergies;
    private Spinner  spGender;
    private Button   btnSave;
    private MediAssistDatabaseHelper dbHelper;
    private int      userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View refs
        etUsername  = findViewById(R.id.etUsername);
        etBloodType = findViewById(R.id.etBloodType);
        etAge       = findViewById(R.id.etAge);
        etWeight    = findViewById(R.id.etWeight);
        etAllergies = findViewById(R.id.etAllergies);
        spGender    = findViewById(R.id.spGender);
        btnSave     = findViewById(R.id.btnSaveProfile);

        dbHelper = new MediAssistDatabaseHelper(this);

        // Populate gender spinner
        String[] genders = new String[]{"Male","Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genders
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Get current user ID from session prefs
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = prefs.getInt("currentUserId", -1);
        if (userId < 0) {
            Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load and display profile
        loadProfile(genderAdapter);

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile(ArrayAdapter<String> genderAdapter) {
        try (Cursor c = dbHelper.getUser(userId)) {
            if (c != null && c.moveToFirst()) {
                String username  = c.getString(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_USERNAME));
                String bloodType = c.getString(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_BLOOD_TYPE));
                int age          = c.getInt(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_AGE));
                String gender    = c.getString(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_GENDER));
                double weight    = c.getDouble(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_WEIGHT));
                String allergies = c.getString(c.getColumnIndexOrThrow(
                        MediAssistDatabaseHelper.COLUMN_USER_ALLERGIES));

                etUsername.setText(username);
                etBloodType.setText(bloodType != null ? bloodType : "");
                etAge.setText(age > 0 ? String.valueOf(age) : "");
                etWeight.setText(weight > 0 ? String.valueOf(weight) : "");
                etAllergies.setText(allergies != null ? allergies : "");

                // Select gender in spinner
                int pos = genderAdapter.getPosition(
                        gender != null ? gender : "Other");
                if (pos >= 0) spGender.setSelection(pos);
            }
        }
    }

    private void saveProfile() {
        String bloodType = etBloodType.getText().toString().trim();
        String ageStr    = etAge.getText().toString().trim();
        String gender    = spGender.getSelectedItem().toString();
        String weightStr = etWeight.getText().toString().trim();
        String allergies = etAllergies.getText().toString().trim();

        int    age    = ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr);
        double weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);

        boolean ok = dbHelper.updateUser(
                userId,
                bloodType,
                age,
                gender,
                weight,
                allergies
        );

        Toast.makeText(
                this,
                ok ? "Profile updated" : "Update failed",
                Toast.LENGTH_SHORT
        ).show();
    }
}
