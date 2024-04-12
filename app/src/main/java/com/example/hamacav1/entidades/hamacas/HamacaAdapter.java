package com.example.hamacav1.entidades.hamacas;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.List;

public class HamacaAdapter extends RecyclerView.Adapter<HamacaAdapter.HamacaViewHolder> {
    private List<Hamaca> listaHamacas;
    private Context context;
    private FragmentManager fragmentManager;


    // Constructor
    public HamacaAdapter(List<Hamaca> listaHamacas, Context context, FragmentManager fragmentManager) {
        this.listaHamacas = listaHamacas;
        this.context = context;
        this.fragmentManager = fragmentManager;  // Asigna el FragmentManager aquí
    }

    @NonNull
    @Override
    public HamacaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hamaca_item, parent, false);
        return new HamacaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HamacaViewHolder holder, int position) {
        Hamaca hamaca = listaHamacas.get(position);
        // Usamos %d porque idHamaca es de tipo long, que es un número entero.
        holder.tvNumeroHamaca.setText(String.format(context.getString(R.string.numero_hamaca), hamaca.getIdHamaca()));

        if (hamaca.isReservada()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorReservada));
        } else if (hamaca.isOcupada()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorOcupada));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorDisponible));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HamacaDetalles hamacaDetalles = new HamacaDetalles(hamaca);
                hamacaDetalles.show(fragmentManager, hamacaDetalles.getTag()); // Necesitas pasar el FragmentManager
            }
        });
    }


    @Override
    public int getItemCount() {
        return listaHamacas.size();
    }

    public static class HamacaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroHamaca;

        public HamacaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumeroHamaca = itemView.findViewById(R.id.tvNumeroHamaca);
        }
    }

    // Método para actualizar la lista de hamacas en el adaptador
    public void setHamacas(List<Hamaca> hamacas) {
        this.listaHamacas = hamacas;
        notifyDataSetChanged();
    }
}
