package com.example.hamacav1.entidades.hamacas;
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

import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.widget.Toast;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reservas.NuevaReserva;
import com.example.hamacav1.entidades.reservas.ReservaFragment;

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
    RadioButton radioLeft, radioRight;

    public static HamacaDetalles newInstance(Hamaca hamaca) {
        HamacaDetalles fragment = new HamacaDetalles();
        Bundle args = new Bundle();
        args.putParcelable(ARG_HAMACA, hamaca);
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
        Button btnVerReserva = view.findViewById(R.id.btnVerReserva);

        radioLeft = view.findViewById(R.id.radioLeft);
        radioRight = view.findViewById(R.id.radioRight);

        Hamaca hamaca = getArguments() != null ? getArguments().getParcelable(ARG_HAMACA) : null;
        if (hamaca != null) {
            tvDetalleNumero.setText("Hamaca #" + hamaca.getIdHamaca());
            tvDetallePrecio.setText("Precio: €" + hamaca.getPrecio());
            actualizarEstado(tvDetalleEstado, hamaca);

            if (hamaca.isReservada()) {
                radioLeft.setVisibility(View.VISIBLE);
                radioRight.setVisibility(View.VISIBLE);
                radioLeft.setClickable(false);
                radioRight.setClickable(false);

                checkLadoHamaca(hamaca);

                btnReservar.setVisibility(View.GONE);
                btnOcupar.setVisibility(View.GONE);
                btnLiberar.setVisibility(View.GONE);
                btnVerReserva.setVisibility(View.VISIBLE);

                btnVerReserva.setOnClickListener(v -> {
                    if (hamaca.getReservaId() != null) {
                        Bundle args = new Bundle();
                        args.putLong(ReservaFragment.EXTRA_RESERVA_ID, hamaca.getReservaId());
                        ReservaFragment fragment = new ReservaFragment();
                        fragment.setArguments(args);

                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.setSelectedItemId(R.id.home);
                            activity.replaceFragment(fragment);
                        }
                        dismiss();  // Cierra el dialogo o fragmento actual
                    } else {
                        Log.d("HamacaDetalles", "Hamaca con id: " + hamaca.getIdHamaca() + " no tiene reservas asociadas");
                        Toast.makeText(getContext(), "No hay reserva asociada a esta hamaca", Toast.LENGTH_SHORT).show();
                    }
                });


            }else if(hamaca.isOcupada()){
                radioLeft.setVisibility(View.VISIBLE);
                radioRight.setVisibility(View.VISIBLE);
                btnReservar.setVisibility(View.VISIBLE);
                btnOcupar.setVisibility(View.GONE);
                btnLiberar.setVisibility(View.VISIBLE);
                btnVerReserva.setVisibility(View.GONE);

                checkLadoHamaca(hamaca);

                btnLiberar.setOnClickListener(v -> {
                    hamaca.setReservada(false);
                    hamaca.setOcupada(false);
                    actualizarEstado(tvDetalleEstado, hamaca);
                    updateHamacaOnServer(hamaca);
                    radioLeft.setVisibility(View.GONE);
                    radioRight.setVisibility(View.GONE);
                    if (updateListener != null) {
                        updateListener.onHamacaUpdated(hamaca);
                    }
                    dismiss();
                });
            }else {
                btnReservar.setVisibility(View.VISIBLE);
                btnOcupar.setVisibility(View.VISIBLE);
                btnLiberar.setVisibility(View.VISIBLE);
                btnVerReserva.setVisibility(View.GONE);
                radioLeft.setVisibility(View.VISIBLE);
                radioRight.setVisibility(View.VISIBLE);
                radioLeft.setClickable(true);
                radioRight.setClickable(true);

                btnReservar.setOnClickListener(v -> {

                    Intent intent = new Intent(getActivity(), NuevaReserva.class);
                    ArrayList<Long> idsHamacas = new ArrayList<>();
                    idsHamacas.add(hamaca.getIdHamaca());
                    intent.putExtra("idsHamacas", idsHamacas);
                    startActivity(intent);
                    dismiss();
                });


                btnOcupar.setOnClickListener(v -> {
                    if (radioLeft.isChecked() || radioRight.isChecked()) {
                        hamaca.setOcupada(true);
                        hamaca.setReservada(false);
                        actualizarEstado(tvDetalleEstado, hamaca);
                        updateHamacaOnServer(hamaca);
                        if (updateListener != null) {
                            updateListener.onHamacaUpdated(hamaca);
                        }
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Seleccione un lado para ocupar", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
        String url = getResources().getString(R.string.url_hamacas) + "updateHamaca/" + hamaca.getIdHamaca();
        Log.d("HamacaUpdate", "Actualizando hamaca con ID: " + hamaca.getIdHamaca() + " con URL: " + url);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idHamaca", hamaca.getIdHamaca());
            jsonObject.put("reservada", hamaca.isReservada());
            jsonObject.put("ocupada", hamaca.isOcupada());
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


    public void checkLadoHamaca(Hamaca hamaca){
        if ("izquierda".equals(hamaca.getLado())) {
            radioLeft.setChecked(true);
        } else if ("derecha".equals(hamaca.getLado())) {
            radioRight.setChecked(true);
        }else if ("ambos".equals(hamaca.getLado())){
            radioLeft.setChecked(true);
            radioRight.setChecked(true);
        }
    }


}


