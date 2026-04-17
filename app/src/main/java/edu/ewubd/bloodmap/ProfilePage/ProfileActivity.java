package edu.ewubd.bloodmap.ProfilePage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import edu.ewubd.bloodmap.ClassModels.LocationModel;
import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.R;

public class ProfileActivity extends AppCompatActivity {
    
    // View Mode UI
    private LinearLayout llViewMode;
    private TextView tvViewName, tvViewEmail, tvViewBloodGroup, tvViewStatus;
    private TextView tvViewPhone, tvViewLocation, tvViewAddress;
    private TextView tvViewGender, tvViewDob;
    private TextView tvViewDonations, tvViewRequests, tvViewAvailability;

    // Edit Mode UI
    private LinearLayout llEditMode;
    private EditText etFullName, etPhone, etAddress, etDob;
    private AutoCompleteTextView etLocation, etBloodGroup, etGender;
    private SwitchMaterial swAvailableToDonate;
    private Button btnSaveProfile, btnCancelEdit;
    
    // Toolbar
    private ImageView btnToggleEdit;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    private UserModel currentModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private boolean isEditing = false;
    
    private final List<LocationModel> locationAreaList = new ArrayList<>();
    private ArrayAdapter<LocationModel> areaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        btnToggleEdit = findViewById(R.id.btnToggleEdit);

        // Init Containers
        llViewMode = findViewById(R.id.llViewMode);
        llEditMode = findViewById(R.id.llEditMode);

        // Init View Mode Textboxs
        tvViewName = findViewById(R.id.tvViewName);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        tvViewBloodGroup = findViewById(R.id.tvViewBloodGroup);
        tvViewStatus = findViewById(R.id.tvViewStatus);
        tvViewPhone = findViewById(R.id.tvViewPhone);
        tvViewLocation = findViewById(R.id.tvViewLocation);
        tvViewAddress = findViewById(R.id.tvViewAddress);
        tvViewGender = findViewById(R.id.tvViewGender);
        tvViewDob = findViewById(R.id.tvViewDob);
        tvViewDonations = findViewById(R.id.tvViewDonations);
        tvViewRequests = findViewById(R.id.tvViewRequests);
        tvViewAvailability = findViewById(R.id.tvViewAvailability);

        // Init Edit Mode Inputs
        etFullName = findViewById(R.id.etFullName);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etLocation = findViewById(R.id.etLocation);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etGender = findViewById(R.id.etGender);
        etDob = findViewById(R.id.etDob);
        swAvailableToDonate = findViewById(R.id.swAvailableToDonate);
        
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        setupPredefinedAdapters();
        fetchFirebaseAreas();
        
        etDob.setFocusable(false);
        etDob.setClickable(true);
        etDob.setOnClickListener(v -> showDatePicker());

