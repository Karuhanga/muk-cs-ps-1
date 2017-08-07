package ug.karuhanga.planimeter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    //File-Wide values
    private GPSDataManager gpsDataManager;
    private AcclDataManager acclDataManager;
    private FloatingActionButton fab_start_recording;
    private FloatingActionButton fab_end_recording;
    private FloatingActionButton fab_on_turn;
    private TextView textViewUpdate;
    private TextView textViewGuide;

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
        textViewUpdate= (TextView) findViewById(R.id.textView_update);
        textViewGuide= (TextView) findViewById(R.id.textView_guide);

        //set on-click listeners
        fab_start_recording.setOnClickListener(this);
        fab_end_recording.setOnClickListener(this);
        fab_on_turn.setOnClickListener(this);


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
    }

    //finish data collection
    private void exitRecordingMode(){
        gpsDataManager.stopRecording();
        acclDataManager.stopRecording();
        textViewGuide.setText(getText(R.string.textView_guide_1));
        fab_start_recording.setVisibility(View.VISIBLE);
        fab_end_recording.setVisibility(View.INVISIBLE);
        fab_on_turn.setVisibility(View.INVISIBLE);
        //TODO: Replace temporary display with better UI
        double gpsArea= this.gpsDataManager.evaluateArea();
        if (gpsArea>0.0) {
            String result = String.format("%.3f", gpsArea);
            this.textViewUpdate.setText(result + " Square Metres!");
        }
        else{
            this.textViewUpdate.setText("Unfortunately, there was no result");
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
    }
}
