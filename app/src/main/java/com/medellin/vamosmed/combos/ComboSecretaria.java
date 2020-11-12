package com.medellin.vamosmed.combos;

import android.app.Application;

import java.io.Serializable;
import java.util.ArrayList;

public class ComboSecretaria extends Application implements Serializable {
    ArrayList<Secretaria> arrSecretaria = new ArrayList<>();

    public void agregarSecretaria(Secretaria secretaria) {
        arrSecretaria.add(secretaria);
    }

    public ArrayList<Secretaria> getComboSecretaria() {
        return arrSecretaria;
    }
}
