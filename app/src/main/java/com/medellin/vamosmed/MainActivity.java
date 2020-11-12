package com.medellin.vamosmed;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.medellin.vamosmed.combos.ComboSecretaria;
import com.medellin.vamosmed.combos.Secretaria;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    TextView user, password;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = (TextView)findViewById(R.id.user);
        password = (TextView)findViewById(R.id.pass);
        cargarSesion();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickLogin(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            iniciarSesion(user.getText().toString(), password.getText().toString());
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
        //iniciarSesion(user.getText().toString(), password.getText().toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void cargarSesion() {
        SharedPreferences sesion = getSharedPreferences("credencial", Context.MODE_PRIVATE);
        String user = sesion.getString("user","Sin info...");
        String pass = sesion.getString("pass","Sin info...");
        /*nombre.setText("Su usuario es: "+user);
        cedula.setText("Su clave es: "+pass);*/
        if(user.equals("Sin info...")){

        }else{
            iniciarSesion(user,pass);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void iniciarSesion(String usuario, String contrasena) {
        String name = "Usuario Prueba VamosMed";
        crearSesionPrueba(usuario, contrasena, name);
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            //String id = usuario.getText().toString();
            //String pass = password.getText().toString();
            String id64 = Base64.getUrlEncoder().encodeToString(usuario.getBytes());
            String pass64 = Base64.getUrlEncoder().encodeToString(contrasena.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/validarUsuario.hyg?id="+id64+"&pass="+pass64).get();
            JSONObject jsonObject = new JSONObject(response.replace("[","").replace("]",""));

            //cedula.setText("Cedula: "+jsonObject.getString("cedula"));
            //email.setText("Email: "+jsonObject.getString("mail"));
            //puesto.setText("Puesto: "+jsonObject.getString("puesto"));
            if(jsonObject.toString().equals("{}")){
                name = "Usuario Prueba VamosMed";
                crearSesionPrueba(usuario, contrasena, name);
                Toast.makeText(getApplicationContext(),"Usuario o contraseña Invalidos",Toast.LENGTH_SHORT).show();
            }else{
                crearSesion(usuario, contrasena, jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Usuario o contraseña Invalidos",Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void crearSesionPrueba(String usuario, String contrasena, String nombre) {
        Modelo modelo = (Modelo) getApplicationContext();
        SharedPreferences sesion = getSharedPreferences("credencial", Context.MODE_PRIVATE);
        SharedPreferences.Editor editar = sesion.edit();
        editar.putString("user", usuario);
        editar.putString("pass", contrasena);
        editar.apply();

        modelo.setNombre(nombre);
        modelo.setUsuario(usuario);
        modelo.setFotoPerfil("");

        String user = sesion.getString("user","Sin info...");
        if(user.equals("Sin info...")){
            Toast.makeText(getApplicationContext(),"La sesion no se ha creado",Toast.LENGTH_SHORT).show();
        }else{
            if(isLocationServiceRunning()){
                //Creamos la intencion para ir a otra activity
                //pendiente
                /*Intent intent = new Intent(MainActivity.this, Mapa.class);
                startActivity(intent);
                //Matamos la Activity Main
                finish();*/
            }else{
                //Creamos la intencion para ir a otra activity
                Intent intent = new Intent(MainActivity.this, Bienvenida.class);
                //intent.putExtra("nombre", nombre);
                //Abrimos la Activity de Bienvenida
                startActivity(intent);

                //cargarDestino(modelo.getUsuario());
                //Matamos la Activity Main
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void crearSesion(String usuario, String contrasena, JSONObject jsonUser) throws JSONException {
        Modelo modelo = (Modelo) getApplicationContext();
        SharedPreferences sesion = getSharedPreferences("credencial", Context.MODE_PRIVATE);
        SharedPreferences.Editor editar = sesion.edit();
        editar.putString("user", usuario);
        editar.putString("pass", contrasena);
        /*nombre.setText("Su usuario es: "+usuario);
        cedula.setText("Su clave es: "+contrasena);*/
        editar.apply();

        modelo.setNombre(jsonUser.getString("nombre"));
        modelo.setUsuario(jsonUser.getString("cedula"));
        modelo.setCorreo(jsonUser.getString("mail"));

        if(jsonUser.getString("puesto").equals("Contratista")) {
            modelo.setTipoContrato(jsonUser.getString("puesto"));
        } else {
            modelo.setTipoContrato("Vinculado");
        }

        cargarUsuario(jsonUser.getString("cedula"));

        String user = sesion.getString("user","Sin info...");
        if(user.equals("Sin info...")){
            Toast.makeText(getApplicationContext(),"La sesion no se ha creado",Toast.LENGTH_SHORT).show();
        }else{
            if(isLocationServiceRunning()){
                //Creamos la intencion para ir a otra activity

                ///pendiente

                /*Intent intent = new Intent(MainActivity.this, Mapa.class);
                startActivity(intent);
                //Matamos la Activity Main
                finish();*/
            }else{
                //Creamos la intencion para ir a otra activity
                Intent intent = new Intent(MainActivity.this, Bienvenida.class);
                //intent.putExtra("nombre", jsonUser.getString("nombre"));
                //Abrimos la Activity de Bienvenida
                startActivity(intent);

                cargarDestino(modelo.getUsuario());
                consultarOrigenDestinoUsuario(modelo.getUsuario());
                //Matamos la Activity Main
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void cargarDestino(String usuario) {
        Modelo modelo = (Modelo) getApplicationContext();
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String user = "'P1':'"+usuario+"'"+"";
            String strSql = "{'SQL':'SQL_MVT_SELECT_USUARIO_DESTINO','N':1,'DATOS':{"+user+"}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String X = object.getString("X");
                String Y = object.getString("Y");
                Integer ID_SEDE = object.getInt("ID_SEDE");
                String ID_USUARIO = object.getString("ID_USUARIO");
                if(ID_SEDE != null){
                    modelo.setIdSedeDestino(ID_SEDE);
                    modelo.setIdUsuario(ID_USUARIO);
                }
                if(!X.equals("null")){
                    modelo.setX(X);
                    modelo.setY(Y);
                    modelo.setIdUsuario(ID_USUARIO);
                }
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
    private void cargarUsuario(String usuario) {
        Modelo modelo = (Modelo) getApplicationContext();
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String user = "'P1':'"+usuario+"'"+"";
            String strSql = "{'SQL':'SQL_MVT_CONSULTAR_USUARIO','N':1,'DATOS':{"+user+"}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            JSONArray jsonArray = new JSONArray(response);
            if(jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    String foto = new String(Base64.getDecoder().decode(object.getString("URL_FOTO")), StandardCharsets.UTF_8);
                    if(URLUtil.isValidUrl(foto)) {
                        modelo.setFotoPerfil(foto);
                    } else {
                        modelo.setFotoPerfil("");
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void consultarOrigenDestinoUsuario(String usuario) {
        Modelo modelo = (Modelo) getApplicationContext();
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String user = "'P1':'"+usuario.trim()+"'"+"";
            String strSql = "{'SQL':'SQL_MVT_CARGAR_ORIGEN_DESTINO_USUARIO','N':1,'DATOS':{"+user+"}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            modelo.setOrigenDestino(new JSONArray(response));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private  boolean isLocationServiceRunning(){
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void cerrarAplicacion() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.logo_vamosmed)
                .setTitle("¿Desea cerrar la aplicación?")
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid()); //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                        //finish(); Si solo quiere mandar la aplicación a segundo plano
                    }
                }).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cerrarAplicacion();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}