package com.example.hamacav1.entidades.reservas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;
import com.example.hamacav1.entidades.sombrillas.SombrillaDetalles;
import com.example.hamacav1.util.Utils;

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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


@SuppressLint("StaticFieldLeak")
public class ReservasViewModel extends ViewModel {
    private MutableLiveData<List<Reserva>> reservas;
    @Getter
    private final MutableLiveData<String> errorMessages = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final Context context;

    private final MutableLiveData<Integer> pendientesCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> pagadasCount = new MutableLiveData<>(0);

    public ReservasViewModel(Context context) {
        this.context = context.getApplicationContext();
    }

    public MutableLiveData<List<Reserva>> getReservas() {
        if (reservas == null) {
            reservas = new MutableLiveData<>();
        }
        return reservas;
    }

    public void loadAllReservas() {
        loading.postValue(true);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = context.getString(R.string.url_reservas);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);
                reservas.postValue(new ArrayList<>());
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
                loading.postValue(false);
            }
        });
    }

    public void loadReservasByDate(Date selectedDate) {
        loading.postValue(true);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(context.getString(R.string.url_reservas))).newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);
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
                loading.postValue(false);
            }
        });
    }

    public void filterReservasByName(String name) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(context.getString(R.string.url_reservas))).newBuilder()
                .addQueryParameter("nombre", name)
                .build();

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

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

    public void loadReservasByDateAndState(Date selectedDate, String estado) {
        loading.postValue(true);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String baseUrl = context.getResources().getString(R.string.url_reservas);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl + "fecha-estado")).newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .addQueryParameter("estado", estado)
                .build();

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        if (idToken == null) {
            Log.e("ReservasViewModel", "Token de autorización no disponible");
            loading.postValue(false);
            errorMessages.postValue("Error de autenticación. Por favor, inicie sesión de nuevo.");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservasViewModel", "Error loading reservations by date and state", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);

                    // Contar hamacas pendientes y pagadas
                    List<Reserva> reservasList = reservas.getValue();
                    if (reservasList != null) {
                        int pendientes = 0;
                        int pagadas = 0;
                        for (Reserva reserva : reservasList) {
                            String cantidadHamacas = reserva.getCantidadHamacas();
                            if (cantidadHamacas != null && !cantidadHamacas.isEmpty()) {
                                try {
                                    int cantidad = Integer.parseInt(cantidadHamacas);
                                    if (reserva.isPagada()) {
                                        pagadas += cantidad;
                                    } else {
                                        pendientes += cantidad;
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("ReservasViewModel", "Error al convertir cantidad de hamacas a entero para la reserva con ID: " + reserva.getIdReserva(), e);
                                }
                            } else {
                                Log.e("ReservasViewModel", "Cantidad de hamacas no válida para la reserva con ID: " + reserva.getIdReserva());
                            }
                        }
                        pendientesCount.postValue(pendientes);
                        pagadasCount.postValue(pagadas);
                    }
                } else {
                    Log.e("ReservasViewModel", "Failed to fetch reservations by date and state");
                    reservas.postValue(new ArrayList<>());
                }
                loading.postValue(false);
            }
        });
    }

    public void filterReservasByState(String state) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(context.getString(R.string.url_reservas))).newBuilder()
                .addQueryParameter("estado", state)
                .build();

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

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

    private void processResponseData(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            List<Reserva> reservas = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.get(i) instanceof JSONObject) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Reserva reserva = new Reserva();
                    reserva.fromJSON(jsonObject);
                    reservas.add(reserva);
                }
            }
            this.reservas.postValue(reservas);
        } catch (JSONException e) {
            Log.e("ViewModel", "Error parsing reservations from JSON", e);
            this.reservas.postValue(new ArrayList<>());
        }
    }

    public void updatePagoReserva(Reserva reserva, Consumer<Boolean> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Calendar calendarNow = Calendar.getInstance();
        SimpleDateFormat sdfNow = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String finalFechaPago = sdfNow.format(calendarNow.getTime());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("idReserva", reserva.getIdReserva());
                json.put("pagada", reserva.isPagada());
                json.put("metodoPago", reserva.getMetodoPago());
                json.put("fechaPago", Utils.convertToIso8601(finalFechaPago));
                json.put("estado",reserva.getEstado());
                Log.e("", reserva.getEstado());

                SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(context.getString(R.string.url_reservas) + "actualizarReserva/" + reserva.getIdReserva())
                        .addHeader("Authorization", "Bearer " + idToken)
                        .put(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        handler.post(() -> {
                            Toast.makeText(context, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show();
                            loadReservasByDateAndState(new Date(), "Pendiente");
                            callback.accept(true);
                        });
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        handler.post(() -> {
                            Utils.showError(context, "Error al actualizar reserva: " + errorMessage);
                            callback.accept(false);
                        });
                    }
                } catch (Exception e) {
                    handler.post(() -> {
                        Utils.showError(context, "Error de conexión al servidor: " + e.getMessage());
                        callback.accept(false);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    Utils.showError(context, "Error al preparar datos: " + e.getMessage());
                    callback.accept(false);
                });
            }
        });
    }

    public void updateLlegadaReserva(Reserva reserva, Consumer<Boolean> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("idReserva", reserva.getIdReserva());
                json.put("estado", "Ha llegado");

                SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(context.getString(R.string.url_reservas) + "actualizarReserva/" + reserva.getIdReserva())
                        .addHeader("Authorization", "Bearer " + idToken)
                        .put(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        handler.post(() -> {
                            String titulo = context.getString(R.string.titulo_llegada_reserva);
                            String descripcion = "La reserva de " + reserva.getCliente().getNombreCompleto() + " ha llegado.";
                            NuevoReporte.crearReporte(context, titulo, descripcion);

                            Toast.makeText(context, "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show();
                            loadReservasByDateAndState(new Date(), "Pendiente");
                            callback.accept(true);
                            for (Sombrilla sombrilla : reserva.getSombrillas()) {
                                handler.post(() -> SombrillaDetalles.updateSombrillaOnServer(sombrilla, context));
                            }
                        });
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        handler.post(() -> {
                            Utils.showError(context, "Error al actualizar reserva: " + errorMessage);
                            callback.accept(false);
                        });
                    }
                } catch (Exception e) {
                    handler.post(() -> {
                        Utils.showError(context, "Error de conexión al servidor: " + e.getMessage());
                        callback.accept(false);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    Utils.showError(context, "Error al preparar datos: " + e.getMessage());
                    callback.accept(false);
                });
            }
        });
    }

    public void updateCancelacionReserva(Reserva reserva, String cancelacionDescripcion, Consumer<Boolean> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("idReserva", reserva.getIdReserva());
                json.put("estado", "Cancelada");

                SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(context.getString(R.string.url_reservas) + "actualizarReserva/" + reserva.getIdReserva())
                        .addHeader("Authorization", "Bearer " + idToken)
                        .put(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        handler.post(() -> {
                            String titulo = context.getString(R.string.titulo_cancelacion_reserva);
                            String descripcion = "La reserva de " + reserva.getCliente().getNombreCompleto() + " ha sido cancelada. Motivo: " + cancelacionDescripcion;
                            NuevoReporte.crearReporte(context, titulo, descripcion);

                            Toast.makeText(context, "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                            loadReservasByDateAndState(new Date(), "Pendiente");
                            callback.accept(true);
                            for (Sombrilla sombrilla : reserva.getSombrillas()) {
                                handler.post(() -> SombrillaDetalles.updateSombrillaOnServer(sombrilla, context));
                            }
                        });
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        handler.post(() -> {
                            Utils.showError(context, "Error al actualizar reserva: " + errorMessage);
                            callback.accept(false);
                        });
                    }
                } catch (Exception e) {
                    handler.post(() -> {
                        Utils.showError(context, "Error de conexión al servidor: " + e.getMessage());
                        callback.accept(false);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    Utils.showError(context, "Error al preparar datos: " + e.getMessage());
                    callback.accept(false);
                });
            }
        });
    }
}
