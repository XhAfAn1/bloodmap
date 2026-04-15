package edu.ewubd.bloodmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import edu.ewubd.bloodmap.Authentication.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import edu.ewubd.bloodmap.DrawerPages.ChatbotActivity;
import edu.ewubd.bloodmap.DrawerPages.BloodBanksActivity;
import edu.ewubd.bloodmap.DrawerPages.DonationHistoryActivity;
import edu.ewubd.bloodmap.DrawerPages.HospitalContactsActivity;
import edu.ewubd.bloodmap.DrawerPages.OpenRequestsActivity;
import edu.ewubd.bloodmap.DrawerPages.OpenResponsesActivity;
import edu.ewubd.bloodmap.DrawerPages.ReceivedHistoryActivity;
import edu.ewubd.bloodmap.HomePages.availableFrag.AvailableFragment;
import edu.ewubd.bloodmap.HomePages.HeatmapFragment;
import edu.ewubd.bloodmap.HomePages.requestFrag.NewRequestFragment;
import edu.ewubd.bloodmap.HomePages.requestFrag.RequestsFragment;
import edu.ewubd.bloodmap.ProfilePage.ProfileActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navRequests, navNewRequest, navAvailable, navHeatmap;
    private DrawerLayout drawer;
    private int currentTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);

        findViewById(R.id.menu_icon).setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        
        findViewById(R.id.app_title).setOnClickListener(v -> selectTab(0));

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
        
        findViewById(R.id.menu_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        if (savedInstanceState == null) {
            selectTab(0);
        }
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
        if (index == 0) selectedFragment = new RequestsFragment();
        else if (index == 1) selectedFragment = new NewRequestFragment();
        else if (index == 2) selectedFragment = new AvailableFragment();
        else if (index == 3) selectedFragment = new HeatmapFragment();

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit(); 
        }
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
