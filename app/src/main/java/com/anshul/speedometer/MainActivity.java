package com.anshul.speedometer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import com.anshul.speedometer.R;
import com.anshul.speedometer.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {
    ActivityMainBinding binding;
    private LocationManager locationManager;
    int PERMISSION_REQUEST_CODE = 1;
    //private static final int OVERLAY_PERMISSION_REQUEST_CODE = 2;
    int max = 0;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(Color.WHITE);

        // Check permissions and request if necessary
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        createNotificationChannel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Speed Direction Channel";
            String description = "Channel for speed and direction updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void updateNotification(int speed, int max) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Speedometer")
                .setContentText("Speed: " + speed+"kmph" + ", Max Speed: " + max+"kmph")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            int speed; // Convert m/s to km/h

            speed = (int) (location.getSpeed() * 3.6);
            if (max < speed) max = speed;
            updateNotification(speed,max);
            binding.speedView.setSpeed(speed,0,null);
            //speedView.setWithTremble(false);
            binding.latitudeTxt.setText("Latitude \uD83C\uDF10\n"+location.getLatitude());
            binding.longitudeTxt.setText("Longitude \uD83C\uDF10\n"+location.getLongitude());
            binding.maxSpeedTxt.setText("Max Speed \uD83C\uDFC1\n"+max+"kmph");

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]); // orientation contains: azimut, pitch and roll
                azimuth = (azimuth + 360) % 360;

                String direction = getDirectionFromAzimuth(azimuth);
                binding.directionTxt.setText("Direction \uD83E\uDDED\n" + direction);
                //Log.d("Compass", "Azimuth: " + azimuth + " Direction: " + direction);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private String getDirectionFromAzimuth(float azimuth) {
        if (azimuth >= 337.5 || azimuth < 22.5) {
            return "North";
        } else if (azimuth >= 22.5 && azimuth < 67.5) {
            return "North East";
        } else if (azimuth >= 67.5 && azimuth < 112.5) {
            return "East";
        } else if (azimuth >= 112.5 && azimuth < 157.5) {
            return "South East";
        } else if (azimuth >= 157.5 && azimuth < 202.5) {
            return "South";
        } else if (azimuth >= 202.5 && azimuth < 247.5) {
            return "South West";
        } else if (azimuth >= 247.5 && azimuth < 292.5) {
            return "West";
        } else if (azimuth >= 292.5 && azimuth < 337.5) {
            return "North West";
        }
        return "Unknown";
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Recalculate orientation on configuration change
        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                String direction = getDirectionFromAzimuth(azimuth);
                binding.directionTxt.setText("Direction \uD83E\uDDED\n" + direction);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(MainActivity.this);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);

    }
}