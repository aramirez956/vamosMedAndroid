package com.medellin.vamosmed;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationService extends Service {

    JSONArray myArray = new JSONArray();
    JSONObject myObject = new JSONObject();

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Modelo modelo = (Modelo) getApplicationContext();
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double longitud = locationResult.getLastLocation().getLongitude();
                double latitud = locationResult.getLastLocation().getLatitude();

                Location locationA = new Location("punto A");
                    locationA.setLongitude(modelo.getLongi());
                    locationA.setLatitude(modelo.getLati());

                Location locationB = new Location("punto B");
                    locationB.setLongitude(longitud);
                    locationB.setLatitude(latitud);

                double distancia = locationA.distanceTo(locationB);
                distancia = Math.round(distancia * 10) / 10d;

                double Mts = distancia + modelo.getMts();
                modelo.setMts(Mts);

                Log.d("MTS", String.valueOf(Math.round(Mts * 10) / 10d));
                Log.d("LOCATION_UPDATE", latitud + ", " + longitud);
                double  x = longitud * 20037508.34 / 180;
                double  y = Math.log(Math.tan((90 + latitud) * Math.PI / 360)) / (Math.PI / 180);
                        y = y * 20037508.34 / 180;
                try {
                    myObject.put(String.valueOf('x'), x);
                    myObject.put(String.valueOf('y'), y);
                    myObject.put("dist", distancia);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                myArray.put(myObject);
               modelo.setJsonArrayViaje(myArray);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        String channelId = "Location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Viaje iniciado.");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Obteniendo Coordenadas...");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "VamosMed", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Este canal es usado por location service VamosMed");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
        myArray = new JSONArray();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Modelo modelo = (Modelo) getApplicationContext();
          if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}

