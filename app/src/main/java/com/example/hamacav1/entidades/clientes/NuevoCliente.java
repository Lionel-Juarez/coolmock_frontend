package com.example.hamacav1.entidades.clientes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NuevoCliente extends AppCompatActivity {
    private EditText etNombreCompleto;
    private EditText etNumeroTelefono;
    private EditText etEmail;
    private boolean isEditMode = false;
    private Cliente cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cliente);

        etNombreCompleto = findViewById(R.id.et_new_nombreCompleto_cliente);
        etNumeroTelefono = findViewById(R.id.et_new_telefono_cliente);
        etEmail = findViewById(R.id.et_new_email_cliente);

        if (getIntent().hasExtra("cliente")) {
            isEditMode = true;
            cliente = (Cliente) getIntent().getSerializableExtra("cliente");
            assert cliente != null;
            fillClientData(cliente);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String rol = sharedPreferences.getString("rol", "CLIENTE");

        if ("CLIENTE".equals(rol)) {
            etEmail.setEnabled(false);
        }
    }

    private void fillClientData(Cliente cliente) {
        etNombreCompleto.setText(cliente.getNombreCompleto());
        etNumeroTelefono.setText(cliente.getNumeroTelefono());
        etEmail.setText(cliente.getEmail());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    public void saveCliente(View view) {
        String nombreCompleto = etNombreCompleto.getText().toString();
        String numeroTelefono = etNumeroTelefono.getText().toString();
        String email = etEmail.getText().toString();

        if (validateInput(nombreCompleto, numeroTelefono, email)) {
            if (Internetop.getInstance(getApplicationContext()).isNetworkAvailable()) {
                String url;
                if (isEditMode) {
                    url = getResources().getString(R.string.url_clientes) + "actualizarCliente/" + cliente.getIdCliente();
                } else {
                    url = getResources().getString(R.string.url_clientes) + "nuevoCliente";
                }
                sendTask(url, nombreCompleto, numeroTelefono, email);
            } else {
                Utils.showError(getApplicationContext(), "error.IOException");
            }
        }
    }

    private boolean validateInput(String nombre, String telefono, String email) {
        boolean isValid = true;
        Resources res = getResources();

        if (nombre.isEmpty()) {
            etNombreCompleto.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (nombre.length() > 50) {
            etNombreCompleto.setError(res.getString(R.string.error_nombre_largo));
            isValid = false;
        }

        if (telefono.isEmpty()) {
            etNumeroTelefono.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (!telefono.matches("\\d{9,15}")) {
            etNumeroTelefono.setError(res.getString(R.string.error_telefono_formato));
            isValid = false;
        }
        if (email.isEmpty()) {
            etEmail.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (email.length() > 50) {
            etEmail.setError(res.getString(R.string.error_nombre_largo));
            isValid = false;
        }
        return isValid;
    }
    private void sendTask(String url, String nombreCompleto, String numeroTelefono, String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("nombreCompleto", nombreCompleto);
                json.put("numeroTelefono", numeroTelefono);
                json.put("email", email);
                json.put("rol", "CLIENTE");

                // Añadir el uid solo si está disponible
                String uid = null;
                if (MainActivity.rol.equals("CLIENTE")) {
                    uid = cliente.getUid(); // Usar el uid del cliente actual si el rol es CLIENTE
                }

                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String idToken = sharedPreferences.getString("idToken", null);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken);

                if (isEditMode) {
                    requestBuilder.put(body); // Usar PUT para actualizar
                } else {
                    requestBuilder.post(body); // Usar POST para crear
                }

                Request request = requestBuilder.build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String responseData = responseBody.string();
                            JSONObject responseObject = new JSONObject(responseData);
                            long idCliente = responseObject.getLong("idCliente");
                            Cliente newClient = new Cliente(idCliente, nombreCompleto, numeroTelefono, email, "CLIENTE", uid);

                            handler.post(() -> {
                                String titulo = getResources().getString(R.string.creacion_cliente);
                                String descripcion = "Cliente " + nombreCompleto + " creado con éxito.";

                                NuevoReporte.crearReporte(getApplicationContext(), titulo, descripcion);
                                // Mostrar mensaje de éxito
                                Toast.makeText(NuevoCliente.this, getString(R.string.cliente_modificado), Toast.LENGTH_SHORT).show();

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("cliente", newClient);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            });
                        } else {
                            throw new IOException("El cuerpo de la respuesta está vacío");
                        }
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        handler.post(() -> Utils.showError(getApplicationContext(), "Error al crear cliente: " + errorMessage));
                    }
                } catch (Exception e) {
                    handler.post(() -> Utils.showError(getApplicationContext(), "Error de conexión al servidor: " + e.getMessage()));
                }
            } catch (Exception e) {
                handler.post(() -> Utils.showError(getApplicationContext(), "Error de conexión al servidor: " + e.getMessage()));
            }
        });
    }

    public void cancel(View view) {
        Utils.closeActivity(this);
    }
}
