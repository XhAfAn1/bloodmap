package edu.ewubd.bloodmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.ewubd.bloodmap.Authentication.AuthActivity;
import edu.ewubd.bloodmap.DrawerPages.ChatbotActivity;
import edu.ewubd.bloodmap.DrawerPages.nearBloodBank.BloodBanksActivity;
import edu.ewubd.bloodmap.DrawerPages.donationHistory.DonationHistoryActivity;
import edu.ewubd.bloodmap.DrawerPages.nearHospital.HospitalContactsActivity;
import edu.ewubd.bloodmap.DrawerPages.myRequst.OpenRequestsActivity;
import edu.ewubd.bloodmap.DrawerPages.myResponse.OpenResponsesActivity;
import edu.ewubd.bloodmap.DrawerPages.requestHistory.ReceivedHistoryActivity;
import edu.ewubd.bloodmap.HomePages.availableFrag.AvailableFragment;
import edu.ewubd.bloodmap.HomePages.HeatmapFragment;
import edu.ewubd.bloodmap.HomePages.requestFrag.NewRequestFragment;
import edu.ewubd.bloodmap.HomePages.requestFrag.RequestsFragment;
import edu.ewubd.bloodmap.ProfilePage.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navRequests, navNewRequest, navAvailable, navHeatmap;
    private TextView tvAppTitle;
    private DrawerLayout drawer;
    private int currentTabIndex = 0;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        tvAppTitle = findViewById(R.id.app_title);

        findViewById(R.id.menu_icon).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        
        tvAppTitle.setOnClickListener(v -> selectTab(0));

        navRequests = findViewById(R.id.nav_requests);
        navNewRequest = findViewById(R.id.nav_new_request);
        navAvailable = findViewById(R.id.nav_available);
        navHeatmap = findViewById(R.id.nav_heatmap);

        navRequests.setOnClickListener(v -> selectTab(0));
        navNewRequest.setOnClickListener(v -> selectTab(1));
        navAvailable.setOnClickListener(v -> selectTab(2));
        navHeatmap.setOnClickListener(v -> selectTab(3));

        // Start Standalone Activities for User Profile        // Sidebar Listeners
        findViewById(R.id.profile_photo_container).setOnClickListener(v -> closeDrawerAndStart(ProfileActivity.class));
        findViewById(R.id.menu_open_requests).setOnClickListener(v -> closeDrawerAndStart(OpenRequestsActivity.class));
        findViewById(R.id.menu_open_responses).setOnClickListener(v -> closeDrawerAndStart(OpenResponsesActivity.class));
        findViewById(R.id.menu_donation_history).setOnClickListener(v -> closeDrawerAndStart(DonationHistoryActivity.class));
        findViewById(R.id.menu_receive_history).setOnClickListener(v -> closeDrawerAndStart(ReceivedHistoryActivity.class));
        findViewById(R.id.menu_hospital_list).setOnClickListener(v -> closeDrawerAndStart(HospitalContactsActivity.class));
        findViewById(R.id.menu_blood_bank_list).setOnClickListener(v -> closeDrawerAndStart(BloodBanksActivity.class));
        findViewById(R.id.menu_chatbot).setOnClickListener(v -> closeDrawerAndStart(ChatbotActivity.class));
        findViewById(R.id.menu_user_manual).setOnClickListener(v -> closeDrawerAndStart(edu.ewubd.bloodmap.Documentation.UserGuideActivity.class));
        
        findViewById(R.id.menu_logout).setOnClickListener(v -> {
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

        if (savedInstanceState == null) {
            // Check if launched from a notification with an openTab extra
            int openTab = getIntent().getIntExtra("openTab", 0);
            selectTab(openTab);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1002);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            runStartupChecks(currentUser.getUid());
        }
    }

    // startup checks

    private void runStartupChecks(String uid) {
        updateUserLocation(uid);
        checkAndRestoreDonorEligibility(uid);
        expireStaleRequests(uid);
        updateFcmToken(uid);
    }

    private void updateFcmToken(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null) {
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update("token", token);
                    }
                });
    }

    // location update
    private void updateUserLocation(String uid) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);
            return;
        }
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Map<String, Object> locationUpdate = new HashMap<>();
                            locationUpdate.put("latitude", location.getLatitude());
                            locationUpdate.put("longitude", location.getLongitude());
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .update(locationUpdate);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // eligibility check
    private void checkAndRestoreDonorEligibility(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    Boolean isAvailable = documentSnapshot.getBoolean("availableToDonate");
                    Date nextEligibleDate = documentSnapshot.getDate("nextEligibleDate");

                    if (isAvailable != null && !isAvailable && nextEligibleDate != null) {
                        if (new Date().after(nextEligibleDate)) {
                            Map<String, Object> eligibilityUpdate = new HashMap<>();
                            eligibilityUpdate.put("availableToDonate", true);
                            eligibilityUpdate.put("nextEligibleDate", null);
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                    .update(eligibilityUpdate);
                        }
                    }
                });
    }

    // expire stale requests
    private void expireStaleRequests(String uid) {
        long nowMillis = System.currentTimeMillis();
        FirebaseFirestore.getInstance().collection("transactions")
                .whereEqualTo("requesterUid", uid)
                .whereEqualTo("status", "OPEN")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long neededByTime = doc.getLong("neededByTime");
                        if (neededByTime != null && neededByTime > 0 && neededByTime < nowMillis) {
                            doc.getReference().update("status", "EXPIRED",
                                    "completedAt", nowMillis);
                        }
                    }
                });
    }


    private void closeDrawerAndStart(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentTabIndex != 0) {
            selectTab(0);
        } else {
            super.onBackPressed();
        }
    }

    public void selectTab(int index) {
        currentTabIndex = index;
        findViewById(R.id.bottom_navbar).setVisibility(View.VISIBLE);
        highlightTabBasedOnIndex(index);

        Fragment selectedFragment = null;
        if (index == 0) {
            selectedFragment = new RequestsFragment();
            tvAppTitle.setText("Blood Requests");
        } else if (index == 1) {
            selectedFragment = new NewRequestFragment();
            tvAppTitle.setText("New Blood Request");
        } else if (index == 2) {
            selectedFragment = new AvailableFragment();
            tvAppTitle.setText("Available Donors");
        } else if (index == 3) {
            selectedFragment = new HeatmapFragment();
            tvAppTitle.setText("Blood Heatmap");
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }
    }

    /** Navigates to the Available tab, optionally pre-filling the search box with a query. */
    public void navigateToAvailableWithQuery(String query) {
        currentTabIndex = 2;
        highlightTabBasedOnIndex(2);
        tvAppTitle.setText("Available Donors");
        AvailableFragment frag = new AvailableFragment();
        if (query != null && !query.isEmpty()) {
            Bundle args = new Bundle();
            args.putString("searchQuery", query);
            frag.setArguments(args);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
    }

    private void highlightTabBasedOnIndex(int index) {
        resetTab(navRequests, android.R.drawable.ic_menu_today);
        resetTab(navNewRequest, android.R.drawable.ic_menu_add);
        resetTab(navAvailable, android.R.drawable.ic_menu_agenda);
        resetTab(navHeatmap, android.R.drawable.ic_menu_mapmode);

        if (index == 0) highlightTab(navRequests, android.R.drawable.ic_menu_today);
        else if (index == 1) highlightTab(navNewRequest, android.R.drawable.ic_menu_add);
        else if (index == 2) highlightTab(navAvailable, android.R.drawable.ic_menu_agenda);
        else if (index == 3) highlightTab(navHeatmap, android.R.drawable.ic_menu_mapmode);
    }

    private void resetTab(LinearLayout layout, int iconRes) {
        layout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        ImageView icon = (ImageView) layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);
        icon.setImageResource(iconRes);
        icon.setColorFilter(android.graphics.Color.parseColor("#888888"));
        text.setTextColor(android.graphics.Color.parseColor("#888888"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void highlightTab(LinearLayout layout, int iconRes) {
        layout.setBackgroundColor(android.graphics.Color.parseColor("#FFF0F0"));
        ImageView icon = (ImageView) layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);
        icon.setImageResource(iconRes);
        icon.setColorFilter(android.graphics.Color.parseColor("#D11A2A"));
        text.setTextColor(android.graphics.Color.parseColor("#D11A2A"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }
}
