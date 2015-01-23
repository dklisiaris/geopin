package gr.upatras.ceid.geopin.maps;

import com.google.android.gms.maps.model.LatLng;

public class Step {

    private LatLng startLocation, endLocation;
    private int durationValue, distanceValue;
    private String durationText, distanceText;
    private String htmlInstructions;

    public Step() {
    }

    public Step(LatLng startLocation, LatLng endLocation, int durationValue, int distanceValue, String durationText, String distanceText, String htmlInstructions) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.durationValue = durationValue;
        this.distanceValue = distanceValue;
        this.durationText = durationText;
        this.distanceText = distanceText;
        this.htmlInstructions = htmlInstructions;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
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

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public void setHtmlInstructions(String htmlInstructions) {
        this.htmlInstructions = htmlInstructions;
    }
}
