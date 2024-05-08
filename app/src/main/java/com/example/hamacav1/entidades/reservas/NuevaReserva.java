package com.example.hamacav1.entidades.reservas;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
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
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.hamacas.Hamaca;
import com.example.hamacav1.entidades.usuarios.Usuario;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NuevaReserva extends AppCompatActivity {
    private CalendarView calendarViewReserva;
    private Spinner spCliente, spEstado, spMetodoPago;
    private CheckBox cbPagada;
    private Button btnGuardarReserva, btnCancelarReserva;
    private String fechaReservaSeleccionada, fechaPago;
    private RadioGroup rgHamacaSide;
    private List<Cliente> fullClientsList;

    private List<Long> idsHamacas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reserva);

        // Obtener el ID de la hamaca desde el Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("idsHamacas")) {
            idsHamacas = (ArrayList<Long>) intent.getSerializableExtra("idsHamacas"); // Recibiendo una lista
        }
        Log.d("NuevaReserva", "ID de la hamaca: " + idsHamacas);

        Button btnOpenCalendar = findViewById(R.id.btnOpenCalendar);
        btnOpenCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });        spMetodoPago = findViewById(R.id.sp_metodo_pago);
        spCliente = findViewById(R.id.sp_cliente);
        cbPagada = findViewById(R.id.cb_reserva_pagada);
        btnGuardarReserva = findViewById(R.id.btn_save_reserva);
        btnCancelarReserva = findViewById(R.id.btn_cancel_reserva);
        spCliente = findViewById(R.id.sp_cliente);

