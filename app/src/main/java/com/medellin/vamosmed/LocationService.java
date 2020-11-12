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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.TimerTask;

public class LocationService extends Service {

    JSONArray myArray = new JSONArray();
    JSONArray myArrayProgreso = new JSONArray();

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Modelo modelo = (Modelo) getApplicationContext();
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double longitud = locationResult.getLastLocation().getLongitude();
                double latitud = locationResult.getLastLocation().getLatitude();

               /* //Enviar datos a activity principal
                String coor = String.valueOf(longitud + latitud);
                Intent localIntent = new Intent(modelo.ACTION_START_LOCATION_SERVICE)
                        .putExtra("coordenadas...", coor);
                LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(localIntent);*/


                //Medir distancia entre posicion actual y posicion inicial
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

                //convertir coordenadas a WebMercator
                //Log.d("MTS", String.valueOf(Math.round(Mts * 10) / 10d));
                //Log.d("LOCATION_UPDATE", latitud + ", " + longitud);
                double  x = longitud * 20037508.34 / 180;
                double  y = Math.log(Math.tan((90 + latitud) * Math.PI / 360)) / (Math.PI / 180);
                        y = y * 20037508.34 / 180;
                try {
                    JSONObject myObjectProgreso = new JSONObject();
                    JSONObject myObject = new JSONObject();
                    myObject.put(String.valueOf('x'), x);
                    myObject.put(String.valueOf('y'), y);
                    //myObject.put("dist", distancia);
                    myObjectProgreso.put("coorX",longitud);
                    myObjectProgreso.put("coorY",latitud);
                    myArray.put(myObject);
                    modelo.setJsonArrayViaje(myArray);
                    myArrayProgreso.put(myObjectProgreso);
                    modelo.setJsonArrayViajeProgreso(myArrayProgreso);
                    //Log.d("LOCATION", myArrayProgreso.toString());

                    String coor = String.valueOf(longitud + latitud);
                    Intent localIntent = new Intent(modelo.ACTION_START_LOCATION_SERVICE)
                            .putExtra("coordenadas", modelo.getJsonArrayViajeProgreso().toString());
                    LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(localIntent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
        locationRequest.setInterval(3000);
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
        myArrayProgreso = new JSONArray();
        Modelo modelo = (Modelo) getApplicationContext();
        Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                .putExtra("coordenadas...", "coor");
        LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(localIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
          if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals("startLocationsService")){
                    startLocationService();
                } else if (action.equals("stopLocationsService")) {
                    //Toast.makeText(getApplicationContext(),myArray.toString(),Toast.LENGTH_SHORT).show();
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("d", "finish");
    }
}

