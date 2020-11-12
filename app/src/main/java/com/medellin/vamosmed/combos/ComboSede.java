package com.medellin.vamosmed.combos;

import java.util.ArrayList;

public class ComboSede {
    ArrayList<Sede> arrSede = new ArrayList<>();

    public void agregarSede(Sede sede) {
        arrSede.add(sede);
    }

    public ArrayList<Sede> getComboSede() {
        return arrSede;
    }
}
