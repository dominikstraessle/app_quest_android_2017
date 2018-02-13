package com.github.dominikstraessle.schatzkarte_01;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final String PREFS_NAME = "POINTS_N";
    public static final String PREFS_KEY = "POINTS_K";

    private MainActivity mainActivity = this;
    private IMapController controller;
    private MapView mapView;
    private LocationManager locationManager;
    private String provider;
    private TextView txtLatitude, txtLongitude;
    private ItemizedIconOverlay<OverlayItem> iconOverlay;
    private ArrayList<OverlayItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        txtLatitude = findViewById(R.id.textViewLatidtude);
        txtLongitude = findViewById(R.id.textViewLongitude);

        initActivity();
        try {
            loadJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initOverlay();

    }

    //nimmt alle einstellungen bezüglich Overlay vor
    private void initOverlay() {
        //das icon, welches angezeigt werden soll bei einem klick
        Drawable marker = getResources().getDrawable(R.drawable.ic_skull, null);
        marker.setBounds(0, marker.getIntrinsicHeight(), marker.getIntrinsicWidth(), 0);

        //initialisiert das overlay
        iconOverlay = new ItemizedIconOverlay<OverlayItem>(items, marker,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast.makeText(mainActivity, item.getPoint().toString(), Toast.LENGTH_LONG).show();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        String toString = String.valueOf(item.getPoint().toString());
                        iconOverlay.removeItem(index);
                        Toast.makeText(mainActivity, "Wurde entfernt: " + toString, Toast.LENGTH_LONG).show();
                        mapView.invalidate();
                        return false;
                    }
                }, this);

        //fügt das overlay der MapView hinzu
        mapView.getOverlays().add(iconOverlay);
    }

    //nimmt alle einstellungen bezüglich MapView und Controller vor.
    private void initActivity() {
        //funktionen
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMaxZoomLevel(25);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setBuiltInZoomControls(true);

        //controller initialize
        controller = mapView.getController();
        controller.setZoom(18);

        //Check if GPS is enabled and Permissions are OK
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkGPS();

        //set Current Location
        setcurrentLocation(getCurrentLocation());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //wird vorher bereits ausgeführt
        } else {
            locationManager.requestLocationUpdates(provider, 1, 0.1F, this);
        }

        //für scrollen und zoomen
        mapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                // txtLatitude.setText(event.getX());
                //txtLongitude.setText(event.getY());
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });
    }

    //prüft ob das GPS an ist
    private void checkGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    //um eine Location zu setzen
    private void setcurrentLocation(Location location) {
        if (location != null) {
            controller.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
            txtLatitude.setText("\t" + String.valueOf(location.getLatitude()));
            txtLongitude.setText("\t" + String.valueOf(location.getLongitude()));
        } else {
            txtLatitude.setText("setCurrentLocation");
            txtLongitude.setText("location == null");
        }
    }

    //um die aktuelle Position zu bekommen.
    private Location getCurrentLocation() {
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        checkLocationPermission();//überprüft die Berechtigungen
        return locationManager.getLastKnownLocation(provider);
    }

    //MainActivity
    //holt ständig die aktuelle Position, wenn die Berechtigungen vorhanden sind.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 1, 0.1F, this);
                    }

                } else {

                    txtLatitude.setText("onRequestPermissionsResult");
                    txtLongitude.setText("Fehlende Berechtigung");

                }
            }
        }
    }

    //https://stackoverflow.com/questions/40142331/how-to-request-location-permission-on-android-6
    //prüft ob die berechtigungen vorhanden sind und holt sie, wenn sie es nicht sind.
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Will berechtigung")
                        .setMessage("Will berechtigung")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        setcurrentLocation(location);
    }

    //LocationListener
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    //LocationListener
    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }

    //LocationListener
    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    //OptionsMenu für Set/Log/Save etc...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add("Log");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    //Log
                    logJSON(createJSON());
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                return false;
            }
        });
        MenuItem menuItem1 = menu.add("Set");
        menuItem1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Location location = getCurrentLocation();
                iconOverlay.addItem(new OverlayItem("MarkPoint", location.toString(), new GeoPoint(location.getLatitude(), location.getLongitude())));
                mapView.invalidate();
                setcurrentLocation(location);
                return false;
            }
        });
        MenuItem menuItem2 = menu.add("Save");
        menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    saveJSON(createJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //laden beim start
    private void loadJSON() throws JSONException {
        SharedPreferences preferencesReader = getSharedPreferences(PREFS_NAME, MainActivity.MODE_PRIVATE);
        String jsonString = preferencesReader.getString(PREFS_KEY, null);
        if (jsonString == null) {
            Toast.makeText(this, "Keine Punkte", Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray array = jsonObject.getJSONArray("points");
        for (int i = 0; i < array.length(); i++) {
            JSONObject point = (JSONObject) array.get(i);
            items.add(new OverlayItem("MarkPoint", "Point", new GeoPoint(Double.parseDouble(point.getString("lat")) / Math.pow(10, 6), Double.parseDouble(point.getString("lon")) / Math.pow(10, 6))));
        }
    }

    //speichern bei ondestroy und save
    private void saveJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            Toast.makeText(this, "Keine Punkte", Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences preferencesReader = getSharedPreferences(PREFS_NAME, MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencesReader.edit();
        editor.putString(PREFS_KEY, jsonObject.toString());
        editor.apply();
    }

    private JSONObject createJSON() throws JSONException {
        JSONArray array = new JSONArray();
        if (items.size() < 1) return null;
        for (OverlayItem item : items) {
            JSONObject object = new JSONObject();
            object.put("lat", (int) Math.round(item.getPoint().getLatitude() * Math.pow(10, 6)));
            object.put("lon", (int) Math.round(item.getPoint().getLongitude() * Math.pow(10, 6)));
            array.put(object);
        }
        JSONObject json = new JSONObject();
        json.put("task", "Schatzkarte");
        json.put("points", array);
        return json;
    }

    //loggen
    private void logJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            Toast.makeText(this, "Keine Punkte", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) { // -> Logbuch App nicht installiert.
            Toast.makeText(this, "Logbuch nicht installiert", Toast.LENGTH_LONG).show(); //Toast ist ein kleiner Aufgepoppert hinweis.
            return;
        }
        intent.putExtra("ch.appquest.logmessage", jsonObject.toString());

        startActivity(intent);
    }


    //beim wiederaufruf
    @Override
    public void onResume() {
        super.onResume();

        checkGPS();
        checkLocationPermission();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

    }

    @Override
    protected void onDestroy() {
        try {
            saveJSON(createJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
