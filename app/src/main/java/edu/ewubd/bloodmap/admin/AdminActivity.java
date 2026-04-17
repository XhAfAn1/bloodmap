package edu.ewubd.bloodmap.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.ewubd.bloodmap.Authentication.AuthActivity;
import edu.ewubd.bloodmap.admin.bloodbankManagement.AdminBloodBanksActivity;
import edu.ewubd.bloodmap.admin.hospitalsManagement.AdminHospitalsActivity;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.locationManagement.AdminManageLocationsActivity;
import edu.ewubd.bloodmap.admin.requestManagement.AdminRequestsActivity;
import edu.ewubd.bloodmap.admin.userManagement.UserListActivity;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private TextView tvUsersCount, tvHospitalsCount, tvBloodBanksCount;
    private TextView  tvActiveRequestsCount, tvCompletedRequestsCount, tvAvailableDonorsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        drawer = findViewById(R.id.admin_drawer_layout);
        tvUsersCount = findViewById(R.id.tvUsersCount);
        tvHospitalsCount = findViewById(R.id.tvHospitalsCount);
        tvBloodBanksCount = findViewById(R.id.tvBloodBanksCount);
        tvActiveRequestsCount = findViewById(R.id.tvActiveRequestsCount);
        tvCompletedRequestsCount = findViewById(R.id.tvCompletedRequestsCount);
        tvAvailableDonorsCount = findViewById(R.id.tvAvailableDonorsCount);

        findViewById(R.id.menu_icon).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        findViewById(R.id.menu_show_users).setOnClickListener(v -> closeDrawerAndStart(UserListActivity.class));
        findViewById(R.id.menu_show_blood_banks).setOnClickListener(v -> closeDrawerAndStart(AdminBloodBanksActivity.class));
        findViewById(R.id.menu_show_hospitals).setOnClickListener(v -> closeDrawerAndStart(AdminHospitalsActivity.class));
        findViewById(R.id.menu_manage_locations).setOnClickListener(v -> closeDrawerAndStart(AdminManageLocationsActivity.class));
        findViewById(R.id.menu_active_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("OPEN"));
        findViewById(R.id.menu_previous_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("COMPLETED"));
        
        findViewById(R.id.menu_logout_admin).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });


    }
 
    @Override
    protected void onStart() {
        super.onStart();
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Show loading state
        tvUsersCount.setText("...");
        tvAvailableDonorsCount.setText("...");
        tvHospitalsCount.setText("...");
        tvBloodBanksCount.setText("...");
        tvActiveRequestsCount.setText("...");
        tvCompletedRequestsCount.setText("...");
 
        db.collection("users").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvUsersCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvUsersCount.setText(String.valueOf(snapshots.size()));
            }
        });
 
        db.collection("users").whereEqualTo("availableToDonate", true).addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvAvailableDonorsCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvAvailableDonorsCount.setText(String.valueOf(snapshots.size()));
            }
        });
 
        db.collection("hospitals").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvHospitalsCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvHospitalsCount.setText(String.valueOf(snapshots.size()));
            }
        });
 
        db.collection("blood_banks").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvBloodBanksCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvBloodBanksCount.setText(String.valueOf(snapshots.size()));
            }
        });
 
        db.collection("transactions").whereEqualTo("status", "OPEN").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvActiveRequestsCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvActiveRequestsCount.setText(String.valueOf(snapshots.size()));
            }
        });

        db.collection("transactions").whereEqualTo("status", "COMPLETED").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) {
                tvCompletedRequestsCount.setText("Error");
                return;
            }
            if (snapshots != null) {
                tvCompletedRequestsCount.setText(String.valueOf(snapshots.size()));
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