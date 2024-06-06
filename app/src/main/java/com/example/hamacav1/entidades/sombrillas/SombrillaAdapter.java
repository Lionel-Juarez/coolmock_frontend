package com.example.hamacav1.entidades.sombrillas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;

import java.util.ArrayList;
import java.util.List;

public class SombrillaAdapter extends RecyclerView.Adapter<SombrillaAdapter.SombrillaViewHolder> {
    private List<Sombrilla> listaSombrillas;
    private final Context context;
    private final FragmentManager fragmentManager;


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
        updateViewImage(holder.ivEstadoSombrilla, sombrilla);

        holder.tvNumeroSombrilla.setText(String.valueOf(sombrilla.getNumeroSombrilla()));

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition(); // Obtiene la posición actualizada
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Sombrilla currentSombrilla = listaSombrillas.get(adapterPosition);
                SombrillaDetalles dialogFragment = SombrillaDetalles.newInstance(currentSombrilla, new SombrillaDetalles.SombrillaUpdateListener() {
                    @Override
                    public void onSombrillaUpdated(Sombrilla updatedSombrilla) {
                        notifyItemChanged(adapterPosition);
                    }

                    @Override
                    public ActivityResultLauncher<Intent> getNuevaReservaLauncher() {
                        if (context instanceof MainActivity) {
                            Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                            if (fragment instanceof SombrillaFragment) {
                                return ((SombrillaFragment) fragment).getNuevaReservaLauncher();
                            }
                        }
                        return null;
                    }
                });
                dialogFragment.show(fragmentManager, "sombrilla_details");
            }
        });
    }


    private void updateViewImage(ImageView imageView, Sombrilla sombrilla) {
        int imageResId = R.drawable.sombrilla_libre;

        if (sombrilla.isReservada()) {
            imageResId = R.drawable.sombrilla_reservada;
        } else if (sombrilla.isOcupada()) {
            imageResId = R.drawable.sombrilla_ocupada;
        }

        imageView.setImageResource(imageResId);
    }

    @Override
    public int getItemCount() {
        return listaSombrillas.size();
    }

    public static class SombrillaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEstadoSombrilla;
        TextView tvNumeroSombrilla;

        public SombrillaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEstadoSombrilla = itemView.findViewById(R.id.ivEstadoSombrilla);
            tvNumeroSombrilla = itemView.findViewById(R.id.etNumeroSombrilla);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSombrillas(List<Sombrilla> sombrillas) {
        this.listaSombrillas = sombrillas;
        notifyDataSetChanged();
    }
}
