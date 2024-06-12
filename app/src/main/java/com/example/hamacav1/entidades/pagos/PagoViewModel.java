package com.example.hamacav1.entidades.pagos;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Utils;
import com.google.firebase.database.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PagoViewModel extends AndroidViewModel {
    @Getter
    private MutableLiveData<List<Pago>> pagos = new MutableLiveData<>();  // Inicialización directa
    @Getter
    private final MutableLiveData<String> errorMessages = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final Context context;

    public PagoViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();
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

    public void createPagoSinReserva(Pago pago, Consumer<Boolean> callback) {
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
                Log.d("PagoViewModel", "Iniciando creación de JSON");
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
                        .url(context.getString(R.string.url_pagos) + "/nuevoPagoSinReserva")
                        .addHeader("Authorization", "Bearer " + idToken)
                        .post(body)
                        .build();

                Log.d("PagoViewModel", "Realizando solicitud HTTP a la URL: " + request.url());

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

    public void loadAllPagos() {
        loading.postValue(true);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = context.getString(R.string.url_pagos);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                Log.e("PagosViewModel", "Error loading payments", e);
                errorMessages.postValue("Error al cargar los pagos: " + e.getMessage());
                loading.postValue(false);
                pagos.postValue(new ArrayList<>());
            }

            @Override
            public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);
                } else {
                    Log.e("PagosViewModel", "Failed to fetch payments");
                    pagos.postValue(new ArrayList<>());
                }
                loading.postValue(false);
            }
        });
    }

    public void loadPagosByDate(Date selectedDate) {
        Log.d("PagosViewModel", "loadPagosByDate called with date: " + selectedDate);

        loading.postValue(true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());  // Cambiar el formato a yyyy-MM-dd
        String formattedDate = sdf.format(selectedDate);

        Log.d("PagosViewModel", "Formatted date: " + formattedDate);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Log.d("PagosViewModel", "ID Token: " + idToken);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(context.getString(R.string.url_pagos))).newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .build();

        Log.d("PagosViewModel", "Request URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                Log.e("PagosViewModel", "Error loading payments", e);
                errorMessages.postValue("Error al cargar los pagos: " + e.getMessage());
                loading.postValue(false);
            }

            @Override
            public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("PagosViewModel", "Response data: " + responseData);
                    if (responseData.isEmpty()) {
                        Log.d("PagosViewModel", "No payments found for the given date");
                        pagos.postValue(new ArrayList<>());
                    } else {
                        processResponseData(responseData);
                    }
                } else {
                    Log.e("PagosViewModel", "Failed to fetch payments");
                    pagos.postValue(new ArrayList<>());
                    loading.postValue(false);
                }
            }
        });
    }

    public void filterPagosByMethod(String method) {
        Log.d("PagosViewModel", "Filtering payments by method: " + method);

        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(context.getString(R.string.url_pagos))).newBuilder()
                .addQueryParameter("metodoPago", method)
                .build();

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Log.d("PagosViewModel", "ID Token: " + idToken);
        Log.d("PagosViewModel", "Request URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                Log.e("PagosViewModel", "Error loading payments by method", e);
                errorMessages.postValue("Error al cargar los pagos por método: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("PagosViewModel", "Response data: " + responseData);
                    processResponseData(responseData);
                } else {
                    Log.e("PagosViewModel", "Failed to fetch payments by method");
                    pagos.postValue(new ArrayList<>());
                }
            }
        });
    }

    public void loadPagosByDateRange(Date startDate, Date endDate) {
        loading.postValue(true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String formattedStartDate = sdf.format(startDate);
        String formattedEndDate = sdf.format(endDate);

        Log.d("PagoViewModel", "Loading payments from " + formattedStartDate + " to " + formattedEndDate);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String baseUrl = context.getResources().getString(R.string.url_pagos);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl + "/fecha-rango")).newBuilder()
                .addQueryParameter("start", formattedStartDate)
                .addQueryParameter("end", formattedEndDate)
                .build();

        Log.d("PagoViewModel", "Request URL: " + url);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        if (idToken == null) {
            Log.e("PagosViewModel", "Token de autorización no disponible");
            loading.postValue(false);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                Log.e("PagosViewModel", "Error loading payments by date range", e);
                errorMessages.postValue("Error al cargar los pagos: " + e.getMessage());
                loading.postValue(false);
            }

            @Override
            public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("PagosViewModel", "Response data: " + responseData);
                    processResponseData(responseData);
                } else {
                    Log.e("PagosViewModel", "Failed to fetch payments by date range. Response: " + response);
                    pagos.postValue(new ArrayList<>());
                }
                loading.postValue(false);
            }
        });
    }

    private void processResponseData(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            List<Pago> pagos = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.get(i) instanceof JSONObject) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Pago pago = new Pago();
                    pago.fromJSON(jsonObject);
                    pagos.add(pago);
                }
            }
            this.pagos.postValue(pagos);
        } catch (JSONException e) {
            Log.e("PagosViewModel", "Error parsing payments from JSON", e);
            this.pagos.postValue(new ArrayList<>());
        } finally {
            loading.postValue(false);
        }
    }
}




