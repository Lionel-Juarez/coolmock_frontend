package com.example.hamacav1.entidades.hamacas;
import android.content.Intent;
import android.os.Bundle;
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
                startActivity(intent);
                hamaca.setReservada(true);
                hamaca.setOcupada(false);
                actualizarEstado(tvDetalleEstado, hamaca);
                if (updateListener != null) {
                    updateListener.onHamacaUpdated(hamaca);
                }
            });

            btnOcupar.setOnClickListener(v -> {
                hamaca.setOcupada(true);
                hamaca.setReservada(false);
                actualizarEstado(tvDetalleEstado, hamaca);
                if (updateListener != null) {
                    updateListener.onHamacaUpdated(hamaca);
                }
            });

            btnLiberar.setOnClickListener(v -> {
                hamaca.setReservada(false);
                hamaca.setOcupada(false);
                actualizarEstado(tvDetalleEstado, hamaca);
                if (updateListener != null) {
                    updateListener.onHamacaUpdated(hamaca);
                }
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
}
