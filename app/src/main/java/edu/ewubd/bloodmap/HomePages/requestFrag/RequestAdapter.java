package edu.ewubd.bloodmap.HomePages.requestFrag;
 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;
 
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
 
    public interface OnRequestActionListener {
        void onRespondClick(BloodTransactionModel model, int position);
    }
 
    private List<BloodTransactionModel> requestList;
    private OnRequestActionListener actionListener;
    private String currentUid;
    private boolean isHistoryMode;
 
    public RequestAdapter(List<BloodTransactionModel> requestList, OnRequestActionListener actionListener, boolean isHistoryMode) {
        this.requestList = requestList;
        this.actionListener = actionListener;
        this.isHistoryMode = isHistoryMode;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUid = "";
        }
    }
 
    public RequestAdapter(List<BloodTransactionModel> requestList, OnRequestActionListener actionListener) {
        this(requestList, actionListener, false);
    }
 
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request, parent, false);
        return new RequestViewHolder(view);
    }
 
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BloodTransactionModel model = requestList.get(position);
        
        holder.tvTitle.setText(model.getBloodGroup() + " Blood Required (" + model.getUnitsRequired() + " Units)");
        
        // Premium request styling
        if (model.isPremiumRequest()) {
            holder.tvPremiumBadge.setVisibility(View.VISIBLE);
            if (holder.itemView instanceof CardView) {
                ((CardView) holder.itemView).setCardBackgroundColor(android.graphics.Color.parseColor("#FFF0F0"));
            }
        } else {
            holder.tvPremiumBadge.setVisibility(View.GONE);
            if (holder.itemView instanceof CardView) {
                ((CardView) holder.itemView).setCardBackgroundColor(android.graphics.Color.WHITE);
            }
        }

        if (model.getUrgencyLevel() != null && !model.getUrgencyLevel().isEmpty()) {
            holder.tvUrgency.setText(model.getUrgencyLevel());
            holder.tvUrgency.setVisibility(View.VISIBLE);
        } else {
            holder.tvUrgency.setVisibility(View.GONE);
        }
 
        String patientInfo = "Patient: " + model.getPatientName();
        if (model.getPatientAge() != null && !model.getPatientAge().isEmpty()) {
            patientInfo += " (Age: " + model.getPatientAge();
            if (model.getPatientGender() != null && !model.getPatientGender().isEmpty()) {
                patientInfo += ", Gender: " + model.getPatientGender();
            }
            patientInfo += ")";
        } else if (model.getPatientGender() != null && !model.getPatientGender().isEmpty()) {
            patientInfo += " (Gender: " + model.getPatientGender() + ")";
        }
        holder.tvPatientDetails.setText(patientInfo);
 
        if (model.getReason() != null && !model.getReason().isEmpty()) {
            holder.tvReason.setText("Reason: " + model.getReason());
            holder.tvReason.setVisibility(View.VISIBLE);
        } else {
            holder.tvReason.setVisibility(View.GONE);
        }
 
        if (model.getNotes() != null && !model.getNotes().isEmpty()) {
            holder.tvNotes.setText("Notes: " + model.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
 
        String location = model.getHospitalNameArea() != null ? model.getHospitalNameArea() : "";
        if (model.getArea() != null && !model.getArea().isEmpty()) {
            if (!location.isEmpty()) location += ", ";
            location += model.getArea();
        }
        holder.tvHospital.setText("Location: " + location);
 
        if (model.getNeededByTime() == 0) {
            holder.tvTime.setText("Needed by: ASAP");
        } else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date(model.getNeededByTime()));
            holder.tvTime.setText("Needed by: " + dateStr);
        }
        
        if (model.getStatusMessage() != null && !model.getStatusMessage().isEmpty()) {
            holder.tvStatusMessage.setText(model.getStatusMessage());
            holder.tvStatusMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusMessage.setVisibility(View.GONE);
        }
        
        if (isHistoryMode) {
            holder.btnRespond.setVisibility(View.GONE);
        } else {
            holder.btnRespond.setVisibility(View.VISIBLE);
            if (model.getResponderUids() != null && model.getResponderUids().contains(currentUid)) {
                holder.btnRespond.setText("RESPONDED");
                holder.btnRespond.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#AAAAAA")));
                holder.btnRespond.setEnabled(false);
            } else {
                holder.btnRespond.setText("RESPOND");
                holder.btnRespond.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D11A2A")));
                holder.btnRespond.setEnabled(true);
            }
 
            holder.btnRespond.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onRespondClick(model, position);
                }
            });
        }
    }
 
    @Override
    public int getItemCount() {
        return requestList.size();
    }
 
    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvHospital, tvTime, tvUrgency, tvPatientDetails, tvReason, tvNotes, tvStatusMessage, tvPremiumBadge;
        Button btnRespond;
 
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
            tvPremiumBadge = itemView.findViewById(R.id.tvPremiumBadge);
            btnRespond = itemView.findViewById(R.id.btnRespond);
        }
    }
}
