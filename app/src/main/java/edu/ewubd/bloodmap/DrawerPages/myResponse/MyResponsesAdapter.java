package edu.ewubd.bloodmap.DrawerPages.myResponse;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.R;

public class MyResponsesAdapter extends RecyclerView.Adapter<MyResponsesAdapter.ViewHolder> {

    public interface OnWithdrawClickListener {
        void onWithdrawClick(BloodTransactionModel model, int position);
    }

    private List<BloodTransactionModel> requestList;
    private OnWithdrawClickListener listener;

    public MyResponsesAdapter(List<BloodTransactionModel> requestList, OnWithdrawClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_response, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodTransactionModel model = requestList.get(position);

        holder.tvTitle.setText(model.getBloodGroup() + " Blood Required (" + model.getUnitsRequired() + " Units)");
        holder.tvPatientDetails.setText("Patient: " + model.getPatientName());

        String location = model.getHospitalNameArea() != null ? model.getHospitalNameArea() : "Unknown Location";
        holder.tvHospital.setText("Location: " + location);

        if (model.getStatusMessage() != null && !model.getStatusMessage().isEmpty()) {
            holder.tvStatusMessage.setText(model.getStatusMessage());
            holder.layoutStatusMessageContainer.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatusMessage.setText("Response Sent! Waiting for Approval.");
            holder.layoutStatusMessageContainer.setVisibility(View.VISIBLE);
        }

        if (model.getNotes() != null && !model.getNotes().isEmpty()) {
            holder.tvNotes.setText(model.getNotes());
            holder.layoutNotesContainer.setVisibility(View.VISIBLE);
        } else {
            holder.layoutNotesContainer.setVisibility(View.GONE);
        }

        holder.btnWithdraw.setOnClickListener(v -> {
            if (listener != null) listener.onWithdrawClick(model, position);
        });

        holder.btnNavigateResponse.setOnClickListener(v -> {
            String uri = String.format(java.util.Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                model.getLatitude(), model.getLongitude(), 
                model.getLatitude(), model.getLongitude(), 
                Uri.encode(model.getHospitalNameArea()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPatientDetails, tvHospital, tvStatusMessage, tvNotes;
        View layoutNotesContainer, layoutStatusMessageContainer;
        Button btnWithdraw, btnNavigateResponse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPatientDetails = itemView.findViewById(R.id.tvPatientDetails);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvStatusMessage = itemView.findViewById(R.id.tvStatusMessage);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            layoutNotesContainer = itemView.findViewById(R.id.layoutNotesContainer);
            layoutStatusMessageContainer = itemView.findViewById(R.id.layoutStatusMessageContainer);
            btnWithdraw = itemView.findViewById(R.id.btnWithdraw);
            btnNavigateResponse = itemView.findViewById(R.id.btnNavigateResponse);
        }
    }
}
