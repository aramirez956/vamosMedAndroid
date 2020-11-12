package com.medellin.vamosmed;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.medellin.vamosmed.combos.ComboSecretaria;
import com.medellin.vamosmed.combos.ComboSubsecretaria;
import com.medellin.vamosmed.combos.Secretaria;
import com.medellin.vamosmed.combos.Subsecretaria;
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

public class Perfil extends AppCompatActivity {
    private static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RESULT_LOAD_IMAGE = 2;
    Spinner spnSecretaria, spnSubsecretaria;
    ArrayAdapter<Secretaria> adapterSecre = null;
    ArrayAdapter<Subsecretaria> adapterSubse = null;
    boolean cargaInicialSecre = false, cargaInicialSubse = false;
    TextView identificacion, nombre, correo;
    String correoU = "";
    ImageView fotoPerfil;
    String currentPhotoPath;
    File photoFile = null;
    Modelo modelo = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        Toolbar toolbar = findViewById(R.id.toolbarPerfil);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        modelo = (Modelo) getApplicationContext();
        identificacion = (TextView) findViewById(R.id.txtIdentificacion);
        nombre = (TextView) findViewById(R.id.txtNombre);
        correo = (TextView) findViewById(R.id.txtCorreo);
        identificacion.setText(modelo.getUsuario());
        nombre.setText(modelo.getNombre());
        correo.setText(modelo.getCorreo());
        fotoPerfil = findViewById(R.id.imageView5);
        consultarSecretaria();

        if(modelo.getFotoPerfil().equals("")) {
            Picasso.get().load(R.drawable.ic_iconusuario).placeholder(R.drawable.ic_iconusuario).error(R.drawable.ic_iconusuario).into(fotoPerfil);
        } else {
            Picasso.get().load(modelo.getFotoPerfil()).placeholder(R.drawable.ic_iconusuario).error(R.drawable.ic_iconusuario).into(fotoPerfil);
        }

