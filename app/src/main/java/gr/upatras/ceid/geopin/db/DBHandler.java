package gr.upatras.ceid.geopin.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gr.upatras.ceid.geopin.db.models.Category;

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
     ******************************* CATEGORIES CRUD **************************************
     **************************************************************************************/

    /**
     * Returns an List of type Category with all categories.
     * @return List of type Category with all categories
     */
    public List<Category> getAllCategories(){
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        return getCategoriesWithRawQuery(selectQuery);
    }

    /**
     * Returns an List of type Category with all categories selected based on the provided raw select query.
     * This method is the method which actually performs query on database.
     * @param rawQuery The select clause. It should be complete and carefully prepared.
     * @return List of type Category with all categories selected
     */
    private List<Category> getCategoriesWithRawQuery(String rawQuery){
        List<Category> categories = new ArrayList<Category>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(rawQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(Integer.parseInt(cursor.getString(0)));
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
    public void insertOrReplaceCategory(Category c) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBInterface.COLUMN_ID, c.getId());
        values.put(DBInterface.COLUMN_NAME, c.getName()); // Category Name
        values.put(DBInterface.COLUMN_COLOR, c.getColor()); // Category Phone

        // updating row
        db.replace(DBInterface.TABLE_CATEGORIES,null, values);

        db.close();
    }

    // Delete a Category by id
    public void deleteCategory(int category_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DBInterface.TABLE_CATEGORIES, DBInterface.COLUMN_ID + " = ?", new String[] { Integer.toString(category_id) });
        db.close();
    }
}