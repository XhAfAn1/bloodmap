package edu.ewubd.bloodmap.admin.bloodbankManagement;
 
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

public class AdminBloodBankAdapter extends RecyclerView.Adapter<AdminBloodBankAdapter.AdminBankViewHolder> {
 
    private List<BloodBankModel> bankList;
 
    public AdminBloodBankAdapter(List<BloodBankModel> bankList) {
        this.bankList = bankList;
    }
 
    @NonNull
    @Override
    public AdminBankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_bank, parent, false);
        return new AdminBankViewHolder(view);
    }
 
    @Override
    public void onBindViewHolder(@NonNull AdminBankViewHolder holder, int position) {
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
 
        // Admin Edit Button - Always visible for this adapter
        holder.btnEditBank.setVisibility(View.VISIBLE);
        holder.btnCallBank.setVisibility(View.GONE);
        holder.btnNavigateBank.setVisibility(View.GONE);

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
    }
 
    @Override
    public int getItemCount() {
        return bankList.size();
    }
 
    static class AdminBankViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvContact, tvAddress, tvStockSummary, tvBankCoordinates, tv24hBadge;
        ImageButton btnEditBank;
        View btnCallBank, btnNavigateBank;
 
        public AdminBankViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStockSummary = itemView.findViewById(R.id.tvStockSummary);
            tvBankCoordinates = itemView.findViewById(R.id.tvBankCoordinates);
            tv24hBadge = itemView.findViewById(R.id.tv24hBadge);
            btnEditBank = itemView.findViewById(R.id.btnEditBank);
            btnCallBank = itemView.findViewById(R.id.btnCallBank);
            btnNavigateBank = itemView.findViewById(R.id.btnNavigateBank);
        }
    }
}
