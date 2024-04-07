package com.example.hamacav1.entidades.usuarios;


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
import android.widget.Button;
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


public class NuevoUsuario extends AppCompatActivity {
    private EditText etNombre;
    private EditText etPassword;
    private Spinner spinnerRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_usuario);

        etNombre = findViewById(R.id.et_new_nombre_usuario);
        etPassword = findViewById(R.id.et_new_password_usuario);
        spinnerRol = findViewById(R.id.spinner_usuario_rol);
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

    public void addUsuario(View view) {
        String nombre = etNombre.getText().toString();
        String password = etPassword.getText().toString();
        String rol = String.valueOf(spinnerRol.getSelectedItemPosition() + 1); // Asegúrate de que los estados en el spinner estén correctamente alineados con tu backend

        if (validateInput(nombre, password)) {
            if (isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_usuarios) + "nuevoUsuario";
                sendTask(url, nombre, password, rol);
            } else {
                showError("error.IOException");
            }
        }
    }

    private boolean validateInput(String nombre, String password) {
        boolean isValid = true;
        Resources res = getResources();

        // Validación para el nombre
        if (nombre.isEmpty()) {
            etNombre.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (nombre.length() > 50) { // Asumiendo que el máximo es 50 caracteres para el nombre
            etNombre.setError(res.getString(R.string.error_nombre_largo));
            isValid = false;
        }
        // Validación para la contraseña
        if (password.isEmpty()) {
            etPassword.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        } else if (password.length() < 8) { // Asumiendo que el mínimo es 8 caracteres para la contraseña
            etPassword.setError(res.getString(R.string.error_password_corto));
            isValid = false;
        } else if (password.length() > 20) { // Y que el máximo es 20 caracteres
            etPassword.setError(res.getString(R.string.error_password_largo));
            isValid = false;
        }

        return isValid;
    }

    private void sendTask(String url, String nombreCompleto, String password, String rol) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject json = new JSONObject();
                json.put("nombreUsuario",nombreCompleto);
                json.put("password", password);
                json.put("rol", rol);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    handler.post(() -> {
                        Log.d("Newusuario", "Respuesta del servidor: " + result);
                        if (response.isSuccessful()) {
                            Log.d("Newusuario", "usuarioe añadido con éxito.");
                            Toast.makeText(getApplicationContext(), "usuarioe añadido con éxito", Toast.LENGTH_SHORT).show();
                            // Actualizar lista de usuarioes
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Log.e("Newusuario", "Error al añadir usuarioe: " + result);
                            showError("Error desconocido al añadir usuarioe.");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Newusuario", "Excepción al enviar tarea: " + e.getMessage(), e);
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
