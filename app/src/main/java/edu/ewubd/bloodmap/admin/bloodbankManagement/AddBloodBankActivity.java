package edu.ewubd.bloodmap.admin.bloodbankManagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.R;

public class AddBloodBankActivity extends AppCompatActivity {

    private EditText etBankName, etBankPhone, etBankAddress, etLat, etLon;
    private EditText etAPos, etANeg, etBPos, etBNeg, etABPos, etABNeg, etOPos, etONeg;
    private SwitchMaterial sw24Hours;
    private Button btnSave;
    private TextView tvFormTitle;
    private String bloodBankId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blood_bank);

        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        etBankName = findViewById(R.id.etBankName);
        etBankPhone = findViewById(R.id.etBankPhone);
        etBankAddress = findViewById(R.id.etBankAddress);
        etLat = findViewById(R.id.etLat);
        etLon = findViewById(R.id.etLon);
        
        etAPos = findViewById(R.id.etAPos);
        etANeg = findViewById(R.id.etANeg);
        etBPos = findViewById(R.id.etBPos);
        etBNeg = findViewById(R.id.etBNeg);
        etABPos = findViewById(R.id.etABPos);
        etABNeg = findViewById(R.id.etABNeg);
        etOPos = findViewById(R.id.etOPos);
        etONeg = findViewById(R.id.etONeg);
        
        sw24Hours = findViewById(R.id.sw24Hours);
        btnSave = findViewById(R.id.btnSaveBank);

        if (getIntent() != null && getIntent().hasExtra("ID")) {
            bloodBankId = getIntent().getStringExtra("ID");
            etBankName.setText(getIntent().getStringExtra("NAME"));
            etBankPhone.setText(getIntent().getStringExtra("PHONE"));
            etBankAddress.setText(getIntent().getStringExtra("ADDRESS"));
            
            if (getIntent().hasExtra("LATITUDE")) {
                etLat.setText(String.valueOf(getIntent().getDoubleExtra("LATITUDE", 0)));
                etLon.setText(String.valueOf(getIntent().getDoubleExtra("LONGITUDE", 0)));
            }
            
            if (getIntent().hasExtra("STOCK")) {
                Map<String, Integer> stock = (Map<String, Integer>) getIntent().getSerializableExtra("STOCK");
                if (stock != null) {
                    etAPos.setText(String.valueOf(stock.getOrDefault("A+", 0)));
                    etANeg.setText(String.valueOf(stock.getOrDefault("A-", 0)));
                    etBPos.setText(String.valueOf(stock.getOrDefault("B+", 0)));
                    etBNeg.setText(String.valueOf(stock.getOrDefault("B-", 0)));
                    etABPos.setText(String.valueOf(stock.getOrDefault("AB+", 0)));
                    etABNeg.setText(String.valueOf(stock.getOrDefault("AB-", 0)));
                    etOPos.setText(String.valueOf(stock.getOrDefault("O+", 0)));
                    etONeg.setText(String.valueOf(stock.getOrDefault("O-", 0)));
                }
            }
            
            sw24Hours.setChecked(getIntent().getBooleanExtra("IS_OPEN_24H", false));
            
            btnSave.setText("Update Blood Bank Info");
            if (tvFormTitle != null) tvFormTitle.setText("Edit Blood Bank");
        }

        btnSave.setOnClickListener(v -> saveBloodBank());
    }

    private void saveBloodBank() {
        String name = etBankName.getText().toString().trim();
        String phone = etBankPhone.getText().toString().trim();
        String address = etBankAddress.getText().toString().trim();
        String latStr = etLat.getText().toString().trim();
        String lonStr = etLon.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lonStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> stock = new HashMap<>();
        stock.put("A+", getIntFromEt(etAPos));
        stock.put("A-", getIntFromEt(etANeg));
        stock.put("B+", getIntFromEt(etBPos));
        stock.put("B-", getIntFromEt(etBNeg));
        stock.put("AB+", getIntFromEt(etABPos));
        stock.put("AB-", getIntFromEt(etABNeg));
        stock.put("O+", getIntFromEt(etOPos));
        stock.put("O-", getIntFromEt(etONeg));

        final String currentId = (bloodBankId != null) ? bloodBankId : UUID.randomUUID().toString();
        
        BloodBankModel model = new BloodBankModel(
                currentId, name, phone, address, latitude, longitude, stock, sw24Hours.isChecked()
        );

        FirebaseFirestore.getInstance().collection("blood_banks").document(currentId)
            .set(model)
            .addOnSuccessListener(aVoid -> {
                String msg = (bloodBankId != null) ? "Blood Bank Updated!" : "Blood Bank Added!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int getIntFromEt(EditText et) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}
