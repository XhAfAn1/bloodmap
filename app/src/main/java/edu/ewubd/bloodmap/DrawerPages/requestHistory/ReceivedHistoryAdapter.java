package edu.ewubd.bloodmap.DrawerPages.requestHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class ReceivedHistoryAdapter extends RecyclerView.Adapter<ReceivedHistoryAdapter.ViewHolder> {

    private List<BloodTransactionModel> requestList;

    public ReceivedHistoryAdapter(List<BloodTransactionModel> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodTransactionModel model = requestList.get(position);

        holder.tvTitle.setText(model.getBloodGroup() + " Blood (" + model.getUnitsRequired() + " Units)");

        String patientInfo = "Patient: " + model.getPatientName();
        holder.tvPatientDetails.setText(patientInfo);

        String location = model.getHospitalNameArea() != null ? model.getHospitalNameArea() : "Unknown Location";
        holder.tvHospital.setText("Location: " + location);

        String completedTxt = "Status: Completed";
        if (model.getCompletedAt() > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault());
            completedTxt += " on " + sdf.format(new java.util.Date(model.getCompletedAt()));
        }
        
        holder.tvCompletedInfo.setText(completedTxt);

        if (model.getStatusMessage() != null && !model.getStatusMessage().isEmpty()) {
            holder.tvStatusMessage.setText(model.getStatusMessage());
            holder.layoutStatusMessageContainer.setVisibility(View.VISIBLE);
        } else {
            holder.layoutStatusMessageContainer.setVisibility(View.GONE);
        }

        if (model.getNotes() != null && !model.getNotes().isEmpty()) {
            holder.tvNotes.setText(model.getNotes());
            holder.layoutNotesContainer.setVisibility(View.VISIBLE);
        } else {
            holder.layoutNotesContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPatientDetails, tvHospital, tvCompletedInfo, tvStatusMessage, tvNotes;
        View layoutNotesContainer, layoutStatusMessageContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvCompletedInfo = itemView.findViewById(R.id.tvCompletedInfo);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            layoutNotesContainer = itemView.findViewById(R.id.layoutNotesContainer);
            layoutStatusMessageContainer = itemView.findViewById(R.id.layoutStatusMessageContainer);
        }
    }
}
