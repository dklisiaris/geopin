package gr.upatras.ceid.geopin.db.models;

public class Place {
    private int id;
    private String title, description;
    double latitude ,longitude;
    private int category_id;

    public Place() {
    }

    public Place(String title, double latitude, double longitude, int category_id) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public Place(String title, String description, double latitude, double longitude, int category_id) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public Place(int id, String title, double latitude, double longitude, int category_id) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public Place(int id, String title, String description, double latitude, double longitude, int category_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", category_id=" + category_id +
                '}';
    }
}
