package edu.ewubd.bloodmap.HomePages.requestFrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class RequestsFragment extends Fragment implements RequestAdapter.OnRequestActionListener {
    
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<BloodTransactionModel> requestList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
        
        loadRequests();
        
        return view;
    }

    private void loadRequests() {
        FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("status", "OPEN")
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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onRespondClick(BloodTransactionModel model, int position) {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) {
            Toast.makeText(getContext(), "You must be logged in to respond.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUid.equals(model.getRequesterUid())) {
            new AlertDialog.Builder(getContext())
                .setTitle("Invalid Action")
                .setMessage("You cannot respond to your own blood request.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Confirm Response")
            .setMessage("Are you sure you want to respond to this request? This will notify the patient.")
            .setPositiveButton("Respond", (dialog, which) -> executeResponse(model, position, currentUid))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void executeResponse(BloodTransactionModel model, int position, String currentUid) {
        FirebaseFirestore.getInstance().collection("transactions").document(model.getTransactionId())
            .update("responderUids", FieldValue.arrayUnion(currentUid))
            .addOnSuccessListener(aVoid -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Response Sent!", Toast.LENGTH_SHORT).show();
                    // Update local model so the button greys out immediately without full reload
                    if (model.getResponderUids() == null) {
                        model.setResponderUids(new ArrayList<>());
                    }
                    model.getResponderUids().add(currentUid);
                    adapter.notifyItemChanged(position);
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to send response.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
