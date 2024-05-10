package com.example.hamacav1.entidades.clientes;


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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.R;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NuevoCliente extends AppCompatActivity {
    private EditText etNombreCompleto;
    private EditText etNumeroTelefono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cliente);

        etNombreCompleto = findViewById(R.id.et_new_nombreCompleto_cliente);
        etNumeroTelefono = findViewById(R.id.et_new_telefono_cliente);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private Boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) {
            return false;
        } else {
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return (actNw != null) && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
    }

    public void addCliente(View view) {
        String nombreCompleto = etNombreCompleto.getText().toString();
        String numeroTelefono = etNumeroTelefono.getText().toString();

        if (validateInput(nombreCompleto, numeroTelefono)) {
            if (isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_clientes) + "nuevoCliente";
                sendTask(url, nombreCompleto, numeroTelefono);
            } else {
                showError("error.IOException");
            }
        }
    }

    private boolean validateInput(String nombre, String telefono) {
        boolean isValid = true;
        Resources res = getResources();

        // Validación para el nombre
        if (nombre.isEmpty()) {
            etNombreCompleto.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (nombre.length() > 50) { // Asumiendo que el máximo es 50 caracteres para el nombre
            etNombreCompleto.setError(res.getString(R.string.error_nombre_largo));
            isValid = false;
        }

        // Validación para el teléfono
        if (telefono.isEmpty()) {
            etNumeroTelefono.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (!telefono.matches("\\d{9,15}")) { // Asume que el teléfono debe tener entre 9 y 15 dígitos
            etNumeroTelefono.setError(res.getString(R.string.error_telefono_formato));
            isValid = false;
        }

        return isValid;
    }


    private void sendTask(String url, String nombreCompleto, String numeroTelefono) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject json = new JSONObject();
                json.put("nombreCompleto",nombreCompleto);
                json.put("numeroTelefono", numeroTelefono);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    handler.post(() -> {
                        Log.d("Newcliente", "Respuesta del servidor: " + result);
                        if (response.isSuccessful()) {
                            Log.d("Newcliente", "clientee añadido con éxito.");
                            Toast.makeText(getApplicationContext(), "clientee añadido con éxito", Toast.LENGTH_SHORT).show();
                            // Actualizar lista de clientees
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Log.e("Newcliente", "Error al añadir clientee: " + result);
                            showError("Error desconocido al añadir clientee.");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Newcliente", "Excepción al enviar tarea: " + e.getMessage(), e);
                handler.post(() -> showError("Error al procesar la solicitud."));
            }
        });
    }

    private void showError(String error) {
        String message;
        Resources res = getResources();
        int duration;
        if(error.equals("error.IOException")){
            duration = Toast.LENGTH_LONG;
            message=res.getString(R.string.error_connection);
        }
        else {
            duration = Toast.LENGTH_SHORT;
            message = res.getString(R.string.error_unknown);
        }
        Context context = this.getApplicationContext();
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
