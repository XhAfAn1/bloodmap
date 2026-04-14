package edu.ewubd.bloodmap.ClassModels;

import org.json.JSONObject;

public class userModel {
    private String uid;
    private String name;
    private String email;
    private boolean isAdmin;
    private String bloodGroup;
    private String locationArea;
    private String contactNumber;

    public userModel() {
    }

    public userModel(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isAdmin = false;
        this.bloodGroup = "";
        this.locationArea = "";
        this.contactNumber = "";
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLocationArea() {
        return locationArea;
    }

    public void setLocationArea(String locationArea) {
        this.locationArea = locationArea;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("uid", uid);
            json.put("name", name);
            json.put("email", email);
            json.put("isAdmin", isAdmin);
            json.put("bloodGroup", bloodGroup);
            json.put("locationArea", locationArea);
            json.put("contactNumber", contactNumber);
            return json.toString();
        } catch (Exception e) { return "{}"; }
    }

    public static userModel fromJson(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            userModel model = new userModel(
                json.optString("uid", ""),
                json.optString("name", ""),
                json.optString("email", "")
            );
            model.setAdmin(json.optBoolean("isAdmin", false));
            model.setBloodGroup(json.optString("bloodGroup", ""));
            model.setLocationArea(json.optString("locationArea", ""));
            model.setContactNumber(json.optString("contactNumber", ""));
            return model;
        } catch (Exception e) { return new userModel(); }
    }
}
