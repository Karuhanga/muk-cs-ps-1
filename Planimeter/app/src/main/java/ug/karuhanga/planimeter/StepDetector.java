package ug.karuhanga.planimeter;

/**
 * Created by karuhanga on 8/1/17.
 */

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * helper class to filter data and determine when step taken
 * partially inspired by open-source work by
 * @link https://github.com/bagilevi/android-pedometer
 */

final class StepDetector extends Thread {
    private static float mLimit;
    private static float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static float mLastDirections[] = new float[3 * 2];
    private static float mLastExtremes[][] = {new float[3 * 2], new float[3 * 2]};
    private static float mLastDiff[] = new float[3 * 2];
    private static int mLastMatch = -1;
    private SensorEvent event;
    private Context context;
    private StepTakenListener listener;

    StepDetector(SensorEvent event, Context context, StepTakenListener listener) {
        this.event = event;
        this.context= context;
        this.listener = listener;
        mLimit = 5f;// TODO: vary this constant to find sweet spot
        mLastMatch = -1;
        int h = 480; // TODO: vary this constant to find sweet spot
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
    }

    public void setSensitivity(float sensitivity) {
        mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    }

    //public void onSensorChanged(int sensor, float[] values) {
    private void processData() {

        synchronized (context) {
            float vSum = 0;
            for (int i = 0; i < 3; i++) {
                final float v = mYOffset + event.values[i] * mScale[1];
                vSum += v;
            }

            int k = 0;
            float v = vSum / 3;

            float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
            if (direction == -mLastDirections[k]) {
                // Direction changed
                int extType = (direction > 0 ? 0 : 1); // minimum or maximum?
                mLastExtremes[extType][k] = mLastValues[k];
                float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                if (diff > mLimit) {
                    boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                    boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                    boolean isNotContra = (mLastMatch != 1 - extType);
                    mLastMatch = extType;

                    if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                        // Log.i(TAG, "step");
                        listener.stepTaken();
                    }
                } else {
                    mLastMatch = -1;
                }
                mLastDiff[k] = diff;
            }
            mLastDirections[k] = direction;
            mLastValues[k] = v;
        }
    }

    @Override
    public void run() {
        processData();
    }
}