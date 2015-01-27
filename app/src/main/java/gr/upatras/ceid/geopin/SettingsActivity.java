package gr.upatras.ceid.geopin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;

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
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    void applyLanguage(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString("language", "en");
        new LanguageHelper(this, language);
    }

}
