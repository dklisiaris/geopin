package gr.upatras.ceid.geopin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import gr.upatras.ceid.geopin.maps.AbstractMapActivity;
import gr.upatras.ceid.geopin.maps.DirectionsClient;
import gr.upatras.ceid.geopin.maps.PlaceMarker;
//import gr.upatras.ceid.geopin.widgets.MultiSpinner;
import gr.upatras.ceid.geopin.widgets.MultiSpinner.MultiSpinnerListener;


public class MainMapActivity extends AbstractMapActivity implements
        OnNavigationListener, OnInfoWindowClickListener, LocationSource, LocationListener,
        ClusterManager.OnClusterClickListener<PlaceMarker>,
        ClusterManager.OnClusterInfoWindowClickListener<PlaceMarker>,
        ClusterManager.OnClusterItemClickListener<PlaceMarker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PlaceMarker>,
        MultiSpinnerListener{

    private static final String STATE_NAV="nav";

    private static final String[] DIRECTION_MODE_NAMES= {"Αυτοκίνητο","Πεζός","Λεωφορείο"};
    private static final String[] DIRECTION_MODES_TYPES= {DirectionsClient.MODE_DRIVING,
            DirectionsClient.MODE_WALKING,DirectionsClient.MODE_TRANSIT};

    private static final String[] CATEGORIES= {"Καφετέρειες", "Σπιτια", "Σουβλακια", "Άλλο"};

    protected String directionMode = DirectionsClient.MODE_DRIVING;
    private GoogleMap map=null;
    private OnLocationChangedListener mapLocationListener=null;
    private LocationManager locMgr=null;
    private Criteria crit=new Criteria();
    private AlertDialog alert=null;
    private Location currentLocation=null;

    protected ClusterManager<PlaceMarker> mClusterManager;
//    protected ArrayList<Place> nearComps=null;
    protected Polyline currentPolyline=null;
    protected Marker selectedMarker=null;
    protected SparseArray<String> sparse=null;
    protected ArrayList<String> selectedCategories = null;
    int selectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        setContentView(R.layout.activity_main_map);

        if (readyToGo()) {
            setContentView(R.layout.map_fragment);

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
            map.setOnCameraChangeListener(mClusterManager);
            map.setOnMarkerClickListener(mClusterManager);
            map.setOnInfoWindowClickListener(this);
            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            if ( !locMgr.isProviderEnabled( LocationManager.GPS_PROVIDER ) &&
                    !locMgr.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }
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

        String provider=null;
        for(String s : locMgr.getProviders(true)){
            Log.d("PROVIDERS",s);
        }

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
                Toast.makeText(this, "Δεν υπάρχει ενεργοποιημένη υπηρεσία εύρεσης τοποθεσίας.", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, "Δεν υπάρχει υπηρεσία εύρεσης τοποθεσίας στη συσκευή σας.", Toast.LENGTH_LONG).show();
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

        if(currentLocation!=null){
            // TODO new MarkerLoader().execute(currentLocation);
        }
        else Toast.makeText(this, "Η τοποθεσία σας δεν βρέθηκε. Ελέγξτε τις ρυθμίσεις gps.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        map.setMyLocationEnabled(false);
        map.setLocationSource(null);
        locMgr.removeUpdates(this);

        if(alert != null) { alert.dismiss(); }

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

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

    @Override
    public boolean onClusterClick(Cluster<PlaceMarker> placeMarkerCluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<PlaceMarker> placeMarkerCluster) {

    }

    @Override
    public boolean onClusterItemClick(PlaceMarker placeMarker) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(PlaceMarker placeMarker) {

    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link <LocationProvider>.OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link <LocationProvider>.TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link <LocationProvider>.AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void initCategorySpinner(){
//        DBHandler db = DBHandler.getInstance(this);
//        ArrayList<Category> cats = db.getCategories("cat_parent_id is not null");
//        ArrayList<String> items=new ArrayList<String>();
//
//        if(sparse==null)
//            sparse = new SparseArray<String>();
//
//		/* We add an extra item which represents ALL categories */
//        items.add("Όλες οι κατηγορίες");
//		/* We also put it in first (zero) position in sparse array*/
//        sparse.put(0, "0");
//
//		/*
//		 * BE CAREFUL! We count from 0 so every category gets added to items
//		 * but we put them in sparse shifted by one because zero position is taken by all categories item.
//		 * */
//        for (int i=0; i<cats.size(); i++) {
//            items.add(cats.get(i).getCat_name());
//            sparse.put(i+1,Integer.toString(cats.get(i).getCat_id()));
//        }
//        MultiSpinner multiSpinner = (MultiSpinner) findViewById(R.id.multi_spinner);
//        multiSpinner.setItems(items,"Όλες οι κατηγορίες",this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        //map.setMapType(MAP_TYPES[itemPosition]);
        directionMode = DIRECTION_MODES_TYPES[itemPosition];
        return(true);
    }

    @SuppressWarnings("deprecation")
    private void initListNav() {
        ArrayList<String> items=new ArrayList<String>();
        ArrayAdapter<String> nav=null;
        ActionBar bar=getSupportActionBar();

        for (String m : DIRECTION_MODE_NAMES) {
            items.add(m);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            nav=
                    new ArrayAdapter<String>(
                            bar.getThemedContext(),
                            android.R.layout.simple_spinner_item,
                            items);
        }
        else {
            nav=
                    new ArrayAdapter<String>(
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


    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onItemsSelected(boolean[] selected) {

    }
}
