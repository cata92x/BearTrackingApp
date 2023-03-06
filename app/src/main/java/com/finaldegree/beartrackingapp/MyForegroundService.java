package com.finaldegree.beartrackingapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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

public class MyForegroundService extends Service {
    private List<Bear> bears;
    private Boolean notified = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bears = new ArrayList<>();
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.myLooper());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            Log.e("Service", "Service is running...");
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
                                                bearJSONObject.getDouble("longitude"), date);

                                        Boolean shouldAdd = true;
                                        if (bears.contains(bear)) {

                                        }
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

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if (userLocation != null) {
                                        for (Bear bear : bears) {
                                            float[] distance = new float[1];
                                            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), bear.getLatitude(), bear.getLongitude(), distance);
                                            if (distance[0] < 200) {
                                                notified = true;

                                                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                                                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(
                                                        getApplicationContext(),
                                                        0,
                                                        myIntent,
                                                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                                final String CHANNELID = "Foreground Service ID2";
                                                NotificationChannel channel = null;
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    channel = new NotificationChannel(
                                                            CHANNELID,
                                                            CHANNELID,
                                                            NotificationManager.IMPORTANCE_LOW
                                                    );
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    getSystemService(NotificationManager.class).createNotificationChannel(channel);
                                                }
                                                Notification.Builder notification = null;

                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    notification = new Notification.Builder(getApplicationContext(), CHANNELID)
                                                            .setSmallIcon(R.drawable.alert)
                                                            .setContentText("There is a bear nerby!")
                                                            .setContentTitle("Alert!")
                                                            .setAutoCancel(true)
                                                            .setPriority(Notification.PRIORITY_MAX)
                                                            .setWhen(System.currentTimeMillis())
                                                            .setContentIntent(pendingIntent);
                                                }

                                                // display the notification
                                                NotificationManager notificationManager =
                                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                                notificationManager.notify(/*notification id*/ 1000, notification.build());
                                            }
                                        }
                                    } else {
                                        Toast.makeText(MyForegroundService.this, "no gps", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            try {
                                if (notified) {
                                    Thread.sleep(15000);
                                } else {
                                    Thread.sleep(2000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();

        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification.Builder notification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), CHANNELID)
                    .setSmallIcon(R.drawable.bear)
                    .setContentText("The Bear Tracking App is using your location")
                    .setContentTitle("Bear Tracking App")
                    .setContentIntent(pendingIntent);
        }
        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
