package gr.upatras.ceid.geopin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Configuration;

import gr.upatras.ceid.geopin.adapters.InstructionsAdapter;
import gr.upatras.ceid.geopin.db.DBHandler;
import gr.upatras.ceid.geopin.db.models.Category;
import gr.upatras.ceid.geopin.db.models.Place;
import gr.upatras.ceid.geopin.dialogs.EditPinDialog;
import gr.upatras.ceid.geopin.dialogs.EditPinDialog.SubmitListener;
import gr.upatras.ceid.geopin.helpers.LanguageHelper;
import gr.upatras.ceid.geopin.maps.AbstractMapActivity;
import gr.upatras.ceid.geopin.maps.DirectionsJSONClient;
import gr.upatras.ceid.geopin.maps.DirectionsXMLClient;
import gr.upatras.ceid.geopin.maps.RouteInfo;
import gr.upatras.ceid.geopin.maps.PlaceMarker;
import gr.upatras.ceid.geopin.maps.Step;
import gr.upatras.ceid.geopin.widgets.MultiSpinner;
import gr.upatras.ceid.geopin.widgets.MultiSpinner.MultiSpinnerListener;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;


public class MainMapActivity extends AbstractMapActivity implements
        OnNavigationListener, OnInfoWindowClickListener, LocationSource, LocationListener,
        ClusterManager.OnClusterClickListener<PlaceMarker>,
        ClusterManager.OnClusterInfoWindowClickListener<PlaceMarker>,
        ClusterManager.OnClusterItemClickListener<PlaceMarker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PlaceMarker>,
        MultiSpinnerListener, SubmitListener, OnMapClickListener, OnMapLongClickListener{

    private static final String STATE_NAV="nav";

    private static final String[] DIRECTION_MODES_TYPES = {
            DirectionsJSONClient.MODE_DRIVING,
            DirectionsJSONClient.MODE_WALKING,
            DirectionsJSONClient.MODE_TRANSIT
    };

    private static final int ACTION_NONE            = 0;
    private static final int ACTION_NEW_PIN         = 1;
    private static final int ACTION_GET_ORIGIN      = 2;
    private static final int ACTION_GET_DESTINATION = 3;

//    private static final String FACEBOOK_APP_ID = "938475192837084";
    private UiLifecycleHelper uiHelper;

//    private static final String[] CATEGORIES= {"Καφετέρειες", "Σπιτια", "Σουβλακια", "Άλλο"};

    protected String directionMode  = DirectionsJSONClient.MODE_DRIVING;
    protected String language       = "en";
    protected String units          = "metric";

    private GoogleMap map                                   = null;
    private OnLocationChangedListener mapLocationListener   = null;
    private LocationManager locMgr                          = null;
    private Criteria crit                                   = new Criteria();
    private AlertDialog alert                               = null;
    private Location currentLocation                        = null;
    private LatLng origin_position                          = null;
    private LatLng destination_position                     = null;

    protected ClusterManager<PlaceMarker> mClusterManager;
    protected List<Place> loadedPlaces                  = null;
    protected Polyline currentPolyline                  = null;
    protected Marker selectedMarker                     = null;
    protected SparseArray<String> sparse                = null;
    protected SparseArray<Float> colors                 = null;
    protected ArrayList<String> selectedCategories      = null;
    protected SlidingUpPanelLayout directionsPanel      = null;
    protected ListView instructionsList                 = null;
    protected InstructionsAdapter instructionsAdapter   = null;
    protected View instructionsHeader                   = null;

    private DBHandler db;

    private boolean isClear = false;
    private boolean expectingLocation = false;
    private int expectingAction = ACTION_NONE;
    private int selectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        applySettings();

        if (readyToGo()) {
            setContentView(R.layout.activity_main_map);

            directionsPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            boolean isSplited = getResources().getBoolean(R.bool.split_action_bar);
            if(!isSplited){
                setMargins(directionsPanel,0,0,0,0);
            }
            //directionsPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
            directionsPanel.setAnchorPoint(0.3f);
            directionsPanel.setDragView(findViewById(R.id.panel_top));

           directionsPanel.setPanelSlideListener(new PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
                    if (slideOffset > 0.94) {
                        if (getSupportActionBar().isShowing()) {
                            getSupportActionBar().hide();
                        }
                    } else {
                        if (!getSupportActionBar().isShowing()) {
                            getSupportActionBar().show();
                        }

                    }
                    if(slideOffset >= 0.7){
//                        Log.i("slideOffset", "down");
//                        if (arrowIcon.getTag() == "up"){
//                            arrowIcon.setImageResource(R.drawable.ic_navigation_expand);
//                            arrowIcon.setTag("down");
//                        }
                    }
                    else{
//                        Log.i("slideOffset", "up");
//                        if (arrowIcon.getTag() == "down"){
//                            arrowIcon.setImageResource(R.drawable.ic_navigation_collapse);
//                            arrowIcon.setTag("up");
//                        }
                    }
//                    Log.i("Slide offset:", Float.toString(slideOffset));

                }

                @Override
                public void onPanelExpanded(View panel) {
                    //Log.i(TAG, "onPanelExpanded");
                }

                @Override
                public void onPanelCollapsed(View panel) {
                    //Log.i(TAG, "onPanelCollapsed");
                }

                @Override
                public void onPanelAnchored(View panel) {
                    //Log.i(TAG, "onPanelAnchored");
                }

                @Override
                public void onPanelHidden(View view) {

                }
            });

            db = DBHandler.getInstance(this);

            SupportMapFragment mapFrag=
                    (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

            initListNav();

            initCategorySpinner();

            map=mapFrag.getMap();

            if (savedInstanceState == null) {
                CameraUpdate center=
                        CameraUpdateFactory.newLatLng(new LatLng(37.9928920,
                                23.6772921));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                map.moveCamera(center);
                map.animateCamera(zoom);
            }

            //map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
            //map.setOnInfoWindowClickListener(this);

            locMgr=(LocationManager)getSystemService(LOCATION_SERVICE);
            crit.setAccuracy(Criteria.ACCURACY_FINE);

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);

            mClusterManager = new ClusterManager<PlaceMarker>(this, map);
            mClusterManager.setRenderer(new PlaceRenderer());

            map.setOnCameraChangeListener(mClusterManager);
            map.setOnMarkerClickListener(mClusterManager);
            map.setOnInfoWindowClickListener(this);
            map.setOnMapClickListener(this);
            map.setOnMapLongClickListener(this);

            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            if ( !locMgr.isProviderEnabled( LocationManager.GPS_PROVIDER ) &&
                    !locMgr.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }

            uiHelper = new UiLifecycleHelper(this, null);
            uiHelper.onCreate(savedInstanceState);
        }
        else{
            Log.e("NEARBY", "Error! Something is missing!");
            Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        String provider=null;
        for(String s : locMgr.getProviders(true)){
            Log.d("PROVIDERS",s);
        }
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        if(locMgr != null)
        {
            boolean networkIsEnabled = locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsIsEnabled = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean passiveIsEnabled = locMgr.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

            if(networkIsEnabled)
            {
                locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60*1000L, 10.0F, this, null);
                provider=LocationManager.NETWORK_PROVIDER;
            }
            else if(gpsIsEnabled)
            {
                locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60*1000L, 10.0F, this, null);
                provider=LocationManager.GPS_PROVIDER;
            }
            else if(passiveIsEnabled)
            {
                locMgr.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 60*1000L, 10.0F, this, null);
                provider=LocationManager.PASSIVE_PROVIDER;
            }
            else
            {
                Toast.makeText(this,  getResources().getString(R.string.no_location_service), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.no_location_service_in_device), Toast.LENGTH_LONG).show();
        }

		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			locMgr.requestLocationUpdates(0L, 0.0f, crit, this, null);
		}else{
			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L, 0.0f, this, null);
		}
		*/
        map.setLocationSource(this);
        if(provider!=null)
            currentLocation = locMgr.getLastKnownLocation(provider);


        if(currentLocation!=null)map.getUiSettings().setMyLocationButtonEnabled(true);
        //drawCircle();
        //Toast.makeText(this, "Lat: "+currentLocation.getLatitude()+" Long: "+currentLocation.getLongitude(), Toast.LENGTH_LONG).show();

        if(currentLocation!=null && isClear==false){
            new MarkerLoader().execute(currentLocation);
        }
        else Toast.makeText(this, getResources().getString(R.string.no_location_check_gps), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        map.setMyLocationEnabled(false);
        map.setLocationSource(null);
        locMgr.removeUpdates(this);

        if(alert != null) { alert.dismiss(); }

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
        uiHelper.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        MenuItem visibility = menu.findItem(R.id.action_show_hide);
        if (isClear) {
            visibility.setIcon(R.drawable.ic_action_action_visibility_off);
            visibility.setTitle(R.string.show_pins);
        }
        else {
            visibility.setIcon(R.drawable.ic_action_action_visibility);
            visibility.setTitle(R.string.hide_pins);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_add_pin:
                capturePinLocation();
                return true;
            case R.id.action_show_hide:
                togglePinsVisibility();
                return true;
            case R.id.action_get_directions:
                if(directionsPanel!=null){
                    if(!directionsPanel.isPanelAnchored())
                        directionsPanel.anchorPanel();
                    else
                        directionsPanel.collapsePanel();
                }
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
    }

    public void applySettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        language = sharedPreferences.getString("language", "en");
        units = sharedPreferences.getString("unit", "metric");
        new LanguageHelper(this, language);
    }

    /**
     * Called on button click to set current location as starting point.
     * @param v
     */
    public void setCurrentLocationAsOrigin(View v){
        expectingAction = ACTION_GET_ORIGIN;
        new ReverseGeocodingTask(this).execute(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    /**
     * Called on button click to set current location as destination.
     * @param v
     */
    public void setCurrentLocationAsDestination(View v){
        expectingAction = ACTION_GET_DESTINATION;
        new ReverseGeocodingTask(this).execute(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    /**
     * Enables location capturing so a new place can be added in that location.
     */
    public void capturePinLocation(){enableLocationCapture(ACTION_NEW_PIN);}

    /**
     * Called on button click to enable location capturing for the starting point.
     * @param v
     */
    public void captureOriginLocation(View v){
        if(directionsPanel.isPanelExpanded())directionsPanel.anchorPanel();
        enableLocationCapture(ACTION_GET_ORIGIN);
    }

    /**
     * Called on button click to enable location capturing for the destination point.
     * @param v
     */
    public void captureDestinationLocation(View v){
        if(directionsPanel.isPanelExpanded())directionsPanel.anchorPanel();
        enableLocationCapture(ACTION_GET_DESTINATION);
    }

    public void enableLocationCapture(int action){
        expectingLocation = true;
        expectingAction = action;

        int msg_res;
        switch (action) {
            case ACTION_NEW_PIN:  msg_res = R.string.tap_to_pin;
                break;
            case ACTION_GET_ORIGIN:  msg_res = R.string.tap_origin;
                break;
            case ACTION_GET_DESTINATION:  msg_res = R.string.tap_destination;
                break;
            default: msg_res = R.string.error;
                break;
        }

        final Crouton crouton = Crouton.makeText(this, msg_res, new Style.Builder()
                .setBackgroundColorValue(getResources().getColor(R.color.purple)).setHeight(getActionbarHeight()).build(),
                (RelativeLayout)findViewById(R.id.root_layout))
                .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());

        crouton.show();
    }

    public void receiveInstructions(View v){receiveInstructions();}

    public void receiveInstructions(){
        AutoCompleteTextView origin = (AutoCompleteTextView)findViewById(R.id.origin_edit);
        AutoCompleteTextView destination = (AutoCompleteTextView)findViewById(R.id.destination_edit);
        origin.setError(null);
        destination.setError(null);
        if(origin_position!=null && destination_position!=null){
            new DirectionsLoader().execute(origin_position, destination_position);
//            Log.d("Receive Instruction", ": "+origin_position+", "+destination_position );
        }
        else{
            if(origin.getText().length()==0)
                origin.setError(this.getString(R.string.origin_required));
            else if(destination.getText().length()==0)
                destination.setError(this.getString(R.string.destination_required));
            else if(origin_position==null){
                setOriginFromAddress(origin.getText().toString());
            }
            else if(destination_position==null){
                setDestinationFromAddress(destination.getText().toString());
            }
        }
    }

    public void receiveInstructions(Marker marker){
        LatLng from = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        LatLng[] fromTo = {from, marker.getPosition()};
        new DirectionsLoader().execute(fromTo);
        directionsPanel.anchorPanel();
    }

    public void setOriginFromAddress(String address){
        expectingAction = ACTION_GET_ORIGIN;
        address = encodeURIComponent(address);
        new GeocodingTask(this).execute(address);
    }

    public void setDestinationFromAddress(String address){
        expectingAction = ACTION_GET_DESTINATION;
        address = encodeURIComponent(address);
        new GeocodingTask(this).execute(address);
    }

    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public void togglePinsVisibility(){
        //toggle isClear bool
        isClear=!isClear;

        if(!isClear){
            // call resume to reload items
            onResume();

        }
        else{
            // Clear all items.
            mClusterManager.clearItems();

            // Recluster with no items, so the map will be empty
            mClusterManager.cluster();
        }

        // Invalidate menu.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        else{
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        uiHelper.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(STATE_NAV,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_NAV));
    }

    @Override
    public void activate(OnLocationChangedListener listener) {

        this.mapLocationListener=listener;
    }

    @Override
    public void deactivate() {

        this.mapLocationListener=null;
    }

    /**
     * Called when the location has changed.
     * There are no restrictions on the use of the supplied Location object.
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOC CHANGED!","");
        if (mapLocationListener != null) {
            mapLocationListener.onLocationChanged(location);

            LatLng latlng=
                    new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cu=CameraUpdateFactory.newLatLng(latlng);

            map.animateCamera(cu);
        }
    }

    /**
     * This callback is executed when a click occurs somewhere in map.
     * If there is
     * @param point
     */
    @Override
    public void onMapClick(LatLng point) {
        if(expectingLocation){
            Crouton.cancelAllCroutons();
            expectingLocation=false;

            if(expectingAction == ACTION_NEW_PIN){
                new EditPinDialog(this, new Place(point.latitude, point.longitude), this).show();
                expectingAction = ACTION_NONE;
            }
            else if(expectingAction == ACTION_GET_ORIGIN){
                new ReverseGeocodingTask(getBaseContext()).execute(point.latitude, point.longitude);
            }
            else if(expectingAction == ACTION_GET_DESTINATION){
                new ReverseGeocodingTask(getBaseContext()).execute(point.latitude, point.longitude);
            }

        }
        else{
            removeSelectedMarker();
        }
    }

    /**
     * This executed when place data are submitted through the dialog.
     */
    @Override
    public void onSubmit(Place place) {
        if(place!=null && place.isValid()){
            int id = (int) db.insertOrReplacePlace(place);
            if(place.isNew()){
                place.setId(id);
                PlaceMarker pm = PlaceMarker.fromPlace(place);
                mClusterManager.addItem(pm);
                mClusterManager.cluster();
            }
            else{
                removeSelectedMarker();
                reloadItems();

            }
            Crouton.makeText(this, R.string.pin_saved, Style.CONFIRM, (RelativeLayout)findViewById(R.id.root_layout)).show();
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        new ReverseGeocodingTask(this).execute(point.latitude, point.longitude);
    }

    @Override
    public boolean onClusterClick(Cluster<PlaceMarker> placeMarkerCluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<PlaceMarker> placeMarkerCluster) {

    }

    @Override
    public boolean onClusterItemClick(PlaceMarker placeMarker) {
        removeSelectedMarker();
        selectedMarker = map.addMarker(new MarkerOptions()
                .position(placeMarker.getPosition())
                .title(placeMarker.getTitle())
                .snippet(placeMarker.getDescription())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        selectedMarker.showInfoWindow();
        selectedID=placeMarker.getId();

        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(PlaceMarker placeMarker) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        //unused
    }

    @Override
    public void onProviderDisabled(String provider) {
        //unused
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //unused
    }

    @Override
    public void onItemsSelected(boolean[] selected) {
        selectedCategories = new ArrayList<String>();
		/* If -all categories- is selected we set selectedCategories null. this value will be used in query building. */
        if(selected[0]){
            selectedCategories=null;
        }
		/* Else we add the ids of selected categories to array list */
        else{
            for(int i=1;i<selected.length;i++){
                if(selected[i]){
                    selectedCategories.add(sparse.get(i));
                }
            }
        }
        //Toast.makeText(this, "Activated: "+activated, Toast.LENGTH_LONG).show();

        // Clear all item when selected cats are changed
        mClusterManager.clearItems();

        //new MarkerLoader().execute(currentLocation);


        // call resume to reload items
        onResume();

//        DataFactory df = new DataFactory();
//        Toast.makeText(this, "DF: "+df.getName()+" "+df.getBusinessName()+" "+df.getRandomWord()+" "+df.getRandomChars(8), Toast.LENGTH_LONG).show();

//        db.generateAndStorePlaces(5, 37.9756556, 23.7339464, 10000 );
//        List<Place> places = db.getAllPlaces();
//        for(Place p : places){
//            Log.d("Found Place", p.toString());
//        }
    }

    /**
     * Populates the multiple choice spinner with all categories plus the all-categories choice.
     * It also creates a sparse array with colors mapped to category ids to provide easy color access to markers.
     */
    private void initCategorySpinner(){
        List<Category> cats = db.getAllCategories();
        List<String> items  = new ArrayList<String>();

        if(sparse==null)
            sparse = new SparseArray<String>();
        if(colors==null)
            colors = new SparseArray<Float>();

		/* We add an extra item which represents ALL categories */
        items.add(getResources().getString(R.string.all_categories));
		/* We also put it in first (zero) position in sparse array*/
        sparse.put(0, "0");

		/*
		 * BE CAREFUL! We count from 0 so every category gets added to items
		 * but we put them in sparse shifted by one because zero position is taken by all categories item.
		 * */
        for (int i=0; i<cats.size(); i++) {
            Category c = cats.get(i);
            items.add(c.getName());
            sparse.put(i+1,Integer.toString(c.getId()));

            int color = Color.parseColor(c.getColor());
            float[] hsv = new float[3];
            Color.colorToHSV (color, hsv);
            colors.put(c.getId(), hsv[0]);
        }
        MultiSpinner multiSpinner = (MultiSpinner) findViewById(R.id.multi_spinner);
        multiSpinner.setItems(items, getResources().getString(R.string.all_categories), this);

    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        //map.setMapType(MAP_TYPES[itemPosition]);
        directionMode = DIRECTION_MODES_TYPES[itemPosition];
        return(true);
    }

    @SuppressWarnings("deprecation")
    private void initListNav() {
        String[] directionModeNames  = {
                getString(R.string.by_car),
                getString(R.string.by_foot),
                getString(R.string.by_transport)
        };

        ArrayList<String> items = new ArrayList<>();
        ArrayAdapter<String> nav;
        ActionBar bar = getSupportActionBar();

        for (String m : directionModeNames) {
            items.add(m);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            nav=
                    new ArrayAdapter<>(
                            bar.getThemedContext(),
                            android.R.layout.simple_spinner_item,
                            items);
        }
        else {
            nav=
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            items);
        }

        nav.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(nav, this);
    }

    @SuppressWarnings("unused")
    private void addMarker(GoogleMap map, double lat, double lon,
                           int title, int snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(getString(title))
                .snippet(getString(snippet)));
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ενεργοποίηση GPS")
                .setMessage("Το GPS της συσκευής σας φαίνεται να είναι απενεργοποιημένο. Θέλετε να το ενεργοποιήσετε?")
                .setCancelable(false)
                .setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Όχι", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options)
                .setCancelable(true)
                .setItems(R.array.infowindow_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: deletePlace(selectedID);
                                break;
                            case 1:
                                editPlace(selectedID);
                                break;
                            case 2:
                                receiveInstructions(selectedMarker);
                                break;
                            case 3:
                                shareOnFacebook(selectedMarker);
                                break;
                            default:
                                break;
                        }

                    }
                });
        builder.create().show();
    }

    protected void editPlace(int id){
        Place placeToEdit = getPlaceFromLoadedPlacesById(id);
        new EditPinDialog(this, placeToEdit, this).show();
    }

    protected void deletePlace(int id){
        // Delete place from database.
        db.deletePlace(id);

        removeSelectedMarker();

        reloadItems();
    }

    protected void shareOnFacebook(Marker marker){
        Log.d("App name: ",getString(R.string.app_name));
        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(MainMapActivity.this)
                    .setLink("http://maps.google.com/maps?z=15&t=m&q=loc:" + marker.getPosition().latitude + "+" + marker.getPosition().longitude)
                    .setName(marker.getTitle())
                    .setCaption("via Geopin App")
                    .setDescription(marker.getSnippet())
                    .setApplicationName(getString(R.string.app_name))
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        }
    }

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("Facebook Dialog", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("Facebook Dialog", "Success!");
        }
    };

    protected void removeSelectedMarker(){
        // Remove marker and polyline from map.
        if(selectedMarker!=null) selectedMarker.remove();
        if(currentPolyline!=null) currentPolyline.remove();
        // Set selected marker's id to 0 (there is no item with id 0).
        selectedID = 0;
    }

    public Place getPlaceFromLoadedPlacesById(int id) throws NoSuchElementException{
        for(Place p : loadedPlaces){
            if(p.getId() == id){
                return p;
            }
        }
        throw new NoSuchElementException();
    }

    protected void reloadItems(){
        // Clear instructions list.
        instructionsAdapter.clear();
        instructionsAdapter.notifyDataSetChanged();
        instructionsList.setAdapter(null);
        instructionsList.removeHeaderView(instructionsHeader);

        // Clear and reload cluster items.
        mClusterManager.clearItems();
        onResume();
    }


    protected class MarkerLoader extends AsyncTask<Location,PlaceMarker,List<PlaceMarker>>{
        DBHandler db;
        List<PlaceMarker> placeMarkers = new ArrayList<>();

        double t1,t2;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setSupportProgressBarIndeterminateVisibility(true);
            t1 = System.nanoTime();
        }

        @Override
        protected List<PlaceMarker> doInBackground(Location... loc) {
            double lat = loc[0].getLatitude();
            double lon = loc[0].getLongitude();

            try{
                db = DBHandler.getInstance(getApplicationContext());
                if(selectedCategories!=null){
                    loadedPlaces = db.getPlacesByCategoryIds(selectedCategories);
                }else{
                    loadedPlaces = db.getAllPlaces();
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                db.close();
            }

            //List<Marker> markers = new ArrayList<Marker>();

            if(loadedPlaces!=null && loadedPlaces.size()>0){
                for(Place p : loadedPlaces){
                    PlaceMarker m = new PlaceMarker(p.getId(), p.getTitle(),
                            p.getDescription(),
                            p.getLatitude(),
                            p.getLongitude(),
                            p.getCategory_id());
                    //mClusterManager.addItem(m);
                    //publishProgress(m);
                    placeMarkers.add(m);
					/*
					Marker m = map.addMarker(new MarkerOptions()
						.position(new LatLng(p.getLatitude(),p.getLongitude()))
						.title(p.getName())
						.snippet(p.getAddress() +", "+p.getArea()));
					markers.add(m);
					*/
                }
            }
            //Log.d("Num of markers",""+loadedPlaces.size());

            return placeMarkers;
        }

        protected void onProgressUpdate(PlaceMarker... companyMarkers){
            //mClusterManager.addItem(companyMarkers[0]);
			/*
			map.addMarker(new MarkerOptions()
				.position(companyMarkers[0].getPosition())
				.title(companyMarkers[0].getTitle())
				.snippet(companyMarkers[0].getSnippet()));
			*/
        }

        protected void onPostExecute(List<PlaceMarker> placeMarkers){
            if(placeMarkers!=null && placeMarkers.size()>0){
                for(PlaceMarker pm : placeMarkers){
                    mClusterManager.addItem(pm);
                }
            }
            setSupportProgressBarIndeterminateVisibility(false);
            mClusterManager.cluster();
            t2 = (System.nanoTime() - t1)/1000000.0;
            Log.d("---- Marker Loader completed in ----", Double.toString(t2));
        }



    }


    protected class DirectionsLoader extends AsyncTask<LatLng,Void,RouteInfo> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setSupportProgressBarIndeterminateVisibility(true);

        }

        @Override
        protected RouteInfo doInBackground(LatLng... params) {
            DirectionsJSONClient djc = new DirectionsJSONClient(params[0], params[1], directionMode, language, units);
            RouteInfo routeInfo = djc.getRouteInfo();

            return routeInfo;
        }

        protected void onPostExecute(RouteInfo routeInfo){
            setSupportProgressBarIndeterminateVisibility(false);

            if(currentPolyline!=null){
                currentPolyline.remove();
            }
            currentPolyline = map.addPolyline(routeInfo.getPolylineOptions());

            double dist = (double) routeInfo.getDistanceValue() * 1.0/1000.0;
            int durat = (int)Math.round(routeInfo.getDurationValue() * 1.0/60.0);

//            for(Step step : routeInfo.getSteps()){
//                Log.d(step.getDistanceText()+", "+step.getDurationText(), step.getHtmlInstructions());
//            }

            if(instructionsList == null)
                instructionsList = (ListView)findViewById(R.id.intructions_list);

            instructionsAdapter = new InstructionsAdapter(getApplicationContext());

            if(instructionsHeader == null)
                instructionsHeader = View.inflate(getApplicationContext(), R.layout.instructions_list_header, null);

            ((TextView)instructionsHeader.findViewById(R.id.origin)).setText(routeInfo.getStartAddress());
            ((TextView)instructionsHeader.findViewById(R.id.destination)).setText(routeInfo.getEndAddress());
            ((TextView)instructionsHeader.findViewById(R.id.duration)).setText(routeInfo.getDurationText());
            ((TextView)instructionsHeader.findViewById(R.id.distance)).setText(routeInfo.getDistanceText());

            instructionsAdapter.setData(routeInfo.getSteps());
            if(instructionsList.getHeaderViewsCount()==0)
                instructionsList.addHeaderView(instructionsHeader);

            instructionsList.setAdapter(instructionsAdapter);

            if(selectedMarker!=null){
                selectedMarker.hideInfoWindow();
                String snip = selectedMarker.getSnippet();

                selectedMarker.setSnippet(snip+ ", " +
                        getString(R.string.distance)+ ": " + routeInfo.getDistanceText() + ", " +
                        getString(R.string.arrival_time) + ": " + routeInfo.getDurationText());
                selectedMarker.showInfoWindow();
            }

            ((AutoCompleteTextView)findViewById(R.id.origin_edit)).setText(routeInfo.getStartAddress());
            ((AutoCompleteTextView)findViewById(R.id.destination_edit)).setText(routeInfo.getEndAddress());

            origin_position=null;
            destination_position=null;
        }

    }

    private class ReverseGeocodingTask extends AsyncTask<Double, Void, String>{
        Context mContext;
        double latitude;
        double longitude;

        public ReverseGeocodingTask(Context context){
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Double... params) {
            Geocoder geocoder = new Geocoder(mContext);
            latitude = params[0].doubleValue();
            longitude = params[1].doubleValue();

            List<Address> addresses = null;
            String addressText="";

            try {
                addresses = geocoder.getFromLocation(latitude, longitude,1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses != null && addresses.size() > 0 ){
                Address address = addresses.get(0);

                addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
            }

            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            // Setting address of the touched Position
            if(expectingAction == ACTION_GET_ORIGIN){
                AutoCompleteTextView origin = (AutoCompleteTextView)findViewById(R.id.origin_edit);
                origin.setText(addressText);
                origin_position = new LatLng(latitude,longitude);
            }
            else if(expectingAction == ACTION_GET_DESTINATION){
                AutoCompleteTextView destination = (AutoCompleteTextView)findViewById(R.id.destination_edit);
                destination.setText(addressText);
                destination_position = new LatLng(latitude,longitude);
            }
            else{
                Toast.makeText(mContext, addressText, Toast.LENGTH_LONG).show();
            }
            expectingAction = ACTION_NONE;
        }
    }

    private class GeocodingTask extends AsyncTask<String, Void, LatLng>{
        Context mContext;
        double latitude;
        double longitude;

        public GeocodingTask(Context context){
            super();
            mContext = context;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                    params[0] + "&sensor=false&language="+language;

            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(stringBuilder.toString());

                if(jsonObject.getString("status").equals("OK")){
                    longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                }
                else{
                    longitude = 0.0;
                    latitude = 0.0;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(latitude>0.0 && longitude>0.0)
                return new LatLng(latitude,longitude);
            else
                return null;
        }

        @Override
        protected void onPostExecute(LatLng latlng) {
            boolean has_errors = false;
            if(expectingAction == ACTION_GET_ORIGIN){
                origin_position = latlng;
                AutoCompleteTextView origin = (AutoCompleteTextView)findViewById(R.id.origin_edit);
                if(origin_position==null){
                    origin.setError(mContext.getString(R.string.address_not_found));
                    has_errors = true;
                }
                else origin.setError(null);
            }
            else if(expectingAction == ACTION_GET_DESTINATION){
                destination_position = latlng;
                AutoCompleteTextView destination = (AutoCompleteTextView)findViewById(R.id.destination_edit);
                if(destination_position==null){
                    destination.setError(mContext.getString(R.string.address_not_found));
                    has_errors = true;
                }
                else destination.setError(null);
            }
            expectingAction = ACTION_NONE;

            if((origin_position!=null || destination_position!=null) && !has_errors){
                receiveInstructions();
            }
        }
    }

    /**
     * Custom renderer to enable the use of custom colors.
     */
    protected class PlaceRenderer extends DefaultClusterRenderer<PlaceMarker> {
        public PlaceRenderer() {
            super(getApplicationContext(), map, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(PlaceMarker placeMarker, MarkerOptions markerOptions) {

            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(colors.get(placeMarker.getCategory_id())));


        }

    }

    public int getActionbarHeight(){

        // Calculate ActionBar height
        int actionBarHeight = 100;
        TypedValue tv = new TypedValue();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
        {
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

            Log.d("1 ab size", ": "+actionBarHeight);
        }
        else if(getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            Log.d("2 ab size", ": "+actionBarHeight);
        }

        Log.d("Final ab size", ": "+actionBarHeight);
        return actionBarHeight;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

}
