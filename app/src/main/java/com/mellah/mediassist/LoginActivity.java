package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private MediAssistDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new MediAssistDatabaseHelper(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean valid = dbHelper.checkUser(username, password);
                if (valid) {
                    int userId = dbHelper.getUserId(username);
                    // Use a separate SharedPreferences file for user sessions
                    SharedPreferences prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putInt("currentUserId", userId); // Storing userId for future use
                    editor.putString("username", username); // Storing username for future use
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter username and password to register", Toast.LENGTH_SHORT).show();
                    return;
                }

                long id = dbHelper.addUser(username, password, "", -1, "", -1, "");
                if (id > 0) {
                    Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Registration failed: Username may already exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Clear the input fields when returning to the LoginActivity.
        etUsername.setText("");
        etPassword.setText("");
    }
}