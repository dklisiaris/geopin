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

    public Place(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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

    /**
     * Checks if object has all required fields completed.
     * This should be used before insert object in database.
     * Object is valid if title, position(latlong) and category are present.
     * @return true if valid
     */
    public boolean isValid(){
        if(title!=null && !title.isEmpty()
                && latitude > 0.0 && longitude > 0.0
                && category_id > 0){
            return true;
        }
        else return false;
    }

    /**
     * Checks if object has been saved in database, based on the presence of id.
     * @return true if object has not been saved in database yet.
     */
    public boolean isNew(){
        if (id == 0) return true;
        else return false;
    }
}
