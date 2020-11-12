package com.medellin.vamosmed.combos;

import java.util.ArrayList;

public class ComboSubsecretaria {
    ArrayList<Subsecretaria> arrSubsecretaria = new ArrayList<>();

    public void agregarSubsecretaria(Subsecretaria subsecretaria) {
        arrSubsecretaria.add(subsecretaria);
    }

    public ArrayList<Subsecretaria> getComboSubsecretaria() {
        return arrSubsecretaria;
    }
}
