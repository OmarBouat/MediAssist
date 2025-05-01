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

public class EmergencyContactsActivity extends AppCompatActivity
        implements ContactAdapter.OnContactActionListener {

    private RecyclerView rvContacts;
    private ContactAdapter adapter;
    private MediAssistDatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        dbHelper   = new MediAssistDatabaseHelper(this);
        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAddContact);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEmergencyContactActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        int userId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor c = dbHelper.getAllEmergencyContacts(userId);
        adapter = new ContactAdapter(this, c, this);
        rvContacts.setAdapter(adapter);
    }

    @Override
    public void onEdit(int contactId, String name, String phone, String relation) {
        Intent i = new Intent(this, AddEmergencyContactActivity.class);
        i.putExtra("ecId",      contactId);
        i.putExtra("name",      name);
        i.putExtra("phone",     phone);
        i.putExtra("relation",  relation);
        startActivity(i);
    }

    @Override
    public void onDelete(int contactId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.deleteEmergencyContact(contactId);
                    loadContacts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
