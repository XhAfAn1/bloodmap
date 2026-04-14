package edu.ewubd.bloodmap;

import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class BloodDonorData {

    public static class DonorArea {
        public String name;
        public int donorCount;
        public List<GeoPoint> border;

        public DonorArea(String name, int donorCount, List<GeoPoint> border) {
            this.name = name;
            this.donorCount = donorCount;
            this.border = border;
        }
    }

    public static List<DonorArea> getDonorAreas() {
        List<DonorArea> areas = new ArrayList<>();

        // Temporary Mock Data for Heatmap
        List<GeoPoint> zone1 = new ArrayList<>();
        zone1.add(new GeoPoint(23.750, 90.370));
        zone1.add(new GeoPoint(23.750, 90.390));
        zone1.add(new GeoPoint(23.730, 90.390));
        zone1.add(new GeoPoint(23.730, 90.370));
        areas.add(new DonorArea("Zone A", 450, zone1));

        List<GeoPoint> zone2 = new ArrayList<>();
        zone2.add(new GeoPoint(23.820, 90.360));
        zone2.add(new GeoPoint(23.820, 90.380));
        zone2.add(new GeoPoint(23.800, 90.380));
        zone2.add(new GeoPoint(23.800, 90.360));
        areas.add(new DonorArea("Zone B", 850, zone2));

        return areas;
    }
}
