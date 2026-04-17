package edu.ewubd.bloodmap.DrawerPages.nearBloodBank;

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

import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.R;

public class BloodBanksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BloodBankAdapter adapter;
    private List<BloodBankModel> bankList;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private Double userLat, userLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_banks);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewBloodBanks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        bankList = new ArrayList<>();
        adapter = new BloodBankAdapter(bankList);
        recyclerView.setAdapter(adapter);
        
        progressBar = findViewById(R.id.progressBarBanks);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private ListenerRegistration bankRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        getUserLocation();
        loadBloodBanks();
    }

    private void getUserLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLong = location.getLongitude();
                        adapter.setUserLocation(userLat, userLong);
                        sortBanksByDistance();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bankRegistration != null) {
            bankRegistration.remove();
            bankRegistration = null;
        }
    }



    private void loadBloodBanks() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
 
        bankRegistration = FirebaseFirestore.getInstance().collection("blood_banks")
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                progressBar.setVisibility(View.GONE);
                
                if (e != null) {
                    Toast.makeText(this, "Failed to load blood banks", Toast.LENGTH_SHORT).show();
                    return;
                }
 
                if (queryDocumentSnapshots != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    bankList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BloodBankModel model = doc.toObject(BloodBankModel.class);
                        bankList.add(model);
                    }
                    sortBanksByDistance();
                    adapter.notifyDataSetChanged();
                }
            });
    }

    private void sortBanksByDistance() {
        if (userLat == null || userLong == null || bankList.isEmpty()) return;

        Collections.sort(bankList, (b1, b2) -> {
            float[] res1 = new float[1];
            float[] res2 = new float[1];
            Location.distanceBetween(userLat, userLong, b1.getLatitude(), b1.getLongitude(), res1);
            Location.distanceBetween(userLat, userLong, b2.getLatitude(), b2.getLongitude(), res2);
            return Float.compare(res1[0], res2[0]);
        });
        adapter.notifyDataSetChanged();
    }
}
