package edu.ewubd.bloodmap.admin.requestManagement;
 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;
 
public class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.AdminRequestViewHolder> {
 
    private List<BloodTransactionModel> requestList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private Map<String, String> userNameCache = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
 
    public AdminRequestAdapter(List<BloodTransactionModel> requestList) {
        this.requestList = requestList;
    }
 
    @NonNull
    @Override
    public AdminRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_blood_request, parent, false);
        return new AdminRequestViewHolder(view);
    }
 
    @Override
    public void onBindViewHolder(@NonNull AdminRequestViewHolder holder, int position) {
        BloodTransactionModel model = requestList.get(position);
 
        holder.tvAdminTitle.setText(model.getBloodGroup() + " (" + model.getUnitsRequired() + " Units)");
 
        String status = model.getStatus() != null ? model.getStatus() : "UNKNOWN";
        holder.tvAdminStatusBadge.setText(status);
        if ("OPEN".equalsIgnoreCase(status)) {
            holder.tvAdminStatusBadge.setBackgroundColor(0xFFE3F2FD); 
            holder.tvAdminStatusBadge.setTextColor(0xFF1565C0);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            holder.tvAdminStatusBadge.setBackgroundColor(0xFFE8F5E9); 
            holder.tvAdminStatusBadge.setTextColor(0xFF2E7D32);
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            holder.tvAdminStatusBadge.setBackgroundColor(0xFFFFEBEE); 
            holder.tvAdminStatusBadge.setTextColor(0xFFC62828);
        }
 
        if (model.getUrgencyLevel() != null && !model.getUrgencyLevel().isEmpty()) {
            holder.tvAdminUrgency.setText(model.getUrgencyLevel());
            holder.tvAdminUrgency.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminUrgency.setVisibility(View.GONE);
        }
 
        if (model.getNeededByTime() == 0) {
            holder.tvAdminTimeNeeded.setText("Needed: ASAP");
        } else {
            holder.tvAdminTimeNeeded.setText("Needed: " + sdf.format(new Date(model.getNeededByTime())));
        }
 
        holder.tvAdminPatientName.setText(model.getPatientName() != null ? model.getPatientName() : "N/A");
 
        holder.tvAdminRequesterUid.setText("UID: " + model.getRequesterUid());
        fetchUserName(model.getRequesterUid(), holder.tvAdminRequesterName);
 
        holder.tvAdminContact.setText(model.getContactNumber() != null ? model.getContactNumber() : "No contact");

        String location = model.getHospitalNameArea() != null ? model.getHospitalNameArea() : "";
        if (model.getArea() != null && !model.getArea().isEmpty()) {
            if (!location.isEmpty()) location += ", ";
            location += model.getArea();
        }
        holder.tvAdminHospital.setText(location.isEmpty() ? "No location set" : location);

        int responders = model.getResponderUids() != null ? model.getResponderUids().size() : 0;
        holder.tvAdminResponders.setText(String.valueOf(responders));

        if (model.getSelectedDonorUid() != null && !model.getSelectedDonorUid().isEmpty() && !model.getSelectedDonorUid().equals("None")) {
            holder.tvAdminSelectedDonor.setText("UID: " + model.getSelectedDonorUid());
            holder.tvAdminSelectedDonor.setVisibility(View.VISIBLE);
            fetchUserName(model.getSelectedDonorUid(), holder.tvAdminSelectedDonorName);
            holder.tvAdminSelectedDonorName.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminSelectedDonorName.setText("None");
            holder.tvAdminSelectedDonor.setVisibility(View.GONE);
        }
 
        if (model.getStatusMessage() != null && !model.getStatusMessage().isEmpty()) {
            holder.tvAdminStatusMessage.setText(model.getStatusMessage());
            holder.layoutAdminStatusMessage.setVisibility(View.VISIBLE);
        } else {
            holder.layoutAdminStatusMessage.setVisibility(View.GONE);
        }

        holder.tvAdminCreatedAt.setText("Created: " + sdf.format(new Date(model.getCreatedAt())));

        if (model.getCompletedAt() > 0) {
            holder.tvAdminCompletedAt.setText("Completed: " + sdf.format(new Date(model.getCompletedAt())));
            holder.tvAdminCompletedAt.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminCompletedAt.setVisibility(View.GONE);
        }

        if (model.getNotes() != null && !model.getNotes().isEmpty()) {
            holder.tvAdminNotes.setText(model.getNotes());
            holder.layoutAdminNotes.setVisibility(View.VISIBLE);
        } else {
            holder.layoutAdminNotes.setVisibility(View.GONE);
        }
    }
 
    private void fetchUserName(String uid, TextView targetView) {
        if (uid == null || uid.isEmpty() || uid.equals("None")) {
            targetView.setText("N/A");
            return;
        }
 
        if (userNameCache.containsKey(uid)) {
            targetView.setText(userNameCache.get(uid));
            return;
        }
 
        // Set a temporary "Loading..." 
        targetView.setText("Loading...");
 
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    if (name == null || name.isEmpty()) name = "Unnamed User";
                    userNameCache.put(uid, name);
                    targetView.setText(name);
                } else {
                    userNameCache.put(uid, "Unknown");
                    targetView.setText("Unknown");
                }
            })
            .addOnFailureListener(e -> {
                targetView.setText("Error");
            });
    }
 
    @Override
    public int getItemCount() {
        return requestList.size();
    }
 
    static class AdminRequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminTitle, tvAdminStatusBadge, tvAdminUrgency, tvAdminTimeNeeded;
        TextView tvAdminPatientName, tvAdminRequesterUid, tvAdminRequesterName, tvAdminContact, tvAdminHospital;
        TextView tvAdminResponders, tvAdminSelectedDonor, tvAdminSelectedDonorName, tvAdminStatusMessage;
        TextView tvAdminCreatedAt, tvAdminCompletedAt, tvAdminNotes;
        View layoutAdminNotes, layoutAdminStatusMessage;
 
        public AdminRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminTitle = itemView.findViewById(R.id.tvAdminTitle);
            tvAdminStatusBadge = itemView.findViewById(R.id.tvAdminStatusBadge);
            tvAdminUrgency = itemView.findViewById(R.id.tvAdminUrgency);
            tvAdminTimeNeeded = itemView.findViewById(R.id.tvAdminTimeNeeded);
            tvAdminPatientName = itemView.findViewById(R.id.tvAdminPatientName);
            tvAdminRequesterUid = itemView.findViewById(R.id.tvAdminRequesterUid);
            tvAdminRequesterName = itemView.findViewById(R.id.tvAdminRequesterName);
            tvAdminContact = itemView.findViewById(R.id.tvAdminContact);
            tvAdminHospital = itemView.findViewById(R.id.tvAdminHospital);
            tvAdminResponders = itemView.findViewById(R.id.tvAdminResponders);
            tvAdminSelectedDonor = itemView.findViewById(R.id.tvAdminSelectedDonor);
            tvAdminSelectedDonorName = itemView.findViewById(R.id.tvAdminSelectedDonorName);
            tvAdminStatusMessage = itemView.findViewById(R.id.tvAdminStatusMessage);
            tvAdminCreatedAt = itemView.findViewById(R.id.tvAdminCreatedAt);
            tvAdminCompletedAt = itemView.findViewById(R.id.tvAdminCompletedAt);
            tvAdminNotes = itemView.findViewById(R.id.tvAdminNotes);
            layoutAdminNotes = itemView.findViewById(R.id.layoutAdminNotes);
            layoutAdminStatusMessage = itemView.findViewById(R.id.layoutAdminStatusMessage);
        }
    }
}
