package com.medellin.vamosmed.ui.viaje;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.medellin.vamosmed.Bienvenida;
import com.medellin.vamosmed.LocationService;
import com.medellin.vamosmed.MainActivity;
import com.medellin.vamosmed.Modelo;
import com.medellin.vamosmed.R;
import com.medellin.vamosmed.conexion;
import com.medellin.vamosmed.ui.estadisticas.EstadisticasFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.concurrent.ExecutionException;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class ViajeFragment extends Fragment {

    /*Se declara una variable de tipo LocationManager encargada de proporcionar acceso al servicio de localización del sistema.*/
    private LocationManager locManager;
    /*Se declara una variable de tipo Location que accederá a la última posición conocida proporcionada por el proveedor.*/
    private Location loc;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private HomeViewModel homeViewModel;

    View vista;
    TextView tvLatitud, tvLongitud;
    ProgressDialog progressDialog;//dialogo cargando
    ImageView ubicarmeMapa, ayudaMpa, aprobar;
    Button buttonIniciarViaje, buttonFinalizarViaje;

    private GraphicsOverlay mGraphicsOverlay;
    private MapView mMapView;
    private LocationDisplay mLocationDisplay;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_viaje, container, false);

        progressDialog = ProgressDialog.show(getContext(), "Vamos Med", "Cargando mapa...");

        tvLatitud = vista.findViewById(R.id.tvLatitud);
        tvLongitud = vista.findViewById(R.id.tvLongitud);
        ubicarmeMapa = vista.findViewById(R.id.ubicarmeMapa);
        aprobar = vista.findViewById(R.id.aprobar);
        buttonIniciarViaje = vista.findViewById(R.id.iniciaViaje);
        buttonFinalizarViaje = vista.findViewById(R.id.finalizarViaje);
        // *** ADD para el mapa***
        mMapView = vista.findViewById(R.id.mapView);
        Modelo modelo = (Modelo) getActivity().getApplicationContext();

        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //Toast.makeText(getContext(), "sin fragmento", Toast.LENGTH_SHORT).show();
            setupMap(); // Mapa
            addServicio(); // Cargar servicio
            setupLocationDisplay();// Localización*/

            ubicarmeMapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLocationDisplay();
                    Toast.makeText(getContext(), "Obteniendo ubicación actual", Toast.LENGTH_SHORT).show();
                }
            });

            aprobar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLocationDisplay();
                    Toast.makeText(getContext(), "Obteniendo ubicación actual", Toast.LENGTH_SHORT).show();
                }
            });

            buttonIniciarViaje.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    onClickIniciarViaje(v);
                }
            });

            buttonFinalizarViaje.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    stopLocationService();
                }
            });

            if (isLocationServiceRunning()) {
                buttonIniciarViaje.setVisibility(View.INVISIBLE);
                buttonFinalizarViaje.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Viaje iniciado y obteniendo coordenadas", Toast.LENGTH_SHORT).show();
            } else {
                buttonIniciarViaje.setVisibility(View.VISIBLE);
                buttonFinalizarViaje.setVisibility(View.INVISIBLE);
                //Toast.makeText(this,"Cargando mapa",Toast.LENGTH_SHORT).show();
            }

            if(modelo.getEsJefe() > 0) {
                aprobar.setVisibility(View.VISIBLE);
            } else {
                aprobar.setVisibility(View.INVISIBLE);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 4000L);

            // Filtro de acciones que serán alertadas
            IntentFilter filter = new IntentFilter(
                    modelo.ACTION_START_LOCATION_SERVICE);
            filter.addAction(modelo.ACTION_STOP_LOCATION_SERVICE);

            // Crear un nuevo ResponseReceiver
            ResponseReceiver receiver = new ResponseReceiver();
            // Registrar el receiver y su filtro
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
            return vista;
        }else {
            String tag = getActivity().getSupportFragmentManager().getBackStackEntryAt(getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

            if (tag.equals("Dashboard")) {
                //Toast.makeText(getContext(), "fragmento " + tag, Toast.LENGTH_SHORT).show();
                Fragment ViajeFragment = new ViajeFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, ViajeFragment)
                        .addToBackStack("viaje")
                        .commit();
            }
            setupMap(); // Mapa
            addServicio(); // Cargar servicio
            setupLocationDisplay();// Localización*/

            ubicarmeMapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLocationDisplay();
                    Toast.makeText(getContext(), "Obteniendo ubicación actual", Toast.LENGTH_SHORT).show();
                }
            });

            aprobar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLocationDisplay();
                    Toast.makeText(getContext(), "Obteniendo ubicación actual", Toast.LENGTH_SHORT).show();
                }
            });

            buttonIniciarViaje.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    onClickIniciarViaje(v);
                }
            });

            buttonFinalizarViaje.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    stopLocationService();
                }
            });

            if (isLocationServiceRunning()) {
                buttonIniciarViaje.setVisibility(View.INVISIBLE);
                buttonFinalizarViaje.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Viaje iniciado y obteniendo coordenadas", Toast.LENGTH_SHORT).show();
            } else {
                buttonIniciarViaje.setVisibility(View.VISIBLE);
                buttonFinalizarViaje.setVisibility(View.INVISIBLE);
                //Toast.makeText(this,"Cargando mapa",Toast.LENGTH_SHORT).show();
            }

            if(modelo.getEsJefe() > 0) {
                aprobar.setVisibility(View.VISIBLE);
            } else {
                aprobar.setVisibility(View.INVISIBLE);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 4000L);

            // Filtro de acciones que serán alertadas
            IntentFilter filter = new IntentFilter(
                    modelo.ACTION_START_LOCATION_SERVICE);
            filter.addAction(modelo.ACTION_STOP_LOCATION_SERVICE);

            // Crear un nuevo ResponseReceiver
            ResponseReceiver receiver = new ResponseReceiver();
            // Registrar el receiver y su filtro
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
            return vista;
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {
        // Modelo modelo = (Modelo) getActivity().getApplicationContext();

        // Sin instancias
        private ResponseReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(getContext(), "VamosMed obteniendo coor...",Toast.LENGTH_SHORT).show();
            switch (intent.getAction()) {
                case Modelo.ACTION_START_LOCATION_SERVICE:
                    try {
                        Bundle objectRecibido = intent.getExtras();
                        if(objectRecibido!=null){
                            //Toast.makeText(getContext(), "coordenadas...",Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getContext(), "coordenadas..."+ intent.getStringExtra("coordenadas"),Toast.LENGTH_SHORT).show();
                            //createGraphics();
                            mGraphicsOverlay = new GraphicsOverlay();
                            mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
                            Modelo modelo = (Modelo) getActivity().getApplicationContext();
                            JSONArray jsonArray = modelo.getJsonArrayViajeProgreso();
                            PointCollection polylinePoints = new PointCollection(SpatialReferences.getWgs84());
                            //Toast.makeText(getContext(), jsonArray.toString() ,Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                polylinePoints.add(new Point(object.getDouble("coorX"), object.getDouble("coorY")));
                            }
                            Polyline polyline = new Polyline(polylinePoints);
                            SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 5.0f);
                            Graphic polylineGraphic = new Graphic(polyline, polylineSymbol);
                            mGraphicsOverlay.getGraphics().add(polylineGraphic);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Toast.makeText(getContext(), "coordenadas..."+ intent.getStringExtra("coordenadas"),Toast.LENGTH_SHORT).show();
                    break;

                case Modelo.ACTION_STOP_LOCATION_SERVICE:

                    //Toast.makeText(getContext(), "detenido", Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    break;
            }

        }
    }

    private void setupMap() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                .putExtra("coordenadas...", "coor");
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);
        if (mMapView != null) {
            ArcGISRuntimeEnvironment.setLicense(getResources().getString(R.string.arcgis_license_key));
            Basemap.Type basemapType = Basemap.Type.STREETS_VECTOR;
            //Basemap.Type basemapType1 = Basemap.Type.NATIONAL_GEOGRAPHIC;
            double latitude = 6.2555782;
            double longitude = -75.6017194;
            int levelOfDetail = 10;
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, levelOfDetail);
            mMapView.setMap(map);
        }
    }

    private void addServicio() {
        String url = "https://www.medellin.gov.co/mapas/rest/services/ServiciosMovilidad/RutasTPublico/MapServer/1";
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
        ArcGISMap map = mMapView.getMap();
        map.getOperationalLayers().add(featureLayer);
    }

    //Establezca el modo de panorámica del mapa para la navegación de la brújula e inicie la visualización de la ubicación.
    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        /* ** ADD ** */
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(getContext(), requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getContext(), requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions((Activity) getContext(), requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        /* ** ADD ** */
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
        mLocationDisplay.startAsync();
    }

    //Verifique que el usuario haya otorgado permiso y, si es así, inicie la visualización de la ubicación.
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                starLocationService();
                mLocationDisplay.startAsync();
            } else {
                Toast.makeText(getContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickIniciarViaje(View view) {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            modelo.setLongi(Double.valueOf(loc.getLongitude()));
            modelo.setLati(Double.valueOf(loc.getLatitude()));

            tvLatitud.setText(String.valueOf(loc.getLatitude()));
            tvLongitud.setText(String.valueOf(loc.getLongitude()));
            //Toast.makeText(getContext(),"Longitud "+modelo.getLongi(),Toast.LENGTH_SHORT).show();

            starLocationService();
            buttonIniciarViaje.setVisibility(View.INVISIBLE);
            buttonFinalizarViaje.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void starLocationService() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        Intent intent = new Intent(getActivity(), LocationService.class);
        intent.setAction(modelo.ACTION_START_LOCATION_SERVICE);
        getActivity().startService(intent);
        Toast.makeText(getContext(), "Viaje iniciado", Toast.LENGTH_SHORT).show();
        modelo.setEstadoViaje(true);
        iniciarViajeDb();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void iniciarViajeDb() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        conexion connection = new conexion();
        String longitud = String.valueOf(modelo.getLongi());
        String latitud = String.valueOf(modelo.getLati());

        try {
            //Preparamos los parametros para enviarlos al servicio
            String ID_USUARIO = "[{\"P1\":\"" + modelo.getIdUsuario() + "\"" + "";
            String X_ORIGEN = "\"P2\":\"" + longitud + "\"" + "";
            String Y_ORIGEN = "\"P3\":\"" + latitud + "\"" + "";
            String ID_SEDE_DESTINO = "\"P4\":\"" + modelo.getIdSedeDestino() + "\"" + "";
            String ID_MEDIO = "\"P5\":\"" + modelo.getIdMedio() + "\"" + "}]";

            String strSql = "[{\"SQL\":\"SQL_MVT_GUARDAR_INICIO_VIAJE_BICI\", \"N\":5,\"DATOS\":" + ID_USUARIO + "," + X_ORIGEN + "," + Y_ORIGEN + "," + ID_SEDE_DESTINO + "," + ID_MEDIO + "}]";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/guardarDatos.hyg?str_sql=" + datosPost).get();
            //Manejo de repuesta desde el servicio Web
            Toast.makeText(getContext(), "Viaje iniciado : " + response, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopLocationService() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getActivity(), LocationService.class);
            intent.setAction(modelo.ACTION_STOP_LOCATION_SERVICE);
            getActivity().startService(intent);
            Toast.makeText(getContext(), "Localización Detenida", Toast.LENGTH_SHORT).show();
            buttonIniciarViaje.setVisibility(View.VISIBLE);
            buttonFinalizarViaje.setVisibility(View.INVISIBLE);
            modelo.setEstadoViaje(false);
            mMapView.getGraphicsOverlays().clear();
            consultarUltimoViajePendiente();

            // Crea el nuevo fragmento y la transacción.
            Fragment NotificationsFragment = new EstadisticasFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, NotificationsFragment);
            transaction.addToBackStack(null);

            // Commit a la transacción
            transaction.commit();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void consultarUltimoViajePendiente() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String usuario = "'P1':'" + modelo.getUsuario() + "'" + "";
            String strSql = "{'SQL':'SQL_MVT_SELECT_ULTIMO_VIAJE_PENDIENTE','N':1,'DATOS':{" + usuario + "}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql=" + datosPost).get();
            if (response.equals("[]")) {
                Intent main = new Intent(getActivity(), MainActivity.class);
                startActivity(main);
                //Matamos la Activity de Bienvenida
                getActivity().finish();
                Toast.makeText(getContext(), "Error en la sesión inicie nuevamente.", Toast.LENGTH_SHORT).show();
            } else {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String ID_VIAJE = object.getString("ID_VIAJE");
                    if (!ID_VIAJE.equals("null")) {
                        modelo.setIdViajeTem(ID_VIAJE);
                        //Toast.makeText(getApplicationContext(),"IdViaje # : "+ID_VIAJE,Toast.LENGTH_SHORT).show();
                    }
                }
                finalizarViajeDb();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void finalizarViajeDb() {
        Modelo modelo = (Modelo) getActivity().getApplicationContext();
        conexion connection = new conexion();
        try {
            int srid = 3857;
            double kms = modelo.getMts() / 1000;
            kms = Math.round(kms * 10) / 10d;

            JSONObject obj = new JSONObject();
            try {
                obj.put("srid", srid);
                obj.put("coord", modelo.getJsonArrayViaje());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray traking = new JSONArray();
            traking.put(obj);
            String trakingFinal = traking.toString().replace("\"", "'");

            //Preparamos los parametros para enviarlos al servicio
            String X_DESTINO = "[{\"P1\":\"" + modelo.getX() + "\"";
            String Y_DESTINO = "\"P2\":\"" + modelo.getY() + "\"";
            String ID_USUARIO_REGISTRA = "\"P3\":\"" + modelo.getIdUsuario() + "\"";
            String COORDENADAS = "\"P4\":\"" + trakingFinal + "\"";
            String KMS = "\"P5\":\"" + kms + "\"";
            String TIPO_VIAJE = "\"P6\":\"" + modelo.getTipoViaje() + "\"";
            String ID_VIAJE = "\"P7\":\"" + modelo.getIdViajeTem() + "\"}]";

            String strSql = "[{\"SQL\":\"SQL_MVT_ACTUALIZAR_VIAJE_PENDIENTE\", \"N\":7,\"DATOS\":" + X_DESTINO + "," + Y_DESTINO + "," + ID_USUARIO_REGISTRA + "," + COORDENADAS + "," + KMS + "," + TIPO_VIAJE + "," + ID_VIAJE + "}]";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/viajesApp.hyg?json=" + datosPost).get();
            if (response.equals("Ok")) {
                Toast.makeText(getContext(), "Viaje registrado. : " + response, Toast.LENGTH_SHORT).show();

                // Crea el nuevo fragmento y la transacción.
                Fragment NotificationsFragment = new EstadisticasFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, NotificationsFragment);
                transaction.addToBackStack(null);

                // Commit a la transacción
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "error... : " + response, Toast.LENGTH_SHORT).show();
            }
            //Log.d("traking", trakingFinal);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isLocationServiceRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return true;
            }
        } else {
            Intent bienvenida = new Intent(getActivity(), Bienvenida.class);
            startActivity(bienvenida);
            Toast.makeText(getContext(), "Cargando el contenido del perfil", Toast.LENGTH_SHORT).show();
            //Matamos la Activity de Bienvenida
            getActivity().finish();
            return true;
        }
        return true;
    }

}