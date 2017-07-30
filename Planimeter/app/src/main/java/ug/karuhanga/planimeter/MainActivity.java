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
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Constants, View.OnClickListener {

    private LocationCollector locationCollector;
    private FloatingActionButton fab_start_recording;
    private FloatingActionButton fab_end_recording;
    private FloatingActionButton fab_on_turn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationCollector= new LocationCollector(this);

        setContentView(R.layout.activity_main);

        fab_start_recording= (FloatingActionButton) findViewById(R.id.fab_start_recording);
        fab_end_recording= (FloatingActionButton) findViewById(R.id.fab_end_recording);
        fab_on_turn= (FloatingActionButton) findViewById(R.id.fab_on_turn);

        fab_start_recording.setOnClickListener(this);
        fab_end_recording.setOnClickListener(this);
        fab_on_turn.setOnClickListener(this);


    }

    private void startGPSComponent(String flag){
        if (flag.equals("Failed")){
            Toast.makeText(this, "Unfortunately, the GPS Service is unavailable", Toast.LENGTH_LONG).show();
        }
        else{
            if (permitted()){
                locationCollector.startRecording();
            }
            else{
                getLocationPermission();
            }
        }

    }

    private void getLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODE_LOCATION_REQUEST);
    }

    private boolean permitted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGPSComponent("Passed");
        }
        else {
            startGPSComponent("Failed");
        }
    }

    private void enterRecordingMode(){
        startGPSComponent("Initial");
        fab_start_recording.setVisibility(View.INVISIBLE);
        fab_end_recording.setVisibility(View.VISIBLE);
        fab_on_turn.setVisibility(View.VISIBLE);
    }

    private void exitRecordingMode(){
        locationCollector.stopRecording();
        fab_start_recording.setVisibility(View.VISIBLE);
        fab_end_recording.setVisibility(View.INVISIBLE);
        fab_on_turn.setVisibility(View.INVISIBLE);
    }

    public void onTurn(){
        if (permitted()){
            locationCollector.recordTurn();
        }
    }

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
