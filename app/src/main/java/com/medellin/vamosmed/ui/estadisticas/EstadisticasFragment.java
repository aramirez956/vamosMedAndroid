package com.medellin.vamosmed.ui.estadisticas;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.medellin.vamosmed.Modelo;
import com.medellin.vamosmed.R;
import com.medellin.vamosmed.ui.filtros.FiltrosFragment;

public class EstadisticasFragment extends Fragment {

    ProgressDialog progressDialog;//dialogo cargando
    ImageView viajesTrabajo;
    View vista;

    private NotificationsViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //Toast.makeText(getContext(), "sin fragmento", Toast.LENGTH_SHORT).show();
            // Inflate the layout for this fragment
            vista = inflater.inflate(R.layout.fragment_estadisticas, container, false);

            progressDialog = ProgressDialog.show(getContext(), "Vamos Med", "Cargando estadisticas...");
            viajesTrabajo = vista.findViewById(R.id.viajesTrabajo);

            Modelo modelo = (Modelo) getActivity().getApplicationContext();
            Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                    .putExtra("coordenadas...", "coor");
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);

            viajesTrabajo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Crea el nuevo fragmento y la transacción.
                    Fragment Dashboard = new FiltrosFragment();

                    // Obtener el administrador de fragmentos a través de la actividad
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    // Definir una transacción
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, Dashboard,"Dashboard");
                    transaction.addToBackStack("Dashboard");
                    transaction.commit();

                    fragmentTransaction.replace(R.id.nav_host_fragment, Dashboard,"Dashboard");
                    fragmentTransaction.addToBackStack("Dashboard");
                    fragmentTransaction.commit();

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            },1000L);
            return vista;
        }else {
            String tag = getActivity().getSupportFragmentManager().getBackStackEntryAt(getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

            if (tag.equals("Dashboard")) {
                //Toast.makeText(getContext(), "fragmento "+tag, Toast.LENGTH_SHORT).show();
                Fragment EstadisticasFragment = new EstadisticasFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment,EstadisticasFragment)
                        .addToBackStack("estadisticas")
                        .commit();
            }

            vista = inflater.inflate(R.layout.fragment_estadisticas, container, false);

            progressDialog = ProgressDialog.show(getContext(), "Vamos Med", "Cargando estadisticas...");
            viajesTrabajo = vista.findViewById(R.id.viajesTrabajo);

            Modelo modelo = (Modelo) getActivity().getApplicationContext();
            Intent localIntent = new Intent(modelo.ACTION_STOP_LOCATION_SERVICE)
                    .putExtra("coordenadas...", "coor");
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);

            viajesTrabajo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Crea el nuevo fragmento y la transacción.
                    Fragment Dashboard = new FiltrosFragment();

                    // Obtener el administrador de fragmentos a través de la actividad
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    // Definir una transacción
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, Dashboard,"Dashboard");
                    transaction.addToBackStack("Dashboard");
                    transaction.commit();

                    fragmentTransaction.replace(R.id.nav_host_fragment, Dashboard,"Dashboard");
                    fragmentTransaction.addToBackStack("Dashboard");
                    fragmentTransaction.commit();

                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            },1000L);
            return vista;
        }

    }

}