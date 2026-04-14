package edu.ewubd.bloodmap.Authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import edu.ewubd.bloodmap.MainActivity;
import edu.ewubd.bloodmap.R;

public class AuthActivity extends AppCompatActivity {

    private boolean isLoginMode = true;
    private TextView tvTitle;
    private LinearLayout llName;
    private EditText etName, etEmail, etPassword;
    private Button btnSubmit;
    private TextView tvSwitchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            startMainActivity();
            return;
        }
        
        setContentView(R.layout.activity_auth);

        tvTitle = findViewById(R.id.tvTitle);
        llName = findViewById(R.id.llName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);

        tvSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });

        btnSubmit.setOnClickListener(v -> handleAuth());
    }

    private void updateUI() {
        if (isLoginMode) {
            tvTitle.setText("Login");
            llName.setVisibility(View.GONE);
            btnSubmit.setText("Login");
            tvSwitchMode.setText("Don't have an account? Sign Up");
        } else {
            tvTitle.setText("Sign Up");
            llName.setVisibility(View.VISIBLE);
            btnSubmit.setText("Sign Up");
            tvSwitchMode.setText("Already have an account? Login");
        }
    }

    private void handleAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences( "AuthPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (isLoginMode) {
            String savedEmail = prefs.getString("email", null);
            String savedPass = prefs.getString("password", null);
            
            if (savedEmail != null && savedEmail.equals(email) && savedPass != null && savedPass.equals(password)) {
                editor.putBoolean("isLoggedIn", true);
                editor.apply();
                startMainActivity();
            } else {
                Toast.makeText(this, "Invalid credentials or account not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("password", password);
            editor.putBoolean("isLoggedIn", true);
            editor.apply();
            Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
