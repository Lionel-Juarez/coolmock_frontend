package com.example.hamacav1.entidades.hamacas;

import android.app.Activity;
import android.app.DatePickerDialog;
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

import android.widget.ImageView;
import android.widget.Toast;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HamacaFragment extends Fragment implements HamacaDetalles.HamacaUpdateListener {

    private RecyclerView hamacasRecyclerView;
    private HamacaAdapter hamacasAdapter;
    private List<Hamaca> todasLasHamacas = new ArrayList<>();
    private ImageView openDatePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hamaca, container, false);
        setupRecyclerView(view);
        cargarHamacas(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        setupOpenDatePicker(view);
        return view;
    }

    private void setupOpenDatePicker(View view) {
        openDatePicker = view.findViewById(R.id.openDatePicker);
        openDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Lógica para cargar las hamacas con la fecha seleccionada...
                    cargarHamacas(year, month, dayOfMonth);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupRecyclerView(View view) {
        hamacasRecyclerView = view.findViewById(R.id.hamacasRecyclerView);
        hamacasRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 9));
        hamacasAdapter = new HamacaAdapter(todasLasHamacas, getContext(), getChildFragmentManager());
        hamacasRecyclerView.setAdapter(hamacasAdapter);
    }
    private void cargarHamacas(int year, int month, int dayOfMonth) {
        LocalDate today = LocalDate.of(year, month + 1, dayOfMonth);
        Log.d("HamacaFragment", "Cargando hamacas para la fecha actual: " + today);

        String url = getResources().getString(R.string.url_hamacas).concat("hamacas");
        HttpUrl urlWithParams = HttpUrl.parse(url).newBuilder().build();
        Request request = new Request.Builder().url(urlWithParams.toString()).get().build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("HamacaLoad", "Error al cargar hamacas: " + e.getMessage(), e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar hamacas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    getActivity().runOnUiThread(() -> {
                        try {
                            todasLasHamacas.clear();
                            JSONArray jsonArray = new JSONArray(responseBody);
                            Log.d("HamacaLoad", "Hamacas cargadas correctamente: " + responseBody);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Hamaca hamaca = Hamaca.fromJSON(jsonObject);
                                JSONArray reservas = jsonObject.optJSONArray("reservas");
                                if (reservas != null) {
                                    Log.d("HamacaFragment", "Verificando reservas para hamaca ID: " + hamaca.getIdHamaca());
                                }
                                checkReservationsAndSetReserved(hamaca, reservas, today);
                                todasLasHamacas.add(hamaca);
                            }
                            hamacasAdapter.setHamacas(todasLasHamacas);
                            hamacasAdapter.notifyDataSetChanged();
                            Log.d("HamacaFragment", "Hamacas cargadas y actualizadas en el adaptador.");
                        } catch (JSONException e) {
                            Log.e("HamacaFragment", "Error al procesar los datos de hamacas", e);
                            Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("HamacaLoad", "Respuesta no exitosa del servidor: " + response.code());
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Respuesta no exitosa del servidor", Toast.LENGTH_SHORT). show());
                }
            }
        });
    }

    private void checkReservationsAndSetReserved(Hamaca hamaca, JSONArray reservas, LocalDate today) throws JSONException {
        boolean isReserved = false;
        if (reservas != null) {
            for (int i = 0; i < reservas.length(); i++) {
                JSONObject reservaObj = reservas.optJSONObject(i);
                if (reservaObj == null) {
                    Log.d("checkReservationsAndSetReserved", "Skip non-JSONObject entry at index: " + i);
                    continue;
                }
                String fechaReservaStr = reservaObj.optString("fechaReserva");
                if (!fechaReservaStr.isEmpty()) {
                    try {
                        LocalDateTime fechaReserva = LocalDateTime.parse(fechaReservaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        if (fechaReserva.toLocalDate().isEqual(today)) {
                            isReserved = true;
                            break;
                        }
                    } catch (DateTimeParseException e) {
                        Log.e("checkReservationsAndSetReserved", "Error parsing date: " + fechaReservaStr, e);
                    }
                }
            }
        }
        hamaca.setReservada(isReserved);
    }





    @Override
    public void onHamacaUpdated(Hamaca updatedHamaca) {
        int index = todasLasHamacas.indexOf(updatedHamaca);
        if (index != -1) {
            todasLasHamacas.set(index, updatedHamaca);
            hamacasAdapter.notifyItemChanged(index);
            Log.d("HamacaFragment", "Actualización de la vista de hamaca en el índice: " + index);
        }
    }
}