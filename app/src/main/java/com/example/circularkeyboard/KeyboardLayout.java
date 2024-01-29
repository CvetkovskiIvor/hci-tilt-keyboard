package com.example.circularkeyboard;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.TextView;

public class KeyboardLayout extends FrameLayout
        implements SensorEventListener {

    int margin = 0;
    DisplayMetrics dm;
    Context context;
    LayoutInflater inflater;
    InputConnection inputConn;
    int availableHeight;
    int availableWidth;
    float KEYBOARD_HEIGHT_SCALE_FACTOR;

    private RollingMarbleView marbleView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    int sensorX, sensorY;
    private float SENSOR_THRESHOLD_X = 1.0f;
    private float SENSOR_THRESHOLD_Y = 1.0f;
    private boolean ALLOW_CONSECUTIVE_CONFIRMATION = false;
    private int DWELL_TIME_MS = 1000;
    boolean keyConfirmed = false;
    boolean dwellTimerActive = false;
    private long dwellStart = 0;
    private float POINTER_SPEED_UP = 1.0f;
    int newX, newY, xdiff, ydiff, diff;
    double angle;
    TextView myTV;
    int degint = 0;
    private int RADIUS_MARGIN = 65;
    private String target_letter ="";

    public KeyboardLayout(LayoutInflater inflater,
                          Context ctx,
                          InputConnection inputConn,
                          float kbHeight) {
        super(ctx);
        this.context = ctx;
        this.inflater = inflater;
        this.inputConn = inputConn;

        // Calculate dimensions of the keyboard, relative to the display size:
        this.KEYBOARD_HEIGHT_SCALE_FACTOR = kbHeight;
        this.margin = (int) (context.getResources().getDisplayMetrics().density + 0.5f);
        this.dm = context.getResources().getDisplayMetrics();
        float kbScale = KEYBOARD_HEIGHT_SCALE_FACTOR * dm.heightPixels;

        //this.availableHeight = (int) kbScale;
        this.availableWidth = dm.widthPixels;
        this.availableHeight = dm.widthPixels;

        // Sensor support: register sensor (accelerometer) listener
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // inflate your layout (keyboard_layout.xml):
        inflater.inflate(R.layout.keyboard_layout, this);

        // apply keyboard height
        FrameLayout basicLL = (FrameLayout) this.findViewById(R.id.middlePart);
        LayoutParams params = (LayoutParams) basicLL.getLayoutParams();
        params.height = this.availableHeight;
        basicLL.setLayoutParams(params);

        marbleView = new RollingMarbleView(ctx, 50, this.availableWidth, this.availableHeight);
        basicLL.addView(marbleView);
        myTV = (TextView) this.findViewById(R.id.textView);
    }

    private String getLetterForAngle(int angle) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int segmentSize = 360 / alphabet.length();

        // Reverse the direction by subtracting the angle from 360
        int adjustedAngle = 360 - angle;

        int index = adjustedAngle / segmentSize;
        index = index % alphabet.length(); // Ensure index stays within bounds
        return String.valueOf(alphabet.charAt(index));
    }


    // Apply character to the input stream
    private void commitText(String s){
        if (inputConn != null)
            inputConn.commitText(s, 1);
    }

    // Apply action to the input stream
    private void commitDeletion(){
        if (inputConn != null)
                inputConn.deleteSurroundingText(1, 0);
    }

    // We need this so as to save battery, i.e. avoid battery drain!
    public void removeListener(){
       sensorManager.unregisterListener(this, accelerometer);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = (int) (sensorEvent.values[0] * POINTER_SPEED_UP);
            sensorY = (int) (sensorEvent.values[1] * POINTER_SPEED_UP);

            newX = marbleView.startX() - sensorX;
            newY = marbleView.startY() + sensorY;
            xdiff = Math.abs(marbleView.getMarbleCenterW() - newX);
            ydiff = Math.abs(marbleView.getMarbleCenterH() - newY);
            diff = (int)Math.round(Math.sqrt(xdiff*xdiff + ydiff*ydiff));
            angle = Math.toDegrees(Math.atan2(ydiff, xdiff));

            if (newX >= marbleView.getMarbleCenterW()
                    && (newY <= marbleView.getMarbleCenterH())) {
                // q1
                degint = (int)Math.round(angle);
            } else if (newX < marbleView.getMarbleCenterW()
                    && (newY <= marbleView.getMarbleCenterH())) {
                //q2
                degint = 180 - (int)Math.round(angle);

            } else if (newX < marbleView.getMarbleCenterW()
                    && (newY > marbleView.getMarbleCenterH())) {
                //q3
                degint = 180 + (int)Math.round(angle);

            } else if (newX >= marbleView.getMarbleCenterW()
                    && (newY > marbleView.getMarbleCenterH())) {
                //q4
                degint = 360 - (int) Math.round(angle);
            }

            if (!((degint >= 355 && degint <= 360) || (degint >= 0 && degint <= 10))) {
                target_letter = getLetterForAngle(degint);
            } else {
                // Special cases for space and delete
                if ((degint >= 355 && degint <= 360) || (degint >= 0 && degint <= 10)) {
                    target_letter = "SP";
                } else if (degint >= 170 && degint <= 190) {
                    target_letter = "DEL";
                }
            }

            myTV.setText(target_letter);

            // Check for pointer movement according to the set sensor thresholds:
            if ((Math.abs(sensorEvent.values[0]) < SENSOR_THRESHOLD_X) &&
                    (Math.abs(sensorEvent.values[1]) < SENSOR_THRESHOLD_Y)) {

                // Pointer is STEADY
                // Do not proceed if key has been submitted just before:
                if (!ALLOW_CONSECUTIVE_CONFIRMATION) {
                    if (keyConfirmed) return;
                }

                if (!dwellTimerActive) {
                    // Start measuring time in steady position:

                    dwellStart = SystemClock.elapsedRealtime();
                    dwellTimerActive = true;
                } else {
                    // Check if dwell time has passed:
                    long dwellStop = SystemClock.elapsedRealtime();
                    if ((dwellStop - dwellStart) > DWELL_TIME_MS) {

                        // dwell time passed => confirm/submit key:
                        if ((!target_letter.equals("")) &&
                            (diff > this.availableWidth/2 - RADIUS_MARGIN - 100)) {

                            if (target_letter.equals("SP")) {
                                commitText(" ");
                            } else if (target_letter.equals("DEL")) {
                                commitDeletion();
                            } else {
                                commitText(target_letter);
                            }
                        }

                        keyConfirmed = true;
                        dwellTimerActive = false;
                    }
                }
            } else
            {
                // Pointer is in MOVING state
                keyConfirmed= false;
                dwellTimerActive = false;

                if (diff < this.availableWidth/2 - RADIUS_MARGIN) {
                    marbleView.setStartX(marbleView.startX() - sensorX);
                    marbleView.setStartY(marbleView.startY() + sensorY);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}