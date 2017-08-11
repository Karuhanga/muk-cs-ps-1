package ug.karuhanga.planimeter;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

/**
 * Created by karuhanga on 8/11/17.
 */

interface GPSResultListener {
    void showLoader();
    void hideLoader();
    boolean displayResult(PolylineOptions polylineOptions, Double area, LatLng center);
}
