package gr.upatras.ceid.geopin.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.fluttercode.datafactory.impl.DataFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gr.upatras.ceid.geopin.db.models.Category;
import gr.upatras.ceid.geopin.db.models.Place;

public class DBHandler extends SQLiteOpenHelper implements DBInterface {
    private static final int DATABASE_VERSION = 1;

    private static DBHandler mInstance = null;

    public Context ctx;


    /**
     * Use singleton pattern because we want only one instance of DBHandler
     * on the whole app.
     */
    public static DBHandler getInstance(Context context){
        // Use the application context, to avoid accidental memory leaks.
        if (mInstance == null) {
            mInstance = new DBHandler(context.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                TABLE_CATEGORIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " VARCHAR NOT NULL,"
                + COLUMN_COLOR + " VARCHAR);";

        String CREATE_CATEGORY_TRANSLATIONS_TABLE = "CREATE TABLE " +
                TABLE_CATEGORY_TRANSLATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " VARCHAR NOT NULL,"
                + COLUMN_LANGUAGE + " VARCHAR NOT NULL,"
                + " FOREIGN KEY ("+COLUMN_CATEGORY_ID+") REFERENCES "+TABLE_CATEGORIES+" ("+COLUMN_ID+"));";


//        String INSERT_CATEGORIES = "INSERT INTO "+
//                TABLE_CATEGORIES +
//                " SELECT 'Cafe' AS " + COLUMN_NAME+ ", '0xf44336' AS " + COLUMN_COLOR +
//                " UNION SELECT 'Food', '0x2196f3'" +
//                " UNION SELECT 'Entertainment', '0x4caf50'" +
//                " UNION SELECT 'Other', '0xff9800'";

        String INSERT_CATEGORIES = "INSERT INTO " + TABLE_CATEGORIES
                + "(" + COLUMN_NAME + ", " + COLUMN_COLOR + ") VALUES"
                + "('Cafe', '#f44336'), "
                + "('Food', '#2196f3'), "
                + "('Entertainment', '#4caf50'), "
                + "('Other', '#ff9800');";


        String CREATE_PLACES_TABLE = "CREATE TABLE " +
                TABLE_PLACES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " VARCHAR NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_CATEGORY_ID + " INTEGER,"
                + " FOREIGN KEY ("+COLUMN_CATEGORY_ID+") REFERENCES "+TABLE_CATEGORIES+" ("+COLUMN_ID+"));";


        db.beginTransaction();
        try {
            db.execSQL(CREATE_CATEGORIES_TABLE);
            db.execSQL(CREATE_CATEGORY_TRANSLATIONS_TABLE);
            db.execSQL(INSERT_CATEGORIES);
            db.execSQL(CREATE_PLACES_TABLE);
            db.setTransactionSuccessful();
            Log.d("---onCreate---", "Success");

        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);

    }

    /**************************************************************************************
     ******************************* PLACES CRUD ******************************************
     **************************************************************************************/

    /**
     * Returns a List of type Place with all places.
     * @return List of type Place with all places
     */
    public List<Place> getAllPlaces(){
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PLACES;
        return getPlacesWithRawQuery(selectQuery);
    }

    /**
     * Returns a List of type Place with places in multiple categories.
     * @param category_ids A string (Be careful!: STRING NOT INT) list with ids of selected categories.
     * @return List of type Place with places in multiple categories.
     */
    public List<Place> getPlacesByCategoryIds(List<String> category_ids){
        // Select only places specific categories.
        String whereCategories = "";
        if(category_ids!=null){
            boolean isFirst=true;
            for(String cid : category_ids){
                if(isFirst){
                    whereCategories += "( "+ COLUMN_CATEGORY_ID +" = '"+cid+"'";
                    isFirst=false;
                }
                else{
                    whereCategories += " OR "+ COLUMN_CATEGORY_ID + " = '"+cid+"'";
                }
            }
            whereCategories += " )";
        }
        String selectQuery = "SELECT  * FROM " + TABLE_PLACES +" WHERE "+ whereCategories;
        return getPlacesWithRawQuery(selectQuery);
    }

    /**
     * Returns a List of type Place with places in a specific category.
     * @param category_id The id of selected category.
     * @return List of places in a specific category.
     */
    public List<Place> getPlacesByCategoryId(int category_id){
        // Select only places in a specific category.
        String selectQuery = "SELECT  * FROM " + TABLE_PLACES +" WHERE "+ COLUMN_CATEGORY_ID +" = " + category_id;
        return getPlacesWithRawQuery(selectQuery);
    }

    /**
     * Returns a List of type Place with all places selected based on the provided raw select query.
     * This method is the method which actually performs query on database.
     * @param rawQuery The select clause. It should be complete and carefully prepared.
     * @return List of type Place with all places selected
     */
    private List<Place> getPlacesWithRawQuery(String rawQuery){
        List<Place> places = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Place place = new Place();
                place.setId(cursor.getInt(0));
                place.setTitle(cursor.getString(1));
                place.setDescription(cursor.getString(2));
                place.setLatitude(cursor.getDouble(3));
                place.setLongitude(cursor.getDouble(4));
                place.setCategory_id(Integer.parseInt(cursor.getString(5)));

                // Adding place to list
                places.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // return category list
        return places;
    }

    // Insert a new Place or update an existing one
    public long insertOrReplacePlace(Place p) {
        SQLiteDatabase db = this.getWritableDatabase();
        long record_id;

        ContentValues values = new ContentValues();

        values.put(DBInterface.COLUMN_TITLE, p.getTitle()); // Place Name
        values.put(DBInterface.COLUMN_DESCRIPTION, p.getDescription()); // Place Descr
        values.put(DBInterface.COLUMN_LATITUDE, p.getLatitude());
        values.put(DBInterface.COLUMN_LONGITUDE, p.getLongitude());
        values.put(DBInterface.COLUMN_CATEGORY_ID, p.getCategory_id());

        if(p.getId()>0){
            Log.d("DB UPDATE", "Place #"+p.getId()+" Updated!");
            record_id = db.update(DBInterface.TABLE_PLACES, values, DBInterface.COLUMN_ID + " = ?", new String[] { Integer.toString(p.getId()) });
        }
        else{
            record_id = db.insert(DBInterface.TABLE_PLACES,null, values);
            Log.d("DB UPDATE", "Place #"+p.getId()+" Inserted!");
        }
        Log.d("DB UPDATE", "Run...");
        db.close();

        return record_id;
    }

    // Delete a Place by id
    public void deletePlace(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBInterface.TABLE_PLACES, DBInterface.COLUMN_ID + " = ?", new String[] { Integer.toString(id) });
        db.close();
    }


    /**************************************************************************************
     ******************************* CATEGORIES CRUD **************************************
     **************************************************************************************/

    /**
     * Returns a List of type Category with all categories.
     * @return List of type Category with all categories
     */
    public List<Category> getAllCategories(){
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        return getCategoriesWithRawQuery(selectQuery);
    }

    /**
     * Returns a List of type Category with all categories selected based on the provided raw select query.
     * This method is the method which actually performs query on database.
     * @param rawQuery The select clause. It should be complete and carefully prepared.
     * @return List of type Category with all categories selected
     */
    private List<Category> getCategoriesWithRawQuery(String rawQuery){
        List<Category> categories = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setColor(cursor.getString(2));

                // Adding category to list
                categories.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // return category list
        return categories;
    }

    // Insert a new Category or update an existing one
    public long insertOrReplaceCategory(Category c) {
        SQLiteDatabase db = this.getWritableDatabase();
        long record_id;

        ContentValues values = new ContentValues();
        values.put(DBInterface.COLUMN_ID, c.getId());
        values.put(DBInterface.COLUMN_NAME, c.getName()); // Category Name
        values.put(DBInterface.COLUMN_COLOR, c.getColor()); // Category Color

        // updating row
        record_id = db.replace(DBInterface.TABLE_CATEGORIES,null, values);

        db.close();
        return record_id;
    }

    // Delete a Category by id
    public void deleteCategory(int category_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBInterface.TABLE_CATEGORIES, DBInterface.COLUMN_ID + " = ?", new String[] { Integer.toString(category_id) });
        db.close();
    }

