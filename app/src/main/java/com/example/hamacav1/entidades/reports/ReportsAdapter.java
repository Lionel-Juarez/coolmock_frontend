package com.example.hamacav1.entidades.reports;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reportsList;
    private Context context;
    View expandableView;
    public ReportsAdapter(List<Report> reportsList, Context context) {
        this.reportsList = reportsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportsList.get(position);
        holder.title.setText(report.getTitle());
        holder.description.setText(report.getDescription());
        holder.state.setText(report.getState());
        // Aquí puedes configurar los otros campos, asegurándote de que estén ocultos inicialmente si están dentro del contenedor expandible.

        // Configura el click listener para expandir/colapsar la tarjeta
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandableView(holder.expandableView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, state;
        View expandableView; // Agrega una referencia al contenedor expandible

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            description = itemView.findViewById(R.id.reportDescription);
            state = itemView.findViewById(R.id.reportState);
            expandableView = itemView.findViewById(R.id.expandable_view); // Asegúrate de tener este ID en tu layout

            // Inicializa otros campos que serán parte de expandableView
        }
    }

    public static void toggleExpandableView(View expandableView) {
        if (expandableView.getVisibility() == View.GONE) {
            // Expandir vista
            expandableView.setVisibility(View.VISIBLE);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            expandableView.measure(widthSpec, heightSpec);

            ValueAnimator animator = ValueAnimator.ofInt(0, expandableView.getMeasuredHeight());
            animator.addUpdateListener(animation -> {
                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = expandableView.getLayoutParams();
                layoutParams.height = value;
                expandableView.setLayoutParams(layoutParams);
            });
            animator.setDuration(300);
            animator.start();
        } else {
            // Colapsar vista
            int finalHeight = expandableView.getHeight();

            ValueAnimator animator = ValueAnimator.ofInt(finalHeight, 0);
            animator.addUpdateListener(animation -> {
                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = expandableView.getLayoutParams();
                layoutParams.height = value;
                expandableView.setLayoutParams(layoutParams);
                if (value == 0) {
                    expandableView.setVisibility(View.GONE);
                }
            });
            animator.setDuration(300);
            animator.start();
        }
    }

}
