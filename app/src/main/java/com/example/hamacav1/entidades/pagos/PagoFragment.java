package com.example.hamacav1.entidades.pagos;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hamacav1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PagoFragment extends Fragment implements PagoAdapter.PagoAdapterCallback {
    private RecyclerView pagoVistaRecyclerView;
    private ProgressBar progressBar;
    private PagoAdapter pagoAdapter;
    private List<Pago> pagoList;
    private TextView tvNoPagosMessage;
    private ImageView headerImageSavings;
    private ImageView headerImageQuestion;
    private PagoViewModel pagoViewModel;
    private TextView tvPendientesCount;
    private TextView tvPagadasCount;
    private Date startDate, endDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pagos, container, false);

        pagoViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())).get(PagoViewModel.class);

        pagoVistaRecyclerView = view.findViewById(R.id.pagosVistaRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        pagoVistaRecyclerView.setLayoutManager(layoutManager);
        tvNoPagosMessage = view.findViewById(R.id.tvNoPagosMessage);
        progressBar = view.findViewById(R.id.progressBar);
        headerImageSavings = view.findViewById(R.id.headerImageSavings);
        headerImageQuestion = view.findViewById(R.id.headerImageQuestion);

        tvPendientesCount = view.findViewById(R.id.tvPendientesCount);
        tvPagadasCount = view.findViewById(R.id.tvPagadasCount);
        TextView tvTotalPagosHoyCount = view.findViewById(R.id.tvTotalPagosHoyCount); // Agregado para el total de pagos

        pagoList = new ArrayList<>();
        pagoAdapter = new PagoAdapter(pagoList, getContext(), tvPendientesCount, tvPagadasCount, tvTotalPagosHoyCount); // Agregado tvTotalPagosHoyCount
        pagoVistaRecyclerView.setAdapter(pagoAdapter);

        TextView tvFechaReserva = view.findViewById(R.id.tvFechaReserva);
        @SuppressLint("SimpleDateFormat") String fechaActual = new SimpleDateFormat("dd/MM/yy").format(new Date());
        tvFechaReserva.setText(fechaActual);
        tvFechaReserva.setTextColor(ContextCompat.getColor(requireContext(), R.color.principalColor));


        setupObservers();
        loadPagosFromBackend();
        setupFilterMenu(view);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupObservers() {
        pagoViewModel.getPagos().observe(getViewLifecycleOwner(), nuevosPagos -> {
            if (nuevosPagos != null) {
                pagoList.clear();
                pagoList.addAll(nuevosPagos);
                pagoAdapter.notifyDataSetChanged();
                pagoAdapter.updateTotalPagos();

                if (pagoList.isEmpty()) {
                    tvNoPagosMessage.setVisibility(View.VISIBLE);
                    pagoVistaRecyclerView.setVisibility(View.GONE);
                    headerImageSavings.setVisibility(View.GONE);
                    headerImageQuestion.setVisibility(View.VISIBLE);
                } else {
                    tvNoPagosMessage.setVisibility(View.GONE);
                    pagoVistaRecyclerView.setVisibility(View.VISIBLE);
                    headerImageSavings.setVisibility(View.VISIBLE);
                    headerImageQuestion.setVisibility(View.GONE);
                }
            }
        });

        pagoViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        pagoViewModel.getErrorMessages().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setupFilterMenu(View view) {
        ImageView filterButton = view.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(this::showFilterPopup);
    }

    private void showFilterPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.popup_pagos_filter_layout, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view.getRootView(), Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.action_filter_date).setOnClickListener(v -> {
            showDatePickerDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_filter_method).setOnClickListener(v -> {
            showMethodSelectionDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_filter_range).setOnClickListener(v -> {
            showDateRangePickerDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_show_all).setOnClickListener(v -> {
            pagoViewModel.loadAllPagos();
            popupWindow.dismiss();
        });
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    pagoViewModel.loadPagosByDate(selectedDate.getTime());
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showDateRangePickerDialog() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog startDatePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    startDate = selectedDate.getTime();
                    Log.d("PagoFragment", "Start date selected: " + startDate);
                    showEndDatePickerDialog();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        startDatePickerDialog.show();
    }

    private void showEndDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog endDatePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    endDate = selectedDate.getTime();
                    Log.d("PagoFragment", "End date selected: " + endDate);
                    pagoViewModel.loadPagosByDateRange(startDate, endDate);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        endDatePickerDialog.show();
    }


    private void showMethodSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Método de Pago");
        String[] methods = {"Efectivo", "Tarjeta"};
        builder.setItems(methods, (dialog, which) -> pagoViewModel.filterPagosByMethod(methods[which]));

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void navigateToNuevoFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new NuevoFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void loadPagosFromBackend() {
        progressBar.setVisibility(View.VISIBLE);

        LocalDate today = LocalDate.now();
        String baseUrl = getResources().getString(R.string.url_pagos);
        String url = baseUrl + "?fecha=" + today.toString();

        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        if (idToken == null) {
            Log.e("PagoFragment", "Token de autorización no disponible");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error de autenticación. Por favor, inicie sesión de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        Log.d("PagoFragment", "Iniciando carga de Pagos desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PagoFragment", "Error al cargar Pagos: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvNoPagosMessage.setVisibility(View.VISIBLE);
                        headerImageSavings.setVisibility(View.GONE);
                        headerImageQuestion.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("PagoFragment", "Respuesta no exitosa del servidor: " + response);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            tvNoPagosMessage.setVisibility(View.VISIBLE);
                            headerImageSavings.setVisibility(View.GONE);
                            headerImageQuestion.setVisibility(View.VISIBLE);
                        });
                    }
                    return;
                }

                assert response.body() != null;
                final String responseData = response.body().string();
                Log.d("PagoFragment", "Pagos cargados correctamente: " + responseData);

                if (!isAdded()) {
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            JSONArray jsonArray = new JSONArray(responseData);
                            List<Pago> nuevosPagos = new ArrayList<>();
                            int countPendientes = 0;
                            int countPagadas = 0;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Pago pago = new Pago();
                                pago.fromJSON(jsonObject);
                                nuevosPagos.add(pago);

                                // Contar reservas pendientes y pagadas
                                if (pago.getReserva() != null) {
                                    if (pago.isPagado()) {
                                        countPagadas++;
                                    } else {
                                        countPendientes++;
                                    }
                                }
                            }
                            pagoList.clear();
                            pagoAdapter.setPagos(nuevosPagos);

                            if (pagoList.isEmpty()) {
                                tvNoPagosMessage.setVisibility(View.VISIBLE);
                                pagoVistaRecyclerView.setVisibility(View.GONE);
                                headerImageSavings.setVisibility(View.GONE);
                                headerImageQuestion.setVisibility(View.VISIBLE);
                            } else {
                                tvNoPagosMessage.setVisibility(View.GONE);
                                pagoVistaRecyclerView.setVisibility(View.VISIBLE);
                                headerImageSavings.setVisibility(View.VISIBLE);
                                headerImageQuestion.setVisibility(View.GONE);
                            }

                            // Actualizar los contadores
                            tvPendientesCount.setText(String.valueOf(countPendientes));
                            tvPagadasCount.setText(String.valueOf(countPagadas));
                        } catch (JSONException e) {
                            Log.e("PagoFragment", "Error al parsear Pagos: ", e);
                            progressBar.setVisibility(View.GONE);
                        } finally {
                            progressBar.setVisibility(View.GONE); // Ocultar ProgressBar al completar la carga
                        }
                    });
                }
            }
        });
    }
}


