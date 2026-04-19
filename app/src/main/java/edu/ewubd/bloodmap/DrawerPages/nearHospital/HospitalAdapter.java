package edu.ewubd.bloodmap.DrawerPages.nearHospital;

import android.content.Intent;
import android.net.Uri;
import android.location.Location;
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
import edu.ewubd.bloodmap.admin.hospitalsManagement.AddHospitalActivity;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private List<HospitalContactModel> hospitalList;
    private Double userLat, userLong;

    public HospitalAdapter(List<HospitalContactModel> hospitalList) {
        this.hospitalList = hospitalList;
    }

    public void setUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLong = lng;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
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
        String coordStr = String.format("Coordinates: %.4f, %.4f", model.getLatitude(), model.getLongitude());
        holder.tvCoordinates.setText(coordStr);

        if (userLat != null && userLong != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLong, model.getLatitude(), model.getLongitude(), results);
            float distanceKm = results[0] / 1000f;
            holder.tvDistance.setText(String.format("%.2f km away", distanceKm));
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        // Display Blood Bank Status
        if (model.isHasBloodBank()) {
            holder.tvBloodBankStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvBloodBankStatus.setVisibility(View.GONE);
        }

        holder.btnCallHospital.setOnClickListener(v -> {
            String phone = model.getContactNumber();
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                v.getContext().startActivity(intent);
            }
        });

        holder.btnNavigateHospital.setOnClickListener(v -> {
            String uri = String.format(java.util.Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                model.getLatitude(), model.getLongitude(), 
                model.getLatitude(), model.getLongitude(), 
                Uri.encode(model.getHospitalName()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });

        // Always hide edit button for normal users
        holder.btnEditHospital.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvHospitalContact, tvHospitalAddress, tvFacilities, tvCoordinates, tvDistance, tvBloodBankStatus;
        ImageButton btnEditHospital;
        View btnCallHospital, btnNavigateHospital;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvHospitalContact = itemView.findViewById(R.id.tvHospitalContact);
            tvHospitalAddress = itemView.findViewById(R.id.tvHospitalAddress);
            tvFacilities = itemView.findViewById(R.id.tvFacilities);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvBloodBankStatus = itemView.findViewById(R.id.tvBloodBankStatus);
            btnEditHospital = itemView.findViewById(R.id.btnEditHospital);
            btnCallHospital = itemView.findViewById(R.id.btnCallHospital);
            btnNavigateHospital = itemView.findViewById(R.id.btnNavigateHospital);
        }
    }
}
