package gr.upatras.ceid.geopin.db.models;

public class Place {
    private int _id;
    private String title, description;
    double latitude ,longitude;
    private int category_id;

    public Place() {
    }

    public Place(String title, int category_id, double latitude, double longitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public Place(int _id, String title, int category_id, double latitude, double longitude) {
        this._id = _id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public Place(int _id, String title, String description, int category_id, double latitude, double longitude) {
        this._id = _id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }
}
