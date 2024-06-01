package com.example.hamacav1.entidades.sombrillas;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sombrilla_detalles, container, false);

        TextView tvDetalleNumero = view.findViewById(R.id.tvDetalleNumeroSombrilla);
        TextView tvDetallePrecio = view.findViewById(R.id.tvDetallePrecio);
        TextView tvDetalleEstado = view.findViewById(R.id.tvDetalleEstado);
        Button btnReservar = view.findViewById(R.id.btnReservar);
        Button btnOcupar = view.findViewById(R.id.btnOcupar);
        Button btnLiberar = view.findViewById(R.id.btnLiberar);
        Button btnVerReserva = view.findViewById(R.id.btnVerReserva);

        radioOne = view.findViewById(R.id.radioOne);
        radioTwo = view.findViewById(R.id.radioTwo);
        radioGroupHamacas = view.findViewById(R.id.radioGroupHamacas);

        Sombrilla sombrilla = getArguments() != null ? getArguments().getParcelable(ARG_HAMACA) : null;
        if (sombrilla != null) {
            tvDetalleNumero.setText("Sombrilla #" + sombrilla.getIdSombrilla());
            tvDetallePrecio.setText("Precio: €" + sombrilla.getPrecio());
            actualizarEstado(tvDetalleEstado, sombrilla);

            if (sombrilla.isReservada()) {
                checkCantidadHamacas(sombrilla);


                btnReservar.setVisibility(View.GONE);
                btnOcupar.setVisibility(View.GONE);
                btnLiberar.setVisibility(View.GONE);
                btnVerReserva.setVisibility(View.VISIBLE);

                btnVerReserva.setOnClickListener(v -> {
                    if (sombrilla.getReservaId() != null) {
                        Bundle args = new Bundle();
                        args.putLong(ReservaFragment.EXTRA_RESERVA_ID, sombrilla.getReservaId());
                        ReservaFragment fragment = new ReservaFragment();
                        fragment.setArguments(args);

                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.setSelectedItemId(R.id.home);
                            activity.replaceFragment(fragment);
                        }
                        dismiss();  // Cierra el dialogo o fragmento actual
                    } else {
                        Log.d("SombrillaDetalles", "Sombrilla con id: " + sombrilla.getIdSombrilla() + " no tiene reservas asociadas");
                        Toast.makeText(getContext(), "No hay reserva asociada a esta sombrilla", Toast.LENGTH_SHORT).show();
                    }
                });


            }else if(sombrilla.isOcupada()){
                radioOne.setVisibility(View.VISIBLE);
                radioTwo.setVisibility(View.VISIBLE);
                btnReservar.setVisibility(View.VISIBLE);
                btnOcupar.setVisibility(View.GONE);
                btnLiberar.setVisibility(View.VISIBLE);
                btnVerReserva.setVisibility(View.GONE);

                checkCantidadHamacas(sombrilla);

                btnLiberar.setOnClickListener(v -> {
                    sombrilla.setReservada(false);
                    sombrilla.setOcupada(false);
                    actualizarEstado(tvDetalleEstado, sombrilla);
                    updateSombrillaOnServer(sombrilla);
                    radioOne.setVisibility(View.GONE);
                    radioTwo.setVisibility(View.GONE);

                    // Crear reporte para la acción realizada
                    String titulo = getString(R.string.titulo_liberando_sombrilla);
                    String descripcion = getString(R.string.descripcion_liberando_sombrilla, sombrilla.getIdSombrilla(), sombrilla.getCantidadHamacas());
                    NuevoReporte.crearReporte(getContext(), getCurrentUserId(), getCurrentUserName(), titulo, descripcion);


                    if (updateListener != null) {
                        updateListener.onSombrillaUpdated(sombrilla);
                    }
                    dismiss();
                });
            }else {
                btnReservar.setVisibility(View.VISIBLE);
                btnOcupar.setVisibility(View.VISIBLE);
                btnLiberar.setVisibility(View.VISIBLE);
                btnVerReserva.setVisibility(View.GONE);
                radioOne.setVisibility(View.VISIBLE);
                radioTwo.setVisibility(View.VISIBLE);
                radioOne.setClickable(true);
                radioTwo.setClickable(true);

                btnReservar.setOnClickListener(v -> {

                    Intent intent = new Intent(getActivity(), NuevaReserva.class);
                    ArrayList<Long> idsSombrillas = new ArrayList<>();
                    idsSombrillas.add(sombrilla.getIdSombrilla());
                    intent.putExtra("idsSombrillas", idsSombrillas);
                    startActivity(intent);
                    dismiss();
                });
                btnOcupar.setOnClickListener(v -> {
                    if (radioGroupHamacas.getCheckedRadioButtonId() != -1) {
                        sombrilla.setOcupada(true);
                        sombrilla.setReservada(false);
                        String cantidadHamacas = radioOne.isChecked() ? "1" : "2";
                        sombrilla.setCantidadHamacas(cantidadHamacas);

                        actualizarEstado(tvDetalleEstado, sombrilla);
                        updateSombrillaOnServer(sombrilla);

                        // Crear reporte para la acción realizada
                        String titulo = getString(R.string.titulo_ocupando_sombrilla);
                        String descripcion = getString(R.string.descripcion_ocupando_sombrilla, sombrilla.getIdSombrilla(), cantidadHamacas);
                        NuevoReporte.crearReporte(getContext(), getCurrentUserId(), getCurrentUserName(), titulo, descripcion);

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


    private void actualizarEstado(TextView tvDetalleEstado, Sombrilla sombrilla) {
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
    }

    private SombrillaUpdateListener updateListener;

    public static SombrillaDetalles newInstance(Sombrilla sombrilla, SombrillaUpdateListener listener) {
        SombrillaDetalles fragment = new SombrillaDetalles();
        fragment.updateListener = listener;
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAMACA, sombrilla);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateSombrillaOnServer(Sombrilla sombrilla) {
        String url = getResources().getString(R.string.url_sombrillas) + "updateSombrilla/" + sombrilla.getIdSombrilla();
        Log.d("SombrillaUpdate", "Actualizando sombrilla con ID: " + sombrilla.getIdSombrilla() + " con URL: " + url);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idSombrilla", sombrilla.getIdSombrilla());
            jsonObject.put("reservada", sombrilla.isReservada());
            jsonObject.put("ocupada", sombrilla.isOcupada());
            jsonObject.put("cantidadHamacas", sombrilla.getCantidadHamacas());
            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .put(body) // Usar PUT aquí
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
                        String responseBody = response.body().string(); // Leer la respuesta del servidor
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
                    radioGroupHamacas.clearCheck(); // Ningún botón seleccionado
                    break;
            }
        } else {
            // Si no hay cantidad especificada, no seleccionar ninguno
            radioGroupHamacas.clearCheck();
        }
    }

    private String getCurrentUserName() {
        // Implementa la lógica para obtener el nombre del usuario actual
        return "nombreUsuario"; // Esto es solo un ejemplo, deberías obtenerlo de tus datos de usuario
    }

    public long getCurrentUserId(){
        return 1;
    }
}


