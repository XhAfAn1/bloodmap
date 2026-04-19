package edu.ewubd.bloodmap.DrawerPages.requestHistory;

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

public class ReceivedHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReceivedHistoryAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private android.view.View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_history);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewHistory);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        requestList = new ArrayList<>();
        adapter = new ReceivedHistoryAdapter(requestList);
        recyclerView.setAdapter(adapter);
    }

    private ListenerRegistration historyRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        loadReceivedHistory();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (historyRegistration != null) {
            historyRegistration.remove();
            historyRegistration = null;
        }
    }
    
    private void loadReceivedHistory() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;
        
        historyRegistration = FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("requesterUid", currentUid)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) return;
                
                if (queryDocumentSnapshots != null) {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BloodTransactionModel model = doc.toObject(BloodTransactionModel.class);
                        String status = model.getStatus();
                        if (status != null && !status.equals("OPEN")) {
                            requestList.add(model);
                        }
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
