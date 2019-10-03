package com.example.project4;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static DecimalFormat FORMAT = new DecimalFormat("#.###");


    LocationManager locationManager;
    LocationListener locationListener;
    SensorManager sensorManager;
    private LinearLayout staticCell;
    private UserLocation userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Size of list is 1, and this is the static cell
        ViewGroup viewGroup = findViewById(R.id.layout);
        TextView textView = new TextView(this);
        textView.setText("Current Reading");
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        staticCell = createVerticalLayout();

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(10, Color.BLACK);
        drawable.setCornerRadius(8);
        drawable.setColor(Color.LTGRAY);
        staticCell.setBackground(drawable);

        viewGroup.addView(textView);
        viewGroup.addView(staticCell);

        initGPS();
    }

    private LinearLayout createVerticalLayout() {
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(params);
        linearLayout.setPadding(25, 10, 10, 10);
        params.setMargins(10, 10, 10, 10);
        return linearLayout;
    }

    private void initGPS() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            int readingNumber = 1;

            @Override
            public void onLocationChanged(Location location) {
                LinearLayout baseLayout = findViewById(R.id.layout);

                if (userLocation == null || LocationUtils.distance(
                        location.getLatitude(),
                        location.getLongitude(),
                        userLocation.getOriginLocation().getLatitude(),
                        userLocation.getOriginLocation().getLongitude()) > UserLocation.LOCATION_CHANGE_RANGE) {

                    userLocation = new UserLocation(location);

                    TextView textView = new TextView(MainActivity.this);
                    textView.setTextSize(20);
                    textView.setText("Point #" + readingNumber);
                    textView.setGravity(Gravity.CENTER);
                    readingNumber++;
                    baseLayout.addView(textView, 2);

                    LinearLayout linearLayout = createVerticalLayout();

                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.RECTANGLE);
                    drawable.setStroke(10, Color.BLACK);
                    drawable.setCornerRadius(8);
                    drawable.setColor(Color.LTGRAY);
                    linearLayout.setBackground(drawable);

                    Location origin = userLocation.getOriginLocation();
                    for (TextView view : getFormatted(userLocation, origin.getLongitude(), origin.getLatitude(), origin.getAltitude())) {
                        linearLayout.addView(view); // Adds text views that contain cell information (to the cell)
                    }

                    linearLayout.removeViewAt(3);
                    baseLayout.addView(linearLayout, 3); // size is at least 1
                } else {
                    LinearLayout linearLayout = (LinearLayout) baseLayout.getChildAt(3);
                    TextView averageSensorReading = (TextView) linearLayout.getChildAt(3);
                    averageSensorReading.setText("Average Sensor Reading: " + FORMAT.format(userLocation.getAverage()) + " hPa");
                }

                // Static cell with information constantly changing
                LinearLayout linearLayout = (LinearLayout) baseLayout.getChildAt(1);
                linearLayout.removeAllViews();
                for (TextView textView : getFormatted(userLocation, location.getLongitude(), location.getLatitude(), location.getAltitude())) {
                    linearLayout.addView(textView);
                }
            }

            private TextView[] getFormatted(UserLocation userLocation, double lon, double lat, double alt) {
                String[] strings = new String[]{
                        "Longitude: " + lon + "°",
                        "Latitude: " + lat + "°",
                        "Altitude: " + FORMAT.format(alt) + " m",
                        "Sensor Reading: " + FORMAT.format(userLocation.getCurrentSensorValue()) + " hPa",
                        "Average Sensor Reading: " + FORMAT.format(userLocation.getAverage()) + " hPa",
                        "Location: " + getAddress(userLocation.getOriginLocation()),
                };

                TextView[] views = new TextView[6];
                for (int i = 0; i < views.length; i++) {
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText(strings[i]);
                    views[i] = textView;
                }

                return views;
            }

            private String getAddress(Location location) {
                Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;

                try {
                    addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "N/A";
                }

                if (addresses != null && addresses.size() > 0)
                    return addresses.get(0).getAddressLine(0);
                return "N/A";
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initGPS();
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                // set title
                alertDialogBuilder.setTitle("Permission not Granted");

                // set dialog message
                alertDialogBuilder
                        .setMessage("You did not grant permission, so the app cannot run. The app will close now.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, close current activity
                                MainActivity.this.finish();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] value = event.values;
        if (userLocation != null)
            userLocation.addToAverage(value[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}