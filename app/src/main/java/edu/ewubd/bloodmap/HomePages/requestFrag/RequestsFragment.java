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
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;

import android.content.Intent;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.Notifications.NotificationSender;
import edu.ewubd.bloodmap.ProfilePage.ProfileActivity;
import edu.ewubd.bloodmap.R;

public class RequestsFragment extends Fragment implements RequestAdapter.OnRequestActionListener {
    
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<BloodTransactionModel> requestList;
    private ListenerRegistration requestsRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList, this);
        recyclerView.setAdapter(adapter);
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        loadRequests();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (requestsRegistration != null) {
            requestsRegistration.remove();
            requestsRegistration = null;
        }
    }

    private void loadRequests() {
        requestsRegistration = FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("status", "OPEN")
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                
                if (queryDocumentSnapshots != null) {
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BloodTransactionModel model = doc.toObject(BloodTransactionModel.class);
                        requestList.add(model);
                    }
                    // Sort premium requests to top
                    Collections.sort(requestList, (a, b) -> Boolean.compare(b.isPremiumRequest(), a.isPremiumRequest()));
                    adapter.notifyDataSetChanged();
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

        // First check if user is banned
        FirebaseFirestore.getInstance().collection("users").document(currentUid).get()
            .addOnSuccessListener(userDoc -> {
                if (getContext() == null) return;
                String userStatus = userDoc.getString("status");
                if ("BLOCKED".equalsIgnoreCase(userStatus)) {
                    new AlertDialog.Builder(getContext())
                        .setTitle("Account Suspended")
                        .setMessage("You have been banned. You cannot perform this operation.")
                        .setPositiveButton("OK", null)
                        .show();
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

                // check profile before allowing response
                String bloodGroup = userDoc.getString("bloodGroup");
                String contactNumber = userDoc.getString("contactNumber");
                boolean incomplete = (bloodGroup == null || bloodGroup.isEmpty())
                    || (contactNumber == null || contactNumber.isEmpty());
                if (incomplete) {
                    new AlertDialog.Builder(getContext())
                        .setTitle("Profile Incomplete")
                        .setMessage("Please set up your blood group and contact number in your profile before responding to requests.")
                        .setPositiveButton("Go to Profile", (dialog, which) -> {
                            startActivity(new Intent(getContext(), ProfileActivity.class));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    return;
                }
                new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Response")
                    .setMessage("Are you sure you want to respond to this request? This will notify the patient.")
                    .setPositiveButton("Respond", (dialog, which) -> executeResponse(model, position, currentUid))
                    .setNegativeButton("Cancel", null)
                    .show();
            });
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
                    
                    // Trigger notification to requester
                    triggerNotificationToRequester(model, currentUid);
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to send response.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void triggerNotificationToRequester(BloodTransactionModel model, String responderUid) {
        String requesterUid = model.getRequesterUid();
        
        // 1. Get requester's token
        FirebaseFirestore.getInstance().collection("users").document(requesterUid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String requesterToken = documentSnapshot.getString("token");
                    if (requesterToken != null && !requesterToken.isEmpty()) {
                        
                        // 2. Get responder's name for the notification body
                        FirebaseFirestore.getInstance().collection("users").document(responderUid).get()
                            .addOnSuccessListener(responderDoc -> {
                                String responderName = responderDoc.getString("name");
                                if (responderName == null) responderName = "A donor";
                                
                                String title = "New Blood Response!";
                                String body = responderName + " has responded to your request for " + model.getBloodGroup();
                                
                                NotificationSender.sendNotification(
                                    getContext(),
                                    requesterToken, 
                                    title, 
                                    body, 
                                    model.getTransactionId(),
                                    responderName
                                );
                            });
                    }
                }
            });
    }
}
