package com.collab.productivity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.collab.productivity.R;
import com.collab.productivity.utils.FirebaseManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity - Handles user authentication
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText displayNameInput;
    private View displayNameLayout;
    private Button loginButton;
    private Button signupButton;
    private TextView toggleAuthModeText;
    private ProgressBar progressBar;
    private FirebaseManager firebaseManager;

    private boolean isSignupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseManager = FirebaseManager.getInstance();

        // Check if user is already logged in
        if (firebaseManager.isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        displayNameInput = findViewById(R.id.display_name_input);
        displayNameLayout = findViewById(R.id.display_name_layout);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);
        toggleAuthModeText = findViewById(R.id.toggle_auth_mode);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v -> handleSignup());
        toggleAuthModeText.setOnClickListener(v -> toggleAuthMode());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInputs(email, password, false)) {
            return;
        }

        showLoading(true);

        firebaseManager.signIn(email, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleSignup() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String displayName = displayNameInput.getText().toString().trim();

        if (!validateInputs(email, password, true)) {
            return;
        }

        showLoading(true);

        firebaseManager.signUp(email, password, displayName, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Signup failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(String email, String password, boolean isSignup) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }

        if (isSignup) {
            String displayName = displayNameInput.getText().toString().trim();
            if (displayName.isEmpty()) {
                displayNameInput.setError("Display name is required");
                displayNameInput.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void toggleAuthMode() {
        isSignupMode = !isSignupMode;

        if (isSignupMode) {
            displayNameLayout.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            signupButton.setVisibility(View.VISIBLE);
            toggleAuthModeText.setText("Already have an account? Login");
        } else {
            displayNameLayout.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            signupButton.setVisibility(View.GONE);
            toggleAuthModeText.setText("Don't have an account? Sign up");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        signupButton.setEnabled(!show);
        emailInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
        displayNameInput.setEnabled(!show);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

