package edu.ewubd.bloodmap.HomePages;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import edu.ewubd.bloodmap.DrawerPages.nearBloodBank.BloodBanksActivity;
import edu.ewubd.bloodmap.DrawerPages.nearHospital.HospitalContactsActivity;
import edu.ewubd.bloodmap.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import android.widget.RadioGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import edu.ewubd.bloodmap.ClassModels.LocationModel;
import edu.ewubd.bloodmap.ClassModels.UserModel;
import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.ClassModels.BloodBankModel;

public class HeatmapFragment extends Fragment {

    private MapView map = null;
    private RadioGroup rgMapMode;
    private FirebaseFirestore db;
    
    private List<LocationModel> cachedAreas = new ArrayList<>();
    private List<HospitalContactModel> cachedHospitals = new ArrayList<>();
    private List<BloodBankModel> cachedBloodBanks = new ArrayList<>();
    private final Map<String, Integer> donorCounts = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_heatmap, container, false);
        map = view.findViewById(R.id.map);
        rgMapMode = view.findViewById(R.id.rgMapMode);
        db = FirebaseFirestore.getInstance();

        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            IMapController mapController = map.getController();
            mapController.setZoom(11.5);
            GeoPoint startPoint = new GeoPoint(23.7808, 90.3932); // Dhaka
            mapController.setCenter(startPoint);
        }

        rgMapMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDonors) {
                loadDonors();
            } else if (checkedId == R.id.rbHospitals) {
                loadHospitals();
            } else if (checkedId == R.id.rbBloodBanks) {
                loadBloodBanks();
            }
        });

        // Initialize mapping caches aggressively
        preloadLocations();

        return view;
    }

    private void preloadLocations() {
        // Cache Areas
        db.collection("locations_areas").get().addOnSuccessListener(querySnapshots -> {
            cachedAreas.clear();
            for (QueryDocumentSnapshot doc : querySnapshots) {
                cachedAreas.add(doc.toObject(LocationModel.class));
            }
            // By default, trigger donors logic first natively since it was selected
            if (rgMapMode.getCheckedRadioButtonId() == R.id.rbDonors) loadDonors();
        });

        // Cache Hospitals quietly in the background
        db.collection("hospitals").get().addOnSuccessListener(querySnapshots -> {
            cachedHospitals.clear();
            for (QueryDocumentSnapshot doc : querySnapshots) {
                cachedHospitals.add(doc.toObject(HospitalContactModel.class));
            }
        });
        
        // Cache Blood Banks actively
        db.collection("blood_banks").get().addOnSuccessListener(querySnapshots -> {
            cachedBloodBanks.clear();
            for (QueryDocumentSnapshot doc : querySnapshots) {
                cachedBloodBanks.add(doc.toObject(BloodBankModel.class));
            }
        });
    }

    private void loadDonors() {
        if (map == null) return;
        map.getOverlays().clear();

        db.collection("users").whereEqualTo("availableToDonate", true).get()
            .addOnSuccessListener(querySnapshots -> {
                donorCounts.clear();
                for (QueryDocumentSnapshot doc : querySnapshots) {
                    UserModel user = doc.toObject(UserModel.class);
                    String area = user.getLocationArea();
                    if (area != null && !area.isEmpty()) {
                        donorCounts.put(area, donorCounts.getOrDefault(area, 0) + 1);
                    }
                }

                // Match counts against coordinates precisely
                for (LocationModel area : cachedAreas) {
                    int count = donorCounts.getOrDefault(area.getName(), 0);
                    if (count > 0) {
                        spawnMarker(area.getLatitude(), area.getLongitude(), area.getName(), count + " Active Donors Available");
                    }
                }
                map.invalidate();
            });
    }

    private void loadHospitals() {
        if (map == null) return;
        map.getOverlays().clear();

        for (HospitalContactModel hosp : cachedHospitals) {
            spawnMarker(hosp.getLatitude(), hosp.getLongitude(), hosp.getHospitalName() != null ? hosp.getHospitalName() : "Unknown Hospital", "Healthcare Facility");
        }
        
        map.invalidate();
    }

    private void loadBloodBanks() {
        if (map == null) return;
        map.getOverlays().clear();

        for (BloodBankModel bank : cachedBloodBanks) {
            spawnMarker(bank.getLatitude(), bank.getLongitude(), bank.getBankName() != null ? bank.getBankName() : "Unknown Bank", "Blood Bank Facility");
        }
        
        map.invalidate();
    }

    private void spawnMarker(double lat, double lon, String title, String snippet) {
        if (lat == 0.0 && lon == 0.0) return; // Avoid dropping pins into the Gulf of Guinea organically
        GeoPoint point = new GeoPoint(lat, lon);
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSnippet(snippet);
        
        marker.setOnMarkerClickListener((m, mapView) -> {
            m.showInfoWindow();
            
            if (m.getInfoWindow() != null && m.getInfoWindow().getView() != null) {
                android.view.View infoView = m.getInfoWindow().getView();
                
                // Osmdroid default layouts often trap clicks in nested TextViews. 
                // We use OnTouchListener on the parent wrapper to aggressively intercept the physical tap.
                infoView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        m.closeInfoWindow(); // Close cleanly first
                        if (rgMapMode.getCheckedRadioButtonId() == R.id.rbDonors) {
                            edu.ewubd.bloodmap.HomePages.availableFrag.AvailableFragment.pendingSearchQuery = title;
                            if (requireActivity() instanceof edu.ewubd.bloodmap.MainActivity) {
                                ((edu.ewubd.bloodmap.MainActivity) requireActivity()).selectTab(2);
                            }
                        } else if (rgMapMode.getCheckedRadioButtonId() == R.id.rbHospitals) {
                            android.content.Intent intent = new android.content.Intent(requireContext(), HospitalContactsActivity.class);
                            startActivity(intent);
                        } else {
                            android.content.Intent intent = new android.content.Intent(requireContext(), BloodBanksActivity.class);
                            startActivity(intent);
                        }
                    }
                    return true;
                });
            }
            return true;
        });
        
        map.getOverlays().add(marker);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
