package com.example.hamacav1.entidades.reservas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hamacav1.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ReservasViewModel extends ViewModel {
    private MutableLiveData<List<Reserva>> reservas;

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
                e.printStackTrace();
                reservas.postValue(null); // Considera postear una lista vacía en lugar de null
                // Informa a la interfaz de usuario que la carga falló, por ejemplo mediante un Toast
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
}
