package com.medellin.vamosmed.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.medellin.vamosmed.Bienvenida;
import com.medellin.vamosmed.Modelo;
import com.medellin.vamosmed.Perfil;
import com.medellin.vamosmed.R;
import com.medellin.vamosmed.combos.ComboSecretaria;
import com.medellin.vamosmed.combos.ComboSede;
import com.medellin.vamosmed.combos.ComboSubsecretaria;
import com.medellin.vamosmed.combos.Secretaria;
import com.medellin.vamosmed.combos.Sede;
import com.medellin.vamosmed.combos.Subsecretaria;
import com.medellin.vamosmed.conexion;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Desplazamiento extends AppCompatActivity {
    Spinner spnSedes;
    ArrayAdapter<Sede> adapterSedes = null;
    boolean cargaInicialSedes = false;
    EditText txtDireccionCasa = null;
    EditText txtCorreoJefe = null;
    Modelo modelo = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desplazamiento);

        Toolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        txtDireccionCasa = (EditText) findViewById(R.id.txtDireccionCasa);
        txtCorreoJefe = (EditText) findViewById(R.id.txtCorreoJefe);
        modelo = (Modelo) getApplicationContext();
        txtCorreoJefe.setText(modelo.getCorreoJefe());
        consultarSede();

        try {
            JSONArray origenDestino = modelo.getOrigenDestino();
            if(origenDestino.length() > 0) {
                Sede sedeAux = null;
                for(int i = 0; i < origenDestino.length(); i++) {
                    JSONObject objOrigDest = origenDestino.getJSONObject(i);
                    if(objOrigDest.getString("TIPO").equals("OR")){
                        txtDireccionCasa.setText(objOrigDest.getString("DIRECCION"));
                        modelo.setNombreSitio(objOrigDest.getString("NOMBRE"));
                        modelo.setxOrigen(objOrigDest.getString("X"));
                        modelo.setxOrigen(objOrigDest.getString("Y"));
                    } else if(objOrigDest.getString("TIPO").equals("DE")) {
                        modelo.setIdSedeDestino(objOrigDest.getInt("ID_SEDE"));
                        for(int j = 0; j < adapterSedes.getCount(); j++) {
                            sedeAux = adapterSedes.getItem(i);
                            if(Integer.valueOf(sedeAux.getId()) == modelo.getIdSedeDestino()) {
                                cargaInicialSedes = true;
                                spnSedes.setSelection(adapterSedes.getPosition(sedeAux));
                                break;
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {}
    }

    private void atras() {
        Intent desplazamiento = new Intent(Desplazamiento.this, Perfil.class);
        startActivity(desplazamiento);
        finish();
    }

    public void onClickSiguiente(View view) {
        Sede sedeElegida = (Sede) spnSedes.getSelectedItem();
        CheckBox checkBox = findViewById(R.id.chkIos);
        String mensaje = "";

        if(sedeElegida.getId() == "-1") {
            mensaje += "- Debe seleccionar la secretaría.\n";
        }
        if(!checkBox.isChecked()){
            mensaje += "- Debes aceptar los términos y condiciones.";
        }

        if(mensaje.equals("")) {
            //cambiar pantalla a desplazamiento
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Información")
                    .setMessage(mensaje)
                    .setPositiveButton(android.R.string.ok, null).show();
        }
    }

    public void onClickAtras(View view) {
        atras();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            atras();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onSupportNavigateUp() {
        atras();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void consultarSede() {
        Modelo modelo = (Modelo) getApplicationContext();
        ComboSede comboSede = null;
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String opcion = Base64.getUrlEncoder().encodeToString("no".getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/buscarDestinos.hyg?coordOK="+opcion).get();
            JSONArray jsonArray = new JSONArray(response);
            comboSede = new ComboSede();
            Sede sede = new Sede();
            sede.setId("-1");
            sede.setName("Seleccione");
            comboSede.agregarSede(sede);
            if(jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    sede = new Sede();
                    sede.setId(object.getString("id"));
                    sede.setName(object.getString("name"));
                    comboSede.agregarSede(sede);
                }

                spnSedes = (Spinner) findViewById(R.id.cmbSedeAdm);
                adapterSedes = new ArrayAdapter<Sede>(this, android.R.layout.simple_spinner_dropdown_item, comboSede.getComboSede());
                spnSedes.setAdapter(adapterSedes);

                spnSedes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(cargaInicialSedes) {
                            cargaInicialSedes = false;
                            return;
                        }
                        Sede sedeEleg = (Sede) parent.getSelectedItem();
                        modelo.setIdSecretaria(Integer.valueOf(sedeEleg.getId()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
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