package com.example.hamacav1.entidades.hamacas;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

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
        holder.tvNumeroHamaca.setText(String.format(context.getString(R.string.numero_hamaca), hamaca.getIdHamaca()));
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


    // Método para actualizar el color del ImageView basado en el estado de la hamaca
    private void updateViewColor(ImageView imageView, Hamaca hamaca) {
        if (hamaca.isReservada()) {
            imageView.setBackgroundColor(context.getResources().getColor(R.color.colorReservada));
        } else if (hamaca.isOcupada()) {
            imageView.setBackgroundColor(context.getResources().getColor(R.color.colorOcupada));
        } else {
            imageView.setBackgroundColor(context.getResources().getColor(R.color.colorDisponible));
        }
    }


    @Override
    public int getItemCount() {
        return listaHamacas.size();
    }

    public static class HamacaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroHamaca;
        ImageView ivEstadoHamaca;  // Agregado para gestionar el cambio de color

        public HamacaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroHamaca = itemView.findViewById(R.id.tvNumeroHamaca);
            ivEstadoHamaca = itemView.findViewById(R.id.ivEstadoHamaca);  // Inicializar ImageView
        }
    }


    // Método para actualizar la lista de hamacas en el adaptador
    public void setHamacas(List<Hamaca> hamacas) {
        this.listaHamacas = hamacas;
        notifyDataSetChanged();
    }


}
