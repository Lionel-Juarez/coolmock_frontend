package com.example.hamacav1.entidades.reservas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hamacav1.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    private MutableLiveData<String> errorMessages = new MutableLiveData<>();

    public LiveData<String> getErrorMessages() {
        return errorMessages;
    }


    public MutableLiveData<List<Reserva>> getReservas() {
        if (reservas == null) {
            reservas = new MutableLiveData<>();
            loadReservas();
        }
        return reservas;
    }

    public void loadReservas() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/reservas/";  // Asegúrate de usar la URL correcta
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                reservas.postValue(new ArrayList<>()); // Post an empty list instead of null
            }


            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
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
                        reservas.postValue(listaReservas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        reservas.postValue(null);
                    }
                } else {
                    reservas.postValue(null);
                }
            }
        });
    }
    public void loadReservasByDate(Date selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        OkHttpClient client = new OkHttpClient();
        HttpUrl url = HttpUrl.parse("http://10.0.2.2:8080/api/reservas/").newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
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
            }
        });
    }

    private void processResponseData(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            List<Reserva> reservas = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Reserva reserva = new Reserva();
                reserva.fromJSON(jsonObject);
                reservas.add(reserva);
            }
            this.reservas.postValue(reservas);
        } catch (JSONException e) {
            Log.e("ViewModel", "Error parsing reservations from JSON", e);
            this.reservas.postValue(new ArrayList<>()); // Publicar una lista vacía en caso de error
        }
    }



    public void filterReservasByName(String name) {
        List<Reserva> filteredList = reservas.getValue().stream()
                .filter(reserva -> reserva.getCliente().getNombreCompleto().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
        reservas.postValue(filteredList);
    }

    public void filterReservasByState(String state) {
        List<Reserva> filteredList = reservas.getValue().stream()
                .filter(reserva -> reserva.getEstado().equalsIgnoreCase(state))
                .collect(Collectors.toList());
        reservas.postValue(filteredList);
    }



}
