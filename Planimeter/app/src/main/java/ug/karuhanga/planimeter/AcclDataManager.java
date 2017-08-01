package ug.karuhanga.planimeter;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

/**
 * Created by
 * @author Karuhanga Lincoln
 * on 7/31/17.
 * Accl Data collection and Analysis Class
 */

final class AcclDataManager implements SensorEventListener, StepTakenListener {
    private Context context;
    private SensorManager acclManager;
    private Sensor acclSensor;
    private int stepCount;
    private float previousReadings[]= {0.0f, 0.0f, 0.0f};

    AcclDataManager(Context context){
        this.context= context;
        this.stepCount= 0;
        this.acclManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        acclSensor= acclManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void stepTaken(){
        //TODO: do stuff with the fact that a step was taken
        //Run on main thread for UI interactions
        stepCount++;
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO: Remove Unnecessary Toast
                Toast.makeText(context, "Step "+String.valueOf(stepCount), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void startRecording(){
        //TODO: clear previous data and start on clean slate
        boolean result= acclManager.registerListener(this, acclSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (result){
            Toast.makeText(context, "Recording", Toast.LENGTH_SHORT).show();
        }
        //TODO: In case commence record fails
    }

    void stopRecording(){
        acclManager.unregisterListener(this, acclSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if (sensorEvent.values[0]==previousReadings[0] && sensorEvent.values[1]==previousReadings[1] && sensorEvent.values[2]==previousReadings[2]){
                //return if no relevant changes(g(accl due to gravity is being detected))
                return;
            }
            else{
                for (int i = 0; i < 3; i++) {
                    previousReadings[i]= sensorEvent.values[i];
                }
            }
            new StepDetector(sensorEvent, context, this).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //TODO: Vary StepDetector Accuracy based on this reading
    }
}