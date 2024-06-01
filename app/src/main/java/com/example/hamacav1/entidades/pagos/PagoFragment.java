package com.example.hamacav1.entidades.pagos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.hamacav1.R;


public class PagoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagos, container, false);

        // Obtener referencias a las tarjetas
        androidx.cardview.widget.CardView cardPrimeraPantalla = view.findViewById(R.id.cardPrimeraPantalla);
        androidx.cardview.widget.CardView cardSegundaPantalla = view.findViewById(R.id.cardSegundaPantalla);
        androidx.cardview.widget.CardView cardTerceraPantalla = view.findViewById(R.id.cardTerceraPantalla);

        cardPrimeraPantalla.setOnClickListener(v -> {
            // Acción para la primera tarjeta
        });

        cardSegundaPantalla.setOnClickListener(v -> {
            // Acción para la segunda tarjeta
        });

        cardTerceraPantalla.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new PagosVistaFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }
}
