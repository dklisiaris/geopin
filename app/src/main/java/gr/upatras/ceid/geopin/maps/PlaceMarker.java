package gr.upatras.ceid.geopin.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import gr.upatras.ceid.geopin.db.models.Place;

public class PlaceMarker implements ClusterItem {
    private int id;
    private String title, description;
    private final LatLng mPosition;
    private int category_id;

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

    public PlaceMarker(int id, String title, String description, double lat, double lng, int category_id) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.mPosition = new LatLng(lat, lng);
        this.category_id = category_id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public static PlaceMarker fromPlace(Place place){
        return new PlaceMarker(place.getId(), place.getTitle(), place.getDescription(), place.getLatitude(), place.getLongitude(), place.getCategory_id());
    }
}
