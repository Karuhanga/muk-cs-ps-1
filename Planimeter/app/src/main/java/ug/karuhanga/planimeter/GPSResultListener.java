package ug.karuhanga.planimeter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by karuhanga on 8/11/17.
 */

interface GPSResultListener {
    void showLoader();
    void hideLoader();
    boolean displayResult(JSONObject layerData, Double area, LatLng center);
}
