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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;
import edu.ewubd.bloodmap.database.LocalDatabaseManager;

public class AvailableFragment extends Fragment {

    private RecyclerView rvAvailableHierarchy;
    private EditText etSearchAvailable;
    private String pendingSearchQuery = null; // set via Bundle args, not a static field
    
    private AvailableHierarchyAdapter adapter;
    private List<AvailableItemModel> masterList = new ArrayList<>();
    private List<AvailableItemModel> filteredList = new ArrayList<>();
    private ListenerRegistration availableRegistration;
    private LocalDatabaseManager dbManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available, container, false);

        dbManager = new LocalDatabaseManager(getContext());
        rvAvailableHierarchy = view.findViewById(R.id.rvAvailableHierarchy);
        etSearchAvailable = view.findViewById(R.id.etSearchAvailable);

        rvAvailableHierarchy.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new AvailableHierarchyAdapter(getContext(), filteredList);
        rvAvailableHierarchy.setAdapter(adapter);

        etSearchAvailable.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Read search query from Bundle args
        if (getArguments() != null) {
            pendingSearchQuery = getArguments().getString("searchQuery");
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchAvailableDonors();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (availableRegistration != null) {
            availableRegistration.remove();
            availableRegistration = null;
        }
    }

    private void fetchAvailableDonors() {
        availableRegistration = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("availableToDonate", true)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        loadFromCache();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<UserModel> donorList = new ArrayList<>();
                        Map<String, List<UserModel>> areaGroups = new HashMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                UserModel user = doc.toObject(UserModel.class);
                                donorList.add(user);
                                String area = (user.getLocationArea() != null && !user.getLocationArea().isEmpty()) 
                                                ? user.getLocationArea() : "Unknown Area";
                                
                                if (!areaGroups.containsKey(area)) {
                                    areaGroups.put(area, new ArrayList<>());
                                }
                                areaGroups.get(area).add(user);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        // Sync to local DB
                        dbManager.syncDonors(donorList);

                        buildHierarchy(areaGroups);
                        
                        if (pendingSearchQuery != null) {
                            etSearchAvailable.setText(pendingSearchQuery);
                            pendingSearchQuery = null;
                        }
                    } else {
                        loadFromCache();
                    }
                });
    }

    private void loadFromCache() {
        List<UserModel> cachedDonors = dbManager.getCachedDonors();
        if (cachedDonors != null && !cachedDonors.isEmpty()) {
            Map<String, List<UserModel>> areaGroups = new HashMap<>();
            for (UserModel user : cachedDonors) {
                String area = (user.getLocationArea() != null && !user.getLocationArea().isEmpty()) 
                                ? user.getLocationArea() : "Unknown Area";
                if (!areaGroups.containsKey(area)) {
                    areaGroups.put(area, new ArrayList<>());
                }
                areaGroups.get(area).add(user);
            }
            buildHierarchy(areaGroups);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Showing cached offline results.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "No offline data available.", Toast.LENGTH_SHORT).show();
            }
        }
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

        // 2. EMPTY DIVISIONS 
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Khulna"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Barishal"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Chittagong"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Rajshahi"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Sylhet"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Rangpur"));
        masterList.add(new AvailableItemModel(AvailableItemModel.TYPE_DIVISION, "Mymensingh"));

        filterData(""); 
    }

    private void filterData(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            for (AvailableItemModel item : masterList) {
                if (item.getType() == AvailableItemModel.TYPE_DIVISION) {
                    filteredList.add(item);
                } else if (item.getType() == AvailableItemModel.TYPE_AREA) {
                    
                    if (item.getName().toLowerCase().contains(lowerQuery)) {
                        filteredList.add(item);
                        continue;
                    }
                    
                    List<UserModel> matchingDonors = new ArrayList<>();
                    for (UserModel donor : item.getDonors()) {
                        if (donor.getBloodGroup() != null && donor.getBloodGroup().toLowerCase().contains(lowerQuery)) {
                            matchingDonors.add(donor);
                        }
                    }
                    
                    if (!matchingDonors.isEmpty()) {
                        AvailableItemModel filteredAreaNode = new AvailableItemModel(AvailableItemModel.TYPE_AREA, item.getName(), matchingDonors);
                        filteredAreaNode.setExpanded(true); 
                        filteredList.add(filteredAreaNode);
                    }
                }
            }
        }
        adapter.updateData(filteredList);
    }
}
