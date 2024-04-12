package com.example.hamacav1.entidades.hamacas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HamacaFragment extends Fragment {

    private RecyclerView hamacasRecyclerView;
    private HamacaAdapter hamacasAdapter;
    private List<Hamaca> todasLasHamacas; // Lista de todas las hamacas disponibles
    private Spinner spinnerPlanos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hamaca, container, false);

        inicializarHamacas(); // Simulamos la creación de las hamacas y su asignación a planos

        // Inicialización del RecyclerView con GridLayoutManager
        hamacasRecyclerView = view.findViewById(R.id.hamacasRecyclerView);
        int numberOfColumns = 4; // Establecemos el número de columnas en la cuadrícula
        hamacasRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns)); // Usamos GridLayoutManager
        hamacasAdapter = new HamacaAdapter(new ArrayList<>(), getContext()); // Inicializa con lista vacía
        hamacasRecyclerView.setAdapter(hamacasAdapter);

        // Configuración del Spinner para seleccionar planos
        spinnerPlanos = view.findViewById(R.id.spinner_planos);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.planos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlanos.setAdapter(adapter);

        spinnerPlanos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                cargarHamacasPorPlano(position + 1); // Asumiendo que tus planos empiezan en 1
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        return view;
    }


    private void inicializarHamacas() {
        todasLasHamacas = new ArrayList<>();
        // Simulamos la creación de 45 hamacas distribuidas entre 3 planos
        for (int i = 1; i <= 45; i++) {
            int planoId = i <= 10 ? 1 : i <= 25 ? 2 : 3; // Asigna el plano basado en el índice de la hamaca
            todasLasHamacas.add(new Hamaca((long) i, 10.0, false, false, planoId));
        }
    }

    private void cargarHamacasPorPlano(int planoId) {
        List<Hamaca> hamacasFiltradas = new ArrayList<>();
        for (Hamaca hamaca : todasLasHamacas) {
            if (hamaca.getPlanoId() == planoId) {
                hamacasFiltradas.add(hamaca);
            }
        }
        hamacasAdapter.setHamacas(hamacasFiltradas);
        hamacasAdapter.notifyDataSetChanged();
    }
}