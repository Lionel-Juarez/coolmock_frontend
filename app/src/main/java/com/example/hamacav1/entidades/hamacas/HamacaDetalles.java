package com.example.hamacav1.entidades.hamacas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hamacav1.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
public class HamacaDetalles  extends BottomSheetDialogFragment {
    private Hamaca hamaca;

    public HamacaDetalles(Hamaca hamaca) {
        this.hamaca = hamaca;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hamaca_details, container, false);

        TextView tvNumero = view.findViewById(R.id.tvHamacaDetalleNumero);
        TextView tvEstado = view.findViewById(R.id.tvHamacaDetalleEstado);
        TextView tvPrecio = view.findViewById(R.id.tvHamacaDetallePrecio);

        tvNumero.setText("Hamaca #" + hamaca.getIdHamaca());
        tvEstado.setText(hamaca.isReservada() ? "Reservada" : "Disponible");
        tvPrecio.setText("Precio: €" + hamaca.getPrecio());

        return view;
    }
}
