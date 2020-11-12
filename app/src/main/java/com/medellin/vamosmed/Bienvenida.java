package com.medellin.vamosmed;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.medellin.vamosmed.ui.RoundedCornersTransform;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

public class Bienvenida extends AppCompatActivity{
    TextView saludo;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        Toolbar toolbar = findViewById(R.id.toolbarBienvenida);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Modelo modelo = (Modelo) getApplicationContext();
        saludo = (TextView) findViewById(R.id.saludo);
        saludo.setText(modelo.getNombre());
        imageView = findViewById(R.id.imageView4);

        if(modelo.getFotoPerfil().equals("")) {
            Picasso.get().load(R.drawable.ic_perfil).placeholder(R.drawable.ic_perfil).error(R.drawable.ic_perfil).into(imageView);
        } else {
            Picasso.get().load(modelo.getFotoPerfil()).placeholder(R.drawable.ic_perfil).error(R.drawable.ic_perfil).into(imageView);
        }

        //Picasso.get().load(R.drawable.ic_perfil).transform(new RoundedCornersTransform()).into(imageView);

        //CARGAR ALERTAS DE APROBACIONES CUANDO ES JEFE
        //CARGAR ALERTAS DE CARROS CUANDO ELIJA VIAJAR EN CARRO

        /*Bundle bundle = this.getIntent().getExtras();
        if(bundle!=null){
            String nombre = bundle.getString("nombre");
            saludo.setText(""+nombre);
            Picasso.get()
                    .load("https://www.jumpstarttech.com/files/2018/08/Network-Profile.png")
                    // .resize(50,50)
                    .placeholder(R.drawable.ic_perfil)
                    .error(R.drawable.ic_perfil)
                    .into(imageView);
            modelo.setFotoPerfil("https://www.jumpstarttech.com/files/2018/08/Network-Profile.png");
        }else{
            saludo.setText(modelo.getNombre());
            Picasso.get()
                    .load(modelo.getFotoPerfil())
                    // .resize(50,50)
                    .placeholder(R.drawable.ic_perfil)
                    .error(R.drawable.ic_perfil)
                    .into(imageView);
        }*/

    }

    public void onClickSalir(View view) {
        cerrarAplicacion();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cerrarAplicacion();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cerrarAplicacion() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.logo_vamosmed)
                .setTitle("¿Desea cerrar la sesión?")
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //android.os.Process.killProcess(android.os.Process.myPid()); //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                        SharedPreferences sesion = getSharedPreferences("credencial", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editar = sesion.edit();
                        editar.putString("user","Sin info...");
                        editar.putString("pass", "Sin info...");
                        editar.apply();
                        //Abrimos la Activity Main
                        Intent salida = new Intent(Bienvenida.this, MainActivity.class);
                        startActivity(salida);
                        Toast.makeText(getApplicationContext(),"La sesion ha terminado",Toast.LENGTH_SHORT).show();
                        //Matamos la Activity de Bienvenida
                        finish();
                    }
                }).show();
    }

    public void onClickViajeBici(View view) {
        Modelo modelo = (Modelo) getApplicationContext();
        Intent viajar = new Intent(Bienvenida.this, ViajeBici.class);
        startActivity(viajar);
        modelo.setIdMedio("1");
        //Toast.makeText(getApplicationContext(),"Iniciando el contenido",Toast.LENGTH_SHORT).show();
        //Matamos la Activity de Bienvenida
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickMiPerfil(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Modelo modelo = (Modelo) getApplicationContext();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            consultarDatosPerfil(modelo.getUsuario());
            Intent perfil = new Intent(Bienvenida.this, Perfil.class);
            startActivity(perfil);
            Toast.makeText(getApplicationContext(),"Mi Perfil",Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // No hay conexión a Internet en este momento
            new AlertDialog.Builder(this)
                    .setTitle("Información")
                    .setMessage("Esta aplicación requiere una conexión de datos activa")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //android.os.Process.killProcess(android.os.Process.myPid());
                            //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                            //opcion en caso de ser positiva la respuesta por parte del usuario
                            Toast.makeText(getApplicationContext(),"Sin conexión a internet",Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void consultarDatosPerfil(String usuario) {
        Modelo modelo = (Modelo) getApplicationContext();
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String user = "'P1':'"+usuario+"'"+"";
            String strSql = "{'SQL':'SQL_MVT_CONSULTAR_DATOS_BICI','N':1,'DATOS':{"+user+"}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            JSONArray jsonArray = new JSONArray(response);
            if(jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    modelo.setIdBici(object.getInt("ID_BICI"));
                    modelo.setSerialBici(object.getString("SERIAL"));
                    modelo.setIdSecretaria(object.getInt("ID_SECRETARIA"));
                    modelo.setIdSubsecretaria(object.getInt("ID_SUBSECRETARIA"));

                    if(object.getString("CORREO_JEFE").indexOf("@medellin.gov.co") != -1) {
                        modelo.setCorreoJefe(object.getString("CORREO_JEFE").replace("@medellin.gov.co", ""));
                    } else {
                        modelo.setCorreoJefe(object.getString("CORREO_JEFE"));
                    }

                    String foto = new String(Base64.getDecoder().decode(object.getString("URL_FOTO")), StandardCharsets.UTF_8);
                    if(URLUtil.isValidUrl(foto)) {
                        modelo.setFotoBici(foto);
                    } else {
                        modelo.setFotoBici("");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}