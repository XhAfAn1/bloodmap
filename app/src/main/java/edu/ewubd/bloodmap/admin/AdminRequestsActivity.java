package edu.ewubd.bloodmap.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class AdminRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminRequestAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private String statusFilter = "OPEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_requests);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        if (getIntent() != null && getIntent().hasExtra("STATUS_FILTER")) {
            statusFilter = getIntent().getStringExtra("STATUS_FILTER");
        }

        TextView tvTitle = findViewById(R.id.tvAdminRequestsTitle);
        tvTitle.setText(statusFilter.equals("OPEN") ? "Active Requests" : "Previous Requests");

        recyclerView = findViewById(R.id.recyclerViewAdminRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new AdminRequestAdapter(requestList);
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("status", statusFilter)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                requestList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    BloodTransactionModel model = doc.toObject(BloodTransactionModel.class);
                    requestList.add(model);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
            });
    }
}
