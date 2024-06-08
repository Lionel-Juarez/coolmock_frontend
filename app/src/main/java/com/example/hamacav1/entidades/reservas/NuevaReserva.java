package com.example.hamacav1.entidades.reservas;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.clientes.NuevoCliente;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private AutoCompleteTextView actvCliente;
    private Spinner spMetodoPago;
    private CheckBox cbPagada;
    private String fechaReservaSeleccionada;
    private List<Long> idsSombrillas;
    private RadioButton radioOne, radioTwo;
    private NumberPicker numberPickerHoraLlegada;
    private List<Cliente> clientsList;
    private ArrayAdapter<Cliente> clienteAdapter;

    private final ActivityResultLauncher<Intent> nuevoClienteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Cliente newClient = (Cliente) result.getData().getSerializableExtra("cliente");
                    if (newClient != null) {
                        clientsList.add(newClient);
                        clienteAdapter.notifyDataSetChanged();
                        actvCliente.setText(newClient.getNombreCompleto());
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reserva);

        initializeUIComponents();
        loadData();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initializeUIComponents() {
        ImageView btnOpenCalendar = findViewById(R.id.btnOpenCalendar);
        spMetodoPago = findViewById(R.id.sp_metodo_pago);
        actvCliente = findViewById(R.id.actv_cliente);
        cbPagada = findViewById(R.id.cb_reserva_pagada);
        radioOne = findViewById(R.id.radioOne);
        radioTwo = findViewById(R.id.radioTwo);
        numberPickerHoraLlegada = findViewById(R.id.numberPickerHoraLlegada);

        // Configurar NumberPicker con el array de horas
        String[] hours = getResources().getStringArray(R.array.hour_array);
        numberPickerHoraLlegada.setMinValue(0);
        numberPickerHoraLlegada.setMaxValue(hours.length - 1);
        numberPickerHoraLlegada.setDisplayedValues(hours);
        numberPickerHoraLlegada.setValue(0);

        btnOpenCalendar.setOnClickListener(v -> showDatePickerDialog());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.payment_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMetodoPago.setAdapter(adapter);

        radioOne.setChecked(true);
    }


    private void loadData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("idsSombrillas")) {
            Serializable serializable = intent.getSerializableExtra("idsSombrillas");
            if (serializable instanceof ArrayList) {
                idsSombrillas = new ArrayList<>();
                for (Object obj : (ArrayList<?>) serializable) {
                    if (obj instanceof Long) {
                        idsSombrillas.add((Long) obj);
                    }
                }
            }
        }
        Log.d("NuevaReserva", "ID de la sombrilla: " + idsSombrillas);

        loadClientsFromBackend();
    }


    public void addReserva(View view) {
        String fechaReserva = fechaReservaSeleccionada;

        // Si la fecha de reserva no está seleccionada o es null, se asigna la fecha actual
        if (fechaReserva == null || fechaReserva.isEmpty()) {
            Calendar calendarNow = Calendar.getInstance();
            SimpleDateFormat sdfNow = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            fechaReserva = sdfNow.format(calendarNow.getTime());
        }

        String estado = "Pendiente";
        String metodoPago = spMetodoPago.getSelectedItem().toString();
        boolean pagada = cbPagada.isChecked();
        String cantidadHamacas = getSelectedSide();

        Calendar calendarNow = Calendar.getInstance();
        SimpleDateFormat sdfNow = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String fechaReservaRealizada = sdfNow.format(calendarNow.getTime());

        String[] hours = getResources().getStringArray(R.array.hour_array);
        String horaLlegada = hours[numberPickerHoraLlegada.getValue()];

        Cliente cliente = getSelectedCliente();
        if (cliente == null) {
            Toast.makeText(this, "Por favor seleccione un cliente válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validateInput(cliente, idsSombrillas, horaLlegada, cantidadHamacas)) {
            if (Internetop.getInstance(getApplicationContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reservas) + "nuevaReserva";
                sendTask(url, fechaReserva, fechaReservaRealizada, estado, pagada, metodoPago, cliente.getIdCliente(), idsSombrillas, cantidadHamacas, horaLlegada);
            } else {
                Utils.showError(getApplicationContext(), "No hay conexión a Internet.");
            }
        }
    }


    private void sendTask(String url, String fechaReserva, String fechaReservaRealizada, String estado, boolean pagada, String metodoPago, long idCliente, List<Long> idsSombrillas, String cantidadHamacas, String horaLlegada) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Log.e("sendTask", "El usuario no está autenticado.");
                    handler.post(() -> Utils.showError(getApplicationContext(), "El usuario no está autenticado."));
                    return;
                }
                String uidUsuario = user.getUid();
                String nombreUsuario = MainActivity.nombreUsuario;

                json.put("fechaReserva", Utils.convertToIso8601(fechaReserva));
                json.put("fechaReservaRealizada", Utils.convertToIso8601(fechaReservaRealizada));
                json.put("estado", estado);
                json.put("pagada", pagada);
                json.put("metodoPago", metodoPago);
                json.put("horaLlegada", horaLlegada);
                json.put("idCliente", idCliente);
                json.put("uid", uidUsuario); // Asegúrate de usar "uid" aquí
                json.put("nombreUsuario", nombreUsuario);
                json.put("idSombrillas", new JSONArray(idsSombrillas));
                if (pagada) {
                    json.put("fechaPago", Utils.convertToIso8601(fechaReserva));
                }

                Log.d("NombreUsuario", nombreUsuario);

                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken)
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
                            updateSombrillasAsReserved(idsSombrillas, idReserva, cantidadHamacas);

                            handler.post(() -> {
                                List<Integer> numSombrillas = idsSombrillas.stream().map(Long::intValue).collect(Collectors.toList());
                                String titulo = getResources().getString(R.string.titulo_reservando_sombrilla);
                                String descripcion = "Sombrilla/s " + numSombrillas + " reservadas, cantidad: " + cantidadHamacas;

                                NuevoReporte.crearReporte(getApplicationContext(), titulo, descripcion);

                                Toast.makeText(getApplicationContext(), "Reserva añadida con éxito", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            });
                        } else {
                            throw new IOException("El cuerpo de la respuesta está vacío");
                        }
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        Log.e("sendTask", "Error al añadir reserva, respuesta del servidor: " + errorMessage);
                        handler.post(() -> Utils.showError(getApplicationContext(), "Error al añadir reserva: " + errorMessage));
                    }
                } catch (Exception e) {
                    Log.e("sendTask", "Excepción al enviar datos de reserva: " + e.getMessage(), e);
                    handler.post(() -> Utils.showError(getApplicationContext(), "Error de conexión al servidor: " + e.getMessage()));
                }
            } catch (Exception e) {
                Log.e("sendTask", "Excepción al enviar datos de reserva: " + e.getMessage(), e);
                handler.post(() -> Utils.showError(getApplicationContext(), "Error de conexión al servidor: " + e.getMessage()));
            }
        });
    }


    private void updateSombrillasAsReserved(List<Long> idsSombrillas, long idReserva, String cantidadHamacas) {
        Log.e("UpdateSombrilla", "dentro de la funcion updateSombrillas");

        OkHttpClient client = new OkHttpClient();
        for (Long idSombrilla : idsSombrillas) {
            String url = getResources().getString(R.string.url_sombrillas) + "updateReservaSombrilla/" + idSombrilla;
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("idReserva", String.valueOf(idReserva));
            urlBuilder.addQueryParameter("cantidadHamacas", cantidadHamacas);

            Request request = new Request.Builder()
                    .url(urlBuilder.build().toString())
                    .patch(RequestBody.create("", null))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e("updateSombrilla", "Error al actualizar sombrilla ID " + idSombrilla + ": " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        assert response.body() != null;
                        Log.e("updateSombrilla", "Error al actualizar sombrilla ID " + idSombrilla + ", respuesta del servidor: " + response.body().string());
                    } else {
                        Log.d("updateSombrilla", "Sombrilla ID " + idSombrilla + " actualizada correctamente.");
                    }
                }
            });
        }
    }

    private Cliente getSelectedCliente() {
        String clienteNombre = actvCliente.getText().toString();
        for (Cliente cliente : clientsList) {
            if (cliente.getNombreCompleto().equals(clienteNombre)) {
                return cliente;
            }
        }
        return null;
    }

    private void updateClientsAutoComplete(List<Cliente> clientsList) {
        this.clientsList = clientsList;
        clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, clientsList);
        actvCliente.setAdapter(clienteAdapter);
        actvCliente.setThreshold(0);
        Log.d("NuevaReserva", "Clientes actualizados en la interfaz de usuario.");
    }

    private void loadClientsFromBackend() {
        String url = getResources().getString(R.string.url_clientes);
        OkHttpClient client = new OkHttpClient();

        Log.d("NuevaReserva", "Iniciando carga de clientes desde el backend: " + url);
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("NuevaReserva", "Error al cargar clientes: ", e);
                runOnUiThread(() -> Toast.makeText(NuevaReserva.this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("NuevaReserva", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                assert response.body() != null;
                final String responseData = response.body().string();
                Log.d("NuevaReserva", "Clientes cargados correctamente: " + responseData);

                runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<Cliente> clientsList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            long id = jsonObject.getLong("idCliente");
                            String nombre = jsonObject.getString("nombreCompleto");
                            String telefono = jsonObject.optString("numeroTelefono");
                            String email = jsonObject.optString("email");
                            String rol = jsonObject.optString("rol");
                            String uid = null;
                            clientsList.add(new Cliente(id, nombre, telefono, email, rol, uid));
                        }
                        updateClientsAutoComplete(clientsList);
                    } catch (JSONException e) {
                        Log.e("NuevaReserva", "Error al parsear clientes: ", e);
                    }
                });
            }
        });
    }

    public void openNuevoCliente(View view) {
        Intent intent = new Intent(this, NuevoCliente.class);
        nuevoClienteLauncher.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Cliente newClient = (Cliente) data.getSerializableExtra("cliente");
            if (newClient != null) {
                clientsList.add(newClient);
                clienteAdapter.notifyDataSetChanged();
                actvCliente.setText(newClient.getNombreCompleto());
            }
        }
    }

    private String getSelectedSide() {
        radioOne = findViewById(R.id.radioOne);
        radioTwo = findViewById(R.id.radioTwo);

        if (radioOne.isChecked()) {
            return "1";
        } else if (radioTwo.isChecked()) {
            return "2";
        } else {
            radioOne.setChecked(true);
            return "1";
        }
    }

    private boolean validateInput(Cliente cliente, List<Long> idsSombrillas, String horaLlegada, String cantidadHamacas) {
        boolean isValid = true;
        if (cliente == null || cliente.getIdCliente() <= 0 || idsSombrillas == null || idsSombrillas.isEmpty()) {
            Utils.showError(getApplicationContext(), "Información crítica de la reserva está incompleta o incorrecta.");
            isValid = false;
        }
        if (horaLlegada == null) {
            Utils.showError(getApplicationContext(), "Hora de llegada no seleccionada.");
            isValid = false;
        }
        if (cantidadHamacas == null) {
            Utils.showError(getApplicationContext(), "Cantidad de hamacas no seleccionada.");
            isValid = false;
        }
        return isValid;
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

    // Método cancel que cierra la actividad actual usando Utils
    public void cancel(View view) {
        Utils.closeActivity(this);
    }

}

