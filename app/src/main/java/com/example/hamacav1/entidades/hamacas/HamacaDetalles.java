package com.example.hamacav1.entidades.hamacas;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reservas.NuevaReserva;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HamacaDetalles  extends DialogFragment {
    private static final String ARG_HAMACA = "hamaca";

    public static HamacaDetalles newInstance(Hamaca hamaca) {
        HamacaDetalles fragment = new HamacaDetalles();
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAMACA, hamaca);  // Usamos putParcelable en lugar de putSerializable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hamaca_detalles, container, false);

        TextView tvDetalleNumero = view.findViewById(R.id.tvDetalleNumeroHamaca);
        TextView tvDetallePrecio = view.findViewById(R.id.tvDetallePrecio);
        TextView tvDetalleEstado = view.findViewById(R.id.tvDetalleEstado);
        Button btnReservar = view.findViewById(R.id.btnReservar);
        Button btnOcupar = view.findViewById(R.id.btnOcupar);
        Button btnLiberar = view.findViewById(R.id.btnLiberar);

        Hamaca hamaca = getArguments() != null ? getArguments().getParcelable(ARG_HAMACA) : null;
        if (hamaca != null) {
            tvDetalleNumero.setText("Hamaca #" + hamaca.getIdHamaca());
            tvDetallePrecio.setText("Precio: €" + hamaca.getPrecio());
            actualizarEstado(tvDetalleEstado, hamaca);

            btnReservar.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NuevaReserva.class);
                ArrayList<Long> idsHamacas = new ArrayList<>();
                idsHamacas.add(hamaca.getIdHamaca());
                intent.putExtra("idsHamacas", idsHamacas);
                startActivity(intent);
                dismiss(); // Cierra el diálogo después de iniciar la actividad
            });

            btnOcupar.setOnClickListener(v -> {
                hamaca.setOcupada(true);
                hamaca.setReservada(false);
                actualizarEstado(tvDetalleEstado, hamaca);
                updateHamacaOnServer(hamaca);
                if (updateListener != null) {
                    updateListener.onHamacaUpdated(hamaca);
                }
                dismiss(); // Cierra el diálogo después de actualizar la hamaca
            });

            btnLiberar.setOnClickListener(v -> {
                hamaca.setReservada(false);
                hamaca.setOcupada(false);
                actualizarEstado(tvDetalleEstado, hamaca);
                if (updateListener != null) {
                    updateListener.onHamacaUpdated(hamaca);
                }
                dismiss(); // Cierra el diálogo después de actualizar la hamaca
            });
        }

        return view;
    }

    private void actualizarEstado(TextView tvDetalleEstado, Hamaca hamaca) {
        String estado = hamaca.isReservada() ? "Reservada" : hamaca.isOcupada() ? "Ocupada" : "Disponible";
        tvDetalleEstado.setText("Estado: " + estado);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;  // Estilo personalizado para animación
        return dialog;
    }

    public interface HamacaUpdateListener {
        void onHamacaUpdated(Hamaca hamaca);
    }

    private HamacaUpdateListener updateListener;

    public static HamacaDetalles newInstance(Hamaca hamaca, HamacaUpdateListener listener) {
        HamacaDetalles fragment = new HamacaDetalles();
        fragment.updateListener = listener;
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAMACA, hamaca);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateHamacaOnServer(Hamaca hamaca) {
        String url = getResources().getString(R.string.url_hamacas) + "updateHamaca/" + hamaca.getIdHamaca(); // Asegúrate de que la URL es correcta
        Log.d("HamacaUpdate", "Actualizando hamaca con ID: " + hamaca.getIdHamaca() + " con URL: " + url);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idHamaca", hamaca.getIdHamaca());
            jsonObject.put("reservada", hamaca.isReservada());
            jsonObject.put("ocupada", hamaca.isOcupada());
            jsonObject.put("precio", hamaca.getPrecio()); // Asume que quieres enviar todos los datos relevantes
            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .put(body) // Usar PUT aquí
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.e("HamacaUpdate", "Error al realizar la solicitud de actualización: " + e.getMessage(), e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String responseBody = response.body().string(); // Leer la respuesta del servidor
                        Log.e("HamacaUpdate", "Respuesta no exitosa del servidor al actualizar hamaca: HTTP " + response.code() + " - " + responseBody);
                    } else {
                        Log.d("HamacaUpdate", "Actualización exitosa de la hamaca con ID: " + hamaca.getIdHamaca());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("HamacaUpdate", "Error al crear JSON para actualizar la hamaca: " + e.getMessage(), e);
        }
    }



}


