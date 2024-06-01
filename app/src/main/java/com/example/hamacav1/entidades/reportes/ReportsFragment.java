package com.example.hamacav1.entidades.reportes;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    private final boolean hasMoreReports = true;
    private final int LOAD_MORE_SIZE = 5;
    ActivityResultLauncher<Intent> nuevoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarReportes();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportsList = new ArrayList<>();
        reporteAdapter = new ReporteAdapter(reportsList, getContext());
        reportsRecyclerView.setAdapter(reporteAdapter);
        emptyView = view.findViewById(R.id.emptyView);

        int PAGE_SIZE = 10;
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

        // Encontrar y configurar el ImageView para el filtro
        ImageView btnFilter = view.findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
            });
        } else {
            Log.e("ReportsFragment", "ImageView btnFilter is null");
        }

        // Encontrar y configurar el Button para nuevo reporte
        Button btnNew = view.findViewById(R.id.btnNew);
        if (btnNew != null) {
            btnNew.setOnClickListener(this::newReport);
        } else {
            Log.e("ReportsFragment", "Button btnNew is null");
        }

        return view;
    }

    private void loadReportsFromBackend(int page, int size) {
        String url = getResources().getString(R.string.url_reportes) + "?page=" + page + "&size=" + size;
        Log.d("ReportsFragment", "Iniciando carga de reportes desde el backend: " + url);

        if (!Internetop.getInstance(requireContext()).isNetworkAvailable()) {
            Utils.showError(requireContext(), "No hay conexión a internet");
            isLoading = false;
            return;
        }
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

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
                    // Manejar el código de error aquí
                    if (response.code() == 403) {
                        requireActivity().runOnUiThread(() -> Utils.showError(requireContext(), "Acceso denegado. Por favor, verifica tus permisos."));
                    }
                    throw new IOException("Código inesperado " + response);
                }

                assert response.body() != null;
                final String responseData = response.body().string();
                Log.d("ReportsFragment", "Reportes cargados correctamente: " + responseData);

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            List<Reporte> newReportsList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject reportObject = jsonArray.getJSONObject(i);
                                Reporte reporte = new Reporte();
                                reporte.fromJSON(reportObject);

                                if (!reportsList.contains(reporte)) {
                                    newReportsList.add(reporte);
                                }
                            }
                            Collections.reverse(newReportsList);
                            if (newReportsList.isEmpty() && reportsList.isEmpty()) {
                                emptyView.setVisibility(View.VISIBLE);
                                reportsRecyclerView.setVisibility(View.GONE);
                            } else {
                                emptyView.setVisibility(View.GONE);
                                reportsRecyclerView.setVisibility(View.VISIBLE);
                                reportsList.addAll(newReportsList);
                                reporteAdapter.notifyDataSetChanged();
                            }
                            isLoading = false;
                        } catch (JSONException e) {
                            Log.e("ReportsFragment", "Error al parsear reportes: ", e);
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

    private void cargarReportes() {
        Log.d("ReportsFragment", "Intentando cargar reportes...");
        if (Internetop.getInstance(requireContext()).isNetworkAvailable()) {
            Log.d("ReportsFragment", "Conexión de red disponible. Cargando reportes...");

            Resources res = getResources();
            String url = res.getString(R.string.url_reportes);
            Log.d("ReportsFragment", "URL de carga de reportes: " + url);

            getListaTask(url);
        } else {
            Log.e("ReportsFragment", "Conexión de red no disponible.");
            Utils.showError(requireContext(), "error.IOException");
        }
    }


    private void getListaTask(String url) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Internetop internetop = Internetop.getInstance(getContext());
            String result = internetop.getString(url);
            handler.post(() -> {
                if (result.equalsIgnoreCase("error.IOException") ||
                        result.equals("error.OKHttp")) {

                    Utils.showError(requireContext(), result);
                } else if (result.equalsIgnoreCase("null")) {
                    Utils.showError(requireContext(), "error.desconocido");
                } else {
                    resetLista(result);
                }
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetLista(String result) {
        try {
            JSONArray listaReportesJson = new JSONArray(result);
            reportsList.clear();  // Clear the existing list to prevent duplicates
            for (int i = 0; i < listaReportesJson.length(); ++i) {
                JSONObject jsonUser = listaReportesJson.getJSONObject(i);
                Reporte reporte = new Reporte();
                reporte.fromJSON(jsonUser);

                // Verificar si el reporte ya existe en la lista
                if (!reportsList.contains(reporte)) {
                    reportsList.add(reporte);
                }
            }
            reporteAdapter.notifyDataSetChanged();
            checkEmptyView();
        } catch (JSONException e) {
            Utils.showError(requireContext(), e.getMessage());
        }
    }
    public void newReport(View view) {
        Intent intent = new Intent(getContext(), NuevoReporte.class);
        nuevoResultLauncher.launch(intent);
    }
}
