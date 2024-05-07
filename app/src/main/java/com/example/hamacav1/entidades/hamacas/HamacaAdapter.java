package com.example.hamacav1.entidades.hamacas;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HamacaAdapter extends RecyclerView.Adapter<HamacaAdapter.HamacaViewHolder> {
    private List<Hamaca> listaHamacas = new ArrayList<>();
    private Context context;
    private FragmentManager fragmentManager;


    // Constructor
    public HamacaAdapter(List<Hamaca> listaHamacas, Context context, FragmentManager fragmentManager) {
        this.listaHamacas = listaHamacas != null ? listaHamacas : new ArrayList<>();
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public HamacaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_hamaca, parent, false);
        return new HamacaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HamacaViewHolder holder, int position) {
        Hamaca hamaca = listaHamacas.get(position);
//        holder.tvNumeroHamaca.setText(String.format(context.getString(R.string.numero_hamaca), hamaca.getIdHamaca()));
        updateViewColor(holder.ivEstadoHamaca, hamaca);

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition(); // Obtiene la posición actualizada
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Hamaca currentHamaca = listaHamacas.get(adapterPosition);
                HamacaDetalles dialogFragment = HamacaDetalles.newInstance(currentHamaca, updatedHamaca -> {
                    notifyItemChanged(adapterPosition);
                });
                dialogFragment.show(fragmentManager, "hamaca_details");
            }
        });
    }
    private void updateViewColor(ImageView imageView, Hamaca hamaca) {
        int colorResId = R.color.colorDisponible; // Default color

        if (hamaca.isReservada()) {
            colorResId = R.color.colorReservada; // Verde si está reservada
        } else if (hamaca.isOcupada()) {
            colorResId = R.color.colorOcupada; // Rojo si está ocupada
        }

        imageView.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_IN);
    }


    @Override
    public int getItemCount() {
        return listaHamacas.size();
    }

    public static class HamacaViewHolder extends RecyclerView.ViewHolder {
//        TextView tvNumeroHamaca;
        ImageView ivEstadoHamaca;  // Agregado para gestionar el cambio de color

        public HamacaViewHolder(@NonNull View itemView) {
            super(itemView);
//            tvNumeroHamaca = itemView.findViewById(R.id.tvNumeroHamaca);
            ivEstadoHamaca = itemView.findViewById(R.id.ivEstadoHamaca);  // Inicializar ImageView
        }
    }

    public void setHamacas(List<Hamaca> hamacas) {
        this.listaHamacas = hamacas;
        notifyDataSetChanged();
    }
}
