package gr.upatras.ceid.geopin.maps;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class DirectionsJSONClient {
    public final static String MODE_DRIVING = "driving";
    public final static String MODE_WALKING = "walking";
    public final static String MODE_BICYCLING = "bicycling";
    public final static String MODE_TRANSIT = "transit";

    private JSONObject directions;
    private JSONObject route;
    private JSONObject leg;

    public DirectionsJSONClient(LatLng start, LatLng end, String mode, String language, String units) {
        String url = makeUrl(start, end, mode, language, units);

        String response = getJSONResponse(url);

        if(response != null){
            try {
                directions = new JSONObject(response);
                if(!directions.getString("status").equals("OK")){
                    directions = null;
                }
                else{
                    // No alternatives. Just get the first route available.
                    route = directions.getJSONArray("routes").getJSONObject(0);
                    leg = route.getJSONArray("legs").getJSONObject(0);

                }
            }
            catch (JSONException e) {e.printStackTrace();}
        }
    }


    /**
     * Extracts generic information such as duration and distance for the current route.
     * @return a RouteInfo object with route information.
     */
    public RouteInfo getRouteInfo(){
        RouteInfo routeInfo = new RouteInfo();
        try {
            routeInfo.setStartAddress(leg.getString("start_address"));
            routeInfo.setEndAddress(leg.getString("end_address"));

            routeInfo.setDistanceText(leg.getJSONObject("distance").getString("text"));
            routeInfo.setDistanceValue(leg.getJSONObject("distance").getInt("value"));

            routeInfo.setDurationText(leg.getJSONObject("duration").getString("text"));
            routeInfo.setDurationValue(leg.getJSONObject("duration").getInt("value"));

            String encodedPoly = route.getJSONObject("overview_polyline").getString("points");
            List<LatLng> polyPoints = decodePoly(encodedPoly);
            PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);
            for(LatLng point : polyPoints){
                rectLine.add(point);
            }
            routeInfo.setPolylineOptions(rectLine);

            routeInfo.setSteps(getSteps());


        }
        catch (JSONException e) {e.printStackTrace();}

        return routeInfo;
    }

    public List<Step> getSteps(){
        List<Step> steps = new ArrayList<>();
        try {
            JSONArray jSteps = leg.getJSONArray("steps");
            for (int i=0; i < jSteps.length(); i++){
                JSONObject jStep = jSteps.getJSONObject(i);
                Step step = new Step();

                step.setStartLocation(new LatLng(jStep.getJSONObject("start_location").getDouble("lat"), jStep.getJSONObject("start_location").getDouble("lng")));
                step.setEndLocation(new LatLng(jStep.getJSONObject("end_location").getDouble("lat"), jStep.getJSONObject("end_location").getDouble("lng")));

                step.setDistanceText(jStep.getJSONObject("distance").getString("text"));
                step.setDistanceValue(jStep.getJSONObject("distance").getInt("value"));

                step.setDurationText(jStep.getJSONObject("duration").getString("text"));
                step.setDurationValue(jStep.getJSONObject("duration").getInt("value"));

                step.setHtmlInstructions(jStep.getString("html_instructions"));

                steps.add(step);
            }

        }
        catch (JSONException e) {e.printStackTrace();}

        return steps;
    }


    /**
     * Creates a url for directions api based on specific paramaeters.
     * @param start the starting point LatLng object
     * @param end the starting point LatLng object
     * @param units this string can be metric or imperial
     * @param mode travel mode
     * @param language language code: el or en
     * @return
     */
    private String makeUrl(LatLng start, LatLng end, String mode, String language, String units){
        long time = System.currentTimeMillis()/1000;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://maps.googleapis.com/maps/api/directions/json");
        urlBuilder.append("?origin=");
        urlBuilder.append(Double.toString(start.latitude));
        urlBuilder.append(",");
        urlBuilder.append(Double.toString(start.longitude));
        urlBuilder.append("&destination=");
        urlBuilder.append(Double.toString(end.latitude));
        urlBuilder.append(",");
        urlBuilder.append(Double.toString(end.longitude));
        urlBuilder.append("&sensor=false&units=");
        urlBuilder.append(units);
        urlBuilder.append("&mode=");
        urlBuilder.append(mode);
        urlBuilder.append("&language=");
        urlBuilder.append(language);
        urlBuilder.append("&departure_time=");
        urlBuilder.append(Long.toString(time));
        return urlBuilder.toString();
    }

    /**
     * Decodes a polyline such as "{z{fFu{yoCFo@TyC@EBq@BYHaANiC" to list of LatLng objects.
     * @param encoded
     * @return
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    private String getJSONResponse(String url){
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
        HttpPost httppost = new HttpPost(url);

        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
        }catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        return result;
    }

}
