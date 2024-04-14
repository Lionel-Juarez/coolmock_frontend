package com.example.hamacav1.entidades.reservas;


import android.app.Activity;
import android.content.Context;
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
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.hamacas.Hamaca;
import com.example.hamacav1.entidades.reports.Report;
import com.example.hamacav1.entidades.usuarios.Usuario;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NuevaReserva extends AppCompatActivity {


    private CalendarView calendarViewReserva;
    private Spinner spCliente, spEstado, spMetodoPago;
    private CheckBox cbPagada;
    private Button btnGuardarReserva;
    private String fechaReservaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reserva);

        calendarViewReserva = findViewById(R.id.calendarView);
        spMetodoPago = findViewById(R.id.sp_metodo_pago);
        spCliente = findViewById(R.id.sp_cliente);
        cbPagada = findViewById(R.id.cb_reserva_pagada);
        btnGuardarReserva = findViewById(R.id.btn_save_reserva);
        spEstado = findViewById(R.id.sp_estado);
        spCliente = findViewById(R.id.sp_cliente);

        // Configura el listener del CalendarView para capturar la fecha seleccionada
        calendarViewReserva.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Guardar la fecha seleccionada en el formato "dd-MM-yyyy"
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                fechaReservaSeleccionada = sdf.format(calendar.getTime());
            }
        });

        btnGuardarReserva.setOnClickListener(v -> guardarReserva());

        // Cargar clientes en el spinner
        loadClientsFromBackend();
    }

    private void loadClientsFromBackend() {
        String url = getResources().getString(R.string.url_clientes);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Log.d("NuevaReserva", "Iniciando carga de clientes desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("NuevaReserva", "Error al cargar clientes: ", e);
                // Mostrar algún mensaje de error en la interfaz de usuario
                runOnUiThread(() -> Toast.makeText(NuevaReserva.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("NuevaReserva", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                final String responseData = response.body().string();
                Log.d("NuevaReserva", "Clientes cargados correctamente: " + responseData);

                runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<Cliente> clientsList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Long id = jsonObject.getLong("idCliente");
                            String nombre = jsonObject.getString("nombreCompleto");
                            String telefono = jsonObject.optString("numeroTelefono"); // Opcional si no todos los clientes tienen teléfono
                            clientsList.add(new Cliente(id, nombre, telefono));
                        }
                        updateClientsSpinner(clientsList);
                    } catch (JSONException e) {
                        Log.e("NuevaReserva", "Error al parsear clientes: ", e);
                    }
                });
            }
        });
    }

    private void updateClientsSpinner(List<Cliente> clientsList) {
        ArrayAdapter<Cliente> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCliente.setAdapter(adapter);
        Log.d("NuevaReserva", "Clientes actualizados en la interfaz de usuario.");
    }


    private void guardarReserva() {
        if (validateInput()) {
            Cliente selectedCliente = (Cliente) spCliente.getSelectedItem();
            String estado = spEstado.getSelectedItem().toString();
            String metodoPago = spMetodoPago.getSelectedItem().toString();

            // Construcción de la reserva con todos los campos necesarios
            Reserva reserva = new Reserva();
            reserva.setCliente(selectedCliente);
            reserva.setEstado(estado);
            reserva.setMetodoPago(metodoPago);
            reserva.setPagada(cbPagada.isChecked());
            reserva.setFechaReserva(fechaReservaSeleccionada);

            // Simula obtener un Usuario y una Hamaca, reemplazar con lógica real
            Usuario usuarioCreador = obtenerUsuarioCreador();
            Hamaca hamacaAsociada = obtenerHamacaSeleccionada();  // Esta función debe implementarse según cómo se gestione la selección de hamacas

            reserva.setCreadoPor(usuarioCreador);
            reserva.setHamaca(hamacaAsociada);

            if (isNetworkAvailable()) {
                sendTask(reserva);
            } else {
                showError("No hay conexión a Internet.");
            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private Boolean isNetworkAvailable() {
        /*La clase ConnectivityManager nos devolverá información sobre el estado de la conexión a
         * Internet. Puede ser que no tengamos cobertura o que directamente no tengamos activado
         * la red WiFi o la red de datos móviles*/
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /*Si la versión de Android es superior a Android M, debemos usar las clase Network
             * en lugar de NetworkInfo para comprobar la conectividad*/
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) {
                return false;
            } else {
                NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
                return (actNw != null) && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }

    private boolean validateInput() {
        if (fechaReservaSeleccionada == null || fechaReservaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione una fecha.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void sendTask(Reserva reserva) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject json = new JSONObject();
                json.put("idCliente", reserva.getCliente().getIdCliente());  // Asegúrate de que el cliente tiene un id
                json.put("estado", reserva.getEstado());
                json.put("metodoPago", reserva.getMetodoPago());
                json.put("pagada", reserva.isPagada());
                json.put("fechaReserva", reserva.getFechaReserva());

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url("URL_A_TU_API_DE_RESERVAS").post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    handler.post(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Reserva guardada con éxito", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            showError("Error al guardar reserva: " + result);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("NuevaReserva", "Excepción al enviar reserva: " + e.getMessage(), e);
                handler.post(() -> showError("Error al procesar la solicitud."));
            }
        });
    }


    private Usuario obtenerUsuarioCreador() {
        // Debes implementar la lógica para obtener el usuario que crea la reserva
        // Puede ser un usuario actualmente logueado o similar
        return new Usuario();  // Retorna un objeto usuario adecuado
    }

    private Hamaca obtenerHamacaSeleccionada() {
        // Implementa la lógica para obtener la hamaca seleccionada, si es relevante
        return new Hamaca();  // Retorna la hamaca seleccionada o por defecto
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
