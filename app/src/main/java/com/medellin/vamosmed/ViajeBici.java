package com.medellin.vamosmed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.medellin.vamosmed.ui.estadisticas.EstadisticasFragment;

public class ViajeBici extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viaje_bici);

        Toolbar toolbar = findViewById(R.id.toolbarViaje);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //MOSTRAR EN LA PARTE DE ABAJO PESTAÑAS SEGUN EL TIPO DE CONTRATO

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }

    public void onClickAtrasViaje(View view) {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Intent viaje = new Intent(this, Bienvenida.class);
            startActivity(viaje);
            Modelo modelo = (Modelo) getApplicationContext();
            Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE).putExtra("coordenadas...", "coor");
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            finish();
        }else{
            String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            Toast.makeText(this, tag, Toast.LENGTH_SHORT).show();
            if (tag.equals("Dashboard")) {
                // Crea el nuevo fragmento y la transacción.
                Fragment EstadisticasFragment = new EstadisticasFragment();
                this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment,EstadisticasFragment)
                        .addToBackStack("estadisticas")
                        .commit();
            }
            if (tag.equals("estadisticas")||tag.equals("viaje")) {
                Intent viaje = new Intent(this, Bienvenida.class);
                startActivity(viaje);
                Modelo modelo = (Modelo) getApplicationContext();
                Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                        .putExtra("coordenadas...", "coor");
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                Intent viaje = new Intent(this, Bienvenida.class);
                startActivity(viaje);
                Modelo modelo = (Modelo) getApplicationContext();
                Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE).putExtra("coordenadas...", "coor");
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                finish();
            }else{
                String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
                Toast.makeText(this, tag, Toast.LENGTH_SHORT).show();
                if (tag.equals("Dashboard")) {
                    // Crea el nuevo fragmento y la transacción.
                    Fragment EstadisticasFragment = new EstadisticasFragment();
                    this.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment,EstadisticasFragment)
                            .addToBackStack("estadisticas")
                            .commit();
                }
                if (tag.equals("estadisticas")||tag.equals("viaje")) {
                    Intent viaje = new Intent(this, Bienvenida.class);
                    startActivity(viaje);
                    Modelo modelo = (Modelo) getApplicationContext();
                    Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                            .putExtra("coordenadas...", "coor");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}