package edu.ewubd.bloodmap.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.ewubd.bloodmap.ClassModels.userModel;
import edu.ewubd.bloodmap.MainActivity;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.AdminActivity;

public class AuthActivity extends AppCompatActivity {

    private boolean isLoginMode = true;
    private TextView tvTitle;
    private LinearLayout llName;
    private EditText etName, etEmail, etPassword;
    private Button btnSubmit;
    private TextView tvSwitchMode;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Check if already logged in
        if (mAuth.getCurrentUser() != null) {
            checkAdminAndRoute(mAuth.getCurrentUser().getUid());
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

        if (isLoginMode) {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        if (mAuth.getCurrentUser() != null) {
                            checkAdminAndRoute(mAuth.getCurrentUser().getUid());
                        } else {
                            startMainActivity();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        } else {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userModel model = new userModel(user.getUid(), name, email);
                            db.collection("users").document(user.getUid()).set(model)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                    checkAdminAndRoute(user.getUid());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                        }
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkAdminAndRoute(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                userModel user = documentSnapshot.toObject(userModel.class);
                if (user != null && user.isAdmin()) {
                    startActivity(new Intent(this, AdminActivity.class));
                    finish();
                } else {
                    startMainActivity();
                }
            } else {
                startMainActivity();
            }
        }).addOnFailureListener(e -> {
            startMainActivity();
        });
    }
}
