package edu.ewubd.bloodmap.HomePages.requestFrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.UUID;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.util.Log;

import edu.ewubd.bloodmap.ClassModels.BloodTransactionModel;
import edu.ewubd.bloodmap.ClassModels.LocationModel;
import edu.ewubd.bloodmap.MainActivity;
import edu.ewubd.bloodmap.Notifications.NotificationSender;
import edu.ewubd.bloodmap.R;

public class NewRequestFragment extends Fragment {
    private EditText etPatientName, etPatientAge, etUnitsRequired, etReason, etContactNumber, etNotes, etNeededByTime;
    private AutoCompleteTextView etHospitalDetails, etArea;
    private long neededByTimeInMillis = 0;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private Spinner spinnerPatientGender, spinnerBloodGroup, spinnerUrgencyLevel;
    private Button btnSubmitRequest;
    
    private List<LocationModel> hospitalList = new ArrayList<>();
    private ArrayAdapter<LocationModel> hospitalAdapter;
    private List<LocationModel> areaList = new ArrayList<>();
    private ArrayAdapter<LocationModel> areaAdapter;
    private ListenerRegistration hospitalsRegistration;
    private ListenerRegistration areasRegistration;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_request, container, false);


        etPatientName = view.findViewById(R.id.etPatientName);
        etPatientAge = view.findViewById(R.id.etPatientAge);
        spinnerPatientGender = view.findViewById(R.id.spinnerPatientGender);
        spinnerBloodGroup = view.findViewById(R.id.spinnerBloodGroup);
        etUnitsRequired = view.findViewById(R.id.etUnitsRequired);
        spinnerUrgencyLevel = view.findViewById(R.id.spinnerUrgencyLevel);
        etReason = view.findViewById(R.id.etReason);
        etHospitalDetails = view.findViewById(R.id.etHospitalDetails);
        etArea = view.findViewById(R.id.etArea);
        etContactNumber = view.findViewById(R.id.etContactNumber);
        etNotes = view.findViewById(R.id.etNotes);
        etNeededByTime = view.findViewById(R.id.etNeededByTime);
        btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);

        etNeededByTime.setOnClickListener(v -> showDateTimePicker());

        setupSpinners();

        btnSubmitRequest.setOnClickListener(v -> submitRequest());
        return view;
    }

    private void setupSpinners() {
        if (getContext() == null) return;
        
        String[] genders = {"Select Gender *", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientGender.setAdapter(genderAdapter);

        String[] bloodGroups = {"Select Blood Group *", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, bloodGroups);
        bloodGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodGroupAdapter);

        String[] urgencies = {"Select Urgency *", "Normal", "High", "Critical"};
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, urgencies);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgencyLevel.setAdapter(urgencyAdapter);

        // Setup Autocomplete Adapters using Firebase
        hospitalAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, hospitalList);
        etHospitalDetails.setAdapter(hospitalAdapter);

        areaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, areaList);
        etArea.setAdapter(areaAdapter);

        etHospitalDetails.setOnItemClickListener((parent, view, position, id) -> {
            LocationModel selected = hospitalAdapter.getItem(position);
            if (selected != null) {
                selectedLatitude = selected.getLatitude();
                selectedLongitude = selected.getLongitude();
            }
        });

        etArea.setOnItemClickListener((parent, view, position, id) -> {
            LocationModel selected = areaAdapter.getItem(position);
            if (selected != null) {
                selectedLatitude = selected.getLatitude();
                selectedLongitude = selected.getLongitude();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        hospitalsRegistration = db.collection("locations_hospitals").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e == null && queryDocumentSnapshots != null) {
                hospitalList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    hospitalList.add(doc.toObject(LocationModel.class));
                }
                if (hospitalAdapter != null) hospitalAdapter.notifyDataSetChanged();
            }
        });

        areasRegistration = db.collection("locations_areas").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e == null && queryDocumentSnapshots != null) {
                areaList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    areaList.add(doc.toObject(LocationModel.class));
                }
                if (areaAdapter != null) areaAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (hospitalsRegistration != null) {
            hospitalsRegistration.remove();
            hospitalsRegistration = null;
        }
        if (areasRegistration != null) {
            areasRegistration.remove();
            areasRegistration = null;
        }
    }

    private void showDateTimePicker() {
        if (getContext() == null) return;
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timeView, selectedHour, selectedMinute) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                neededByTimeInMillis = selectedCalendar.getTimeInMillis();
                String formattedDateTime = String.format(java.util.Locale.getDefault(), "%02d/%02d/%d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute);
                etNeededByTime.setText(formattedDateTime);
            }, hour, minute, false);
            timePickerDialog.show();
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void submitRequest() {
        String patientName = etPatientName.getText().toString().trim();
        String patientAge = etPatientAge.getText().toString().trim();
        String gender = spinnerPatientGender.getSelectedItem().toString();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();
        String unitsStr = etUnitsRequired.getText().toString().trim();
        String urgency = spinnerUrgencyLevel.getSelectedItem().toString();
        String reason = etReason.getText().toString().trim();
        String hospitalDetails = etHospitalDetails.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String contactNumber = etContactNumber.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (patientName.isEmpty() || patientAge.isEmpty() || unitsStr.isEmpty()  ||
            hospitalDetails.isEmpty() || area.isEmpty() || contactNumber.isEmpty() || neededByTimeInMillis == 0) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.equals("Select Gender *") || bloodGroup.equals("Select Blood Group *") || urgency.equals("Select Urgency *")) {
            Toast.makeText(getContext(), "Please select valid options from dropdowns", Toast.LENGTH_SHORT).show();
            return;
        }

        int unitsRequired = 0;
        try {
            unitsRequired = Integer.parseInt(unitsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid units required", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "You must be logged in to request blood", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check ban & premium status before submitting
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
            .addOnSuccessListener(userDoc -> {
                if (getContext() == null) return;

                // Ban check
                String userStatus = userDoc.getString("status");
                if ("BLOCKED".equalsIgnoreCase(userStatus)) {
                    new AlertDialog.Builder(getContext())
                        .setTitle("Account Suspended")
                        .setMessage("You have been banned. You cannot perform this operation.")
                        .setPositiveButton("OK", null)
                        .show();
                    return;
                }

                boolean isPremium = "PREMIUM".equalsIgnoreCase(userDoc.getString("subscriptionPlan"));
                submitRequestToFirestore(user, bloodGroup, isPremium);
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to verify account status.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void submitRequestToFirestore(FirebaseUser user, String bloodGroup, boolean isPremium) {
        String patientName = etPatientName.getText().toString().trim();
        String patientAge = etPatientAge.getText().toString().trim();
        String gender = spinnerPatientGender.getSelectedItem().toString();
        String unitsStr = etUnitsRequired.getText().toString().trim();
        String urgency = spinnerUrgencyLevel.getSelectedItem().toString();
        String reason = etReason.getText().toString().trim();
        String hospitalDetails = etHospitalDetails.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String contactNumber = etContactNumber.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        int unitsRequired = 0;
        try { unitsRequired = Integer.parseInt(unitsStr); } catch (NumberFormatException ignored) {}

        String transactionId = UUID.randomUUID().toString();
        BloodTransactionModel model = new BloodTransactionModel();
        model.setTransactionId(transactionId);
        model.setRequesterUid(user.getUid());
        model.setPatientName(patientName);
        model.setPatientAge(patientAge);
        model.setPatientGender(gender);
        model.setBloodGroup(bloodGroup);
        model.setUnitsRequired(unitsRequired);
        model.setUrgencyLevel(urgency);
        model.setReason(reason);
        model.setHospitalNameArea(hospitalDetails);
        model.setArea(area);
        model.setContactNumber(contactNumber);
        model.setNotes(notes);
        model.setNeededByTime(neededByTimeInMillis);
        model.setStatus("OPEN");
        model.setCreatedAt(System.currentTimeMillis());
        model.setCompletedAt(0);
        model.setLatitude(selectedLatitude);
        model.setLongitude(selectedLongitude);
        model.setPremiumRequest(isPremium);

        FirebaseFirestore.getInstance().collection("transactions").document(transactionId).set(model)
            .addOnSuccessListener(aVoid -> {
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update("totalRequests", FieldValue.increment(1));
                
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Blood request submitted!", Toast.LENGTH_SHORT).show();
                    if (isPremium) {
                        android.content.Context appContext = getContext().getApplicationContext();
                        broadcastPremiumNotification(appContext, transactionId, bloodGroup);
                    }
                    resetForm();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).selectTab(0);
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to submit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void broadcastPremiumNotification(android.content.Context appContext, String transactionId, String bloodGroup) {
        Log.d("PremiumBroadcast", "Starting broadcast for blood group: '" + bloodGroup + "', transactionId: " + transactionId);

        String currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
            ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("availableToDonate", true)  
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int total = queryDocumentSnapshots.size();
                Log.d("PremiumBroadcast", "Found " + total + " users with blood group: " + bloodGroup);

                int notified = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String uid = doc.getId();
                    String status = doc.getString("status");
                    String token = doc.getString("token");
                    String storedBloodGroup = doc.getString("bloodGroup");

                    Log.d("PremiumBroadcast", "User: " + uid + ", status: " + status + ", bloodGroup: " + storedBloodGroup + ", hasToken: " + (token != null && !token.isEmpty()));

                    if (uid.equals(currentUserUid)) {
                        Log.d("PremiumBroadcast", "Skipping requester themselves: " + uid);
                        continue;
                    }

                    if ("BLOCKED".equalsIgnoreCase(status)) {
                        Log.d("PremiumBroadcast", "Skipping banned user: " + uid);
                        continue;
                    }
                    if (token != null && !token.isEmpty()) {
                        Log.d("PremiumBroadcast", "Sending notification to user: " + uid);
                        NotificationSender.sendPremiumBroadcast(appContext, token, bloodGroup, transactionId);
                        notified++;
                    } else {
                        Log.w("PremiumBroadcast", "User " + uid + " has no FCM token, skipping.");
                    }
                }
                Log.d("PremiumBroadcast", "Broadcast complete. Notified " + notified + " users.");
            })
            .addOnFailureListener(e -> {
                Log.e("PremiumBroadcast", "Firestore query failed: " + e.getMessage(), e);
            });
    }

    private void resetForm() {
        etPatientName.setText("");
        etPatientAge.setText("");
        spinnerPatientGender.setSelection(0);
        spinnerBloodGroup.setSelection(0);
        etUnitsRequired.setText("");
        spinnerUrgencyLevel.setSelection(0);
        etReason.setText("");
        etHospitalDetails.setText("");
        etArea.setText("");
        etContactNumber.setText("");
        etNotes.setText("");
        etNeededByTime.setText("");
        neededByTimeInMillis = 0;
        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
    }
}
