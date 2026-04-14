package edu.ewubd.bloodmap.DrawerPages;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import edu.ewubd.bloodmap.R;

public class BloodBanksActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_banks);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }
}
