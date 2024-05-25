package com.example.hamacav1.entidades.reportes;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.io.IOException;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.R;
import com.example.hamacav1.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReportsFragment extends Fragment implements ReporteAdapter.ReportsAdapterCallback {

    private RecyclerView reportsRecyclerView;
    private ReporteAdapter reporteAdapter;
    private List<Reporte> reportsList;
    private TextView emptyView;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreReports = true;
    private final int PAGE_SIZE = 10;
    private final int LOAD_MORE_SIZE = 5;
    ActivityResultLauncher<Intent> nuevoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        cargarReportes();
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportsList = new ArrayList<>();
        reporteAdapter = new ReporteAdapter(reportsList, getContext(), this);
        reportsRecyclerView.setAdapter(reporteAdapter);
        emptyView = view.findViewById(R.id.emptyView);

        loadReportsFromBackend(currentPage, PAGE_SIZE);

        reportsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && hasMoreReports && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == reportsList.size() - 1) {
                    // Último ítem visible
                    loadReportsFromBackend(++currentPage, LOAD_MORE_SIZE); // Load next page with 5 items
                    isLoading = true;
                }
            }
        });

        return view;
    }

    private void loadReportsFromBackend(int page, int size) {
        String url = getResources().getString(R.string.url_reportes) + "?page=" + page + "&size=" + size;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Log.d("ReportsFragment", "Iniciando carga de reportes desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReportsFragment", "Error al cargar reportes: ", e);
                isLoading = false;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("ReportsFragment", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                final String responseData = response.body().string();
                Log.d("ReportsFragment", "Reportes cargados correctamente: " + responseData);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            List<Reporte> newReportsList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject reportObject = jsonArray.getJSONObject(i);
                                Reporte reporte = new Reporte();
                                reporte.fromJSON(reportObject);
                                newReportsList.add(reporte);
                            }
                            for (Reporte reporte : newReportsList) {
                                if (!reportsList.contains(reporte)) {
                                    reportsList.add(reporte);
                                }
                            }
                            reporteAdapter.notifyDataSetChanged();
                            checkEmptyView();
                            Log.d("ReportsFragment", "Reportes actualizados en la interfaz de usuario.");
                        } catch (JSONException e) {
                            Log.e("ReportsFragment", "Error al parsear reportes: ", e);
                        } finally {
                            isLoading = false;
                        }
                    }
                });
            }
        });
    }

    private void checkEmptyView() {
        if (reporteAdapter.getItemCount() == 0) {
            reportsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            reportsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void deletePressed(int position) {
        AlertDialog diaBox = AskOption(position);
        diaBox.show();//Mostramos un diálogo de confirmación
    }
    private AlertDialog AskOption(final int position) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getActivity())

                .setTitle(R.string.eliminar_reporte)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        eliminarReporte(position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private void eliminarReporte(int position){
        if(reportsList !=null && reportsList.size() > position) {
            Reporte reporte = reportsList.get(position);
            Log.d("ReportsFragment", "Eliminando reporte: " + reporte.getIdReporte());

            if (Internetop.getInstance(getContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reportes) + "deleteReport/" + reporte.getIdReporte();
                eliminarTask(url);
            } else {
                Log.e("ReportsFragment", "Conexión de red no disponible para eliminar reporte.");
                Utils.showError(getContext(),"error.IOException");
            }
        } else {
            Log.e("ReportsFragment", "Posición de reporte no válida o lista de reportes vacía.");
            Utils.showError(getContext(),"error.desconocido");
        }
    }
    private void eliminarTask(String url){
        //La clase Executor será la encargada de lanzar un nuevo hilo en background con la tarea
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Handler es la clase encargada de manejar el resultado de la tarea ejecutada en segundo plano
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {//Ejecutamos el nuevo hilo
            @Override
            public void run() {
                /*Aquí ejecutamos el código en segundo plano, que consiste en obtener del servidor
                 * la lista de alumnos*/
                Internetop internetop = Internetop.getInstance(getContext());
                String result = internetop.deleteTask(url);
                handler.post(new Runnable() {/*Una vez handler recoge el resultado de la tarea en
                segundo plano, hacemos los cambios pertinentes en la interfaz de usuario en función
                del resultado obtenido*/
                    @Override
                    public void run() {
                        if(result.equalsIgnoreCase("error.IOException")||
                                result.equals("error.OKHttp")) {//Controlamos los posibles errores
                            Utils.showError(getContext(),result);
                        }
                        else if(result.equalsIgnoreCase("null")){
                            Utils.showError(getContext(),"error.desconocido");
                        }
                        else{
//                            ProgressBar pbMain = (ProgressBar) findViewById(R.id.pb_main);
//                            pbMain.setVisibility(View.GONE);
                            cargarReportes();
                        }
                    }
                });
            }
        });
    }

    private void cargarReportes() {
        Log.d("ReportsFragment", "Intentando cargar reportes...");
        if (Internetop.getInstance(requireContext()).isNetworkAvailable()) {
            Log.d("ReportsFragment", "Conexión de red disponible. Cargando reportes...");

            // Aquí podría ir el código para mostrar una barra de progreso si es necesario
            // ProgressBar pbMain = (ProgressBar) findViewById(R.id.pb_main);
            // pbMain.setVisibility(View.VISIBLE);

            Resources res = getResources();
            String url = res.getString(R.string.url_reportes);
            Log.d("ReportsFragment", "URL de carga de reportes: " + url);

            getListaTask(url);
        } else {
            Log.e("ReportsFragment", "Conexión de red no disponible.");
            Utils.showError(getContext(),"error.IOException");
        }
    }


    private void getListaTask(String url) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Internetop internetop = Internetop.getInstance(getContext());
                String result = internetop.getString(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(result.equalsIgnoreCase("error.IOException")||
                                result.equals("error.OKHttp")) {

                            Utils.showError(getContext(),result);
                        }
                        else if(result.equalsIgnoreCase("null")){
                            Utils.showError(getContext(),"error.desconocido");
                        }
                        else{
                            resetLista(result);
                        }
                    }
                });
            }
        });
    }
    private void resetLista(String result) {
        try {
            JSONArray listaReportesJson = new JSONArray(result);
            reportsList.clear();  // Clear the existing list to prevent duplicates
            for (int i = 0; i < listaReportesJson.length(); ++i) {
                JSONObject jsonUser = listaReportesJson.getJSONObject(i);
                Reporte reporte = new Reporte();
                reporte.fromJSON(jsonUser);
                reportsList.add(reporte);
            }
            reporteAdapter.notifyDataSetChanged();
            checkEmptyView();
        } catch (JSONException e) {
            Utils.showError(getContext(), e.getMessage());
        }
    }


    private void newReport() {
        Intent intent = new Intent(getContext(), NuevoReporte.class);
        nuevoResultLauncher.launch(intent);
    }

}