        btnToggleEdit.setOnClickListener(v -> toggleEditMode(true));
        btnCancelEdit.setOnClickListener(v -> toggleEditMode(false));
        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private ListenerRegistration profileRegistration;

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            loadUserProfile();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileRegistration != null) {
            profileRegistration.remove();
            profileRegistration = null;
        }
    }

    private void setupPredefinedAdapters() {
        // Blood Groups
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        etBloodGroup.setAdapter(bloodAdapter);
        etBloodGroup.setOnClickListener(v -> etBloodGroup.showDropDown());

        // Gender
        String[] genders = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        etGender.setAdapter(genderAdapter);
        etGender.setOnClickListener(v -> etGender.showDropDown());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        if (currentModel != null && currentModel.getDateOfBirth() != null) {
            c.setTime(currentModel.getDateOfBirth());
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            c.set(Calendar.YEAR, year1);
            c.set(Calendar.MONTH, month1);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etDob.setText(dateFormat.format(c.getTime()));
        }, year, month, day);
        picker.show();
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;
        if (enable) {
            llViewMode.setVisibility(View.GONE);
            llEditMode.setVisibility(View.VISIBLE);
            btnToggleEdit.setVisibility(View.GONE);
        } else {
            llViewMode.setVisibility(View.VISIBLE);
            llEditMode.setVisibility(View.GONE);
            btnToggleEdit.setVisibility(View.VISIBLE);
        }
    }
    
    private void fetchFirebaseAreas() {
        areaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationAreaList);
        etLocation.setAdapter(areaAdapter);

        db.collection("locations_areas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            locationAreaList.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                try {
                    LocationModel location = doc.toObject(LocationModel.class);
                    locationAreaList.add(location);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            areaAdapter.notifyDataSetChanged();
        });
    }

    private void loadUserProfile() {
        profileRegistration = db.collection("users").document(currentUser.getUid())
            .addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    currentModel = documentSnapshot.toObject(UserModel.class);
                    if (currentModel != null) {
                        populateViewMode();
                        // Only populate edit mode if not currently editing to avoid disrupting user input
                        if (!isEditing) {
                            populateEditMode();
                        }
                        checkProfileCompleteness();
                    }
                }
            });
    }

    private void checkProfileCompleteness() {
        if (currentModel == null) return;
        boolean bloodGroupMissing = isNullOrEmpty(currentModel.getBloodGroup());
        boolean contactMissing = isNullOrEmpty(currentModel.getContactNumber());
        if (bloodGroupMissing || contactMissing) {
            Toast.makeText(this, "Please complete your profile to start responding to requests.", Toast.LENGTH_LONG).show();
            toggleEditMode(true);
        }
    }

    private void populateViewMode() {
        tvViewName.setText(currentModel.getName() != null && !currentModel.getName().isEmpty() ? currentModel.getName() : "Anonymous");
        tvViewEmail.setText(currentModel.getEmail() != null ? currentModel.getEmail() : "No email");
        
        String bg = currentModel.getBloodGroup();
        if (bg == null || bg.isEmpty()) {
            tvViewBloodGroup.setText("N/A");
            tvViewBloodGroup.setBackgroundColor(0xFF888888);
        } else {
            tvViewBloodGroup.setText(bg);
            tvViewBloodGroup.setBackgroundColor(0xFFD11A2A);
        }

        String status = currentModel.getStatus();
        if (status == null || status.isEmpty()) status = "ACTIVE";
        tvViewStatus.setText(status);
        if (status.equalsIgnoreCase("BLOCKED")) {
            tvViewStatus.setBackgroundColor(0xFFFFCDD2);
            tvViewStatus.setTextColor(0xFFD32F2F);
        } else {
            tvViewStatus.setBackgroundColor(0xFFE8F5E9);
            tvViewStatus.setTextColor(0xFF2E7D32);
        }

        tvViewPhone.setText("Phone: " + (isNullOrEmpty(currentModel.getContactNumber()) ? "Not set" : currentModel.getContactNumber()));
        tvViewLocation.setText("Area: " + (isNullOrEmpty(currentModel.getLocationArea()) ? "Not set" : currentModel.getLocationArea()));
        tvViewAddress.setText("Address: " + (isNullOrEmpty(currentModel.getAddress()) ? "Not set" : currentModel.getAddress()));
        
        tvViewGender.setText("Gender: " + (isNullOrEmpty(currentModel.getGender()) ? "Not set" : currentModel.getGender()));
        tvViewDob.setText("DOB: " + (currentModel.getDateOfBirth() != null ? dateFormat.format(currentModel.getDateOfBirth()) : "Not set"));
        
        tvViewDonations.setText("Total Donations: " + currentModel.getTotalDonations());
        tvViewRequests.setText("Total Requests: " + currentModel.getTotalRequests());
        tvViewAvailability.setText("Available to Donate: " + (currentModel.isAvailableToDonate() ? "YES" : "NO"));
    }

    private void populateEditMode() {
        etFullName.setText(currentModel.getName());
        etBloodGroup.setText(currentModel.getBloodGroup());
        etLocation.setText(currentModel.getLocationArea());
        etAddress.setText(currentModel.getAddress());
        etPhone.setText(currentModel.getContactNumber());
        etGender.setText(currentModel.getGender());
        if (currentModel.getDateOfBirth() != null) {
            etDob.setText(dateFormat.format(currentModel.getDateOfBirth()));
        }
        swAvailableToDonate.setChecked(currentModel.isAvailableToDonate());
    }

    private void saveUserProfile() {
        if (currentModel == null) return;
        
        currentModel.setName(etFullName.getText().toString().trim());
        currentModel.setBloodGroup(etBloodGroup.getText().toString().trim());
        currentModel.setLocationArea(etLocation.getText().toString().trim());
        currentModel.setAddress(etAddress.getText().toString().trim());
        currentModel.setContactNumber(etPhone.getText().toString().trim());
        currentModel.setGender(etGender.getText().toString().trim());
        boolean isNowAvailable = swAvailableToDonate.isChecked();
        currentModel.setAvailableToDonate(isNowAvailable);

        // If turning availability ON manually, clear the nextEligibleDate so they are immediately eligible
        if (isNowAvailable) {
            currentModel.setNextEligibleDate(null);
        }

        String dobStr = etDob.getText().toString().trim();
        if (!dobStr.isEmpty()) {
            try {
                Date dob = dateFormat.parse(dobStr);
                currentModel.setDateOfBirth(dob);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid Date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentModel.setUpdatedAt(new Date());

        db.collection("users").document(currentUser.getUid()).set(currentModel)
            .addOnSuccessListener(aVoid -> {
                // If they turned availability ON, also explicitly delete nextEligibleDate from Firestore
                if (isNowAvailable) {
                    db.collection("users").document(currentUser.getUid())
                        .update("nextEligibleDate", FieldValue.delete());
                }
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                populateViewMode();
                toggleEditMode(false);
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
