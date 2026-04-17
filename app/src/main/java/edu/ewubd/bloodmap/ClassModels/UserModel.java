package edu.ewubd.bloodmap.ClassModels;

import java.util.Date;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private boolean isAdmin;
    private String bloodGroup;
    private String locationArea;
    private String contactNumber;
    private String token;
    private String profileImageUrl;
    private double latitude;
    private double longitude;
    private int totalDonations;
    private int totalRequests;
    private String gender;
    private Date dateOfBirth;
    private String address;
    private boolean availableToDonate;
    private Date lastDonationDate;
    private Date nextEligibleDate;
    private String status; // ACTIVE, BLOCKED
    private String subscriptionPlan;
    private Date createdAt;
    private Date updatedAt;

    public UserModel() {
    }

    public UserModel(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isAdmin = false;
        this.bloodGroup = "";
        this.locationArea = "";
        this.contactNumber = "";
        this.token = "";
        this.profileImageUrl = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.totalDonations = 0;
        this.totalRequests = 0;
        this.gender = "";
        this.dateOfBirth = null;
        this.address = "";
        this.availableToDonate = true;
        this.lastDonationDate = null;
        this.nextEligibleDate = null;
        this.status = "ACTIVE";
        this.subscriptionPlan = "FREE";
        this.createdAt = new Date();
        this.updatedAt = new Date();
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(int totalDonations) {
        this.totalDonations = totalDonations;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isAvailableToDonate() {
        return availableToDonate;
    }

    public void setAvailableToDonate(boolean availableToDonate) {
        this.availableToDonate = availableToDonate;
    }

    public Date getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(Date lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public Date getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(Date nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
