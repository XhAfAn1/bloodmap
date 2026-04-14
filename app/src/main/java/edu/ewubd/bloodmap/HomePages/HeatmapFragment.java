package edu.ewubd.bloodmap.HomePages;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.ewubd.bloodmap.BloodDonorData;
import edu.ewubd.bloodmap.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

public class HeatmapFragment extends Fragment {

    private MapView map = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_heatmap, container, false);
        map = view.findViewById(R.id.map);

        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            IMapController mapController = map.getController();
            mapController.setZoom(11.5);
            GeoPoint startPoint = new GeoPoint(23.7808, 90.3932);
            mapController.setCenter(startPoint);

            List<BloodDonorData.DonorArea> donorAreas = BloodDonorData.getDonorAreas();
            
            if (donorAreas != null) {
                for (final BloodDonorData.DonorArea area : donorAreas) {
                    Polygon polygon = new Polygon();
                    polygon.setPoints(area.border);
                    
                    int fillColor = getAreaColor(area.donorCount);
                    polygon.getFillPaint().setColor(fillColor);
                    polygon.getFillPaint().setAlpha(150);
                    
                    polygon.getOutlinePaint().setColor(Color.WHITE);
                    polygon.getOutlinePaint().setStrokeWidth(3);

                    polygon.setOnClickListener((poly, mapView, eventPos) -> {
                        Toast.makeText(requireContext(), area.name + ": " + area.donorCount + " Donors", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                    polygon.setTitle(area.name + "\nDonors: " + area.donorCount);
                    map.getOverlays().add(polygon);
                }
            }
            map.invalidate();
        }

        return view;
    }

    private int getAreaColor(int count) {
        float ratio = (float) Math.min(Math.max((count - 300) / 700.0, 0.0), 1.0);
        
        int r, g, b = 0;
        if (ratio < 0.5) {
            r = 255;
            g = (int) (ratio * 2 * 255);
        } else {
            r = (int) ((1.0 - ratio) * 2 * 255);
            g = 255;
        }
        return Color.rgb(r, g, b);
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
