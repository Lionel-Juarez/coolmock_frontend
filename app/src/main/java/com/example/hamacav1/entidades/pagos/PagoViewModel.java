package com.example.hamacav1.entidades.pagos;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PagoViewModel extends ViewModel {
    private MutableLiveData<List<Pago>> pagos;
    @Getter
    private final MutableLiveData<String> errorMessages = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final Context context;

    public PagoViewModel(Context context) {
        this.context = context.getApplicationContext();
    }

    public MutableLiveData<List<Pago>> getPagos() {
        if (pagos == null) {
            pagos = new MutableLiveData<>();
        }
        return pagos;
    }

    public void createPago(Pago pago, Consumer<Boolean> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Handler handler = new Handler(Looper.getMainLooper());
        Calendar calendarNow = Calendar.getInstance();
        SimpleDateFormat sdfNow = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String fechaPago = sdfNow.format(calendarNow.getTime());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("reserva", new JSONObject().put("idReserva", pago.getReserva().getIdReserva()));
                json.put("cantidad", pago.getCantidad());
                json.put("metodoPago", pago.getMetodoPago());
                json.put("pagado", pago.isPagado());
                json.put("fechaPago", Utils.convertToIso8601(fechaPago));
                json.put("detallesPago", pago.getDetallesPago());
                json.put("tipoHamaca", pago.getTipoHamaca());

                SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Log.d("PagoViewModel", "JSON Body: " + json);

                Request request = new Request.Builder()
                        .url(context.getString(R.string.url_pagos) + "/nuevoPago")
                        .addHeader("Authorization", "Bearer " + idToken)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d("PagoViewModel", "Response Code: " + response.code());  // Log para el código de respuesta
                    if (response.isSuccessful()) {
                        handler.post(() -> callback.accept(true));
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        Log.e("PagoViewModel", "Error Message: " + errorMessage);  // Log para el mensaje de error
                        handler.post(() -> {
                            Utils.showError(context, "Error al crear el pago: " + errorMessage);
                            callback.accept(false);
                        });
                    }
                } catch (Exception e) {
                    Log.e("PagoViewModel", "Exception during request execution: " + e.getMessage(), e);  // Log para excepciones durante la ejecución de la solicitud
                    handler.post(() -> {
                        Utils.showError(context, "Error de conexión al servidor: " + e.getMessage());
                        callback.accept(false);
                    });
                }
            } catch (Exception e) {
                Log.e("PagoViewModel", "Exception during JSON creation: " + e.getMessage(), e);  // Log para excepciones durante la creación del JSON
                handler.post(() -> {
                    Utils.showError(context, "Error al preparar datos: " + e.getMessage());
                    callback.accept(false);
                });
            }
        });
    }
}




