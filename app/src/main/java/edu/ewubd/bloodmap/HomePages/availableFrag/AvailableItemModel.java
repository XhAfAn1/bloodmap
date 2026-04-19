package edu.ewubd.bloodmap.HomePages.availableFrag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ewubd.bloodmap.ClassModels.UserModel;

public class AvailableItemModel {
    public static final int TYPE_DIVISION = 0;
    public static final int TYPE_AREA = 1;

    private int type;

    private String name;
    private int donorCount;

    private List<UserModel> donors = new ArrayList<>();
    private boolean isExpanded = false;

    public AvailableItemModel(int type, String name) {
        this.type = type;
        this.name = name;
        this.donorCount = 0;
    }

    public AvailableItemModel(int type, String name, List<UserModel> donors) {
        this.type = type;
        this.name = name;
        this.donors = donors;
        this.donorCount = donors.size();
    }

    public int getType() { return type; }
    public String getName() { return name; }
    public int getDonorCount() { return donorCount; }
    public List<UserModel> getDonors() { return donors; }
    public boolean isExpanded() { return isExpanded; }

    public void setExpanded(boolean expanded) { isExpanded = expanded; }
    public void setDonorCount(int count) { this.donorCount = count; }

    public String getBloodStats() {
        if (donors == null || donors.isEmpty()) return "No Blood Stats Available";
        
        Map<String, Integer> stats = new HashMap<>();
        for (UserModel user : donors) {
            String bg = user.getBloodGroup();
            if (bg != null && !bg.isEmpty()) {
                stats.put(bg, stats.getOrDefault(bg, 0) + 1);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        return sb.toString();
    }
}
