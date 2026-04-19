package edu.ewubd.bloodmap.DrawerPages.myRequst;

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
import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class ResponderAdapter extends RecyclerView.Adapter<ResponderAdapter.ViewHolder> {

    public interface OnAcceptDonorListener {
        void onAcceptDonorClick(UserModel userModel, int position);
    }

    private List<UserModel> responderList;
    private OnAcceptDonorListener listener;

    public ResponderAdapter(List<UserModel> responderList, OnAcceptDonorListener listener) {
        this.responderList = responderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_responder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = responderList.get(position);

        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown User");
        holder.tvBloodGroup.setText(user.getBloodGroup() != null ? user.getBloodGroup() : "Unknown");
        holder.tvContact.setText("Contact: " + (user.getContactNumber() != null ? user.getContactNumber() : "N/A"));
        
        holder.tvStats.setText("Total Donations: " + user.getTotalDonations());

        holder.tvStats.setText("Total Donations: " + user.getTotalDonations());

        holder.btnCallDonor.setOnClickListener(v -> {
            String phone = user.getContactNumber();
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                v.getContext().startActivity(intent);
            } else {
                android.widget.Toast.makeText(v.getContext(), "Contact number not available", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnAcceptDonor.setOnClickListener(v -> {
            if (listener != null) listener.onAcceptDonorClick(user, position);
        });
    }

    @Override
    public int getItemCount() {
        return responderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBloodGroup, tvContact, tvStats;
        android.view.View btnAcceptDonor, btnCallDonor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvStats = itemView.findViewById(R.id.tvStats);
            btnAcceptDonor = itemView.findViewById(R.id.btnAcceptDonor);
            btnCallDonor = itemView.findViewById(R.id.btnCallDonor);
        }
    }
}
