package com.example.hamacav1.entidades.reports;



import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.hamacav1.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class NewReport extends AppCompatActivity {


    private String createdBy;
    private String creationDate;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText fullCommentEditText;
    private Spinner stateSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_report);

        titleEditText = findViewById(R.id.et_new_title_report);
        //descriptionEditText = findViewById(R.id.et_new_description_report);
        stateSpinner = findViewById(R.id.spinner_report_state);
        fullCommentEditText = findViewById(R.id.et_full_comment);

        // Obteniendo la fecha y hora actuales
        creationDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
        // Obteniendo el ID del usuario actualmente logueado (ajustar según tu lógica de autenticación)
        createdBy = getCurrentUserId();

        Button btnAceptar = findViewById(R.id.bt_report_accept);
        btnAceptar.setOnClickListener(v -> agregarNuevoReporte());
    }

    private void agregarNuevoReporte() {
        String title = titleEditText.getText().toString().trim();
        //String description = descriptionEditText.getText().toString().trim();
        String state = stateSpinner.getSelectedItem().toString();
        String fullComment = fullCommentEditText.getText().toString().trim();

        if (!title.isEmpty() && !state.isEmpty() && !fullComment.isEmpty()) {
            // Aquí deberías tener la lógica para agregar el reporte a tu base de datos o backend
            new AgregarReporteAsyncTask().execute(title, state, fullComment, creationDate, createdBy);
        } else {
            Toast.makeText(this, "Por favor, completa todos los campos para el reporte.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método simulado para obtener el ID del usuario actualmente logueado
    private String getCurrentUserId() {
        // Esto es solo un placeholder. Ajusta esta lógica para obtener el ID de usuario real de tu sistema de autenticación
        return "userId_simulado";
    }

    private class AgregarReporteAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // Simula la adición del reporte a la base de datos o backend
            // Implementa la lógica de envío del reporte aquí
            return "Resultado simulado del envío del reporte"; // Placeholder para resultado de operación
        }

        @Override
        protected void onPostExecute(String resultado) {
            if (resultado != null && !resultado.isEmpty() && !resultado.startsWith("error")) {
                Toast.makeText(NewReport.this, "Reporte agregado correctamente", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(NewReport.this, "Error al agregar el reporte", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
