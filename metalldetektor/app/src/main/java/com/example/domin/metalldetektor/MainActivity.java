package com.example.domin.metalldetektor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    private static final int SCAN_QR_CODE_REQUEST_CODE = 0;

    private SensorManager sensorManager;
    private Sensor magnetSensor;
    private TextView textView;
    private ProgressBar progressBar;
    private Button buttonAdd;
    private View.OnClickListener buttonAddListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        textView = (TextView) findViewById(R.id.textView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
        if (magnetSensor == null) {
            throw new RuntimeException("Kein Magnetsensor gefunden");
        }
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAddListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
                    return;
                }
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);

            }
        };
        buttonAdd.setOnClickListener(buttonAddListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == magnetSensor) {
            float[] values = event.values;
            float x = values[0] * values[0];
            float y = values[1] * values[1];
            float z = values[2] * values[2];
            float value = (float) Math.sqrt(x + y + z);
//            textView.setText(Float.toString(value));
            progressBar.setProgress((int) value);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                textView.setText(intent.getStringExtra("SCAN_RESULT"));
                log(intent.getStringExtra("SCAN_RESULT"));
            }
        }
    }

    private void log(String qrcode) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }
//        TODO: QRCOODE to JSON
        JSONObject json = new JSONObject();
        try {
            json.accumulate("task", "Metalldetektor");
            json.accumulate("solution", qrcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra("ch.appquest.logmessage", json.toString());
        startActivity(intent);
    }
}
