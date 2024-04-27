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

import com.example.hamacav1.MainActivity;
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


public class HamacaFragment extends Fragment implements HamacaDetalles.HamacaUpdateListener {

    private RecyclerView hamacasRecyclerView;
    private HamacaAdapter hamacasAdapter;
    private List<Hamaca> todasLasHamacas = new ArrayList<>(); // Initialize directly
    private Spinner spinnerPlanos;
    private ArrayAdapter<CharSequence> adapter;
    private int selectedPlano = 0;

    @Override
    public void onResume() {
        super.onResume();
        if (spinnerPlanos != null && adapter != null) {
            spinnerPlanos.setSelection(selectedPlano);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hamaca, container, false);
        setupRecyclerView(view);
        setupSpinner(view);
        return view;
    }


    private void setupRecyclerView(View view) {
        hamacasRecyclerView = view.findViewById(R.id.hamacasRecyclerView);
        hamacasRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 9));
        hamacasAdapter = new HamacaAdapter(todasLasHamacas, getContext(), getChildFragmentManager());
        hamacasRecyclerView.setAdapter(hamacasAdapter);
    }

    private void setupSpinner(View view) {
        spinnerPlanos = view.findViewById(R.id.spinner_planos);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.planos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlanos.setAdapter(adapter);
        spinnerPlanos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlano = position;
                cargarHamacasPorPlano(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar hamacas", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if (todasLasHamacas != null) { // Check if list is not null
                                todasLasHamacas.clear();
                                try {
                                    JSONArray jsonArray = new JSONArray(responseBody);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        Hamaca hamaca = new Hamaca(); // Assuming you have a constructor or method to parse
                                        hamaca.fromJSON(jsonObject);
                                        todasLasHamacas.add(hamaca);
                                    }
                                    List<Hamaca> hamacasFiltradas = todasLasHamacas.stream()
                                            .filter(h -> h.getPlanoId() == selectedPlano + 1)
                                            .collect(Collectors.toList());
                                    hamacasAdapter.setHamacas(hamacasFiltradas);
                                    hamacasAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Optionally reinitialize the list or handle the case where it is null
                                todasLasHamacas = new ArrayList<>();
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Respuesta no exitosa del servidor", Toast.LENGTH_SHORT).show());
                }
            }

        });
    }

    @Override
    public void onHamacaUpdated(Hamaca hamaca) {
        cargarHamacasPorPlano(selectedPlano + 1);  // Recargar la lista de hamacas
    }

}