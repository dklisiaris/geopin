package gr.upatras.ceid.geopin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import gr.upatras.ceid.geopin.db.DBHandler;
import gr.upatras.ceid.geopin.helpers.LanguageHelper;

public class SettingsActivity extends ActionBarActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        applyLanguage();

        // Call settings fragment in layout.
        setContentView(R.layout.activity_settings);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_generate_pins:
                generatePins(100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    void applyLanguage(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString("language", "en");
        new LanguageHelper(this, language);
    }

    void generatePins(int num_of_pins){
        Bundle bundle = getIntent().getExtras();
        double latitude = bundle.getDouble("latitude");
        double longitude = bundle.getDouble("longitude");
        if(latitude > 0.0 && longitude > 0.0){
            new GeneratePinsTask(this).execute((double)num_of_pins, latitude, longitude);
        }
        else
            Toast.makeText(this, getString(R.string.arrival_time), Toast.LENGTH_LONG).show();
    }

    private class GeneratePinsTask extends AsyncTask<Double, Void, Void> {
        Context mContext;
        double latitude, longitude;
        int num_of_pins;

        public GeneratePinsTask(Context context){
            super();
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(mContext, getString(R.string.wait), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Double... params) {
            num_of_pins = (int)params[0].doubleValue();

            latitude = params[1].doubleValue();
            longitude = params[2].doubleValue();

            DBHandler db = DBHandler.getInstance(mContext);
            db.generateAndStorePlaces(num_of_pins, latitude, longitude, 10000);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(mContext, getString(R.string.pins_generated), Toast.LENGTH_LONG).show();
        }
    }

}
