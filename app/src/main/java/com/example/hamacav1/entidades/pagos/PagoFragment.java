package com.example.hamacav1.entidades.pagos;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PagoFragment extends Fragment implements PagoAdapter.PagoAdapterCallback {

    private RecyclerView pagoRecyclerView;
    private PagoAdapter pagoAdapter;
    private List<Pago> pagoList;
    private boolean isLoading = false;
    private int currentPage = 0;
    private int pageSize = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagos, container, false);
        pagoRecyclerView = view.findViewById(R.id.pagosRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        pagoRecyclerView.setLayoutManager(layoutManager);
        pagoList = new ArrayList<>();
        pagoAdapter = new PagoAdapter(pagoList, getContext(), this);
        pagoRecyclerView.setAdapter(pagoAdapter);

        pagoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadMorePagos();
                }
            }
        });

        loadPagosFromBackend();
        return view;
    }
    private void loadPagosFromBackend() {
        loadPagos(currentPage, pageSize);
    }

    private void loadMorePagos() {
        isLoading = true;
        currentPage++;
        loadPagos(currentPage, 5);
    }

    private void loadPagos(int page, int size) {
        LocalDate today = LocalDate.now();
        String url = "http://10.0.2.2:8080/api/pagos?fecha=" + today.toString() + "&page=" + page + "&size=" + size;
        OkHttpClient client = new OkHttpClient();

        // Elimina temporalmente la parte del token si no es necesario
    /*SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
    String idToken = sharedPreferences.getString("idToken", null);*/

        Request request = new Request.Builder()
                .url(url)
                // .addHeader("Authorization", "Bearer " + idToken) // Elimina temporalmente si no es necesario
                .build();

        Log.d("PagoFragment", "Iniciando carga de Pagos desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PagoFragment", "Error al cargar Pagos: ", e);
                isLoading = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("PagoFragment", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                final String responseData = response.body().string();
                Log.d("PagoFragment", "Pagos cargados correctamente: " + responseData);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            List<Pago> nuevosPagos = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Pago pago = new Pago();
                                pago.fromJSON(jsonObject);
                                nuevosPagos.add(pago);
                            }
                            pagoAdapter.addPagos(nuevosPagos);
                            isLoading = false;
                        } catch (JSONException e) {
                            Log.e("PagoFragment", "Error al parsear Pagos: ", e);
                            isLoading = false;
                        }
                    }
                });
            }
        });
    }

    private void cargarPagos() {
        Log.d("PagoFragment", "Intentando cargar Pagos...");
        if (Internetop.getInstance(getContext()).isNetworkAvailable()) {
            Log.d("PagoFragment", "Conexión de red disponible. Cargando Pagos...");

            // Aquí podría ir el código para mostrar una barra de progreso si es necesario
            // ProgressBar pbMain = (ProgressBar) findViewById(R.id.pb_main);
            // pbMain.setVisibility(View.VISIBLE);

            Resources res = getResources();
            String url = res.getString(R.string.url_pagos);
            Log.d("PagoFragment", "URL de carga de Pagos: " + url);

            getListaTask(url);
        } else {
            Log.e("PagoFragment", "Conexión de red no disponible.");
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
                        if (result.equalsIgnoreCase("error.IOException") ||
                                result.equals("error.OKHttp")) {

                            Utils.showError(getContext(),result);
                        } else if (result.equalsIgnoreCase("null")) {
                            Utils.showError(getContext(),"error.desconocido");
                        } else {
                            resetLista(result);
                        }
                    }
                });
            }
        });
    }

    private void resetLista(String result) {
        try {
            JSONArray listaPagosJson = new JSONArray(result);
            if (pagoList == null) {
                pagoList = new ArrayList<>();
            } else {
                pagoList.clear();
            }
            for (int i = 0; i < listaPagosJson.length(); ++i) {
                JSONObject jsonPago = listaPagosJson.getJSONObject(i);
                Pago pago = new Pago();
                pago.fromJSON(jsonPago);
                pagoList.add(pago);
            }
            if (pagoAdapter == null) {
                pagoAdapter = new PagoAdapter(pagoList, getContext(), this);
                pagoRecyclerView.setAdapter(pagoAdapter);
            } else {
                pagoAdapter.notifyDataSetChanged();
            }
            // Si estás utilizando una ProgressBar, aquí iría el código para ocultarla
            // Por ejemplo:
            // ProgressBar pbMain = findViewById(R.id.pb_main);
            // pbMain.setVisibility(View.GONE);
        } catch (JSONException e) {
            Utils.showError(getContext(),e.getMessage());
        }
    }
}
