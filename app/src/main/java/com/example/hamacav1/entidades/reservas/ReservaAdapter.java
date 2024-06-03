package com.example.hamacav1.entidades.reservas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservaList;
    private final Context context;
    private final ReservasAdapterCallback callback;
    private final SparseBooleanArray expandedState = new SparseBooleanArray();
    private final Long targetReservaId;

    public void expandItem(int position) {
        if (position < 0 || position >= reservaList.size()) {
            return;
        }
        boolean isExpanded = expandedState.get(position, false);
        expandedState.put(position, !isExpanded);
        notifyItemChanged(position);
    }


    public ReservaAdapter(List<Reserva> reservaList, Context context, ReservasAdapterCallback callback, Long targetReservaId) {
        this.reservaList = reservaList;
        this.context = context;
        this.callback = callback;
        this.targetReservaId = targetReservaId;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setReservas(List<Reserva> newReservas) {
        this.reservaList = newReservas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservaList.get(position);

        if (reserva.getCliente() != null) {
            holder.clienteNombre.setText(reserva.getCliente().getNombreCompleto());
        } else {
            holder.clienteNombre.setText(context.getString(R.string.cliente_desconocido));
        }

        if (reserva.getFechaReserva() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            String formattedDate = reserva.getFechaReserva().format(formatter);
            holder.fechaReserva.setText(context.getString(R.string.fecha_reserva, formattedDate));
        } else {
            holder.fechaReserva.setText(context.getString(R.string.fecha_reserva, "Fecha no disponible"));
        }
        holder.horaLlegada.setText(context.getString(R.string.horaLlegada, reserva.getHoraLlegada()));
        holder.estado.setText(context.getString(R.string.estado_reserva, reserva.getEstado()));
        holder.pagada.setText(context.getString(R.string.reserva_pagada, reserva.isPagada() ? "Sí" : "No"));

        // Ocultar fecha de pago si la reserva no está pagada
        if (reserva.isPagada()) {
            holder.fechaPago.setText(context.getString(R.string.fecha_pago, reserva.getFechaPago()));
            holder.fechaPago.setVisibility(View.VISIBLE);
            holder.metodoPago.setText(context.getString(R.string.metodo_pago, reserva.getMetodoPago()));
            holder.metodoPago.setVisibility(View.VISIBLE);

            // Deshabilitar botón de pago y cambiar color
            holder.btnPagar.setEnabled(false);
            holder.btnPagar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBotonDeshabilitado));
        } else {
            holder.fechaPago.setVisibility(View.GONE);
            holder.metodoPago.setVisibility(View.GONE);

            // Habilitar botón de pago y restaurar color
            holder.btnPagar.setEnabled(true);
            holder.btnPagar.setBackgroundColor(ContextCompat.getColor(context, R.color.principalButtonColor));
        }

        if (reserva.getCreadaPor() != null && reserva.getCreadaPor().getUsername() != null) {
            holder.creadaPor.setText(context.getString(R.string.creado_por, reserva.getCreadaPor().getUsername()));
        } else {
            holder.creadaPor.setText(context.getString(R.string.creado_por, "Información no disponible"));
        }

        StringBuilder numerosSombrillasBuilder = new StringBuilder();
        for (Sombrilla sombrilla : reserva.getSombrillas()) {
            numerosSombrillasBuilder.length();
            if (numerosSombrillasBuilder.length() > 0) {
                numerosSombrillasBuilder.append(", ");
            }
            numerosSombrillasBuilder.append(sombrilla.getNumeroSombrilla());
        }

        String numerosSombrillas = numerosSombrillasBuilder.toString();
        holder.sombrillasReservadas.setText(context.getString(R.string.sombrillas_reservadas, numerosSombrillas));


        boolean isExpanded = expandedState.get(position, false); // Obtiene el estado actual
        holder.expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandIcon.setImageResource(isExpanded ? R.drawable.arriba24 : R.drawable.abajo24);

        holder.expandIcon.setOnClickListener(v -> expandItem(position, holder));
        holder.btnPagar.setOnClickListener(v -> callback.onPagarClicked(reserva));

        holder.btnCambiarEstado.setOnClickListener(v -> {
            // Manejar acción de cambiar estado
            callback.onCambiarEstadoClicked(reserva);
        });

        holder.btnModificar.setOnClickListener(v -> {
            // Manejar acción de modificar
            callback.onModificarClicked(reserva);
        });

        if (reserva.getIdReserva().equals(targetReservaId) && isExpanded) {
            holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_selected_expanded));
        } else if (reserva.getIdReserva().equals(targetReservaId)) {
            holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_selected));
        } else if (isExpanded) {
            holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_expanded));
        } else {
            holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));
        }

    }

    public void expandItem(int position, ReservaViewHolder holder) {
        boolean isExpanded = expandedState.get(position);
        expandedState.put(position, !isExpanded);
        Reserva reserva = reservaList.get(position);

        if (!isExpanded) {
            callback.detailExpanded(position);
            if (reserva.getIdReserva().equals(targetReservaId)) {
                holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_selected_expanded));
            } else {
                holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_expanded));
            }
        } else {
            callback.detailCollapsed(position);
            if (reserva.getIdReserva().equals(targetReservaId)) {
                holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border_selected));
            } else {
                holder.cardView.setBackground(ContextCompat.getDrawable(context, R.drawable.card_border));
            }
        }
        notifyItemChanged(position);
    }


    @Override
    public int getItemCount() {
        return reservaList.size();
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView clienteNombre, fechaReserva, estado, metodoPago, pagada, fechaPago, creadaPor, sombrillasReservadas, horaLlegada;
        ImageView expandIcon;
        LinearLayout expandableView;
        CardView cardView;
        Button btnPagar, btnCambiarEstado, btnModificar;
        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            clienteNombre = itemView.findViewById(R.id.tvReservaCliente);
            fechaReserva = itemView.findViewById(R.id.tvFechaReserva);
            horaLlegada = itemView.findViewById(R.id.tvHoraLlegada);
            estado = itemView.findViewById(R.id.tvEstado);
            metodoPago = itemView.findViewById(R.id.tvMetodoPago);
            pagada = itemView.findViewById(R.id.tvPagada);
            fechaPago = itemView.findViewById(R.id.tvFechaPago);
            creadaPor = itemView.findViewById(R.id.tvCreadoPor);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            expandableView = itemView.findViewById(R.id.expandable_view);
            cardView = itemView.findViewById(R.id.reserva_card);
            sombrillasReservadas = itemView.findViewById(R.id.tvSombrillas);
            btnPagar = itemView.findViewById(R.id.btnPagar);
            btnCambiarEstado = itemView.findViewById(R.id.btnCambiarEstado);
            btnModificar = itemView.findViewById(R.id.btnModificar);
        }
    }

    public interface ReservasAdapterCallback {
        void detailExpanded(int position);
        void detailCollapsed(int position);
        void onPagarClicked(Reserva reserva);
        void onCambiarEstadoClicked(Reserva reserva);
        void onModificarClicked(Reserva reserva);
    }
}
