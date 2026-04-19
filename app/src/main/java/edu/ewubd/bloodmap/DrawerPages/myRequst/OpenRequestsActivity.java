package edu.ewubd.bloodmap.DrawerPages.myRequst;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

public class OpenRequestsActivity extends AppCompatActivity implements MyRequestsAdapter.OnManageClickListener {
    private RecyclerView recyclerView;
    private MyRequestsAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private android.view.View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_requests);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewRequests);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        requestList = new ArrayList<>();
        adapter = new MyRequestsAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
    }

    private ListenerRegistration requestsRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        loadOpenRequests();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestsRegistration != null) {
            requestsRegistration.remove();
            requestsRegistration = null;
        }
    }
    
    private void loadOpenRequests() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;
        
        requestsRegistration = FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("requesterUid", currentUid)
            .whereEqualTo("status", "OPEN")
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

    @Override
    public void onManageClick(BloodTransactionModel model, int position) {
        Intent intent = new Intent(this, ManageRequestActivity.class);
        intent.putExtra("transactionId", model.getTransactionId());
        startActivity(intent);
    }
}
