package gr.upatras.ceid.geopin.helpers;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

import gr.upatras.ceid.geopin.SettingsActivity;

public class LanguageHelper {
    Context context;
    Locale locale;

    public LanguageHelper(Context context, String language) {
        this.context = context;
        this.locale = new Locale(language);
        changeLanguage();
    }

    public void changeLanguage(){
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

}
