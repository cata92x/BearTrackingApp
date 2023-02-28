package com.finaldegree.beartrackingapp;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.finaldegree.beartrackingapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private ActivityMapsBinding binding;
    private GoogleMap mMap;

    private List<Bear> bears;
    private Handler handler;
    private Runnable runnable;
    private Boolean bearReported;
    int delayRefresh = 10000;
    int delayDelete = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // on below line we are getting the
                // location name from search view.
                String location = searchView.getQuery().toString();

                // below line is to create a list of address
                // where we will store the list of all address.
                List<Address> addressList = null;

                // checking if the entered location is null or not.
                if (location != null || location.equals("")) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    if (addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    } else {
                        Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        handler = new Handler();
        bears = new ArrayList<>();

        ImageButton imageButton = findViewById(R.id.button_Alert);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportBear(view);
            }
        });

        TextView textView_Alerta = findViewById(R.id.textView_Alert);
        textView_Alerta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportBear(view);
            }
        });

        bearReported = false;

        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setPadding(0, 150, 0, 0);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        LatLng Ulmi = new LatLng(44.487252, 25.781897);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Ulmi, 18));
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delayRefresh);
                Toast.makeText(MapsActivity.this, "called every 10 seconds", Toast.LENGTH_SHORT).show();
                retriveBearCoordinates();
            }
        }, delayRefresh);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
        super.onPause();
    }


    private void retriveBearCoordinates() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.myLooper());

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        executor.execute(() -> {
            try {
                URL serverURL = new URL("http://192.168.0.87:3000/bears");
                HttpURLConnection httpURLConnection = (HttpURLConnection) serverURL.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String buffer = "";
                while ((buffer = bufferedReader.readLine()) != null) {
                    stringBuilder.append(buffer);
                }

                JSONArray bearsJSONArray = new JSONArray(stringBuilder.toString());
                for (int i = 0; i < bearsJSONArray.length(); i++) {
                    JSONObject bearJSONObject = bearsJSONArray.getJSONObject(i);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = dateFormat.parse(bearJSONObject.getString("updatedAt"));

                    Bear bear = new Bear(
                            bearJSONObject.getInt("code"),
                            bearJSONObject.getDouble("latitude"),
                            bearJSONObject.getDouble("longitude"),
                            date);

                    Boolean shouldAdd = true;
                    for (Bear bearIndex : bears) {
                        if (bearIndex.getCode() == bear.getCode()) {
                            bears.set(bears.indexOf(bearIndex), bear);
                            shouldAdd = false;
                            break;
                        }
                    }
                    if (shouldAdd == true) {
                        bears.add(bear);
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (Bear bear : bears) {
                            LatLng bearLocation = new LatLng(bear.getLatitude(), bear.getLongitude());
                            Circle circle = mMap.addCircle(new CircleOptions()
                                    .center(bearLocation)
                                    .radius(5) // in meters
                                    .strokeWidth(5)
                                    .strokeColor(Color.RED)
                                    .fillColor(Color.argb(50, 0, 0, 255)));

                            float[] distance = new float[1];
                            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), bear.getLatitude(), bear.getLongitude(), distance);

                            // Do something with the distance
                            Toast.makeText(MapsActivity.this, "Distance:" + distance[0], Toast.LENGTH_SHORT).show();
                            if (distance[0] < 100) {
                                Toast.makeText(MapsActivity.this, "ALERT!", Toast.LENGTH_SHORT).show();
                            }

                            // Remove the circle after x minutes
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    circle.remove();
                                }
                            }, delayDelete);
                        }
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private void reportBear(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            if (!bearReported) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (userLocation != null) {
                    LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Bear spotted here"));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            marker.remove();
                            bearReported = false;
                        }
                    }, delayDelete);
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
                bearReported = true;
            } else {
                Toast.makeText(this, "You have to wait before you can report another bear", Toast.LENGTH_SHORT).show();
            }
        }
    }
}