package edu.ewubd.bloodmap.DrawerPages;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import edu.ewubd.bloodmap.R;

public class ReceivedHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_history);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }
}
