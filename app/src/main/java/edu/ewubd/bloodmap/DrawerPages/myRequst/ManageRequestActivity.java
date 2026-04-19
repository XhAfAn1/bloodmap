package edu.ewubd.bloodmap.DrawerPages.myRequst;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class ManageRequestActivity extends AppCompatActivity implements ResponderAdapter.OnAcceptDonorListener {

    private RecyclerView recyclerView;
    private ResponderAdapter adapter;
    private List<UserModel> responderList;
    private TextView tvManageHeader, tvEmpty;
    private TextView tvPatientDetails, tvReason, tvUrgency, tvHospital, tvTime, tvStatusMessage, tvNotes;
    private android.view.View layoutStatusMessage, layoutNotes, layoutReason;
    private com.google.android.material.textfield.TextInputEditText etStatusUpdate;
    private android.view.View btnCloseRequest, btnUpdateStatus;

    private String transactionId;
    private BloodTransactionModel currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_request);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        tvManageHeader = findViewById(R.id.tvManageHeader);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvPatientDetails = findViewById(R.id.tvPatientDetails);
        tvReason = findViewById(R.id.tvReason);
        tvUrgency = findViewById(R.id.tvUrgency);
        tvHospital = findViewById(R.id.tvHospital);
        tvTime = findViewById(R.id.tvTime);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        tvNotes = findViewById(R.id.tvNotes);
        layoutStatusMessage = findViewById(R.id.layoutStatusMessage);
        layoutNotes = findViewById(R.id.layoutNotes);
        layoutReason = findViewById(R.id.layoutReason);
        btnCloseRequest = findViewById(R.id.btnCloseRequest);
        etStatusUpdate = findViewById(R.id.etStatusUpdate);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        recyclerView = findViewById(R.id.recyclerViewResponders);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        responderList = new ArrayList<>();
        adapter = new ResponderAdapter(responderList, this);
        recyclerView.setAdapter(adapter);

        transactionId = getIntent().getStringExtra("transactionId");
        if (transactionId == null) {
            Toast.makeText(this, "Error fetching transaction context.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnCloseRequest.setOnClickListener(v -> handleCloseRequest());
        btnUpdateStatus.setOnClickListener(v -> handleUpdateStatusMessage());
    }

    private ListenerRegistration transactionRegistration;
    private ListenerRegistration respondersRegistration;
    private List<String> lastKnownResponderUids = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        listenToTransaction();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (transactionRegistration != null) {
            transactionRegistration.remove();
            transactionRegistration = null;
        }
        if (respondersRegistration != null) {
            respondersRegistration.remove();
            respondersRegistration = null;
        }
    }

    private void listenToTransaction() {
        transactionRegistration = FirebaseFirestore.getInstance().collection("transactions").document(transactionId)
            .addSnapshotListener((documentSnapshot, e) -> {
                if (isFinishing()) return;
                if (e != null || documentSnapshot == null || !documentSnapshot.exists()) {
                    if (documentSnapshot != null && !documentSnapshot.exists()) {
                        tvManageHeader.setText("Transaction Deleted");
                    }
                    return;
                }

                currentTransaction = documentSnapshot.toObject(BloodTransactionModel.class);
                if (currentTransaction != null) {
                    updateUIWithTransaction();

                    List<String> currentUids = currentTransaction.getResponderUids();
                    if (currentUids == null) currentUids = new ArrayList<>();

                    if (!currentUids.equals(lastKnownResponderUids)) {
                        lastKnownResponderUids = new ArrayList<>(currentUids);
                        listenToResponders(currentUids);
                    }
                }
            });
    }

    private void updateUIWithTransaction() {
        tvManageHeader.setText(currentTransaction.getBloodGroup() + " Required (" + currentTransaction.getUnitsRequired() + " Units)");
        
        tvPatientDetails.setText(currentTransaction.formatPatientDetails());

        if (currentTransaction.getReason() != null && !currentTransaction.getReason().isEmpty()) {
            tvReason.setText(currentTransaction.getReason());
            layoutReason.setVisibility(View.VISIBLE);
        } else {
            layoutReason.setVisibility(View.GONE);
        }

        if (currentTransaction.getUrgencyLevel() != null && !currentTransaction.getUrgencyLevel().isEmpty()) {
            tvUrgency.setText(currentTransaction.getUrgencyLevel());
            tvUrgency.setVisibility(View.VISIBLE);
        } else {
            tvUrgency.setVisibility(View.GONE);
        }

        String location = currentTransaction.getHospitalNameArea() != null ? currentTransaction.getHospitalNameArea() : "";
        if (currentTransaction.getArea() != null && !currentTransaction.getArea().isEmpty()) {
            if (!location.isEmpty()) location += ", ";
            location += currentTransaction.getArea();
        }
        tvHospital.setText("Location: " + location);

        if (currentTransaction.getNeededByTime() > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault());
            tvTime.setText("Needed By: " + sdf.format(new java.util.Date(currentTransaction.getNeededByTime())));
        } else {
            tvTime.setText("Needed By: ASAP");
        }

        if (currentTransaction.getStatusMessage() != null && !currentTransaction.getStatusMessage().isEmpty()) {
            tvStatusMessage.setText(currentTransaction.getStatusMessage());
            layoutStatusMessage.setVisibility(View.VISIBLE);
            if (etStatusUpdate.getText().toString().isEmpty()) {
                etStatusUpdate.setText(currentTransaction.getStatusMessage());
            }
        } else {
            layoutStatusMessage.setVisibility(View.GONE);
        }

        if (currentTransaction.getNotes() != null && !currentTransaction.getNotes().isEmpty()) {
            tvNotes.setText(currentTransaction.getNotes());
            layoutNotes.setVisibility(View.VISIBLE);
        } else {
            layoutNotes.setVisibility(View.GONE);
        }
    }

    private void handleUpdateStatusMessage() {
        String newMessage = etStatusUpdate.getText().toString().trim();
        if (newMessage.isEmpty()) {
            Toast.makeText(this, "Please enter a status message", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("transactions").document(transactionId)
            .update("statusMessage", newMessage)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Status message updated globally!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update status message.", Toast.LENGTH_SHORT).show();
            });
    }

    private void listenToResponders(List<String> responderUids) {
        if (respondersRegistration != null) {
            respondersRegistration.remove();
            respondersRegistration = null;
        }

        if (responderUids == null || responderUids.isEmpty()) {
            responderList.clear();
            adapter.notifyDataSetChanged();
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // Firestore whereIn supports up to 30 items.
        List<String> limitedUids = responderUids.size() > 30 ? responderUids.subList(0, 30) : responderUids;

        respondersRegistration = FirebaseFirestore.getInstance().collection("users")
            .whereIn("uid", limitedUids)
            .addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (isFinishing()) return;
                if (e != null) return;

                if (queryDocumentSnapshots != null) {
                    responderList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        responderList.add(doc.toObject(UserModel.class));
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(responderList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
    }

    private void handleCloseRequest() {
        new AlertDialog.Builder(this)
            .setTitle("Close Request Globally")
            .setMessage("Are you sure you want to close this request without assigning a donor? This will completely cancel the active request.")
            .setPositiveButton("YES, CLOSE", (dialog, which) -> {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "CANCELLED");
                updates.put("completedAt", System.currentTimeMillis());

                FirebaseFirestore.getInstance().collection("transactions").document(transactionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Request safely closed.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
            })
            .setNegativeButton("NO", null)
            .show();
    }

    @Override
    public void onAcceptDonorClick(UserModel userModel, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Accept External Donor")
            .setMessage("Are you sure you want to officially accept " + userModel.getName() + " as the assigned responder? This permanently completes the transaction dynamically!")
            .setPositiveButton("ACCEPT", (dialog, which) -> {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "COMPLETED");
                updates.put("selectedDonorUid", userModel.getUid());
                updates.put("completedAt", System.currentTimeMillis());

                FirebaseFirestore.getInstance().collection("transactions").document(transactionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put("totalDonations", FieldValue.increment(1));
                        userUpdates.put("lastDonationDate", new Date());
                        
                        Calendar cal = Calendar.getInstance();

                        //cooldown time
                        cal.add(Calendar.DAY_OF_YEAR, 30);
                        userUpdates.put("nextEligibleDate", cal.getTime());
                        userUpdates.put("availableToDonate", false);

                        FirebaseFirestore.getInstance().collection("users").document(userModel.getUid())
                            .update(userUpdates);

                        Toast.makeText(this, "Transaction completed globally!", Toast.LENGTH_LONG).show();
                        finish();
                    });
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }
}
