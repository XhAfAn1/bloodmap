package edu.ewubd.bloodmap.DrawerPages.nearHospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.location.Location;
import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.database.LocalDatabaseManager;

public class HospitalContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private List<HospitalContactModel> hospitalList;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private Double userLat, userLong;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private LocalDatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_contacts);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        dbManager = new LocalDatabaseManager(this);
        recyclerView = findViewById(R.id.recyclerViewHospitals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        hospitalList = new ArrayList<>();
        adapter = new HospitalAdapter(hospitalList);
        recyclerView.setAdapter(adapter);
        
        progressBar = findViewById(R.id.progressBarHospitals);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private ListenerRegistration contactRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        getUserLocation();
        loadHospitals();
    }

    private void getUserLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLong = location.getLongitude();
                        adapter.setUserLocation(userLat, userLong);
                        sortHospitalsByDistance();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (contactRegistration != null) {
            contactRegistration.remove();
            contactRegistration = null;
        }
    }

    private void loadHospitals() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
 
        contactRegistration = FirebaseFirestore.getInstance().collection("hospitals")
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                progressBar.setVisibility(View.GONE);
                
                if (e != null) {
                    loadFromCache();
                    return;
                }
 
                if (queryDocumentSnapshots != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    hospitalList.clear();
                    List<HospitalContactModel> syncList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HospitalContactModel model = doc.toObject(HospitalContactModel.class);
                        hospitalList.add(model);
                        syncList.add(model);
                    }
                    dbManager.syncHospitals(syncList);
                    sortHospitalsByDistance();
                    adapter.notifyDataSetChanged();
                } else {
                    loadFromCache();
                }
            });
    }

    private void loadFromCache() {
        List<HospitalContactModel> cached = dbManager.getCachedHospitals();
        if (cached != null && !cached.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            hospitalList.clear();
            hospitalList.addAll(cached);
            sortHospitalsByDistance();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Showing cached offline hospitals.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No offline data available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortHospitalsByDistance() {
        if (userLat == null || userLong == null || hospitalList.isEmpty()) return;

        Collections.sort(hospitalList, (h1, h2) -> {
            float[] res1 = new float[1];
            float[] res2 = new float[1];
            Location.distanceBetween(userLat, userLong, h1.getLatitude(), h1.getLongitude(), res1);
            Location.distanceBetween(userLat, userLong, h2.getLatitude(), h2.getLongitude(), res2);
            return Float.compare(res1[0], res2[0]);
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
