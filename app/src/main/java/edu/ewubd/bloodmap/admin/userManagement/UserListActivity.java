package edu.ewubd.bloodmap.admin.userManagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class UserListActivity extends AppCompatActivity implements UserAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBarUsers);
    }
 
    @Override
    protected void onStart() {
        super.onStart();
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
 
        FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                progressBar.setVisibility(View.GONE);
                
                if (e != null) {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
 
                if (queryDocumentSnapshots != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                    userList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel model = doc.toObject(UserModel.class);
                        userList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public void onBanUser(UserModel user) {
        boolean isBanned = "BLOCKED".equalsIgnoreCase(user.getStatus());
        String action = isBanned ? "Unban" : "Ban";
        String newStatus = isBanned ? "ACTIVE" : "BLOCKED";
        String message = isBanned
            ? "Are you sure you want to unban " + user.getName() + "? They will be able to use the app normally again."
            : "Are you sure you want to ban " + user.getName() + "? They will be unable to create requests or respond to blood requests.";

        new AlertDialog.Builder(this)
            .setTitle(action + " User")
            .setMessage(message)
            .setPositiveButton(action.toUpperCase(), (dialog, which) -> {
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, user.getName() + " has been " + action.toLowerCase() + "ned.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    @Override
    public void onUpgradeToPremium(UserModel user) {
        boolean isPremium = "PREMIUM".equalsIgnoreCase(user.getSubscriptionPlan());
        String action = isPremium ? "Remove Premium" : "Upgrade to Premium";
        String newPlan = isPremium ? "FREE" : "PREMIUM";
        String message = isPremium
            ? "Are you sure you want to remove premium status from " + user.getName() + "?"
            : "Are you sure you want to upgrade " + user.getName() + " to PREMIUM? Their requests will be pinned at the top of the feed and all matching blood group users will be notified.";

        new AlertDialog.Builder(this)
            .setTitle(action)
            .setMessage(message)
            .setPositiveButton("CONFIRM", (dialog, which) -> {
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update("subscriptionPlan", newPlan)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, user.getName() + "'s plan set to " + newPlan + ".", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update plan.", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }
}
