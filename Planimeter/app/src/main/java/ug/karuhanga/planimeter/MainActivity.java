package ug.karuhanga.planimeter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Constants, View.OnClickListener, GPSResultListener, OnMapReadyCallback {
    //File-Wide values
    private GPSDataManager gpsDataManager;
    private AcclDataManager acclDataManager;
    private GoogleMap resultMap;
    private GeoJsonLayer geoJsonLayer;
    private LatLng center;
    private FloatingActionButton fab_start_recording;
    private FloatingActionButton fab_end_recording;
    private FloatingActionButton fab_on_turn;
    private FloatingActionButton fab_simulate;
    private TextView textViewUpdate;
    private TextView textViewGuide;
    private MapView mapViewResult;
    private CardView cardMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gpsDataManager = new GPSDataManager(this);
        acclDataManager = new AcclDataManager(this);

        setContentView(R.layout.activity_main);

        //find necessary resources
        fab_start_recording= (FloatingActionButton) findViewById(R.id.fab_start_recording);
        fab_end_recording= (FloatingActionButton) findViewById(R.id.fab_end_recording);
        fab_on_turn= (FloatingActionButton) findViewById(R.id.fab_on_turn);
        fab_simulate= (FloatingActionButton) findViewById(R.id.fab_simulate);
        textViewUpdate= (TextView) findViewById(R.id.textView_update);
        textViewGuide= (TextView) findViewById(R.id.textView_guide);
        mapViewResult= (MapView) findViewById(R.id.mapView_result);
        cardMap= (CardView) findViewById(R.id.card_map);

        //set on-click listeners
        fab_start_recording.setOnClickListener(this);
        fab_end_recording.setOnClickListener(this);
        fab_on_turn.setOnClickListener(this);
        fab_simulate.setOnClickListener(this);


    }

    //start recording user position (only if GPS permission was granted for Android Versions starting Marsmellow)
    private void startGPSComponent(String flag){
        //inform user if failed
        if (flag.equals("Failed")){
            Toast.makeText(this, "Unfortunately, the GPS Service is unavailable", Toast.LENGTH_LONG).show();
        }
        else{
            if (permitted()){
                //start collection if permitted
                gpsDataManager.startRecording();
            }
            else{
                //request permission
                getLocationPermission();
            }
        }

    }

    //request location permission (for versions since Marshmallow... request permission Dynamically)
    private void getLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODE_LOCATION_REQUEST);
    }

    //check if granted location permission (for versions since Marshmallow... request permission Dynamically)
    private boolean permitted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //try restart after permission granted
            startGPSComponent("Passed");
        }
        else {
            //send failed signal if permission rejected
            startGPSComponent("Failed");
        }
    }

    //perform actions that consist of recording necessary data
    private void enterRecordingMode(){
        startGPSComponent("Initial");
        acclDataManager.startRecording();
        textViewUpdate.setText(getText(R.string.textView_update_2));
        textViewGuide.setText(getText(R.string.textView_guide_2));
        fab_start_recording.setVisibility(View.INVISIBLE);
        fab_end_recording.setVisibility(View.VISIBLE);
        fab_on_turn.setVisibility(View.VISIBLE);
        fab_simulate.setVisibility(View.INVISIBLE);
        cardMap.setVisibility(View.INVISIBLE);
    }

    //finish data collection
    private void exitRecordingMode(){
        gpsDataManager.stopRecording();
        acclDataManager.stopRecording();
        textViewGuide.setText(getText(R.string.textView_guide_1));
        fab_start_recording.setVisibility(View.VISIBLE);
        fab_end_recording.setVisibility(View.INVISIBLE);
        fab_on_turn.setVisibility(View.INVISIBLE);
        fab_simulate.setVisibility(View.VISIBLE);
        boolean result= gpsDataManager.evaluateArea();
        if (!result){
            displayResult(null, null, null);
        }

    }

    //data collection actions related to turning
    public void onTurn(){
        if (permitted()){
            gpsDataManager.recordTurn();
        }
    }

    //onClick Listener Methods
    @Override
    public void onClick(View view) {
        if (view.equals(fab_start_recording)){
            enterRecordingMode();
        }
        else if (view.equals(fab_end_recording)){
            exitRecordingMode();
        }
        else if (view.equals(fab_on_turn)){
            onTurn();
        }
        else if (view.equals(fab_simulate)){
            Intent intent= new Intent(this, Simulate.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(getString(R.string.name_objects_intent), new int[] {R.raw.default_obj, R.raw.default_mtl});
            startActivity(intent);
        }
    }

    @Override
    public void showLoader() {

    }

    @Override
    public void hideLoader() {

    }

    @Override
    public boolean displayResult(JSONObject layerData, Double area, LatLng center) {
        double gpsArea= (area==null)? 0.0 : area;
        if (gpsArea>0.0) {
            String result = String.format("%.3f", gpsArea);
            this.textViewUpdate.setText(result + " Square Metres!");
        }
        else{
            this.textViewUpdate.setText("Unfortunately, there was no result");
        }

        try {
            geoJsonLayer= new GeoJsonLayer(this.resultMap, layerData);
        }catch (NullPointerException e){
            return false;
        }
        mapViewResult.getMapAsync(this);
        this.center= (center==null)? this.center: center;
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.resultMap= googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        geoJsonLayer.addLayerToMap();
        cardMap.setVisibility(View.VISIBLE);
    }
}