    /**
     * This is a development helper function for seeding the db.
     * It generates massive amounts of places with fake data and inserts then to database.
     * The x0 and y0 are longitude and latitude respectively, the new places will be generated
     * in a circle around them.
     * @param places_num The number of placed to generate.
     * @param latitude The latitude at the center of circle
     * @param longitude The longitude at the center of circle
     * @param radius The radious of circle in meters.
     */
    public void generateAndStorePlaces(int places_num, double latitude, double longitude, double radius ){
        DataFactory df              = new DataFactory();
        Random randomGenerator      = new Random();
        List<Category> categories   = getAllCategories();
        double radiusInDegrees      = radius / 111000f; // Convert radius from meters to degrees (near the equator)

        while(--places_num >= 0) {
            Category randomCategory = categories.get(randomGenerator.nextInt(categories.size()));

            /* u and v are random number between 0 and 1 */
            double u = randomGenerator.nextDouble();
            double v = randomGenerator.nextDouble();

            /* Random point inside radious algorithm. Credits here: http://gis.stackexchange.com/questions/25877/ */
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(latitude);

            double randomLongitude = new_x + longitude;
            double randomLatitude = y + latitude;

            Place p = new Place(df.getName() + " - " + df.getBusinessName(),
                    df.getRandomWord() + " " + df.getRandomText(35, 55) + " " + df.getRandomWord(),
                    randomLatitude,
                    randomLongitude,
                    randomCategory.getId());

            insertOrReplacePlace(p);
            Log.d("Generated Place", p.toString());
            Log.d("Iteration #", Integer.toString(places_num));
        }
    }
}