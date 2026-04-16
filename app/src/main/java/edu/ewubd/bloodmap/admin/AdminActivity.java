package edu.ewubd.bloodmap.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.ewubd.bloodmap.Authentication.AuthActivity;
import edu.ewubd.bloodmap.DrawerPages.nearBloodBank.BloodBanksActivity;
import edu.ewubd.bloodmap.DrawerPages.nearHospital.HospitalContactsActivity;
import edu.ewubd.bloodmap.R;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private TextView tvUsersCount, tvHospitalsCount, tvBloodBanksCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        drawer = findViewById(R.id.admin_drawer_layout);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvHospitalsCount = findViewById(R.id.tvHospitalsCount);
        tvBloodBanksCount = findViewById(R.id.tvBloodBanksCount);

        findViewById(R.id.menu_icon).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        findViewById(R.id.menu_show_users).setOnClickListener(v -> closeDrawerAndStart(UserListActivity.class));
        findViewById(R.id.menu_show_blood_banks).setOnClickListener(v -> closeDrawerAndStart(BloodBanksActivity.class));
        findViewById(R.id.menu_show_hospitals).setOnClickListener(v -> closeDrawerAndStart(HospitalContactsActivity.class));
        findViewById(R.id.menu_manage_locations).setOnClickListener(v -> closeDrawerAndStart(AdminManageLocationsActivity.class));
        findViewById(R.id.menu_active_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("OPEN"));
        findViewById(R.id.menu_previous_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("COMPLETED"));
        
        findViewById(R.id.menu_logout_admin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tvUsersCount.setText(String.valueOf(task.getResult().getCount()));
            } else {
                tvUsersCount.setText("Error");
            }
        });

        db.collection("hospitals").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tvHospitalsCount.setText(String.valueOf(task.getResult().getCount()));
            } else {
                tvHospitalsCount.setText("Error");
            }
        });

        db.collection("blood_banks").count().get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tvBloodBanksCount.setText(String.valueOf(task.getResult().getCount()));
            } else {
                tvBloodBanksCount.setText("Error");
            }
        });
    }

    private void closeDrawerAndStart(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void closeDrawerAndStartAdminRequests(String status) {
        Intent intent = new Intent(this, AdminRequestsActivity.class);
        intent.putExtra("STATUS_FILTER", status);
        startActivity(intent);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void closeDrawerAndToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}