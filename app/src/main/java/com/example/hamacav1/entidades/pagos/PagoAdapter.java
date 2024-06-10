package com.example.hamacav1.entidades.pagos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoViewHolder> {
    private final List<Pago> pagoList;
    private final Context context;
    private double totalPagos = 0.0; // Variable para el total de pagos
    private final TextView tvPendientesCount;
    private final TextView tvPagadasCount;
    private final TextView tvTotalPagosHoyCount; // Agregado para el total de pagos

    public PagoAdapter(List<Pago> pagoList, Context context, TextView tvPendientesCount, TextView tvPagadasCount, TextView tvTotalPagosHoyCount) {
        this.pagoList = pagoList;
        this.context = context;
        this.tvPendientesCount = tvPendientesCount;
        this.tvPagadasCount = tvPagadasCount;
        this.tvTotalPagosHoyCount = tvTotalPagosHoyCount; // Agregado para el total de pagos
        updateCounts();
    }

    @NonNull
    @Override
    public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_pago, parent, false);
        return new PagoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
        Pago pago = pagoList.get(position);
        holder.bind(pago);
    }

    @Override
    public int getItemCount() {
        return pagoList.size();
    }

    static class PagoViewHolder extends RecyclerView.ViewHolder {

        private final TextView nombreCliente;
        private final TextView cantidad;
        private final TextView fecha;
        private final TextView metodoPago;
        private final ImageView metodoPagoIcon;

        public PagoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCliente = itemView.findViewById(R.id.nombreCompleto);
            cantidad = itemView.findViewById(R.id.cantidad);
            fecha = itemView.findViewById(R.id.fechaPago);
            metodoPago = itemView.findViewById(R.id.metodoPago);
            metodoPagoIcon = itemView.findViewById(R.id.iconoMetodoPago);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Pago pago) {
            if (pago.getReserva() != null && pago.getReserva().getCliente() != null) {
                nombreCliente.setText(pago.getReserva().getCliente().getNombreCompleto());
            } else {
                nombreCliente.setText("Cliente desconocido");
            }
            cantidad.setText(""+pago.getCantidad());

            LocalDateTime localDateTime = pago.getFechaPago();
            if (localDateTime != null) {
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formattedDate = dateFormat.format(date);
                fecha.setText(formattedDate);
            } else {
                fecha.setText("Fecha no disponible");
            }

            metodoPago.setText(pago.getMetodoPago());

            if ("Efectivo".equals(pago.getMetodoPago())) {
                metodoPagoIcon.setImageResource(R.drawable.efectivo32);
            } else {
                metodoPagoIcon.setImageResource(R.drawable.tarjeta32);
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPagos(List<Pago> nuevosPagos) {
        pagoList.clear();
        pagoList.addAll(nuevosPagos);
        notifyDataSetChanged();
        updateCounts();
    }

    @SuppressLint("SetTextI18n")
    private void updateCounts() {
        int countPendientes = 0;
        int countPagadas = 0;
        totalPagos = 0.0;

        for (Pago pago : pagoList) {
            if (pago.getReserva() != null) {
                if (pago.getReserva().isPagada()) {
                    countPagadas++;
                } else {
                    countPendientes++;
                }
            }
            totalPagos += pago.getCantidad(); // Sumar al total de pagos
        }

        tvPendientesCount.setText(String.valueOf(countPendientes));
        tvPagadasCount.setText(String.valueOf(countPagadas));
        tvTotalPagosHoyCount.setText("" + totalPagos); // Actualizar el total de pagos
    }

    @SuppressLint("SetTextI18n")
    public void updateTotalPagos() {
        totalPagos = 0.0;
        for (Pago pago : pagoList) {
            totalPagos += pago.getCantidad();
        }
        tvTotalPagosHoyCount.setText("" + totalPagos);
    }

    public interface PagoAdapterCallback {
    }
}
