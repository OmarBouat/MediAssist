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

public class EmergencyContactsActivity extends AppCompatActivity {
    private RecyclerView rvContacts;
    private ContactAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        dbHelper = new MediAssistDatabaseHelper(this);
        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAddContact);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEmergencyContactActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor cursor = dbHelper.getAllEmergencyContacts(userId);
        if (cursor != null) {
            adapter = new ContactAdapter(this, cursor);
            rvContacts.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show();
        }
    }
}
