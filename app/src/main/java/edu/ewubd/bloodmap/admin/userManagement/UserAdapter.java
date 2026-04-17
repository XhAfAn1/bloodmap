package edu.ewubd.bloodmap.admin.userManagement;
 
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;
 
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
 
    public interface OnUserActionListener {
        void onBanUser(UserModel user);
        void onUpgradeToPremium(UserModel user);
    }

    private List<UserModel> userList;
    private OnUserActionListener actionListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
 
    public UserAdapter(List<UserModel> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.actionListener = listener;
    }
 
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }
 
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        
        holder.tvUserName.setText(user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Anonymous");
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserContact.setText("Phone: " + (user.getContactNumber() == null || user.getContactNumber().isEmpty() ? "Not set" : user.getContactNumber()));

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivUserProfile);
        } else {
            holder.ivUserProfile.setImageResource(android.R.drawable.ic_menu_gallery);
        }
 
        if (user.isAdmin()) {
            holder.tvAdminBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvAdminBadge.setVisibility(View.GONE);
        }

        String status = user.getStatus();
        if (status == null || status.isEmpty()) status = "ACTIVE";
        holder.tvUserStatus.setText(status);
        if (status.equalsIgnoreCase("BLOCKED")) {
            holder.tvUserStatus.setBackgroundColor(0xFFFFCDD2);
            holder.tvUserStatus.setTextColor(0xFFD32F2F);
        } else {
            holder.tvUserStatus.setBackgroundColor(0xFFE8F5E9);
            holder.tvUserStatus.setTextColor(0xFF2E7D32);
        }

        String bg = user.getBloodGroup();
        if (bg == null || bg.isEmpty()) {
            holder.tvUserBloodGroup.setText("N/A");
            holder.tvUserBloodGroup.setBackgroundColor(0xFF888888);
        } else {
            holder.tvUserBloodGroup.setText(bg);
            holder.tvUserBloodGroup.setBackgroundColor(0xFFD32F2F);
        }
 
        if (user.isAvailableToDonate()) {
            holder.tvAvailabilityBadge.setText("AVAILABLE");
            holder.tvAvailabilityBadge.setBackgroundColor(0xFFFFF3E0);
            holder.tvAvailabilityBadge.setTextColor(0xFFE65100);
        } else {
            holder.tvAvailabilityBadge.setText("UNAVAILABLE");
            holder.tvAvailabilityBadge.setBackgroundColor(0xFFEEEEEE);
            holder.tvAvailabilityBadge.setTextColor(0xFF757575);
        }

        holder.tvUserDonations.setText("Donations: " + user.getTotalDonations());
        holder.tvUserRequests.setText("Requests: " + user.getTotalRequests());
        holder.tvLastDonationDate.setText("Last: " + (user.getLastDonationDate() != null ? dateFormat.format(user.getLastDonationDate()) : "N/A"));
        holder.tvNextEligibleDate.setText("Next: " + (user.getNextEligibleDate() != null ? dateFormat.format(user.getNextEligibleDate()) : "TBD"));
        holder.tvUserLocation.setText("Area: " + (user.getLocationArea() == null || user.getLocationArea().isEmpty() ? "Not set" : user.getLocationArea()));

        String plan = user.getSubscriptionPlan();
        String planLabel = (plan == null || plan.isEmpty()) ? "FREE" : plan;
        holder.tvSubscriptionPlan.setText("Plan: " + planLabel);
        if ("PREMIUM".equalsIgnoreCase(planLabel)) {
            holder.tvSubscriptionPlan.setTextColor(0xFFFF8F00); // gold/amber for premium
        } else {
            holder.tvSubscriptionPlan.setTextColor(0xFF1976D2);
        }

        holder.tvCreatedAt.setText("Joined: " + (user.getCreatedAt() != null ? dateFormat.format(user.getCreatedAt()) : "N/A"));

        // 3-dot overflow menu
        holder.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            
            String currentStatus = user.getStatus();
            boolean isBanned = "BLOCKED".equalsIgnoreCase(currentStatus);
            popup.getMenu().add(0, 1, 0, isBanned ? "Unban User" : "Ban User");

            String currentPlan = user.getSubscriptionPlan();
            boolean isPremium = "PREMIUM".equalsIgnoreCase(currentPlan);
            popup.getMenu().add(0, 2, 1, isPremium ? "Remove Premium" : "Upgrade to Premium");

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener == null) return false;
                if (item.getItemId() == 1) {
                    actionListener.onBanUser(user);
                    return true;
                } else if (item.getItemId() == 2) {
                    actionListener.onUpgradeToPremium(user);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }
 
    @Override
    public int getItemCount() {
        return userList.size();
    }
 
    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserProfile;
        ImageButton ivMoreOptions;
        TextView tvUserName, tvUserEmail, tvUserContact, tvUserLocation, tvUserBloodGroup, tvAdminBadge, tvUserStatus;
        TextView tvAvailabilityBadge, tvUserDonations, tvUserRequests, tvLastDonationDate, tvNextEligibleDate, tvSubscriptionPlan, tvCreatedAt;
 
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserProfile = itemView.findViewById(R.id.ivUserProfile);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserContact = itemView.findViewById(R.id.tvUserContact);
            tvUserLocation = itemView.findViewById(R.id.tvUserLocation);
            tvUserBloodGroup = itemView.findViewById(R.id.tvUserBloodGroup);
            tvAdminBadge = itemView.findViewById(R.id.tvAdminBadge);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            tvAvailabilityBadge = itemView.findViewById(R.id.tvAvailabilityBadge);
            tvUserDonations = itemView.findViewById(R.id.tvUserDonations);
            tvUserRequests = itemView.findViewById(R.id.tvUserRequests);
            tvLastDonationDate = itemView.findViewById(R.id.tvLastDonationDate);
            tvNextEligibleDate = itemView.findViewById(R.id.tvNextEligibleDate);
            tvSubscriptionPlan = itemView.findViewById(R.id.tvSubscriptionPlan);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}
