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

import androidx.appcompat.app.AppCompatActivity;


import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        // Establece automáticamente la fecha actual como fecha de creación
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        fechaCreacion = sdf.format(Calendar.getInstance().getTime());
        // Aquí deberías obtener el ID del usuario actualmente autenticado, por ejemplo, desde SharedPreferences o de alguna manera que mantengas el estado de la sesión del usuario
        creadoPor = getCurrentUserId();
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

    public void addReporte(View view) {
        String titulo = etTitulo.getText().toString();
        String comentarioCompleto = etComentarioCompleto.getText().toString();
        String estado = String.valueOf(spinnerEstado.getSelectedItemPosition() + 1); // Asegúrate de que los estados en el spinner estén correctamente alineados con tu backend

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




    private void processResult(String result) {
        Button btAceptar = findViewById(R.id.bt_report_accept);
        btAceptar.setEnabled(true);
        btAceptar.setClickable(true);

        if (result != null && !result.startsWith("error")) {
            setResult(RESULT_OK);
            finish();
        } else {
            Utils.showError(getApplicationContext(),"error.desconocido");
        }
    }

    private long getCurrentUserId() {
        // Implementa la lógica para obtener el ID del usuario actual
        return 1;
    }
}
