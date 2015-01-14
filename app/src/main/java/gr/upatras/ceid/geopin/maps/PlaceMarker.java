package gr.upatras.ceid.geopin.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PlaceMarker implements ClusterItem {
    private int id;
    private String title, description;
    private final LatLng mPosition;

    public PlaceMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public PlaceMarker(int id, String title, String description, LatLng mPosition) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.mPosition = mPosition;
    }

    public PlaceMarker(int id, String title, String description, double lat, double lng) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return description;
    }

    public void setSnippet(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
