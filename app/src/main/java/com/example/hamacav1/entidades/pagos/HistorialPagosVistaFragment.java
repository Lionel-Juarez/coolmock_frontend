//package com.example.hamacav1.entidades.pagos;
//
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.hamacav1.R;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class HistorialPagosVistaFragment extends Fragment implements PagoAdapter.PagoAdapterCallback {
//
//    private RecyclerView pagoVistaRecyclerView;
//    private ProgressBar progressBar;
//    private PagoAdapter pagoAdapter;
//    private List<Pago> pagoList;
//    private boolean isLoading = false;
//    private int currentPage = 0;
//    private TextView tvNoPagosMessage;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_pagos_vista, container, false);
//        pagoVistaRecyclerView = view.findViewById(R.id.pagosVistaRecyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        pagoVistaRecyclerView.setLayoutManager(layoutManager);
//        tvNoPagosMessage = view.findViewById(R.id.tvNoPagosMessage);
//        progressBar = view.findViewById(R.id.progressBar);
//
//        pagoList = new ArrayList<>();
//        pagoAdapter = new PagoAdapter(pagoList, getContext(), this);
//        pagoVistaRecyclerView.setAdapter(pagoAdapter);
//
//        pagoVistaRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (!recyclerView.canScrollVertically(1) && !isLoading) {
//                    loadMorePagos();
//                }
//            }
//        });
//
//        loadPagosFromBackend();
//        return view;
//    }
//
//    private void loadPagosFromBackend() {
//        progressBar.setVisibility(View.VISIBLE);
//        int PAGE_SIZE = 10;
//        loadPagos(currentPage, PAGE_SIZE);
//    }
//
//    private void loadMorePagos() {
//        isLoading = true;
//        currentPage++;
//        loadPagos(currentPage, 5);
//    }
//
//    private void loadPagos(int page, int size) {
//        LocalDate today = LocalDate.now();
//
//        String baseUrl = getResources().getString(R.string.url_pagos);
//        String url = baseUrl + "?fecha=" + today.toString() + "&page=" + page + "&size=" + size;
//
//        OkHttpClient client = new OkHttpClient();
//
//        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
//        String idToken = sharedPreferences.getString("idToken", null);
//
//        if (idToken == null) {
//            Log.e("PagoVistaFragment", "Token de autorización no disponible");
//            progressBar.setVisibility(View.GONE);
//            Toast.makeText(getContext(), "Error de autenticación. Por favor, inicie sesión de nuevo.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Request request = new Request.Builder()
//                .url(url)
//                .addHeader("Authorization", "Bearer " + idToken)
//                .build();
//
//        Log.d("PagoVistaFragment", "Iniciando carga de Pagos desde el backend: " + url);
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.e("PagoVistaFragment", "Error al cargar Pagos: ", e);
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        isLoading = false;
//                        progressBar.setVisibility(View.GONE);
//                    });
//                }
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    Log.e("PagoVistaFragment", "Respuesta no exitosa del servidor: " + response);
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(() -> {
//                            isLoading = false;
//                            progressBar.setVisibility(View.GONE);
//                        });
//                    }
//                    return;
//                }
//
//                assert response.body() != null;
//                final String responseData = response.body().string();
//                Log.d("PagoVistaFragment", "Pagos cargados correctamente: " + responseData);
//
//                if (!isAdded()) {
//                    return;
//                }
//
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        try {
//                            JSONArray jsonArray = new JSONArray(responseData);
//                            List<Pago> nuevosPagos = new ArrayList<>();
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                Pago pago = new Pago();
//                                pago.fromJSON(jsonObject);
//                                nuevosPagos.add(pago);
//                            }
//                            if (nuevosPagos.isEmpty() && pagoList.isEmpty()) {
//                                tvNoPagosMessage.setVisibility(View.VISIBLE);
//                                pagoVistaRecyclerView.setVisibility(View.GONE);
//                            } else {
//                                tvNoPagosMessage.setVisibility(View.GONE);
//                                pagoVistaRecyclerView.setVisibility(View.VISIBLE);
//                                pagoAdapter.addPagos(nuevosPagos);
//                            }
//                            isLoading = false;
//                        } catch (JSONException e) {
//                            Log.e("PagoVistaFragment", "Error al parsear Pagos: ", e);
//                            isLoading = false;
//                        } finally {
//                            progressBar.setVisibility(View.GONE); // Ocultar ProgressBar al completar la carga
//                        }
//                    });
//                }
//            }
//        });
//    }
//}
//
