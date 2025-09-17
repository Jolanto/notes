package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout usernameLayout, passwordLayout;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        progressBar = findViewById(R.id.progressBar);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Set click listener for register text
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void attemptLogin() {
        // Reset errors
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        // Store values at the time of the login attempt
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_field_required));
            focusView = passwordLayout;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordLayout.setError(getString(R.string.error_invalid_password));
            focusView = passwordLayout;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = usernameLayout;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt
            showProgress(true);
            performLogin(username, password);
        }
    }

    private boolean isPasswordValid(String password) {
        // Add your own validation logic here
        return password.length() >= 4;
    }

    private void performLogin(final String username, final String password) {
        // This is where you would perform the actual login
        // For now, we'll simulate network access.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean isValidUser = databaseHelper.checkUser(username, password);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isValidUser) {
                                        // Login successful
                                        User user = databaseHelper.getUser(username);
                                        if (user != null) {
                                            try {
                                                // Save user session
                                                SessionManager sessionManager = new SessionManager(LoginActivity.this);
                                                sessionManager.createLoginSession(user.getId(), user.getUsername());
                                                
                                                // Launch main activity
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                showProgress(false);
                                                Toast.makeText(LoginActivity.this, 
                                                    "Error creating session: " + e.getMessage(), 
                                                    Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        // Login failed
                                        showProgress(false);
                                        passwordLayout.setError("Invalid username or password");
                                        etPassword.requestFocus();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(LoginActivity.this, 
                                        "Login error: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }, 1500);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
