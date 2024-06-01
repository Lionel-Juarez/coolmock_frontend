package com.example.hamacav1.entidades.reservas;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ReservasViewModel extends ViewModel {
    private MutableLiveData<List<Reserva>> reservas;
    @Getter
    private final MutableLiveData<String> errorMessages = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();

    public MutableLiveData<List<Reserva>> getReservas() {
        if (reservas == null) {
            reservas = new MutableLiveData<>();
        }
        return reservas;
    }

    public void loadAllReservas() {
        loading.postValue(true);  // Indicar que la carga está en progreso
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "http://10.0.2.2:8080/api/reservas/";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);  // Indicar que la carga ha finalizado
                reservas.postValue(new ArrayList<>()); // Publicar una lista vacía en caso de error
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<Reserva> listaReservas = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Reserva reserva = new Reserva();
                            reserva.fromJSON(jsonObject);
                            listaReservas.add(reserva);
                        }
                        // Ordena la lista por fecha
                        listaReservas.sort(Comparator.comparing(Reserva::getFechaReserva));

                        reservas.postValue(listaReservas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        reservas.postValue(null);
                    }
                } else {
                    reservas.postValue(null);
                }
                loading.postValue(false);  // Indicar que la carga ha finalizado
            }
        });
    }

    public void loadReservasByDate(Date selectedDate) {
        loading.postValue(true);  // Indicar que la carga está en progreso
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/")).newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);  // Indicar que la carga ha finalizado
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);
                } else {
                    Log.e("ReservasViewModel", "Failed to fetch reservations");
                    reservas.postValue(new ArrayList<>());
                }
                loading.postValue(false);  // Indicar que la carga ha finalizado
            }
        });
    }

    private void processResponseData(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            List<Reserva> reservas = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

            for (int i = 0; i < jsonArray.length(); i++) {
                // Añade una comprobación para asegurarte de que el elemento es un JSONObject antes de procesarlo
                if (jsonArray.get(i) instanceof JSONObject) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Reserva reserva = new Reserva();
                    reserva.fromJSON(jsonObject);
                    Log.d("ViewModel", "Reserva procesada: " + reserva);
                    reservas.add(reserva);
                } else {
                    // Manejo de tipos no JSONObject, si necesario
                    Log.d("ViewModel", "Elemento no es un JSONObject, es de tipo: " + jsonArray.get(i).getClass().getSimpleName());
                }
            }
            this.reservas.postValue(reservas);
        } catch (JSONException e) {
            Log.e("ViewModel", "Error parsing reservations from JSON", e);
            this.reservas.postValue(new ArrayList<>()); // Publicar una lista vacía en caso de error
        }
    }


    public void filterReservasByName(String name) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/")).newBuilder()
                .addQueryParameter("nombre", name)
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations by name", e);
                errorMessages.postValue("Error al cargar las reservas por nombre: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);
                } else {
                    Log.e("ReservasViewModel", "Failed to fetch reservations by name");
                    reservas.postValue(new ArrayList<>());
                }
            }
        });
    }


    public void filterReservasByState(String state) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/")).newBuilder()
                .addQueryParameter("estado", state)  // Asegúrate de que el nombre del parámetro coincide con el usado en el backend
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations by state", e);
                errorMessages.postValue("Error al cargar las reservas por estado: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);
                } else {
                    Log.e("ReservasViewModel", "Failed to fetch reservations by state");
                    reservas.postValue(new ArrayList<>());
                }
            }
        });
    }
}
