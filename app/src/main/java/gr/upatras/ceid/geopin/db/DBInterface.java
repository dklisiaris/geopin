package gr.upatras.ceid.geopin.db;

public interface DBInterface {
    final String DATABASE_NAME = "geopin.db";

    // ID column is the same in all tables.
    static final String COLUMN_ID = "_id";

    static final String TABLE_PLACES = "places";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_LATITUDE = "latitude";
    static final String COLUMN_LONGITUDE = "longitude";
    static final String COLUMN_CATEGORY_ID = "category_id";

    static final String TABLE_CATEGORIES = "categories";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_COLOR = "color";

}