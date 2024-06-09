package com.example.hamacav1.entidades.sombrillas;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import android.app.Dialog;
import android.widget.Toast;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.entidades.reservas.NuevaReserva;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reservas.ReservaFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
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
        Button btnReservar = view.findViewById(R.id.btnReservar);
        Button btnOcupar = view.findViewById(R.id.btnOcupar);
        Button btnLiberar = view.findViewById(R.id.btnLiberar);

        radioOne = view.findViewById(R.id.radioOne);
        radioTwo = view.findViewById(R.id.radioTwo);
        radioGroupHamacas = view.findViewById(R.id.radioGroupHamacas);

        Sombrilla sombrilla = getArguments() != null ? getArguments().getParcelable(ARG_HAMACA) : null;
        if (sombrilla != null) {

            tvDetalleNumero.setText(sombrilla.getNumeroSombrilla());
            tvDetallePrecio.setText(getString(R.string.sunbed_price) + sombrilla.getPrecio());
            actualizarEstado(sombrilla);

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
                    sombrilla.setReservada(false);
                    sombrilla.setOcupada(false);
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
                });
            } else {
                btnReservar.setVisibility(View.VISIBLE);
                btnOcupar.setVisibility(View.VISIBLE);
                btnLiberar.setVisibility(View.VISIBLE);
                radioOne.setVisibility(View.VISIBLE);
                radioTwo.setVisibility(View.VISIBLE);
                radioOne.setEnabled(true);
                radioTwo.setEnabled(true);

                btnReservar.setOnClickListener(v -> {
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

        return view;
    }

    @SuppressLint("SetTextI18n")
    public static void actualizarEstado(Sombrilla sombrilla) {
        String estado = sombrilla.isReservada() ? "Reservada" : sombrilla.isOcupada() ? "Ocupada" : "Disponible";
        tvDetalleEstado.setText("Estado: " + estado);
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
}


