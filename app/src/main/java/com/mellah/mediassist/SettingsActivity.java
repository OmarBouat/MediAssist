package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    private Spinner spinnerCountryCode;
    private Switch switchTheme;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);
        switchTheme        = findViewById(R.id.switchTheme);

        prefs = getSharedPreferences("MediAssistPrefs", Context.MODE_PRIVATE);

        setupCountryCodeSpinner();
        setupThemeSwitch();

        findViewById(R.id.btnSignOut).setOnClickListener(v -> signOut());
    }

    private void setupCountryCodeSpinner() {
        String[] names = getResources().getStringArray(R.array.country_names);
        String[] codes = getResources().getStringArray(R.array.country_codes);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);

        // Pre-select saved code
        String savedCode = prefs.getString("country_code", "+216");
        int idx = Arrays.asList(codes).indexOf(savedCode);
        if (idx >= 0) spinnerCountryCode.setSelection(idx);

        spinnerCountryCode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       View view, int position, long id) {
                String chosenCode = codes[position];
                prefs.edit().putString("country_code", chosenCode).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupThemeSwitch() {
        // existing dark-mode logic (if any), for example:
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchTheme.setChecked(isDark);
        switchTheme.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean("dark_mode", checked).apply();
            // apply theme change...
        });
    }

    private void signOut() {
        // Clear app session (if needed)

        SharedPreferences user = getSharedPreferences("user_session", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = user.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