//        // Configura el listener del CalendarView para capturar la fecha seleccionada
//        calendarViewReserva.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
//                // Guardar la fecha seleccionada en el formato "dd-MM-yyyy"
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(year, month, dayOfMonth);
//                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
//                fechaReservaSeleccionada = sdf.format(calendar.getTime());
//
//            }
//        });

        // Cargar clientes en el spinner
        loadClientsFromBackend();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private Boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    public void addReserva(View view) {
        String fechaReserva = fechaReservaSeleccionada;
//        String estado = spEstado.getSelectedItem().toString();
        String estado = "Pendiente";
        String metodoPago = spMetodoPago.getSelectedItem().toString();
        boolean pagada = cbPagada.isChecked();
        Cliente cliente = (Cliente) spCliente.getSelectedItem();
        String lado = getSelectedSide();


        if (cliente == null) {
            showError("No se ha seleccionado ningún cliente.");
            return;
        }

        long idCliente = cliente.getIdCliente();
        if (idCliente <= 0) {
            showError("El ID del cliente no es válido.");
            return;
        }

        if (validateInput(fechaReserva, cliente, idsHamacas)) {
            if (isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reservas) + "nuevaReserva";
                sendTask(url, fechaReserva, estado, pagada, metodoPago, cliente.getIdCliente(), idsHamacas, 1, lado);
            } else {
                showError("No hay conexión a Internet.");
            }
        }

    }

    private String getSelectedSide() {
        RadioButton radioLeft = findViewById(R.id.radioLeft);
        RadioButton radioRight = findViewById(R.id.radioRight);

        boolean leftSelected = radioLeft.isChecked();
        boolean rightSelected = radioRight.isChecked();

        String lado = "";

        if (leftSelected && rightSelected) {
            lado = "ambos";
        } else if (leftSelected) {
            lado = "izquierda";
        } else if (rightSelected) {
            lado = "derecha";
        }

        return lado;
    }



    private boolean validateInput(String fechaReserva, Cliente cliente, List<Long> idsHamacas) {
        boolean isValid = true;
        if (fechaReserva == null || fechaReserva.isEmpty()) {
            showError("Fecha de reserva no seleccionada.");
            isValid = false;
        }
        if (cliente == null || cliente.getIdCliente() <= 0 || idsHamacas == null || idsHamacas.isEmpty()) {
            showError("Información crítica de la reserva está incompleta o incorrecta.");
            isValid = false;
        }
        return isValid;
    }

    private void sendTask(String url, String fechaReserva, String estado, boolean pagada, String metodoPago, long idCliente, List<Long> idsHamacas, long idUsuario, String lado) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("fechaReserva", convertToIso8601(fechaReserva));  // Asegurarse de que la fecha está en formato ISO-8601
                json.put("estado", estado);
                json.put("pagada", pagada);
                json.put("metodoPago", metodoPago);
                json.put("idCliente", idCliente);
                json.put("idUsuario", idUsuario);
                json.put("idHamacas", new JSONArray(idsHamacas));
                json.put("lado", lado); //

                if (pagada) {
                    json.put("fechaPago", convertToIso8601(fechaReserva));  // Usar la misma fecha de reserva para la fecha de pago
                }

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String responseData = responseBody.string();
                            JSONObject responseObject = new JSONObject(responseData);
                            long idReserva = responseObject.getLong("idReserva");
                            Log.d("NuevaReserva", "Reserva creada con éxito, ID: " + idReserva);
                            updateHamacasAsReserved(idsHamacas, idReserva, lado);

                            handler.post(() -> {
                                Toast.makeText(getApplicationContext(), "Reserva añadida con éxito", Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_OK);
                                finish();
                            });
                        } else {
                            throw new IOException("El cuerpo de la respuesta está vacío");
                        }
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        Log.e("sendTask", "Error al añadir reserva, respuesta del servidor: " + errorMessage);
                        handler.post(() -> showError("Error al añadir reserva: " + errorMessage));
                    }
                } catch (Exception e) {
                    Log.e("sendTask", "Excepción al enviar datos de reserva: " + e.getMessage(), e);
                    handler.post(() -> showError("Error de conexión al servidor: " + e.getMessage()));
                }
            } catch (Exception e) {
                Log.e("sendTask", "Excepción al enviar datos de reserva: " + e.getMessage(), e);
                handler.post(() -> showError("Error de conexión al servidor: " + e.getMessage()));
            }
        });
    }


    // Función para convertir la fecha al formato ISO-8601
    private String convertToIso8601(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            Log.e("convertToIso8601", "Error parsing date: " + e.getMessage());
            return null;
        }
    }


    private void updateClientsSpinner(List<Cliente> clientsList) {
        ArrayAdapter<Cliente> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCliente.setAdapter(adapter);
        Log.d("NuevaReserva", "Clientes actualizados en la interfaz de usuario.");
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
    private void updateHamacasAsReserved(List<Long> idsHamacas, long idReserva, String lado) {
        for (Long idHamaca : idsHamacas) {
            // Cambiar a la nueva URL del endpoint que maneja la actualización de la reserva específicamente
            String urlUpdate = getResources().getString(R.string.url_hamacas) + "updateReservaHamaca/" + idHamaca;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            HttpUrl.Builder urlBuilder = HttpUrl.parse(urlUpdate).newBuilder();
            urlBuilder.addQueryParameter("idReserva", String.valueOf(idReserva)); // Agregar idReserva como parámetro de consulta
            urlBuilder.addQueryParameter("lado", lado);
            Request request = new Request.Builder()
                    .url(urlBuilder.build()) // Construir la URL con el parámetro
                    .patch(RequestBody.create("", JSON)) // Usar método PATCH y enviar cuerpo vacío si el ID de reserva se pasa como parámetro de URL
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("HamacaUpdate", "Failed to update hamaca: " + e.getMessage(), e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("HamacaUpdate", "Server response unsuccessful while updating hamaca: " + response.code());
                    } else {
                        Log.d("HamacaUpdate", "Successfully updated hamaca with ID: " + idHamaca);
                        // Opcionalmente, activar actualizaciones de UI o acciones adicionales
                    }
                }
            });
        }
    }
    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                    fechaReservaSeleccionada = sdf.format(selectedDate.getTime());
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private Usuario obtenerUsuarioCreador() {
        // Debes implementar la lógica para obtener el usuario que crea la reserva
        // Puede ser un usuario actualmente logueado o similar
        return new Usuario();  // Retorna un objeto usuario adecuado
    }

    public void cancel(View view) {
        // Simplemente termina la actividad actual
        finish();

        // Si necesitas navegar a otro fragmento o actividad al cancelar, necesitarás implementar la lógica aquí.
        // Esto podría incluir comunicarse con el fragmento de reserva a través de un Intent o utilizando un ViewModel.
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

