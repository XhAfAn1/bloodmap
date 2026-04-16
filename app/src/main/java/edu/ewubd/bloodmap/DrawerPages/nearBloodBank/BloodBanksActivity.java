package edu.ewubd.bloodmap.DrawerPages.nearBloodBank;

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

import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.AddBloodBankActivity;

public class BloodBanksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BloodBankAdapter adapter;
    private List<BloodBankModel> bankList;
    private FloatingActionButton fabAddBloodBank;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_banks);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewBloodBanks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        bankList = new ArrayList<>();
        adapter = new BloodBankAdapter(bankList, isAdmin);
        recyclerView.setAdapter(adapter);
        
        fabAddBloodBank = findViewById(R.id.fabAddBloodBank);
        fabAddBloodBank.setOnClickListener(v -> startActivity(new Intent(this, AddBloodBankActivity.class)));
        
        checkAdminStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBloodBanks();
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
                            fabAddBloodBank.setVisibility(View.VISIBLE);
                            adapter = new BloodBankAdapter(bankList, isAdmin);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
        }
    }

    private void loadBloodBanks() {
        FirebaseFirestore.getInstance().collection("blood_banks")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                bankList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    BloodBankModel model = doc.toObject(BloodBankModel.class);
                    bankList.add(model);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load blood banks", Toast.LENGTH_SHORT).show();
            });
    }
}
