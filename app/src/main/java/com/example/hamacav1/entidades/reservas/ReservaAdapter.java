package com.example.hamacav1.entidades.reservas;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    private List<Reserva> reservaList;
    private Context context;
    private ReservasAdapterCallback callback;

    public ReservaAdapter(List<Reserva> reservaList, Context context, ReservasAdapterCallback callback) {
        this.reservaList = reservaList;
        this.context = context;
        this.callback = callback;
    }

    // Método para actualizar la lista de reservas
    public void setReservas(List<Reserva> newReservas) {
        this.reservaList = newReservas;
        notifyDataSetChanged();  // Notifica al adaptador que los datos han cambiado
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
            holder.clienteNombre.setText("Cliente desconocido");
        }

        holder.fechaReserva.setText(context.getString(R.string.fecha_reserva, reserva.getFechaReserva()));
        holder.estado.setText(context.getString(R.string.estado_reserva, reserva.getEstado()));
        holder.pagada.setText(context.getString(R.string.reserva_pagada, reserva.isPagada() ? "Sí" : "No"));

        // Ocultar fecha de pago si la reserva no está pagada
        if (reserva.isPagada()) {
            holder.fechaPago.setText(context.getString(R.string.fecha_pago, reserva.getFechaPago()));
            holder.fechaPago.setVisibility(View.VISIBLE);
            holder.metodoPago.setText(context.getString(R.string.metodo_pago, reserva.getMetodoPago()));
            holder.metodoPago.setVisibility(View.VISIBLE);
        } else {
            holder.fechaPago.setVisibility(View.GONE);
            holder.metodoPago.setVisibility(View.GONE);
        }

        if (reserva.getCreadaPor() != null && reserva.getCreadaPor().getNombreUsuario() != null) {
            holder.creadaPor.setText(context.getString(R.string.creado_por, reserva.getCreadaPor().getNombreUsuario()));
        } else {
            holder.creadaPor.setText(context.getString(R.string.creado_por, "Información no disponible"));
        }

        holder.expandIcon.setOnClickListener(v -> {
            boolean isExpanded = holder.expandableView.getVisibility() == View.VISIBLE;
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, new AutoTransition());
            holder.expandableView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            holder.expandIcon.setImageResource(isExpanded ? R.drawable.baseline_expand_more_24 : R.drawable.baseline_arrow_drop_down_24);
        });

        //holder.delete.setOnClickListener(v -> callback.deletePressed(position));
    }



    @Override
    public int getItemCount() {
        return reservaList.size();
    }

    public static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView clienteNombre, fechaReserva, estado, metodoPago, pagada, fechaPago, creadaPor;
        ImageView delete, expandIcon;
        LinearLayout expandableView;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            clienteNombre = itemView.findViewById(R.id.tvReservaCliente);
            fechaReserva = itemView.findViewById(R.id.tvFechaReserva);
            estado = itemView.findViewById(R.id.tvEstado);
            metodoPago = itemView.findViewById(R.id.tvMetodoPago);
            pagada = itemView.findViewById(R.id.tvPagada);
            fechaPago = itemView.findViewById(R.id.tvFechaPago);
            creadaPor = itemView.findViewById(R.id.tvCreadoPor);
            delete = itemView.findViewById(R.id.deleteIcon);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            expandableView = itemView.findViewById(R.id.expandable_view);
        }
    }

    public interface ReservasAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }
}
