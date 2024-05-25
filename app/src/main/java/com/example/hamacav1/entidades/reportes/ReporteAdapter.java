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

import java.util.Collections;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReportViewHolder> {

    private List<Reporte> reportsList;
    private Context context;
    private ReportsAdapterCallback callback;

    public ReporteAdapter(List<Reporte> reportsList, Context context, ReportsAdapterCallback callback) {
        this.reportsList = reportsList;
        this.context = context;
        this.callback = callback;
        Collections.reverse(this.reportsList);
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

        holder.fullComment.setMaxLines(2);
        holder.state.setText("Type: " + reporte.getEstado());
        holder.creationDate.setText(reporte.getFechaCreacion());
        holder.createdBy.setText("Created by: " + reporte.getCreadoPor());

        boolean isExpanded = holder.expandableView.getVisibility() == View.VISIBLE;
        holder.expandIcon.setImageResource(isExpanded ? R.drawable.arriba24 : R.drawable.abajo24);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExpanded = holder.expandableView.getVisibility() == View.VISIBLE;

                AutoTransition transition = new AutoTransition();
                transition.setDuration(300);
                TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView, transition);

                if (isExpanded) {
                    holder.expandableView.setVisibility(View.GONE);
                    holder.fullComment.setMaxLines(2);
                } else {
                    holder.expandableView.setVisibility(View.VISIBLE);
                    holder.fullComment.setMaxLines(Integer.MAX_VALUE);
                }
                holder.expandIcon.setImageResource(isExpanded ? R.drawable.abajo24 : R.drawable.arriba24);
            }
        });

        holder.delete.setOnClickListener(v -> callback.deletePressed(position));
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }
    public void updateReportsList(List<Reporte> newReportsList) {
        this.reportsList = newReportsList;
        Collections.reverse(this.reportsList);
        notifyDataSetChanged();
    }
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, fullComment, state, creationDate, createdBy;
        ImageView delete, expandIcon;
        View expandableView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            fullComment = itemView.findViewById(R.id.reportDescription);
            state = itemView.findViewById(R.id.reportState);
            creationDate = itemView.findViewById(R.id.creationDate);
            createdBy = itemView.findViewById(R.id.createdBy);
            delete = itemView.findViewById(R.id.deleteIcon);
            expandableView = itemView.findViewById(R.id.expandable_view);
            expandIcon = itemView.findViewById(R.id.expand_icon);

            expandableView.setVisibility(View.GONE);
        }
    }

    public interface ReportsAdapterCallback {
        void deletePressed(int position);
    }

}
