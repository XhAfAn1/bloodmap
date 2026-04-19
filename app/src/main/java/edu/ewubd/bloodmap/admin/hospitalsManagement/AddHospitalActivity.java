package edu.ewubd.bloodmap.admin.hospitalsManagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.R;

public class AddHospitalActivity extends AppCompatActivity {

    private EditText etHospitalName, etHospitalPhone, etHospitalAddress, etLatitude, etLongitude, etFacilities;
    private SwitchMaterial switchBloodBank;
    private Button btnSave;
    private String hospitalId = null;
    private TextView tvFormTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hospital);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        etHospitalName = findViewById(R.id.etHospitalName);
        etHospitalPhone = findViewById(R.id.etHospitalPhone);
        etHospitalAddress = findViewById(R.id.etHospitalAddress);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etFacilities = findViewById(R.id.etFacilities);
        switchBloodBank = findViewById(R.id.switchBloodBank);
        btnSave = findViewById(R.id.btnSaveHospital);
        tvFormTitle = findViewById(R.id.tvHospitalFormTitle);

        if (getIntent() != null && getIntent().hasExtra("ID")) {
            hospitalId = getIntent().getStringExtra("ID");
            etHospitalName.setText(getIntent().getStringExtra("NAME"));
            etHospitalPhone.setText(getIntent().getStringExtra("PHONE"));
            etHospitalAddress.setText(getIntent().getStringExtra("ADDRESS"));
            etLatitude.setText(String.valueOf(getIntent().getDoubleExtra("LATITUDE", 0)));
            etLongitude.setText(String.valueOf(getIntent().getDoubleExtra("LONGITUDE", 0)));
            etFacilities.setText(getIntent().getStringExtra("FACILITIES"));
            switchBloodBank.setChecked(getIntent().getBooleanExtra("HAS_BLOOD_BANK", false));
            
            btnSave.setText("Update Hospital Info");
            if (tvFormTitle != null) tvFormTitle.setText("Edit Hospital");
        }

        btnSave.setOnClickListener(v -> saveHospital());
    }

    private void saveHospital() {
        String name = etHospitalName.getText().toString().trim();
        String phone = etHospitalPhone.getText().toString().trim();
        String address = etHospitalAddress.getText().toString().trim();
        String latStr = etLatitude.getText().toString().trim();
        String lonStr = etLongitude.getText().toString().trim();
        String facilitiesStr = etFacilities.getText().toString().trim();
        boolean hasBloodBank = switchBloodBank.isChecked();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> facilities = new ArrayList<>();
        if (!facilitiesStr.isEmpty()) {
            String[] parts = facilitiesStr.split(",");
            for (String p : parts) {
                facilities.add(p.trim());
            }
        }

        final String currentId = (hospitalId != null) ? hospitalId : UUID.randomUUID().toString();
        
        HospitalContactModel model = new HospitalContactModel(
                currentId, name, phone, address, latitude, longitude, facilities, hasBloodBank
        );

        FirebaseFirestore.getInstance().collection("hospitals").document(currentId)
            .set(model)
            .addOnSuccessListener(aVoid -> {
                String msg = (hospitalId != null) ? "Hospital Info Updated!" : "Hospital Added!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
