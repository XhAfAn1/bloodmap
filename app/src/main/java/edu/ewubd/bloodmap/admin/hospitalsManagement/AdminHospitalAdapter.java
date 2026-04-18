package edu.ewubd.bloodmap.admin.hospitalsManagement;
 
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
 
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.R;

public class AdminHospitalAdapter extends RecyclerView.Adapter<AdminHospitalAdapter.AdminHospitalViewHolder> {
 
    private List<HospitalContactModel> hospitalList;
 
    public AdminHospitalAdapter(List<HospitalContactModel> hospitalList) {
        this.hospitalList = hospitalList;
    }
 
    @NonNull
    @Override
    public AdminHospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new AdminHospitalViewHolder(view);
    }
 
    @Override
    public void onBindViewHolder(@NonNull AdminHospitalViewHolder holder, int position) {
        HospitalContactModel model = hospitalList.get(position);
        holder.tvHospitalName.setText(model.getHospitalName());
        holder.tvHospitalContact.setText("Phone: " + model.getContactNumber());
        holder.tvHospitalAddress.setText(model.getAddress());
 
        // Display Facilities
        if (model.getAvailableFacilities() != null && !model.getAvailableFacilities().isEmpty()) {
            StringBuilder sb = new StringBuilder("Facilities: ");
            for (int i = 0; i < model.getAvailableFacilities().size(); i++) {
                sb.append(model.getAvailableFacilities().get(i));
                if (i < model.getAvailableFacilities().size() - 1) sb.append(", ");
            }
            holder.tvFacilities.setText(sb.toString());
            holder.tvFacilities.setVisibility(View.VISIBLE);
        } else {
            holder.tvFacilities.setVisibility(View.GONE);
        }
 
        // Display Coordinates
        holder.tvCoordinates.setText(String.format("Coordinates: %.4f, %.4f", model.getLatitude(), model.getLongitude()));
 
        // Display Blood Bank Status
        if (model.isHasBloodBank()) {
            holder.tvBloodBankStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvBloodBankStatus.setVisibility(View.GONE);
        }
 
        // Admin Edit Button - Always visible for this adapter
        holder.btnEditHospital.setVisibility(View.VISIBLE);
        holder.btnCallHospital.setVisibility(View.GONE);
        holder.btnNavigateHospital.setVisibility(View.GONE);

        holder.btnEditHospital.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddHospitalActivity.class);
            intent.putExtra("ID", model.getHospitalId());
            intent.putExtra("NAME", model.getHospitalName());
            intent.putExtra("PHONE", model.getContactNumber());
            intent.putExtra("ADDRESS", model.getAddress());
            intent.putExtra("LATITUDE", model.getLatitude());
            intent.putExtra("LONGITUDE", model.getLongitude());
            intent.putExtra("HAS_BLOOD_BANK", model.isHasBloodBank());
            
            if (model.getAvailableFacilities() != null) {
                StringBuilder fsb = new StringBuilder();
                for (int i = 0; i < model.getAvailableFacilities().size(); i++) {
                    fsb.append(model.getAvailableFacilities().get(i));
                    if (i < model.getAvailableFacilities().size() - 1) fsb.append(", ");
                }
                intent.putExtra("FACILITIES", fsb.toString());
            }
            v.getContext().startActivity(intent);
        });
    }
 
    @Override
    public int getItemCount() {
        return hospitalList.size();
    }
 
    static class AdminHospitalViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvHospitalContact, tvHospitalAddress, tvFacilities, tvCoordinates, tvBloodBankStatus;
        ImageButton btnEditHospital;
        View btnCallHospital, btnNavigateHospital;
 
        public AdminHospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvHospitalContact = itemView.findViewById(R.id.tvHospitalContact);
            tvHospitalAddress = itemView.findViewById(R.id.tvHospitalAddress);
            tvFacilities = itemView.findViewById(R.id.tvFacilities);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvBloodBankStatus = itemView.findViewById(R.id.tvBloodBankStatus);
            btnEditHospital = itemView.findViewById(R.id.btnEditHospital);
            btnCallHospital = itemView.findViewById(R.id.btnCallHospital);
            btnNavigateHospital = itemView.findViewById(R.id.btnNavigateHospital);
        }
    }
}
