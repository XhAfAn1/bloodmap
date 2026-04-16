package edu.ewubd.bloodmap.DrawerPages.nearHospital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.AddHospitalActivity;

public class HospitalContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private List<HospitalContactModel> hospitalList;
    private FloatingActionButton fabAddHospital;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_contacts);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewHospitals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        hospitalList = new ArrayList<>();
        // Initialize with false, will update once checkAdminStatus returns
        adapter = new HospitalAdapter(hospitalList, isAdmin);
        recyclerView.setAdapter(adapter);
        
        fabAddHospital = findViewById(R.id.fabAddHospital);
        fabAddHospital.setOnClickListener(v -> startActivity(new Intent(this, AddHospitalActivity.class)));
        
        checkAdminStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHospitals();
    }

    private void checkAdminStatus() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        edu.ewubd.bloodmap.ClassModels.UserModel userProfile = documentSnapshot.toObject(edu.ewubd.bloodmap.ClassModels.UserModel.class);
                        if (userProfile != null && userProfile.isAdmin()) {
                            isAdmin = true;
                            fabAddHospital.setVisibility(View.VISIBLE);
                            // Re-initialize adapter with new isAdmin status
                            adapter = new HospitalAdapter(hospitalList, isAdmin);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
        }
    }

    private void loadHospitals() {
        FirebaseFirestore.getInstance().collection("hospitals")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                hospitalList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    HospitalContactModel model = doc.toObject(HospitalContactModel.class);
                    hospitalList.add(model);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
            });
    }
}