        if(modelo.getIdSecretaria() != null && modelo.getIdSecretaria() != -1) {
            Secretaria secretariaAux = null;
            for(int i = 0; i < adapterSecre.getCount(); i++) {
                secretariaAux = adapterSecre.getItem(i);
                if(Integer.valueOf(secretariaAux.getId()) == modelo.getIdSecretaria()) {
                    cargaInicialSecre = true;
                    spnSecretaria.setSelection(adapterSecre.getPosition(secretariaAux));
                    break;
                }
            }

            if(modelo.getIdSubsecretaria() != null && modelo.getIdSubsecretaria() != -1) {
                Subsecretaria subsecretariaAux = null;
                for(int i = 0; i < adapterSubse.getCount(); i++) {
                    subsecretariaAux = adapterSubse.getItem(i);
                    if(Integer.valueOf(subsecretariaAux.getId()) == modelo.getIdSubsecretaria()) {
                        cargaInicialSubse = true;
                        spnSubsecretaria.setSelection(adapterSubse.getPosition(subsecretariaAux));
                        break;
                    }
                }
            }
        } else {
            spnSecretaria.setSelection(0);
        }
    }

    private void atras() {
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.logo_vamosmed)
            .setTitle("¿Desea cancelar la edición de datos?")
            .setCancelable(false)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent perfil = new Intent(Perfil.this, Bienvenida.class);
                    startActivity(perfil);
                    finish();
                }
            }).show();
    }

    public void onClickSiguiente(View view) {
        Secretaria secretElegida = (Secretaria) spnSecretaria.getSelectedItem();
        Subsecretaria subsecretElegida = (Subsecretaria) spnSubsecretaria.getSelectedItem();
        CheckBox checkBox = findViewById(R.id.chkIos);
        String mensaje = "";

        if(secretElegida.getId() == "-1") {
            mensaje += "- Debe seleccionar la secretaría.\n";
        }
        if(subsecretElegida.getId() == "-1") {
            mensaje += "- Debe seleccionar la subsecretaría.\n";
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
    private void consultarSecretaria() {
        Modelo modelo = (Modelo) getApplicationContext();
        ComboSecretaria comboSecretaria = null;
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String strSql = "{'SQL':'SQL_MVT_CONSULTAR_SECRETARIA','N':0,'DATOS':{}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            JSONArray jsonArray = new JSONArray(response);
            comboSecretaria = new ComboSecretaria();
            Secretaria secretaria = new Secretaria();
            secretaria.setId("-1");
            secretaria.setName("Seleccione");
            comboSecretaria.agregarSecretaria(secretaria);
            if(jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    secretaria = new Secretaria();
                    secretaria.setId(object.getString("id"));
                    secretaria.setName(object.getString("name"));
                    comboSecretaria.agregarSecretaria(secretaria);
                }

                spnSecretaria = (Spinner) findViewById(R.id.cmbSecretaria);
                adapterSecre = new ArrayAdapter<Secretaria>(this, android.R.layout.simple_spinner_dropdown_item, comboSecretaria.getComboSecretaria());
                spnSecretaria.setAdapter(adapterSecre);
                consultarSubsecretaria();

                spnSecretaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(cargaInicialSecre) {
                            cargaInicialSecre = false;
                            return;
                        }
                        Secretaria secretariaEleg = (Secretaria) parent.getSelectedItem();
                        modelo.setIdSecretaria(Integer.valueOf(secretariaEleg.getId()));
                        consultarSubsecretaria();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void consultarSubsecretaria() {
        Modelo modelo = (Modelo) getApplicationContext();

        if(modelo.getIdSecretaria() == -1) {
            spnSecretaria.setSelection(0);
            return;
        }

        ComboSubsecretaria comboSubsecretaria = null;
        conexion connection = new conexion();
        try {
            //Preparamos los parametros para enviarlos al servicio
            String idSecre = "'P1':'"+modelo.getIdSecretaria()+"'"+"";
            String strSql = "{'SQL':'SQL_MVT_CONSULTAR_SUBSECRETARIA','N':1,'DATOS':{"+idSecre+"}}";
            String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
            //Envio de petición a el serivio Web
            String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/cargardatos.hyg?str_sql="+datosPost).get();
            JSONArray jsonArray = new JSONArray(response);
            comboSubsecretaria = new ComboSubsecretaria();
            Subsecretaria subsecretaria = new Subsecretaria();
            subsecretaria.setId("-1");
            subsecretaria.setName("Seleccione");
            comboSubsecretaria.agregarSubsecretaria(subsecretaria);

            if(jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    subsecretaria = new Subsecretaria();
                    subsecretaria.setId(object.getString("id"));
                    subsecretaria.setName(object.getString("name"));
                    comboSubsecretaria.agregarSubsecretaria(subsecretaria);
                }

                spnSubsecretaria = (Spinner) findViewById(R.id.cmbSubsecretaria);
                adapterSubse = new ArrayAdapter<Subsecretaria>(this, android.R.layout.simple_spinner_dropdown_item, comboSubsecretaria.getComboSubsecretaria());
                spnSubsecretaria.setAdapter(adapterSubse);

                spnSubsecretaria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(cargaInicialSubse) {
                            cargaInicialSubse = false;
                            return;
                        }
                        Subsecretaria subsecretariaEleg = (Subsecretaria) parent.getSelectedItem();
                        modelo.setIdSubsecretaria(Integer.valueOf(subsecretariaEleg.getId()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } else if(jsonArray.length() == 0) {
                new AlertDialog.Builder(this).setTitle("Información").setMessage("La secretaría seleccionada no tiene subsecretarías asociadas.").show();
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
    public void onClickEliminarFoto(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Modelo modelo = (Modelo) getApplicationContext();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            if(!modelo.getFotoPerfil().equals("")) {
                conexion connection = new conexion();
                try {
                    //Preparamos los parametros para enviarlos al servicio
                    String user = "'P1':'"+modelo.getUsuario().trim()+"'"+"";
                    String strSql = "{'SQL':'SQL_MVT_ELIMINAR_USUARIO_FOTO','N':1,'APP':26,'PERFILES':-1,'DATOS':{"+user+"}}";
                    String datosPost = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
                    String formTipo = Base64.getUrlEncoder().encodeToString("1".getBytes());
                    String cedula = Base64.getUrlEncoder().encodeToString(modelo.getUsuario().trim().getBytes());
                    String idViaje = Base64.getUrlEncoder().encodeToString("".getBytes());
                    //Envio de petición a el serivio Web
                    String response = connection.execute("https://www.medellin.gov.co/servicios/vamosmed/eliminarArchivo.hyg?strTipo="+formTipo+"&cedula="+cedula+"&str_sql="+datosPost+"&id_viaje="+idViaje).get();
                    if(response=="Ok"){
                        Picasso.get().load(R.drawable.ic_perfil).placeholder(R.drawable.ic_perfil).error(R.drawable.ic_perfil).into(fotoPerfil);
                    } else if(response=="NoE"){
                        new AlertDialog.Builder(this)
                                .setTitle("Información")
                                .setMessage("La imagen no existe en el servidor.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //android.os.Process.killProcess(android.os.Process.myPid());
                                        //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                        //opcion en caso de ser positiva la respuesta por parte del usuario
                                    }
                                }).show();
                    } else {
                        new AlertDialog.Builder(this)
                            .setTitle("Información")
                            .setMessage("Error eliminando.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //android.os.Process.killProcess(android.os.Process.myPid());
                                    //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                    //opcion en caso de ser positiva la respuesta por parte del usuario
                                }
                            }).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
                        }
                    }).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickTomarFoto(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Modelo modelo = (Modelo) getApplicationContext();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            PackageManager pm = this.getPackageManager();
            boolean b = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (b && takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Error al guardar el archivo")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //android.os.Process.killProcess(android.os.Process.myPid());
                                    //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                    //opcion en caso de ser positiva la respuesta por parte del usuario
                                }
                            }).show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.medellin.vamosmed", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    //startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Información")
                        .setMessage("No tienes cámara para ejecutar esta acción")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //android.os.Process.killProcess(android.os.Process.myPid());
                                //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                //opcion en caso de ser positiva la respuesta por parte del usuario
                            }
                        }).show();
            }
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
                        }
                    }).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String filePath = photoFile.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

            ExifInterface ei = null;
            Bitmap rotatedBitmap = null;
            try {
                ei = new ExifInterface(filePath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap scaleBitmap = getResizedBitmap(rotatedBitmap, 120, 120);
            fotoPerfil.setImageBitmap(scaleBitmap);
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //fotoPerfil.setImageBitmap(imageBitmap);

            String[] respuesta = subirImagen(filePath);
            if(!respuesta[0].equals("")) {
                new AlertDialog.Builder(this)
                        .setTitle("Información")
                        .setMessage(respuesta[0])
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //android.os.Process.killProcess(android.os.Process.myPid());
                                //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                //opcion en caso de ser positiva la respuesta por parte del usuario
                            }
                        }).show();
            }
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);

            ExifInterface ei = null;
            Bitmap rotatedBitmap = null;
            try {
                ei = new ExifInterface(filePath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap scaleBitmap = getResizedBitmap(rotatedBitmap, 120, 120);

            fotoPerfil.setImageBitmap(scaleBitmap);
            String[] respuesta = subirImagen(filePath);
            if(!respuesta[0].equals("")) {
                new AlertDialog.Builder(this)
                        .setTitle("Información")
                        .setMessage(respuesta[0])
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {// un listener que al pulsar, cierre la aplicacion
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //android.os.Process.killProcess(android.os.Process.myPid());
                                //Su funcion es algo similar a lo que se llama cuando se presiona el botón "Forzar Detención" o "Administrar aplicaciones", lo cuál mata la aplicación
                                //opcion en caso de ser positiva la respuesta por parte del usuario
                            }
                        }).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String[] subirImagen(String file) {
        String[] respuesta = {""};
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        boolean validMail = pattern.matcher(correo.getText()).matches();
        if(correo.getText().toString().indexOf("@") != -1) {
            if(!validMail) {
                respuesta[0] = "Se debe ingresar un formato de correo correcto.";
            } else {
                correoU = correo.getText().toString().replaceAll(" ", "");
            }
        } else {
            correoU = correo.getText().toString().replaceAll(" ", "") + "@medellin.gov.co";
        }

        if(respuesta[0].equals("")) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try  {
                        HttpURLConnection conn = null;
                        DataOutputStream dos = null;
                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****";
                        int bytesRead, bytesAvailable, bufferSize;
                        byte[] buffer;
                        int maxBufferSize = 1 * 1024 * 1024;
                        File sourceFile = new File(file);

                        try {
                            // open a URL connection to the Servlet
                            FileInputStream fileInputStream = new FileInputStream(sourceFile);
                            URL url = new URL("https://www.medellin.gov.co/servicios/vamosmed/subirArchivo.hyg");

                            // Open a HTTP  connection to  the URL
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true); // Allow Inputs
                            conn.setDoOutput(true); // Allow Outputs
                            conn.setUseCaches(false); // Don't use a Cached Copy
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Connection", "Keep-Alive");
                            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                            conn.setRequestProperty("uploaded_file", "imagePerfil");

                            String ID_USUARIO = "[{\"P1\":\"" + modelo.getUsuario().trim() + "\"" + "";
                            String CONTRATISTA = "\"P2\":\"" + (modelo.getTipoContrato().equals("Contratista") ? "CO" : "VI") + "\"" + "";
                            String PARAM = "\"P3\":\"" + "1" + "\"" + "";
                            String URL = "\"P4\":\"" + "?URL?" + "\"" + "";
                            String DIR = "\"P5\":\"" + "?DIR?" + "\"" + "";
                            String TIPO = "\"P6\":\"" + "1" + "\"" + "";
                            String NOMBRE = "\"P7\":\"" + modelo.getNombre() + "\"" + "";
                            String CORREOU = "\"P8\":\"" + correoU + "\"" + "}]";

                            String strSql = "[{\"SQL\":\"SQL_MVT_GUARDAR_USUARIO_FOTO_2\", \"N\":8, \"APP\":26, \"PERFILES\":-1," +
                                    "\"DATOS\":" + ID_USUARIO + "," + CONTRATISTA + "," + PARAM + "," + URL + "," + DIR + "," + TIPO + "," + NOMBRE + "," + CORREOU + "}]";

                            String idPersona = Base64.getUrlEncoder().encodeToString(modelo.getUsuario().getBytes());
                            String json = Base64.getUrlEncoder().encodeToString(strSql.getBytes());
                            String tipo = Base64.getUrlEncoder().encodeToString("1".getBytes());
                            String idViaje = Base64.getUrlEncoder().encodeToString("".getBytes());
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("cedula", idPersona));
                            params.add(new BasicNameValuePair("str_sql", json));
                            params.add(new BasicNameValuePair("strTipo", tipo));
                            params.add(new BasicNameValuePair("id_viaje", idViaje));

                            dos = new DataOutputStream(conn.getOutputStream());
                            dos.writeBytes(twoHyphens + boundary + lineEnd);
                            dos.writeBytes("Content-Disposition: form-data; name=uploadedFile;filename=imagePerfil" + lineEnd);
                            dos.writeBytes(lineEnd);

                            // create a buffer of  maximum size
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            buffer = new byte[bufferSize];

                            // read file and write it into form...
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            while (bytesRead > 0) {
                                dos.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            }

                            // send multipart form data necesssary after file data...
                            dos.writeBytes(getQuery(params));
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                            // Responses from the server (code and message)
                            int serverResponseCode = conn.getResponseCode();
                            String serverResponseMessage = conn.getResponseMessage();

                            if(serverResponseCode != 200){
                                //respuesta[0] = "Error cargando la imagen.";
                            } else {
                                //respuesta[0] = "Ok";
                            }

                            //close the streams //
                            fileInputStream.close();
                            dos.flush();
                            dos.close();
                        } catch (MalformedURLException ex) {
                            //respuesta[0] = "MalformedURLException Exception : check script url.";
                        } catch (Exception e) {
                            //respuesta[0] = "Got Exception : see logcat.";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //respuesta[0] = "Error";
                    }
                }
            });

            thread.start();
        }
        return respuesta;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickElegirFoto(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Modelo modelo = (Modelo) getApplicationContext();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Si hay conexión a Internet en este momento
            ActivityCompat.requestPermissions(Perfil.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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
                        }
                    }).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Perfil.this, "Permisos negados para leer archivos de la memoria", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}