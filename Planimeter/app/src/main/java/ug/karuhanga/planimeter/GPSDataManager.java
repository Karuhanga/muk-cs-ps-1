package ug.karuhanga.planimeter;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karuhanga on 7/30/17.
 * GPS Data collection and Analysis Class
 */

final class GPSDataManager implements LocationListener, Constants {
    private Context context;
    private LocationManager locationManager;
    private List<Location> locations;
    private int len_locations;
    private List<Location> turns;
    private int len_turns;

    GPSDataManager(Context context){
        this.context= context;
        this.locationManager= (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locations= new ArrayList<>();
        len_locations= 0;
        turns= new ArrayList<>();
        len_turns= 0;
    }

    //start recording location changes (possibly raise and throw security exception if GPS Access not permitted)
    void startRecording() throws SecurityException{
        this.locations= new ArrayList<>();
        len_locations= 0;
        this.turns= new ArrayList<>();
        len_turns= 0;
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time_sensitivity, distance_sensitivity, this);
    }

    //add last recorded location to turn list on turn
    void recordTurn(){
        if (len_locations>0) {
            Location turn= locations.get(len_locations - 1);
            if (len_turns>0){
                if (turns.get(len_turns-1).equals(turn)){
                    return;
                }
            }
            //TODO Remove Unnecessary Toast
            Toast.makeText(this.context, "Adding Turn: "+turn.toString(), Toast.LENGTH_SHORT).show();
            turns.add(turn);
            len_turns++;
        }
    }

    void stopRecording(){
        //de-register location change listener on collection complete
        this.locationManager.removeUpdates(this);
    }

    //Location Listener Methods
    @Override
    public void onLocationChanged(Location location) {
        //TODO Remove Unnecessary Toast
        Toast.makeText(this.context, "Adding Location: "+location.toString(), Toast.LENGTH_SHORT).show();
        locations.add(location);
        len_locations++;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
