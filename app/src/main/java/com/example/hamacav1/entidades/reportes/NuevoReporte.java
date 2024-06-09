package com.example.hamacav1.entidades.reportes;



import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        fechaCreacion = sdf.format(Calendar.getInstance().getTime());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void addReporte(View view) {
        String titulo = etTitulo.getText().toString();
        String comentarioCompleto = etComentarioCompleto.getText().toString();
        String estado = String.valueOf(spinnerEstado.getSelectedItemPosition() + 1);

        if (validateInput(titulo, comentarioCompleto)) {
            if (Internetop.getInstance(getApplicationContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reportes) + "newReport";
                sendTask(url, titulo, comentarioCompleto, estado, fechaCreacion);
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
    private void sendTask(String url, String titulo, String comentarioCompleto, String estado, String fechaCreacion) {
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

                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder().url(url).post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    assert response.body() != null;
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

    public static void crearReporte(Context context, String titulo, String descripcion) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String fechaCreacion = sdf.format(Calendar.getInstance().getTime());

        try {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

            JSONObject json = new JSONObject();
            json.put("titulo", titulo);
            json.put("comentarioCompleto", descripcion);
            json.put("estado", "1"); // Estado predeterminado para la creación de reportes
            json.put("fechaCreacion", fechaCreacion);

            String url = context.getResources().getString(R.string.url_reportes) + "newReport";
            RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
            Request request = new Request.Builder().url(url).post(body).build();

            Log.d("NuevoReporte", "Enviando reporte con título: " + titulo + " y descripción: " + descripcion);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("NuevoReporte", "Error al añadir reporte: ", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        Log.e("NuevoReporte", "Error al añadir reporte: " + response);
                    } else {
                        Log.d("NuevoReporte", "Reporte añadido con éxito.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("NuevoReporte", "Excepción al crear reporte: " + e.getMessage(), e);
        }
    }
    public void cancel(View view) {
        Utils.closeActivity(this);
    }
}

