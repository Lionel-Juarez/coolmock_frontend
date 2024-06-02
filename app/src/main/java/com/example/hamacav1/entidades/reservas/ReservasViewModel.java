package com.example.hamacav1.entidades.reservas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;
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
        loading.postValue(true);  // Indicar que la carga está en progreso
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "http://10.0.2.2:8080/api/reservas/";
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
                loading.postValue(false);  // Indicar que la carga ha finalizado
                reservas.postValue(new ArrayList<>()); // Publicar una lista vacía en caso de error
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
    public void filterReservasByName(String name) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/")).newBuilder()
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
        loading.postValue(true);  // Indicar que la carga está en progreso
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/fecha-estado")).newBuilder()
                .addQueryParameter("fecha", formattedDate)
                .addQueryParameter("estado", estado)
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
                Log.e("ReservasViewModel", "Error loading reservations by date and state", e);
                errorMessages.postValue("Error al cargar las reservas: " + e.getMessage());
                loading.postValue(false);  // Indicar que la carga ha finalizado
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    processResponseData(responseData);
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
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse("http://10.0.2.2:8080/api/reservas/")).newBuilder()
                .addQueryParameter("estado", state)  // Asegúrate de que el nombre del parámetro coincide con el usado en el backend
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
            this.reservas.postValue(new ArrayList<>()); // Publicar una lista vacía en caso de error
        }
    }

    public void updateReserva(Reserva reserva, Consumer<Boolean> callback) {
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

                JSONArray sombrillasArray = new JSONArray();
                for (Sombrilla sombrilla : reserva.getSombrillas()) {
                    JSONObject sombrillaJson = new JSONObject();
                    sombrillaJson.put("idSombrilla", sombrilla.getIdSombrilla());
                    sombrillaJson.put("reservada", false);
                    sombrillaJson.put("ocupada", true);
                    sombrillasArray.put(sombrillaJson);
                }
                json.put("sombrillas", sombrillasArray);

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

}
