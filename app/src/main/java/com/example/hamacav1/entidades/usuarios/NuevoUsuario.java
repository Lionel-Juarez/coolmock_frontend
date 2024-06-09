package com.example.hamacav1.entidades.usuarios;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

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

        Button cancelar = findViewById(R.id.bt_usuario_cancel);
        cancelar.setBackgroundColor(getResources().getColor(R.color.colorBotonCancelar)); // Cambia el color del botón
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void addUsuario(View view) {
        String nombre = etNombre.getText().toString();
        String password = etPassword.getText().toString();
        String rol = String.valueOf(spinnerRol.getSelectedItemPosition() + 1); // Asegúrate de que los estados en el spinner estén correctamente alineados con tu backend

        if (validateInput(nombre, password)) {
            if (Internetop.getInstance(getApplicationContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_usuarios) + "nuevoUsuario";
                sendTask(url, nombre, password, rol);
            } else {
                Utils.showError(getApplicationContext(),"error.IOException");
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
                json.put("nombreCompleto",nombreCompleto);
                json.put("password", password);
                json.put("rol", rol);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    assert response.body() != null;
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
                            Utils.showError(getApplicationContext(),"Error desconocido al añadir usuarioe.");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Newusuario", "Excepción al enviar tarea: " + e.getMessage(), e);
                handler.post(() -> Utils.showError(getApplicationContext(),"Error al procesar la solicitud."));
            }
        });
    }
    // Método cancel que cierra la actividad actual usando Utils
    public void cancel(View view) {
        Utils.closeActivity(this);
    }
}
