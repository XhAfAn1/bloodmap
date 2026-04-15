package edu.ewubd.bloodmap.HomePages.availableFrag;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class AvailableHierarchyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<AvailableItemModel> items;

    public AvailableHierarchyAdapter(Context context, List<AvailableItemModel> items) {
        this.context = context;
        this.items = items;
    }

    public void updateData(List<AvailableItemModel> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AvailableItemModel.TYPE_DIVISION) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_available_division, parent, false);
            return new DivisionViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.item_available_area, parent, false);
            return new AreaViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AvailableItemModel item = items.get(position);

        if (holder instanceof DivisionViewHolder) {
            DivisionViewHolder divHolder = (DivisionViewHolder) holder;
            String text = item.getName() + " (" + item.getDonorCount() + " Donors)";
            divHolder.tvDivisionName.setText(text);

        } else if (holder instanceof AreaViewHolder) {
            AreaViewHolder areaHolder = (AreaViewHolder) holder;
            areaHolder.tvAreaName.setText(item.getName());
            areaHolder.tvDonorCount.setText(item.getDonorCount() + " Donors");

            if (item.isExpanded()) {
                areaHolder.llExpandedContent.setVisibility(View.VISIBLE);
                areaHolder.ivExpandIcon.setRotation(180f);
                areaHolder.tvBloodStats.setText(item.getBloodStats());

                areaHolder.llDonorListContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(context);
                for (UserModel donor : item.getDonors()) {
                    View donorView = inflater.inflate(R.layout.item_available_donor, areaHolder.llDonorListContainer, false);
                    TextView tvName = donorView.findViewById(R.id.tvDonorName);
                    TextView tvPhone = donorView.findViewById(R.id.tvDonorPhone);
                    TextView tvBadge = donorView.findViewById(R.id.tvDonorBloodBadge);
                    
                    tvName.setText(donor.getName() != null ? donor.getName() : "Anonymous Donor");
                    
                    String phone = donor.getContactNumber() != null ? donor.getContactNumber() : "No Contact";
                    tvPhone.setText("Contact: " + phone);
                    
                    String bg = donor.getBloodGroup();
                    tvBadge.setText((bg != null && !bg.isEmpty()) ? bg : "?");

                    // Optional: click donor view to call
                    donorView.setOnClickListener(v -> {
                        if (!phone.equals("No Contact")) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            context.startActivity(intent);
                        }
                    });

                    areaHolder.llDonorListContainer.addView(donorView);
                }
            } else {
                areaHolder.llExpandedContent.setVisibility(View.GONE);
                areaHolder.ivExpandIcon.setRotation(0f);
            }

            areaHolder.llHeaderToggle.setOnClickListener(v -> {
                item.setExpanded(!item.isExpanded());
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DivisionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDivisionName;
        public DivisionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDivisionName = itemView.findViewById(R.id.tvDivisionName);
        }
    }

    static class AreaViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llHeaderToggle, llExpandedContent, llDonorListContainer;
        TextView tvAreaName, tvDonorCount, tvBloodStats;
        ImageView ivExpandIcon;

        public AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            llHeaderToggle = itemView.findViewById(R.id.llHeaderToggle);
            llExpandedContent = itemView.findViewById(R.id.llExpandedContent);
            llDonorListContainer = itemView.findViewById(R.id.llDonorListContainer);
            tvAreaName = itemView.findViewById(R.id.tvAreaName);
            tvDonorCount = itemView.findViewById(R.id.tvDonorCount);
            tvBloodStats = itemView.findViewById(R.id.tvBloodStats);
            ivExpandIcon = itemView.findViewById(R.id.ivExpandIcon);
        }
    }
}
