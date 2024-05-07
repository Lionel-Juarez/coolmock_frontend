package com.example.hamacav1.entidades.reportes;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReportViewHolder> {

    private List<Reporte> reportsList;
    private Context context;
    private ReportsAdapterCallback callback; // Asegúrate de tener esta interfaz definida y de establecer el callback

    public ReporteAdapter(List<Reporte> reportsList, Context context, ReportsAdapterCallback callback) {
        this.reportsList = reportsList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Reporte reporte = reportsList.get(position);
        holder.title.setText(reporte.getTitulo());
        holder.fullComment.setText(reporte.getComentarioCompleto());

        // Inicialmente mostrar solo 2 líneas del comentario.
        holder.fullComment.setMaxLines(2);
        holder.state.setText("Type: " + reporte.getEstado());
        holder.creationDate.setText(reporte.getFechaCreacion());
        holder.createdBy.setText("Created by: " + reporte.getCreadoPor());
        // Evento de clic para expandir/colapsar el comentario y mostrar los detalles.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Determinar si está actualmente expandido.
                boolean isExpanded = holder.expandableView.getVisibility() == View.VISIBLE;

                // Crear una transición y establecer la duración.
                AutoTransition transition = new AutoTransition();
                transition.setDuration(300); // 500ms para una animación más rápida o lenta dependiendo de tus necesidades.

                // Aplicar la transición personalizada.
                TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, transition);

                if (isExpanded) {
                    // Colapsar.
                    holder.expandableView.setVisibility(View.GONE);
                    holder.fullComment.setMaxLines(2); // Mostrar solo 2 líneas.
                } else {
                    // Expandir.
                    holder.expandableView.setVisibility(View.VISIBLE);
                    holder.fullComment.setMaxLines(Integer.MAX_VALUE); // Mostrar todas las líneas.
                }
            }
        });
        //holder.edit.setOnClickListener(v -> callback.editPressed(position)); // Edita
        holder.delete.setOnClickListener(v -> callback.deletePressed(position)); // Elimina
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, fullComment, state, creationDate, createdBy;
        ImageButton edit;
        ImageView delete; // Cambiado a ImageView para reflejar tu XML
        View expandableView; // Este es el contenedor que quieres expandir/colapsar

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            fullComment = itemView.findViewById(R.id.reportDescription);
            state = itemView.findViewById(R.id.reportState);
            creationDate = itemView.findViewById(R.id.creationDate);
            createdBy = itemView.findViewById(R.id.createdBy);
            delete = itemView.findViewById(R.id.deleteIcon); // Asegúrate de que esto coincida con tu XML
            expandableView = itemView.findViewById(R.id.expandable_view); // Asume que tienes una sección llamada así

            expandableView.setVisibility(View.GONE);
        }
    }

    public interface ReportsAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }

}
