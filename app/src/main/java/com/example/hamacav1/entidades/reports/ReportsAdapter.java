package com.example.hamacav1.entidades.reports;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<Report> reportsList;
    private Context context;
    private ReportsAdapterCallback callback; // Asegúrate de tener esta interfaz definida y de establecer el callback

    public ReportsAdapter(List<Report> reportsList, Context context, ReportsAdapterCallback callback) {
        this.reportsList = reportsList;
        this.context = context;
        this.callback = callback;
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
        holder.fullComment.setText(report.getFullComment());
        holder.state.setText(report.getState());
        holder.creationDate.setText(report.getCreationDate()); // Asegúrate de que este método retorne un String
        // holder.createdBy.setText(report.getCreatedBy().getNombreUsuario()); // Asume que tienes un método getNombreUsuario()

        holder.itemView.setOnClickListener(v -> toggleExpandableView(holder.expandableView)); // Expande/colapsa

        //holder.edit.setOnClickListener(v -> callback.editPressed(position)); // Edita
        //holder.delete.setOnClickListener(v -> callback.deletePressed(position)); // Elimina
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView title, fullComment, state, creationDate, createdBy;
        ImageButton edit, delete;
        View expandableView; // Este es el contenedor que quieres expandir/colapsar

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            fullComment = itemView.findViewById(R.id.reportDescription);
            state = itemView.findViewById(R.id.reportState);
            creationDate = itemView.findViewById(R.id.creationDate);
            createdBy = itemView.findViewById(R.id.createdBy);
            //edit = itemView.findViewById(R.id.bt_li_update);
            delete = itemView.findViewById(R.id.bt_li_delete);
            expandableView = itemView.findViewById(R.id.expandable_view); // Asume que tienes una sección llamada así

            // Puedes inicializar aquí la vista expandible como GONE si quieres que esté colapsada por defecto
            expandableView.setVisibility(View.GONE);
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

    public interface ReportsAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }

}
