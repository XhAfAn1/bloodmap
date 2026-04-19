package edu.ewubd.bloodmap.DrawerPages.nearBloodBank;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.location.Location;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.bloodbankManagement.AddBloodBankActivity;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.BankViewHolder> {

    private List<BloodBankModel> bankList;
    private Double userLat, userLong;

    public BloodBankAdapter(List<BloodBankModel> bankList) {
        this.bankList = bankList;
    }

    public void setUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLong = lng;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_bank, parent, false);
        return new BankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
        BloodBankModel model = bankList.get(position);
        holder.tvBankName.setText(model.getBankName());
        holder.tvContact.setText("Phone: " + model.getContactNumber());
        holder.tvAddress.setText(model.getAddress());

        // Stock Summary
        Map<String, Integer> stock = model.getAvailableStock();
        if (stock != null && !stock.isEmpty()) {
            StringBuilder sb = new StringBuilder("Available: ");
            boolean first = true;
            for (Map.Entry<String, Integer> entry : stock.entrySet()) {
                if (entry.getValue() > 0) {
                    if (!first) sb.append(", ");
                    sb.append(entry.getKey()).append(": ").append(entry.getValue());
                    first = false;
                }
            }
            if (first) sb.append("No stock available");
            holder.tvStockSummary.setText(sb.toString());
        } else {
            holder.tvStockSummary.setText("Inventory data unavailable");
        }

        // Coordinates
        String coordStr = String.format("Coordinates: %.4f, %.4f", model.getLatitude(), model.getLongitude());
        holder.tvBankCoordinates.setText(coordStr);

        if (userLat != null && userLong != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLong, model.getLatitude(), model.getLongitude(), results);
            float distanceKm = results[0] / 1000f;
            holder.tvDistance.setText(String.format("%.2f km away", distanceKm));
            holder.tvDistance.setVisibility(View.VISIBLE);
        } else {
            holder.tvDistance.setVisibility(View.GONE);
        }

        // 24h Badge
        if (model.isOpen24Hours()) {
            holder.tv24hBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tv24hBadge.setVisibility(View.GONE);
        }

        // Always hide edit button for normal users
        holder.btnEditBank.setVisibility(View.GONE);

        holder.btnCallBank.setOnClickListener(v -> {
            String phone = model.getContactNumber();
            if (phone != null && !phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                v.getContext().startActivity(intent);
            }
        });

        holder.btnNavigateBank.setOnClickListener(v -> {
            String uri = String.format(java.util.Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                model.getLatitude(), model.getLongitude(), 
                model.getLatitude(), model.getLongitude(), 
                Uri.encode(model.getBankName()));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvContact, tvAddress, tvStockSummary, tvBankCoordinates, tvDistance, tv24hBadge;
        ImageButton btnEditBank;
        View btnCallBank, btnNavigateBank;

        public BankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStockSummary = itemView.findViewById(R.id.tvStockSummary);
            tvBankCoordinates = itemView.findViewById(R.id.tvBankCoordinates);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tv24hBadge = itemView.findViewById(R.id.tv24hBadge);
            btnEditBank = itemView.findViewById(R.id.btnEditBank);
            btnCallBank = itemView.findViewById(R.id.btnCallBank);
            btnNavigateBank = itemView.findViewById(R.id.btnNavigateBank);
        }
    }
}
