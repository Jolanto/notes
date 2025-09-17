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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private TextInputLayout usernameLayout, passwordLayout, confirmPasswordLayout;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        progressBar = findViewById(R.id.progressBar);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Set click listener for register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        // Set click listener for login text
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                finish();
            }
        });
    }

    private void attemptRegistration() {
        // Reset errors
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Store values at the time of the registration attempt
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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

        // Check for password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordLayout;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_password_mismatch));
            focusView = confirmPasswordLayout;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = usernameLayout;
            cancel = true;
        } else if (databaseHelper.isUsernameTaken(username)) {
            usernameLayout.setError(getString(R.string.error_username_taken));
            focusView = usernameLayout;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt
            showProgress(true);
            performRegistration(username, password);
        }
    }

    private boolean isPasswordValid(String password) {
        // Add your own validation logic here
        return password.length() >= 4;
    }

    private void performRegistration(final String username, final String password) {
        // This is where you would perform the actual registration
        // For now, we'll simulate network access.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        // Create new user with plain text password
                        User newUser = new User(username, password);
                        long userId = databaseHelper.addUser(newUser);
                        
                        if (userId == -1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                    Toast.makeText(RegisterActivity.this, 
                                            "Username already exists. Please choose a different one.", 
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                
                                if (userId != -1) {
                                    // Registration successful
                                    Toast.makeText(RegisterActivity.this, 
                                            "Registration successful!", 
                                            Toast.LENGTH_SHORT).show();
                                    
                                    // Automatically log in the user
                                    User user = databaseHelper.getUser(username);
                                    if (user != null) {
                                        // Save user session
                                        SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                                        sessionManager.createLoginSession(user.getId(), user.getUsername());
                                        
                                        // Launch main activity
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    // Registration failed
                                    Toast.makeText(RegisterActivity.this, 
                                            "Registration failed. Please try again.", 
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }, 1500);
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
