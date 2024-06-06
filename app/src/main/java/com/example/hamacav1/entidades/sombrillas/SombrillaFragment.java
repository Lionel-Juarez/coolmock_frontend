package com.example.hamacav1.entidades.sombrillas;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.util.OkHttpProvider;


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
import java.util.Objects;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SombrillaFragment extends Fragment implements SombrillaDetalles.SombrillaUpdateListener {

    private SombrillaAdapter sombrillasAdapter;
    private final List<Sombrilla> todasLasSombrillas = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sombrilla, container, false);
        setupRecyclerView(view);
        progressBar = view.findViewById(R.id.progressBar);
        cargarSombrillas(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        setupOpenDatePicker(view);
        return view;
    }

    private void setupOpenDatePicker(View view) {
        ImageView openDatePicker = view.findViewById(R.id.openDatePicker);
        openDatePicker.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> cargarSombrillas(year, month, dayOfMonth), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupRecyclerView(View view) {
        RecyclerView sombrillasRecyclerView = view.findViewById(R.id.sombrillasRecyclerView);
        sombrillasRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        sombrillasAdapter = new SombrillaAdapter(todasLasSombrillas, getContext(), getChildFragmentManager());
        sombrillasRecyclerView.setAdapter(sombrillasAdapter);
    }

    private void cargarSombrillas(int year, int month, int dayOfMonth) {
        LocalDate today = LocalDate.of(year, month + 1, dayOfMonth);
        Log.d("SombrillaFragment", "Cargando sombrillas para la fecha actual: " + today);

        progressBar.setVisibility(View.VISIBLE);

        String url = getResources().getString(R.string.url_sombrillas).concat("sombrillas");
        HttpUrl urlWithParams = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder().build();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        if (idToken == null) {
            Log.e("SombrillaLoad", "Token de autorización no disponible");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error de autenticación. Por favor, inicie sesión de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(urlWithParams.toString())
                .addHeader("Authorization", "Bearer " + idToken)
                .get()
                .build();

        OkHttpClient client = OkHttpProvider.getInstance(getContext());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("SombrillaLoad", "Error al cargar sombrillas: " + e.getMessage(), e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error al cargar sombrillas", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                todasLasSombrillas.clear();
                                if (responseBody.isEmpty()) {
                                    Log.e("SombrillaFragment", "El cuerpo de la respuesta está vacío");
                                    Toast.makeText(getContext(), "No hay sombrillas disponibles", Toast.LENGTH_SHORT).show();
                                } else {
                                    JSONArray jsonArray = new JSONArray(responseBody);
                                    Log.d("SombrillaLoad", "Sombrillas cargadas correctamente: " + responseBody);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        Sombrilla sombrilla = Sombrilla.fromJSON(jsonObject);
                                        JSONArray reservas = jsonObject.optJSONArray("reservas");
                                        if (reservas != null) {
                                            Log.d("SombrillaFragment", "Verificando reservas para sombrilla ID: " + sombrilla.getIdSombrilla());
                                        }
                                        checkReservationsAndSetReserved(sombrilla, reservas, today);
                                        todasLasSombrillas.add(sombrilla);
                                    }
                                    sombrillasAdapter.setSombrillas(todasLasSombrillas);
                                    sombrillasAdapter.notifyDataSetChanged();
                                    Log.d("SombrillaFragment", "Sombrillas cargadas y actualizadas en el adaptador.");
                                }
                            } catch (JSONException e) {
                                Log.e("SombrillaFragment", "Error al procesar los datos de sombrillas", e);
                                Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                            } finally {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    Log.e("SombrillaLoad", "Respuesta no exitosa del servidor: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Respuesta no exitosa del servidor", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }

    private void checkReservationsAndSetReserved(Sombrilla sombrilla, JSONArray reservas, LocalDate today) throws JSONException {
        boolean isReserved = false;
        if (reservas != null) {
            for (int i = 0; i < reservas.length(); i++) {
                JSONObject reservaObj = reservas.optJSONObject(i);
                if (reservaObj == null) {
                    Log.d("checkReservationsAndSetReserved", "Skip non-JSONObject entry at index: " + i);
                    continue;
                }
                String fechaReservaStr = reservaObj.optString("fechaReserva");
                if (!fechaReservaStr.equals("null") && !fechaReservaStr.isEmpty()) {
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
        sombrilla.setReservada(isReserved);
    }

    @Override
    public void onSombrillaUpdated(Sombrilla updatedSombrilla) {
        int index = todasLasSombrillas.indexOf(updatedSombrilla);
        if (index != -1) {
            todasLasSombrillas.set(index, updatedSombrilla);
            sombrillasAdapter.notifyItemChanged(index);
            Log.d("SombrillaFragment", "Actualización de la vista de sombrilla en el índice: " + index);
        }
    }
}
