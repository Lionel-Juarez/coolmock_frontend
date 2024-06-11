package com.example.hamacav1.entidades.sombrillas;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.widget.Toast;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.entidades.pagos.Pago;
import com.example.hamacav1.entidades.pagos.PagoViewModel;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.entidades.reservas.NuevaReserva;
import com.example.hamacav1.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SombrillaDetalles  extends DialogFragment {
    private static final String ARG_HAMACA = "sombrilla";
    RadioButton radioOne, radioTwo;
    RadioGroup radioGroupHamacas;
    private SombrillaUpdateListener updateListener;
    private PagoViewModel pagoViewModel;

    @SuppressLint("StaticFieldLeak")
    private static TextView tvDetalleEstado;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof SombrillaUpdateListener) {
            updateListener = (SombrillaUpdateListener) getParentFragment();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sombrilla_detalles, container, false);

        TextView tvDetalleNumero = view.findViewById(R.id.tvDetalleNumeroSombrilla);
        TextView tvDetallePrecio = view.findViewById(R.id.tvDetallePrecio);
        tvDetalleEstado = view.findViewById(R.id.tvDetalleEstado);
        TextView tvCantidadHamacas = view.findViewById(R.id.tvCantidadHamacas);
        Button btnReservar = view.findViewById(R.id.btnReservar);
        Button btnOcupar = view.findViewById(R.id.btnOcupar);
        Button btnLiberar = view.findViewById(R.id.btnLiberar);
        Button btnPagar = view.findViewById(R.id.btnPagarSombrilla);
        TextView tvPagada = view.findViewById(R.id.tvPagada);
        radioOne = view.findViewById(R.id.radioOne);
        radioTwo = view.findViewById(R.id.radioTwo);
        radioGroupHamacas = view.findViewById(R.id.radioGroupHamacas);

        pagoViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(PagoViewModel.class)) {
                    return (T) new PagoViewModel(getActivity().getApplication());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(PagoViewModel.class);

        Sombrilla sombrilla = getArguments() != null ? getArguments().getParcelable(ARG_HAMACA) : null;
        if (sombrilla != null) {
            tvPagada.setText(getString(R.string.reserva_pagada, sombrilla.isPagada() ? "Sí" : "No"));

            if (sombrilla.isOcupada() && !sombrilla.isPagada() && !sombrilla.isReservada()) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setOnClickListener(v -> {
                    Log.d("SombrillaDetalles", "Botón Pagar clickeado");
                    showPaymentMethodDialog(sombrilla, () -> {
                        actualizarEstado(sombrilla);
                        Toast.makeText(getContext(), "Pagada con éxito", Toast.LENGTH_SHORT).show();
                    });
                });
            } else {
                btnPagar.setVisibility(View.GONE);
            }

            tvDetalleNumero.setText(sombrilla.getNumeroSombrilla());
            tvDetallePrecio.setText(getString(R.string.sunbed_price) + " " + sombrilla.getPrecio());
            actualizarEstado(sombrilla);

            // Verificar si el rol es CLIENTE
            if ("CLIENTE".equals(MainActivity.rol)) {
                btnOcupar.setVisibility(View.GONE);
                btnLiberar.setVisibility(View.GONE);
                btnPagar.setVisibility(View.GONE);
                radioOne.setVisibility(View.GONE);
                radioTwo.setVisibility(View.GONE);
                tvPagada.setVisibility(View.GONE);
                tvCantidadHamacas.setVisibility(View.GONE);

                if (sombrilla.isReservada() || sombrilla.isOcupada()) {
                    btnReservar.setVisibility(View.GONE);
                } else {
                    btnReservar.setVisibility(View.VISIBLE);
                }

                btnReservar.setOnClickListener(v -> {
                    Log.d("SombrillaDetalles", "Botón Reservar clickeado");
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    long idCliente = sharedPreferences.getLong("idCliente", -1);
                    if (idCliente != -1) {
                        verificarLimiteReservasYContinuar(idCliente, url -> {
                            Intent intent = new Intent(getActivity(), NuevaReserva.class);
                            ArrayList<Long> idsSombrillas = new ArrayList<>();
                            idsSombrillas.add(sombrilla.getIdSombrilla());
                            intent.putExtra("idsSombrillas", idsSombrillas);
                            if (updateListener != null) {
                                updateListener.getNuevaReservaLauncher().launch(intent);
                            }
                            dismiss();
                        });
                    } else {
                        Toast.makeText(getContext(), "Error al obtener el ID del cliente", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (sombrilla.isReservada()) {
                    checkCantidadHamacas(sombrilla);
                    btnReservar.setVisibility(View.GONE);
                    btnOcupar.setVisibility(View.GONE);
                    btnLiberar.setVisibility(View.GONE);
                } else if (sombrilla.isOcupada()) {
                    radioOne.setVisibility(View.VISIBLE);
                    radioTwo.setVisibility(View.VISIBLE);
                    radioOne.setEnabled(false);
                    radioTwo.setEnabled(false);
                    btnOcupar.setVisibility(View.GONE);
                    btnLiberar.setVisibility(View.VISIBLE);

                    checkCantidadHamacas(sombrilla);

                    btnLiberar.setOnClickListener(v -> {
                        Log.d("SombrillaDetalles", "Botón Liberar clickeado");
                        if (!sombrilla.isPagada()) {
                            Log.d("SombrillaDetalles", "Mostrando diálogo de pago antes de liberar la sombrilla");
                            showPaymentMethodDialog(sombrilla, () -> {
                                Log.d("SombrillaDetalles", "Sombrilla pagada y liberada con éxito");
                                Toast.makeText(getContext(), "Pagada y liberada con éxito", Toast.LENGTH_SHORT).show();
                                liberarSombrilla(sombrilla);
                            });
                        } else {
                            Log.d("SombrillaDetalles", "Liberando sombrilla sin pago necesario");
                            liberarSombrilla(sombrilla);
                        }
                    });
                } else {
                    btnReservar.setVisibility(View.VISIBLE);
                    btnOcupar.setVisibility(View.VISIBLE);
                    btnLiberar.setVisibility(View.GONE);
                    radioOne.setVisibility(View.VISIBLE);
                    radioTwo.setVisibility(View.VISIBLE);
                    radioOne.setEnabled(true);
                    radioTwo.setEnabled(true);
                    tvPagada.setVisibility(View.GONE);

                    btnReservar.setOnClickListener(v -> {
                        Log.d("SombrillaDetalles", "Botón Reservar clickeado");
                        Intent intent = new Intent(getActivity(), NuevaReserva.class);
                        ArrayList<Long> idsSombrillas = new ArrayList<>();
                        idsSombrillas.add(sombrilla.getIdSombrilla());
                        intent.putExtra("idsSombrillas", idsSombrillas);
                        if (updateListener != null) {
                            updateListener.getNuevaReservaLauncher().launch(intent);
                        }
                        dismiss();
                    });
                    btnOcupar.setOnClickListener(v -> {
                        Log.d("SombrillaDetalles", "Botón Ocupar clickeado");
                        if (radioGroupHamacas.getCheckedRadioButtonId() != -1) {
                            sombrilla.setOcupada(true);
                            sombrilla.setReservada(false);
                            String cantidadHamacas = radioOne.isChecked() ? "1" : "2";
                            sombrilla.setCantidadHamacas(cantidadHamacas);

                            actualizarEstado(sombrilla);
                            updateSombrillaOnServer(sombrilla, requireContext());

                            String titulo = getString(R.string.titulo_ocupando_sombrilla);
                            String descripcion = getString(R.string.descripcion_ocupando_sombrilla, sombrilla.getIdSombrilla(), cantidadHamacas);
                            NuevoReporte.crearReporte(getContext(), titulo, descripcion);

                            if (updateListener != null) {
                                updateListener.onSombrillaUpdated(sombrilla);
                            }
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Seleccione una cantidad de hamacas para ocupar", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    public static void actualizarEstado(Sombrilla sombrilla) {
        String estado = sombrilla.isReservada() ? "Reservada" : sombrilla.isOcupada() ? "Ocupada" : "Disponible";
        tvDetalleEstado.setText("Estado: " + estado);
        Log.d("SombrillaDetalles", "Estado actualizado: " + estado + ", Pagada: " + sombrilla.isPagada());
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        return dialog;
    }

    public interface SombrillaUpdateListener {
        void onSombrillaUpdated(Sombrilla sombrilla);
        ActivityResultLauncher<Intent> getNuevaReservaLauncher();
    }

    public static SombrillaDetalles newInstance(Sombrilla sombrilla, SombrillaUpdateListener listener) {
        SombrillaDetalles fragment = new SombrillaDetalles();
        fragment.updateListener = listener;
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAMACA, sombrilla);
        fragment.setArguments(args);
        return fragment;
    }

    public static void updateSombrillaOnServer(Sombrilla sombrilla, Context context) {
        String url = context.getResources().getString(R.string.url_sombrillas) + "updateSombrilla/" + sombrilla.getIdSombrilla();
        Log.d("SombrillaUpdate", "Actualizando sombrilla con ID: " + sombrilla.getIdSombrilla() + " con URL: " + url);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reservada", sombrilla.isReservada());
            jsonObject.put("ocupada", sombrilla.isOcupada());
            jsonObject.put("cantidadHamacas", sombrilla.getCantidadHamacas());
            jsonObject.put("pagada", sombrilla.isPagada());

            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .patch(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e("SombrillaUpdate", "Error al realizar la solicitud de actualización: " + e.getMessage(), e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.e("SombrillaUpdate", "Respuesta no exitosa del servidor al actualizar sombrilla: HTTP " + response.code() + " - " + responseBody);
                    } else {
                        Log.d("SombrillaUpdate", "Actualización exitosa de la sombrilla con ID: " + sombrilla.getIdSombrilla());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("SombrillaUpdate", "Error al crear JSON para actualizar la sombrilla: " + e.getMessage(), e);
        }
    }


    private void checkCantidadHamacas(Sombrilla sombrilla) {
        String cantidadHamacas = sombrilla.getCantidadHamacas();

        radioOne.setVisibility(View.VISIBLE);
        radioTwo.setVisibility(View.VISIBLE);
        radioOne.setClickable(!sombrilla.isReservada());
        radioTwo.setClickable(!sombrilla.isReservada());

        if (cantidadHamacas != null) {
            switch (cantidadHamacas) {
                case "1":
                    radioOne.setChecked(true);
                    break;
                case "2":
                    radioTwo.setChecked(true);
                    break;
                default:
                    radioGroupHamacas.clearCheck();
                    break;
            }
        } else {
            radioGroupHamacas.clearCheck();
        }
    }
    private void verificarLimiteReservasYContinuar(long idCliente, Consumer<String> callback) {
        if (isAdded() && getActivity() != null) {
            String url = getResources().getString(R.string.url_reservas) + "countByClientePendientes/" + idCliente;
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String idToken = sharedPreferences.getString("idToken", null);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + idToken)
                    .build();

            Log.d("verificarLimiteReservas", "URL: " + url);
            Log.d("verificarLimiteReservas", "ID Token: " + idToken);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e("verificarLimiteReservas", "Error al contar reservas", e);
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al contar reservas", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("verificarLimiteReservas", "Response data: " + responseData);

                        try {
                            long count = Long.parseLong(responseData);
                            Log.d("verificarLimiteReservas", "Number of reservations: " + count);

                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (count >= 5) {
                                        Log.d("verificarLimiteReservas", "Limite de reservas alcanzado: " + count);
                                        Toast.makeText(getActivity(), "No puedes realizar más de 5 reservas", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d("verificarLimiteReservas", "Aún puedes realizar reservas: " + count);
                                        String url = getResources().getString(R.string.url_reservas) + "nuevaReserva";
                                        callback.accept(url);
                                    }
                                });
                            }
                        } catch (NumberFormatException e) {
                            Log.e("verificarLimiteReservas", "Error parsing response data", e);
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al contar reservas", Toast.LENGTH_SHORT).show());
                            }
                        }
                    } else {
                        Log.e("verificarLimiteReservas", "Error en la respuesta: " + response.code());
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error al contar reservas", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            });
        }
    }

    private void liberarSombrilla(Sombrilla sombrilla) {
        Log.d("SombrillaDetalles", "Liberando sombrilla con ID: " + sombrilla.getIdSombrilla());
        sombrilla.setReservada(false);
        sombrilla.setOcupada(false);
        sombrilla.setPagada(false);
        actualizarEstado(sombrilla);
        updateSombrillaOnServer(sombrilla, requireContext());
        radioOne.setVisibility(View.GONE);
        radioTwo.setVisibility(View.GONE);

        String titulo = getString(R.string.titulo_liberando_sombrilla);
        String descripcion = getString(R.string.descripcion_liberando_sombrilla, sombrilla.getIdSombrilla(), sombrilla.getCantidadHamacas());
        NuevoReporte.crearReporte(getContext(), titulo, descripcion);

        if (updateListener != null) {
            updateListener.onSombrillaUpdated(sombrilla);
        }
        dismiss();
    }



    private void showPaymentMethodDialog(Sombrilla sombrilla, Runnable onPaymentSuccess) {
        Log.d("SombrillaDetalles", "showPaymentMethodDialog llamado");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Método de Pago");

        String[] paymentMethods = {"Tarjeta", "Efectivo"};
        builder.setItems(paymentMethods, (dialog, which) -> {
            String selectedMethod = paymentMethods[which];
            Log.d("SombrillaDetalles", "Método de pago seleccionado: " + selectedMethod);
            processPayment(sombrilla, selectedMethod, onPaymentSuccess);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void processPayment(Sombrilla sombrilla, String metodoPago, Runnable onPaymentSuccess) {
        Log.d("SombrillaDetalles", "processPayment llamado con método de pago: " + metodoPago);

        int cantidadHamacas = 1;
        try {
            cantidadHamacas = Integer.parseInt(sombrilla.getCantidadHamacas());
        } catch (NumberFormatException e) {
            Log.e("SombrillaDetalles", "Error al convertir la cantidad de hamacas: " + e.getMessage());
        }

        double precioTotal = sombrilla.getPrecio() * cantidadHamacas;

        sombrilla.setPagada(true);
        updateSombrillaPagada(sombrilla);

        Pago pago = new Pago();
        pago.setCantidad(precioTotal);
        pago.setMetodoPago(metodoPago);
        pago.setPagado(true);
        pago.setFechaPago(LocalDateTime.now());
        pago.setDetallesPago("Pago realizado para la sombrilla con ID " + sombrilla.getIdSombrilla() + " con " + cantidadHamacas + " hamacas.");
        pago.setTipoHamaca("Standard");

        pagoViewModel = new ViewModelProvider(requireActivity()).get(PagoViewModel.class);
        pagoViewModel.createPagoSinReserva(pago, (pagoSuccess) -> {
            if (pagoSuccess) {
                String titulo = "Pago de Sombrilla";
                String descripcion = "La sombrilla con ID " + sombrilla.getIdSombrilla() + " ha sido pagada utilizando " + metodoPago + ".";
                NuevoReporte.crearReporte(getContext(), titulo, descripcion);

                onPaymentSuccess.run();
            } else {
                Toast.makeText(getContext(), "Error al crear el pago", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSombrillaPagada(Sombrilla sombrilla) {
        OkHttpClient client = new OkHttpClient();
        String url = getResources().getString(R.string.url_sombrillas) + "updatePagoSombrilla/" + sombrilla.getIdSombrilla();
        RequestBody requestBody = new FormBody.Builder()
                .add("pagada", "true")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .patch(requestBody)
                .build();

        Log.d("updateSombrillaPagada", "URL: " + url);
        Log.d("updateSombrillaPagada", "Request Body: pagada=true");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("updateSombrillaPagada", "Error al actualizar el estado de pago de la sombrilla: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("updateSombrillaPagada", "Respuesta no exitosa del servidor: " + response);
                    Log.e("updateSombrillaPagada", "Código de error: " + response.code());
                    Log.e("updateSombrillaPagada", "Mensaje de error: " + response.message());
                    if (response.body() != null) {
                        Log.e("updateSombrillaPagada", "Cuerpo de respuesta: " + response.body().string());
                    }
                } else {
                    Log.d("updateSombrillaPagada", "Sombrilla actualizada correctamente: " + sombrilla.getIdSombrilla());
                }
            }
        });
    }

}


