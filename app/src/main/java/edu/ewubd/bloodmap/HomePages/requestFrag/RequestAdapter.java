package edu.ewubd.bloodmap.HomePages.requestFrag;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<BloodTransactionModel> requestList;

    public RequestAdapter(List<BloodTransactionModel> requestList) {
        this.requestList = requestList;
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
        
        if (model.getUrgencyLevel() != null && !model.getUrgencyLevel().isEmpty()) {
            holder.tvUrgency.setText(model.getUrgencyLevel());
            holder.tvUrgency.setVisibility(View.VISIBLE);
        } else {
            holder.tvUrgency.setVisibility(View.GONE);
        }

        String patientInfo = "Patient: " + model.getPatientName();
        if (model.getPatientAge() != null && !model.getPatientAge().isEmpty()) {
            patientInfo += " (Age: " + model.getPatientAge() + ")";
        }
        holder.tvPatientDetails.setText(patientInfo);

        if (model.getReason() != null && !model.getReason().isEmpty()) {
            holder.tvReason.setText("Reason: " + model.getReason());
            holder.tvReason.setVisibility(View.VISIBLE);
        } else {
            holder.tvReason.setVisibility(View.GONE);
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
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvHospital, tvTime, tvUrgency, tvPatientDetails, tvReason;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvReason = itemView.findViewById(R.id.tvReason);
        }
    }
}
