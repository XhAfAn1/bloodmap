package edu.ewubd.bloodmap.DrawerPages.donationHistory;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class DonationHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DonationHistoryAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private android.view.View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewDonations);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        requestList = new ArrayList<>();
        adapter = new DonationHistoryAdapter(requestList);
        recyclerView.setAdapter(adapter);
    }

    private ListenerRegistration historyRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        loadDonationHistory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (historyRegistration != null) {
            historyRegistration.remove();
            historyRegistration = null;
        }
    }
    
    private void loadDonationHistory() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;
        
        historyRegistration = FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("selectedDonorUid", currentUid)
            .whereEqualTo("status", "COMPLETED")
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) return;
                
                if (queryDocumentSnapshots != null) {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        requestList.add(doc.toObject(BloodTransactionModel.class));
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (requestList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                }
            });
    }
}
