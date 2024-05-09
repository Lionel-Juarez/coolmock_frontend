package com.example.hamacav1.entidades.sombrillas;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.ArrayList;
import java.util.List;

public class SombrillaAdapter extends RecyclerView.Adapter<SombrillaAdapter.SombrillaViewHolder> {
    private List<Sombrilla> listaSombrillas = new ArrayList<>();
    private Context context;
    private FragmentManager fragmentManager;


    // Constructor
    public SombrillaAdapter(List<Sombrilla> listaSombrillas, Context context, FragmentManager fragmentManager) {
        this.listaSombrillas = listaSombrillas != null ? listaSombrillas : new ArrayList<>();
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public SombrillaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_sombrilla, parent, false);
        return new SombrillaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SombrillaViewHolder holder, int position) {
        Sombrilla sombrilla = listaSombrillas.get(position);
//        holder.tvNumeroSombrilla.setText(String.format(context.getString(R.string.numero_sombrilla), sombrilla.getIdSombrilla()));
        updateViewColor(holder.ivEstadoSombrilla, sombrilla);

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition(); // Obtiene la posición actualizada
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Sombrilla currentSombrilla = listaSombrillas.get(adapterPosition);
                SombrillaDetalles dialogFragment = SombrillaDetalles.newInstance(currentSombrilla, updatedSombrilla -> {
                    notifyItemChanged(adapterPosition);
                });
                dialogFragment.show(fragmentManager, "sombrilla_details");
            }
        });
    }
    private void updateViewColor(ImageView imageView, Sombrilla sombrilla) {
        int colorResId = R.color.colorDisponible; // Default color

        if (sombrilla.isReservada()) {
            colorResId = R.color.colorReservada; // Verde si está reservada
        } else if (sombrilla.isOcupada()) {
            colorResId = R.color.colorOcupada; // Rojo si está ocupada
        }

        imageView.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_IN);
    }


    @Override
    public int getItemCount() {
        return listaSombrillas.size();
    }

    public static class SombrillaViewHolder extends RecyclerView.ViewHolder {
//        TextView tvNumeroSombrilla;
        ImageView ivEstadoSombrilla;  // Agregado para gestionar el cambio de color

        public SombrillaViewHolder(@NonNull View itemView) {
            super(itemView);
//            tvNumeroSombrilla = itemView.findViewById(R.id.tvNumeroSombrilla);
            ivEstadoSombrilla = itemView.findViewById(R.id.ivEstadoSombrilla);  // Inicializar ImageView
        }
    }

    public void setSombrillas(List<Sombrilla> sombrillas) {
        this.listaSombrillas = sombrillas;
        notifyDataSetChanged();
    }
}
