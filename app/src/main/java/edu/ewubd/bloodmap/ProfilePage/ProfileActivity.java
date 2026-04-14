package edu.ewubd.bloodmap.ProfilePage;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import edu.ewubd.bloodmap.R;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }
}
