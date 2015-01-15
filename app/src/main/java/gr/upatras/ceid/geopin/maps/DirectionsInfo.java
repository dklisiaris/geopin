package gr.upatras.ceid.geopin.maps;

import com.google.android.gms.maps.model.PolylineOptions;

public class DirectionsInfo {
    String durationText, distanceText;
    int durationValue, distanceValue;
    PolylineOptions polylineOptions;

    public DirectionsInfo() {
        super();
    }

    public DirectionsInfo(String durationText, int durationValue,
                          String distanceText, int distanceValue,
                          PolylineOptions polylineOptions) {
        super();
        this.durationText = durationText;
        this.distanceText = distanceText;
        this.durationValue = durationValue;
        this.distanceValue = distanceValue;
        this.polylineOptions = polylineOptions;
    }
    public String getDurationText() {
        return durationText;
    }
    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }
    public String getDistanceText() {
        return distanceText;
    }
    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }
    public int getDurationValue() {
        return durationValue;
    }
    public void setDurationValue(int durationValue) {
        this.durationValue = durationValue;
    }
    public int getDistanceValue() {
        return distanceValue;
    }
    public void setDistanceValue(int distanceValue) {
        this.distanceValue = distanceValue;
    }
    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }
    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }


}
