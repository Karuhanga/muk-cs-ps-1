package ug.karuhanga.planimeter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
/*
**TODO  App Icon Change
*       Fab Simulation Change Icon
*       Add other simulation objects
 */


public class MainActivity extends AppCompatActivity implements Constants, View.OnClickListener, GPSResultListener, OnMapReadyCallback, StepTakenListener {
    //File-Wide values
    private GPSDataManager gpsDataManager;
    private AcclDataManager acclDataManager;
    private GoogleMap resultMap;
    private Polyline polyline;
    private PolylineOptions polylineOptions;
    private LatLng center;
    private FloatingActionButton fab_start_recording;
    private FloatingActionButton fab_end_recording;
    private FloatingActionButton fab_simulate;
    private TextView textViewUpdate;
    private TextView textViewGuide;
    private MapView mapViewResult;
    private CardView cardMap;
    private ProgressBar progressBar;
    private boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gpsDataManager = new GPSDataManager(this);
        acclDataManager = new AcclDataManager(this);

        setContentView(R.layout.activity_main);

        //find necessary resources
        fab_start_recording= (FloatingActionButton) findViewById(R.id.fab_start_recording);
        fab_end_recording= (FloatingActionButton) findViewById(R.id.fab_end_recording);
        fab_simulate= (FloatingActionButton) findViewById(R.id.fab_simulate);
        textViewUpdate= (TextView) findViewById(R.id.textView_update);
        textViewGuide= (TextView) findViewById(R.id.textView_guide);
        mapViewResult= (MapView) findViewById(R.id.mapView_result);
        cardMap= (CardView) findViewById(R.id.card_map);
        progressBar= (ProgressBar) findViewById(R.id.progressBar_Main);
        polylineOptions= null;
        polyline= null;
        status= false;


        //set on-click listeners
        fab_start_recording.setOnClickListener(this);
        fab_end_recording.setOnClickListener(this);
        fab_simulate.setOnClickListener(this);

        Bundle bundle;
        try {
            bundle= savedInstanceState.getBundle("mapBundle");
        }catch (RuntimeException e){
            bundle= savedInstanceState;
        }

        mapViewResult.onCreate(bundle);
    }

    //start recording user position (only if GPS permission was granted for Android Versions starting Marsmellow)
    private void startDataRecording(String flag){
        //inform user if failed
        if (flag.equals("Failed")){
            Toast.makeText(this, "Please grant permissions for the service to run", Toast.LENGTH_LONG).show();
            exitRecordingMode();
        }
        else{
            if (permitted()){
                //start collection if permitted
                gpsDataManager.startRecording();
            }
            else{
                //request permission
                getPermissions();
            }
        }

    }

    //request location permission (for versions since Marshmallow... request permission Dynamically)
    private void getPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_LOCATION_REQUEST);
    }

    //check if granted location permission (for versions since Marshmallow... request permission Dynamically)
    private boolean permitted(){
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                );
    }

    private boolean allGranted(@NonNull int[] grantResults){
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //initiate necessary data collection 
    private void enterRecordingMode(){
        startDataRecording("Initial");
        acclDataManager.startRecording();
        progressBar.setVisibility(View.VISIBLE);
        textViewUpdate.setText(getText(R.string.textView_update_2));
        textViewGuide.setText(getText(R.string.textView_guide_2));
        fab_start_recording.setVisibility(View.INVISIBLE);
        fab_end_recording.setVisibility(View.VISIBLE);
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
        fab_simulate.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        boolean result= gpsDataManager.evaluateArea();
        if (!result){
            displayResult(null, null, null);
        }

    }

    @Override
    public void dataReceived() {
        acclDataManager.resetCount();
    }

    @Override
    public boolean displayResult(PolylineOptions polylineOptions, Double area, LatLng center) {
        double gpsArea= (area==null)? 0.0 : area;
        if (gpsArea>0.0) {
            String result = String.format("%.3f", gpsArea);
            this.textViewUpdate.setText(result + " Square Metres!");
        }
        else{
            this.textViewUpdate.setText("Unfortunately, there was no result");
            return false;
        }
        mapViewResult.getMapAsync(this);
        this.center= (center==null)? this.center: center;
        this.polylineOptions= polylineOptions;
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap==null){
            return;
        }
        this.resultMap= googleMap;
        resultMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15.0f));
        if (polylineOptions==null){
            return;
        }
        if (polyline!=null){
            polyline.remove();
        }
        Toast.makeText(this, polylineOptions.toString(), Toast.LENGTH_SHORT).show();
        this.polyline= this.resultMap.addPolyline(polylineOptions);
        cardMap.setVisibility(View.VISIBLE);
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
        else if (view.equals(fab_simulate)){
            Intent intent= new Intent(this, Simulate.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(getString(R.string.name_objects_intent), new int[] {R.raw.default_obj, R.raw.default_mtl});
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && allGranted(grantResults)) {
            //try restart after permission granted
            startDataRecording("Passed");
        }
        else {
            //send failed signal if permission rejected
            startDataRecording("Failed");
        }
    }

    @Override
    public void stepTaken() {
    }

    @Override
    public void pauseRecordings() {
        this.status= true;
        textViewUpdate.setText("Freeze!\nPlease Wait a moment as the connection is restored");
        textViewUpdate.setTextSize(20);
    }

    @Override
    public void resumeRecordings() {
        Toast.makeText(this, "Please proceed", Toast.LENGTH_LONG).show();
        textViewUpdate.setText("Recording...");
        textViewUpdate.setTextSize(30);
        this.status= false;
    }

    @Override
    public boolean paused() {
        return true&&status;
    }

    @Override
    public void onStart(){
        super.onStart();
        mapViewResult.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
        mapViewResult.onStop();
    }

    @Override
    public void onResume(){
        super.onResume();
        mapViewResult.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapViewResult.onPause();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapViewResult.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        Bundle mapBundle= new Bundle();
        outState.putBundle("mapBundle", mapBundle);
        super.onSaveInstanceState(outState);
        mapViewResult.onSaveInstanceState(mapBundle);
    }
}
