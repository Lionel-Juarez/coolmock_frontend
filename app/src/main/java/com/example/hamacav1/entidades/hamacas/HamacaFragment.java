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
import com.example.hamacav1.entidades.reservas.Reserva;
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
import java.util.stream.Collectors;

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

        // Inicializa la lista de todas las hamacas
        todasLasHamacas = new ArrayList<>();

        hamacasRecyclerView = view.findViewById(R.id.hamacasRecyclerView);
        hamacasRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        // Aquí pasamos getChildFragmentManager() si estás dentro de un Fragment
        hamacasAdapter = new HamacaAdapter(todasLasHamacas, getContext(), getChildFragmentManager());
        hamacasRecyclerView.setAdapter(hamacasAdapter);

        // Configuración del Spinner para seleccionar planos
        spinnerPlanos = view.findViewById(R.id.spinner_planos);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.planos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlanos.setAdapter(adapter);

        spinnerPlanos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                cargarHamacasPorPlano(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        return view;
    }

    private void cargarHamacasPorPlano(int planoId) {
        String url =getResources().getString(R.string.url_hamacas) ; // Asegúrate de usar la URL correcta de tu API

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error al cargar hamacas", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    getActivity().runOnUiThread(() -> {
                        try {
                            todasLasHamacas.clear();
                            JSONArray jsonArray = new JSONArray(responseBody);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Reserva reserva = null;
                                if (jsonObject.has("idReserva") && !jsonObject.isNull("idReserva")) {
                                    JSONObject reservaJson = jsonObject.getJSONObject("idReserva");
                                    reserva = new Reserva();
                                    reserva.fromJSON(reservaJson); // Asegúrate de que Reserva tiene un método adecuado para esto
                                }

                                Hamaca hamaca = new Hamaca(
                                        jsonObject.getLong("idHamaca"),
                                        jsonObject.getDouble("precio"),
                                        jsonObject.getBoolean("reservada"),
                                        jsonObject.getBoolean("ocupada"),
                                        jsonObject.getInt("planoId"),
                                        reserva
                                );
                                todasLasHamacas.add(hamaca);
                            }

                            List<Hamaca> hamacasFiltradas = todasLasHamacas.stream().filter(h -> h.getPlanoId() == planoId).collect(Collectors.toList());
                            hamacasAdapter.setHamacas(hamacasFiltradas);
                            hamacasAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Respuesta no exitosa del servidor", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    private int cantidadDeHamacasPorPlano(int planoId) {
        switch (planoId) {
            case 1: return 35;
            case 2: return 30;
            case 3: return 25;
            default: return 0;
        }
    }
}