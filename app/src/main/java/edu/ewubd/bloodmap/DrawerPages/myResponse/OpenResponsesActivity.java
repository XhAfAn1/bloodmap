package edu.ewubd.bloodmap.DrawerPages.myResponse;

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

import android.app.AlertDialog;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;

public class OpenResponsesActivity extends AppCompatActivity implements MyResponsesAdapter.OnWithdrawClickListener {
    private RecyclerView recyclerView;
    private MyResponsesAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private android.view.View tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_responses);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewResponses);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        requestList = new ArrayList<>();
        adapter = new MyResponsesAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
    }

    private ListenerRegistration responsesRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        loadOpenResponses();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (responsesRegistration != null) {
            responsesRegistration.remove();
            responsesRegistration = null;
        }
    }
    
    private void loadOpenResponses() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;
        
        responsesRegistration = FirebaseFirestore.getInstance().collection("transactions")
            .whereArrayContains("responderUids", currentUid)
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
    public void onWithdrawClick(BloodTransactionModel model, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Withdraw Response")
            .setMessage("Are you sure you want to withdraw your availability? The patient will no longer see you as a donor.")
            .setPositiveButton("Withdraw", (dialog, which) -> executeWithdrawal(model, position))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void executeWithdrawal(BloodTransactionModel model, int position) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        FirebaseFirestore.getInstance().collection("transactions").document(model.getTransactionId())
            .update("responderUids", FieldValue.arrayRemove(currentUid))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Response withdrawn.", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to withdraw response.", Toast.LENGTH_SHORT).show();
            });
    }
}
