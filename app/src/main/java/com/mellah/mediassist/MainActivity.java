package com.mellah.mediassist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);


        startActivity(new Intent(this, LoginActivity.class));
        finish();
        /*
        // Check if the app has been configured
        boolean isConfigured = prefs.getBoolean("configured", false);
        if (!isConfigured) {
            // Not configured yet, so show the welcome/setup screen.
            startActivity(new Intent(this, WelcomeSetupActivity.class));
            finish();
            return;
        }

        // Check the user type.
        // For example, "patient" or "caregiver"
        String userRole = prefs.getString("user_role", "patient"); // default to "patient"

        if (userRole.equals("patient")) {
            // Direct patient to the home screen.
            startActivity(new Intent(this, HomeActivity.class));
        } else if (userRole.equals("caregiver")) {
            // Direct caregiver to a PIN login screen.
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();*/
    }
}