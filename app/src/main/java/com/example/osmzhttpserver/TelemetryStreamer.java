package com.example.osmzhttpserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TelemetryStreamer {
    Context context;
    SensorManager sensorManager;
    JSONObject telemetryData = new JSONObject();
    LocationHelper locationHelper;

    TelemetryStreamer(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.locationHelper = new LocationHelper(context);
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            JSONObject sensorData = new JSONObject();
            try {
                sensorData.put("name", event.sensor.getName());
                sensorData.put("values", new JSONArray(event.values));
                telemetryData.put(event.sensor.getName(), sensorData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    private void registerSensors() {
        List<Sensor> sensorList = this.sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            this.sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public JSONObject getTelemetryData(){
        if (telemetryData.length() == 0) {
            this.registerSensors();
        }

        locationHelper.requestLocation(location -> {
            Log.d("POSITION", location.getLatitude() + " " + location.getLongitude());
            try {
                JSONArray locationArray = new JSONArray();
                locationArray.put(location.getLatitude());
                locationArray.put(location.getLongitude());

                JSONObject locationData = new JSONObject();
                locationData.put("name", "Location");
                locationData.put("values", locationArray);

                telemetryData.put("Location", locationData);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        return this.telemetryData;
    }
}
