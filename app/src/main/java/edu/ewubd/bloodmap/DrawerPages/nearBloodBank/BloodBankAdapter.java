package edu.ewubd.bloodmap.DrawerPages.nearBloodBank;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.admin.AddBloodBankActivity;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.BankViewHolder> {

    private List<BloodBankModel> bankList;
    private boolean isAdmin;

    public BloodBankAdapter(List<BloodBankModel> bankList, boolean isAdmin) {
        this.bankList = bankList;
        this.isAdmin = isAdmin;
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
        holder.tvBankCoordinates.setText(String.format("Coordinates: %.4f, %.4f", model.getLatitude(), model.getLongitude()));

        // 24h Badge
        if (model.isOpen24Hours()) {
            holder.tv24hBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tv24hBadge.setVisibility(View.GONE);
        }

        if (isAdmin) {
            holder.btnEditBank.setVisibility(View.VISIBLE);
            holder.btnEditBank.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AddBloodBankActivity.class);
                intent.putExtra("ID", model.getBloodBankId());
                intent.putExtra("NAME", model.getBankName());
                intent.putExtra("PHONE", model.getContactNumber());
                intent.putExtra("ADDRESS", model.getAddress());
                intent.putExtra("LATITUDE", model.getLatitude());
                intent.putExtra("LONGITUDE", model.getLongitude());
                intent.putExtra("IS_OPEN_24H", model.isOpen24Hours());
                intent.putExtra("STOCK", (Serializable) model.getAvailableStock());
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnEditBank.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bankList.size();
    }

    static class BankViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvContact, tvAddress, tvStockSummary, tvBankCoordinates, tv24hBadge;
        ImageButton btnEditBank;

        public BankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStockSummary = itemView.findViewById(R.id.tvStockSummary);
            tvBankCoordinates = itemView.findViewById(R.id.tvBankCoordinates);
            tv24hBadge = itemView.findViewById(R.id.tv24hBadge);
            btnEditBank = itemView.findViewById(R.id.btnEditBank);
        }
    }
}
