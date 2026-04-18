package edu.ewubd.bloodmap.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import edu.ewubd.bloodmap.Authentication.AuthActivity;
import edu.ewubd.bloodmap.Notifications.NotificationSender;
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
    private EditText etBroadcastTitle, etBroadcastMessage;

    private com.google.firebase.firestore.ListenerRegistration usersReg, donorsReg, hospitalsReg, banksReg, activeReqReg, completedReqReg;

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

        etBroadcastTitle = findViewById(R.id.et_broadcast_title);
        etBroadcastMessage = findViewById(R.id.et_broadcast_message);

        findViewById(R.id.menu_icon).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        findViewById(R.id.menu_show_users).setOnClickListener(v -> closeDrawerAndStart(UserListActivity.class));
        findViewById(R.id.menu_show_blood_banks).setOnClickListener(v -> closeDrawerAndStart(AdminBloodBanksActivity.class));
        findViewById(R.id.menu_show_hospitals).setOnClickListener(v -> closeDrawerAndStart(AdminHospitalsActivity.class));
        findViewById(R.id.menu_manage_locations).setOnClickListener(v -> closeDrawerAndStart(AdminManageLocationsActivity.class));
        findViewById(R.id.menu_active_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("OPEN"));
        findViewById(R.id.menu_previous_requests).setOnClickListener(v -> closeDrawerAndStartAdminRequests("COMPLETED"));
        
        findViewById(R.id.btn_mass_alert).setOnClickListener(v -> {
            String title = etBroadcastTitle.getText().toString().trim();
            String message = etBroadcastMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please enter both title and message", Toast.LENGTH_SHORT).show();
                return;
            }

            sendMassNotification(title, message);
        });

        findViewById(R.id.menu_logout_admin).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("token", null)
                        .addOnCompleteListener(task -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(this, AuthActivity.class));
                            finish();
                        });
            } else {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            }
        });


    }
 
    @Override
    protected void onStart() {
        super.onStart();
        loadDashboardStats();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (usersReg != null) { usersReg.remove(); usersReg = null; }
        if (donorsReg != null) { donorsReg.remove(); donorsReg = null; }
        if (hospitalsReg != null) { hospitalsReg.remove(); hospitalsReg = null; }
        if (banksReg != null) { banksReg.remove(); banksReg = null; }
        if (activeReqReg != null) { activeReqReg.remove(); activeReqReg = null; }
        if (completedReqReg != null) { completedReqReg.remove(); completedReqReg = null; }
    }

    private void loadDashboardStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        tvUsersCount.setText("...");
        tvAvailableDonorsCount.setText("...");
        tvHospitalsCount.setText("...");
        tvBloodBanksCount.setText("...");
        tvActiveRequestsCount.setText("...");
        tvCompletedRequestsCount.setText("...");

        usersReg = db.collection("users").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvUsersCount.setText("Error"); return; }
            if (snapshots != null) tvUsersCount.setText(String.valueOf(snapshots.size()));
        });

        donorsReg = db.collection("users").whereEqualTo("availableToDonate", true).addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvAvailableDonorsCount.setText("Error"); return; }
            if (snapshots != null) tvAvailableDonorsCount.setText(String.valueOf(snapshots.size()));
        });

        hospitalsReg = db.collection("hospitals").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvHospitalsCount.setText("Error"); return; }
            if (snapshots != null) tvHospitalsCount.setText(String.valueOf(snapshots.size()));
        });

        banksReg = db.collection("blood_banks").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvBloodBanksCount.setText("Error"); return; }
            if (snapshots != null) tvBloodBanksCount.setText(String.valueOf(snapshots.size()));
        });

        activeReqReg = db.collection("transactions").whereEqualTo("status", "OPEN").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvActiveRequestsCount.setText("Error"); return; }
            if (snapshots != null) tvActiveRequestsCount.setText(String.valueOf(snapshots.size()));
        });

        completedReqReg = db.collection("transactions").whereEqualTo("status", "COMPLETED").addSnapshotListener(this, (snapshots, e) -> {
            if (e != null) { tvCompletedRequestsCount.setText("Error"); return; }
            if (snapshots != null) tvCompletedRequestsCount.setText(String.valueOf(snapshots.size()));
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

    private void sendMassNotification(String title, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereNotEqualTo("token", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String token = doc.getString("token");
                        if (token != null && !token.isEmpty()) {
                            NotificationSender.sendAdminBroadcast(this, token, title, message);
                            count++;
                        }
                    }
                    if (count > 0) {
                        Toast.makeText(this, "Broadcasting to " + count + " users...", Toast.LENGTH_LONG).show();
                        etBroadcastTitle.setText("");
                        etBroadcastMessage.setText("");
                    } else {
                        Toast.makeText(this, "No users with valid tokens found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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