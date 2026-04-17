package edu.ewubd.bloodmap.ClassModels;

import java.util.List;

public class BloodTransactionModel {
    private String transactionId;
    private String requesterUid;
    private List<String> responderUids;
    private String selectedDonorUid; 
    private String bloodGroup;
    private String hospitalNameArea;
    private String patientName;
    private String contactNumber;
    private long neededByTime;
    private int unitsRequired;
    private String status;
    private long createdAt;
    private long completedAt;
    private double latitude;
    private double longitude;

    //optional data
    private String notes;
    private String area;
    private String urgencyLevel;  
    private String reason;     
    private String patientAge;
    private String patientGender;
    private String statusMessage;

    public BloodTransactionModel() {
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getRequesterUid() { return requesterUid; }
    public void setRequesterUid(String requesterUid) { this.requesterUid = requesterUid; }

    public List<String> getResponderUids() { return responderUids; }
    public void setResponderUids(List<String> responderUids) { this.responderUids = responderUids; }

    public String getSelectedDonorUid() { return selectedDonorUid; }
    public void setSelectedDonorUid(String selectedDonorUid) { this.selectedDonorUid = selectedDonorUid; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getHospitalNameArea() { return hospitalNameArea; }
    public void setHospitalNameArea(String hospitalNameArea) { this.hospitalNameArea = hospitalNameArea; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public long getNeededByTime() { return neededByTime; }
    public void setNeededByTime(long neededByTime) { this.neededByTime = neededByTime; }

    public int getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(int unitsRequired) { this.unitsRequired = unitsRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPatientAge() { return patientAge; }
    public void setPatientAge(String patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    private boolean premiumRequest;
    public boolean isPremiumRequest() { return premiumRequest; }
    public void setPremiumRequest(boolean premiumRequest) { this.premiumRequest = premiumRequest; }
}
