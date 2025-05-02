package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME     = "MediAssistPrefs";
    private static final String KEY_FIRST_RUN  = "isFirstRun";
    private static final String USER_SESSION   = "user_session";
    private static final String KEY_LOGGED_IN  = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) First-run check
        SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isFirstRun = appPrefs.getBoolean(KEY_FIRST_RUN, true);
        if (isFirstRun) {
            // mark that we've shown the welcome screen
            appPrefs.edit()
                    .putBoolean(KEY_FIRST_RUN, false)
                    .apply();

            // launch the welcome/setup activity
            startActivity(new Intent(this, WelcomeSetupActivity.class));
            finish();
            return;
        }

        // 2) Normal login flow
        SharedPreferences userPrefs = getSharedPreferences(USER_SESSION, Context.MODE_PRIVATE);
        boolean isLoggedIn = userPrefs.getBoolean(KEY_LOGGED_IN, false);

        if (isLoggedIn) {
            // already logged in → Home
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // not logged in → Login
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
