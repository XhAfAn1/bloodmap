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

import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.R;

public class HospitalContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private List<HospitalContactModel> hospitalList;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private Double userLat, userLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_contacts);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
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
                    Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                    return;
                }
 
                if (queryDocumentSnapshots != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    hospitalList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HospitalContactModel model = doc.toObject(HospitalContactModel.class);
                        hospitalList.add(model);
                    }
                    sortHospitalsByDistance();
                    adapter.notifyDataSetChanged();
                }
            });
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
}
