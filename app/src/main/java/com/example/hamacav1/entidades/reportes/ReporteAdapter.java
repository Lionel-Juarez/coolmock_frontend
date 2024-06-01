package com.example.hamacav1.entidades.reportes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.Collections;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReportViewHolder> {

    private final List<Reporte> reportsList;
    private final Context context;

    public ReporteAdapter(List<Reporte> reportsList, Context context) {
        this.reportsList = reportsList;
        this.context = context;
        Collections.reverse(this.reportsList);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_report, parent, false);
        return new ReportViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Reporte reporte = reportsList.get(position);
        holder.title.setText(reporte.getTitulo());

        // Colorea el título basado en su texto
        if (reporte.getTitulo().equals(context.getString(R.string.titulo_ocupando_sombrilla))) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.colorOcupada));
        } else if (reporte.getTitulo().equals(context.getString(R.string.titulo_liberando_sombrilla))) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.color_liberando_sombrilla));
        } else if (reporte.getTitulo().equals(context.getString(R.string.titulo_reservando_sombrilla))){
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.colorReservada)); // Default color
        }else{
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.fullComment.setText(reporte.getComentarioCompleto());
        holder.fullComment.setMaxLines(2);
        holder.creationDate.setText(reporte.getFechaCreacion());
        holder.createdBy.setText(R.id.createdBy + reporte.getCreadoPor());

        boolean isExpanded = holder.expandableView.getVisibility() == View.VISIBLE;
        holder.expandIcon.setImageResource(isExpanded ? R.drawable.arriba24 : R.drawable.abajo24);

        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded1 = holder.expandableView.getVisibility() == View.VISIBLE;

            AutoTransition transition = new AutoTransition();
            transition.setDuration(300);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, transition);

            if (isExpanded1) {
                holder.expandableView.setVisibility(View.GONE);
                holder.fullComment.setMaxLines(2);
            } else {
                holder.expandableView.setVisibility(View.VISIBLE);
                holder.fullComment.setMaxLines(Integer.MAX_VALUE);
            }
            holder.expandIcon.setImageResource(isExpanded1 ? R.drawable.abajo24 : R.drawable.arriba24);
        });
    }


    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, fullComment, creationDate, createdBy;
        ImageView delete, expandIcon;
        View expandableView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            fullComment = itemView.findViewById(R.id.reportDescription);
            creationDate = itemView.findViewById(R.id.creationDate);
            createdBy = itemView.findViewById(R.id.createdBy);
            expandableView = itemView.findViewById(R.id.expandable_view);
            expandIcon = itemView.findViewById(R.id.expand_icon);

            expandableView.setVisibility(View.GONE);
        }
    }

    public interface ReportsAdapterCallback {
    }

}
