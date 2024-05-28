package com.example.hamacav1.entidades.clientes;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            fillClientData(cliente);
        }
    }

    private void fillClientData(Cliente cliente) {
        etNombreCompleto.setText(cliente.getNombreCompleto());
        etNumeroTelefono.setText(cliente.getNumeroTelefono());
        etEmail.setText(cliente.getEmail());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
            try {
                JSONObject json = new JSONObject();
                json.put("nombreCompleto", nombreCompleto);
                json.put("numeroTelefono", numeroTelefono);
                json.put("email", email);

                Internetop internetop = Internetop.getInstance(getApplicationContext());
                String result;

                if (isEditMode) {
                    json.put("id", cliente.getIdCliente());
                    result = internetop.sendPutRequest(url, json);
                } else {
                    result = internetop.sendPostRequest(url, json);
                }

                handler.post(() -> {
                    if (result.equals("error.OKHttp")) {
                        Utils.showError(getApplicationContext(), "error.OKHttp");
                    } else {
                        try {
                            JSONObject responseObject = new JSONObject(result);
                            long idCliente = responseObject.getLong("idCliente");
                            Cliente newClient = new Cliente(idCliente, nombreCompleto, numeroTelefono, email);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("cliente", newClient);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } catch (JSONException e) {
                            Utils.showError(getApplicationContext(), "error.Exception");
                        }
                    }
                });
            } catch (Exception e) {
                handler.post(() -> Utils.showError(getApplicationContext(), "error.Exception"));
            }
        });
    }
}
