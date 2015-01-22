package gr.upatras.ceid.geopin.maps;

import com.google.android.gms.maps.model.PolylineOptions;

public class RouteInfo {
    private String durationText, distanceText;
    private int durationValue, distanceValue;
    private String startAddress, endAddress;
    private PolylineOptions polylineOptions;

    public RouteInfo() {}

    public RouteInfo(String start_address, String end_address,
                    String durationText, int durationValue,
                    String distanceText, int distanceValue,
                    PolylineOptions polylineOptions) {

        this.startAddress      = startAddress;
        this.endAddress        = endAddress;
        this.durationText       = durationText;
        this.distanceText       = distanceText;
        this.durationValue      = durationValue;
        this.distanceValue      = distanceValue;
        this.polylineOptions    = polylineOptions;
    }

    public RouteInfo(String durationText, int durationValue,
                     String distanceText, int distanceValue,
                     PolylineOptions polylineOptions) {

        this.durationText       = durationText;
        this.distanceText       = distanceText;
        this.durationValue      = durationValue;
        this.distanceValue      = distanceValue;
        this.polylineOptions    = polylineOptions;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
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
