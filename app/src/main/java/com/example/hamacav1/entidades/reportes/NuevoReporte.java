package com.example.hamacav1.entidades.reportes;



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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
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


public class NuevoReporte extends AppCompatActivity {


    private long creadoPor;
    private String fechaCreacion;
    private EditText etTitulo;
    //private EditText descriptionEditText;
    private EditText etComentarioCompleto;
    private Spinner spinnerEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report);

        etTitulo = findViewById(R.id.et_new_title_report);
        etComentarioCompleto = findViewById(R.id.et_full_comment);
        spinnerEstado = findViewById(R.id.spinner_report_state);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        fechaCreacion = sdf.format(Calendar.getInstance().getTime());
        creadoPor = getCurrentUserId();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void addReporte(View view) {
        String titulo = etTitulo.getText().toString();
        String comentarioCompleto = etComentarioCompleto.getText().toString();
        String estado = String.valueOf(spinnerEstado.getSelectedItemPosition() + 1);

        if (validateInput(titulo, comentarioCompleto)) {
            if (Internetop.getInstance(getApplicationContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reportes) + "newReport";
                sendTask(url, titulo, comentarioCompleto, estado, fechaCreacion, String.valueOf(creadoPor));
            } else {
                Utils.showError(getApplicationContext(),"error.IOException");
            }
        }
    }

    private boolean validateInput(String title, String fullComment) {
        boolean isValid = true;
        Resources res = getResources();

        if (title.isEmpty()) {
            etTitulo.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        }

        if (fullComment.isEmpty()) {
            etComentarioCompleto.setError(res.getString(R.string.campo_obligatorio));
            isValid = false;
        }

        return isValid;
    }
    private void sendTask(String url, String titulo, String comentarioCompleto, String estado, String fechaCreacion, String creadoPor) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject json = new JSONObject();
                json.put("titulo", titulo);
                json.put("comentarioCompleto", comentarioCompleto);
                json.put("estado", estado);
                json.put("fechaCreacion", fechaCreacion);
                JSONObject creadoPorObj = new JSONObject();
                creadoPorObj.put("id", creadoPor);
                json.put("creadoPor", creadoPorObj);

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    String result = response.body().string();
                    handler.post(() -> {
                        Log.d("NewReport", "Respuesta del servidor: " + result);
                        if (response.isSuccessful()) {
                            Log.d("NewReport", "Reporte añadido con éxito.");
                            Toast.makeText(getApplicationContext(), "Reporte añadido con éxito", Toast.LENGTH_SHORT).show();
                            // Actualizar lista de reportes
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Log.e("NewReport", "Error al añadir reporte: " + result);
                            Utils.showError(getApplicationContext(),"Error desconocido al añadir reporte.");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("NewReport", "Excepción al enviar tarea: " + e.getMessage(), e);
                handler.post(() -> Utils.showError(getApplicationContext(),"Error al procesar la solicitud."));
            }
        });
    }

    private long getCurrentUserId() {
        // Implementa la lógica para obtener el ID del usuario actual
        return 1;
    }

    // Método estático para crear un reporte de reserva
    public static void crearReporteReserva(Context context, long userId, String nombreUsuario, List<Integer> numSombrillas, int cantidad) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String fechaCreacion = sdf.format(Calendar.getInstance().getTime());

        String titulo = "Creacion reserva";
        String descripcion = "Sombrilla/s " + numSombrillas.toString() + " reservadas, cantidad: " + cantidad;

        try {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

            JSONObject json = new JSONObject();
            json.put("titulo", titulo);
            json.put("comentarioCompleto", descripcion);
            json.put("estado", "1"); // Estado predeterminado para la creación de reportes
            json.put("fechaCreacion", fechaCreacion);
            JSONObject creadoPorObj = new JSONObject();
            creadoPorObj.put("id", userId);
            creadoPorObj.put("nombreUsuario", nombreUsuario);
            json.put("creadoPor", creadoPorObj);

            String url = context.getResources().getString(R.string.url_reportes) + "newReport";
            RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
            Request request = new Request.Builder().url(url).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("NewReport", "Error al añadir reporte: ", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("NewReport", "Error al añadir reporte: " + response);
                    } else {
                        Log.d("NewReport", "Reporte de reserva añadido con éxito.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("NewReport", "Excepción al crear reporte de reserva: " + e.getMessage(), e);
        }
    }
}

