package edu.ewubd.bloodmap.HomePages.availableFrag;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class AvailableFragment extends Fragment {

    public static String pendingSearchQuery = null;

    private RecyclerView rvAvailableHierarchy;
    private EditText etSearchAvailable;
    
    private AvailableHierarchyAdapter adapter;
    private List<AvailableItemModel> masterList = new ArrayList<>();
    private List<AvailableItemModel> filteredList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available, container, false);

        rvAvailableHierarchy = view.findViewById(R.id.rvAvailableHierarchy);
        etSearchAvailable = view.findViewById(R.id.etSearchAvailable);

        rvAvailableHierarchy.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new AvailableHierarchyAdapter(getContext(), filteredList);
        rvAvailableHierarchy.setAdapter(adapter);

        fetchAvailableDonors();

        etSearchAvailable.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void fetchAvailableDonors() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("availableToDonate", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    
                    Map<String, List<UserModel>> areaGroups = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            UserModel user = doc.toObject(UserModel.class);
                            String area = (user.getLocationArea() != null && !user.getLocationArea().isEmpty()) 
                                            ? user.getLocationArea() : "Unknown Area";
                            
                            if (!areaGroups.containsKey(area)) {
                                areaGroups.put(area, new ArrayList<>());
                            }
                            areaGroups.get(area).add(user);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    buildHierarchy(areaGroups);
                    
                    if (pendingSearchQuery != null) {
                        etSearchAvailable.setText(pendingSearchQuery);
                        pendingSearchQuery = null;
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to fetch donors.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buildHierarchy(Map<String, List<UserModel>> areaGroups) {
        masterList.clear();

        // 1. DHAKA DIVISION
        int dhakaTotal = 0;
        List<AvailableItemModel> dhakaAreas = new ArrayList<>();
        
        for (Map.Entry<String, List<UserModel>> entry : areaGroups.entrySet()) {
            AvailableItemModel areaNode = new AvailableItemModel(AvailableItemModel.TYPE_AREA, entry.getKey(), entry.getValue());
            dhakaAreas.add(areaNode);
            dhakaTotal += entry.getValue().size();
        }

        AvailableItemModel dhakaNode = new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Dhaka");
        dhakaNode.setDonorCount(dhakaTotal);
        
        masterList.add(dhakaNode);
        masterList.addAll(dhakaAreas);

        // 2. EMPTY DIVISIONS (Placeholder Logic)
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Khulna"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Barishal"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Chittagong"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Rajshahi"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Sylhet"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Rangpur"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Mymensingh"));

        filterData(""); // Initialize list unconditionally
    }

    private void filterData(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (AvailableItemModel item : masterList) {
                if (item.getType() == AvailableItemModel.TYPE_DIVISION) {
                    // Always show divisions (or we can hide them, but showing is structured)
                    filteredList.add(item);
                } else if (item.getType() == AvailableItemModel.TYPE_AREA) {
                    
                    // Priority 1: Area matched name
                    if (item.getName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(item);
                        continue;
                    }
                    
                    // Priority 2: Filter Donors by Blood Group and rebuild node if matches exist
                    List<UserModel> matchingDonors = new ArrayList<>();
                    for (UserModel donor : item.getDonors()) {
                        if (donor.getBloodGroup() != null && donor.getBloodGroup().toLowerCase().contains(lowerQuery)) {
                            matchingDonors.add(donor);
                        }
                    }
                    
                    if (!matchingDonors.isEmpty()) {
                        AvailableItemModel filteredAreaNode = new AvailableItemModel(AvailableItemModel.TYPE_AREA, item.getName(), matchingDonors);
                        filteredAreaNode.setExpanded(true); // Auto-expand when manually matched
                        filteredList.add(filteredAreaNode);
                    }
                }
            }
        }
        adapter.updateData(filteredList);
    }
}
