package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnSignOut = findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        // Clear user session data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Removes all key-value pairs from the preferences
        editor.apply();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Go back to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear the activity stack so the user can't go back to the HomeActivity by pressing back
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}