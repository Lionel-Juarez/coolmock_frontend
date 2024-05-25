package com.example.hamacav1.entidades.pagos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.R;
import com.example.hamacav1.entidades.pagos.Pago;

import java.util.List;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoViewHolder> {

    private List<Pago> pagoList;
    private Context context;
    private PagoAdapterCallback callback;

    public PagoAdapter(List<Pago> pagoList, Context context, PagoAdapterCallback callback) {
        this.pagoList = pagoList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_pago, parent, false);
        return new PagoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
        Pago pago = pagoList.get(position);
        holder.bind(pago);
    }

    @Override
    public int getItemCount() {
        return pagoList.size();
    }

    class PagoViewHolder extends RecyclerView.ViewHolder {

        private TextView nombreCliente;
        private TextView cantidad;
        private TextView fecha;
        private TextView metodoPago;
        private ImageView metodoPagoIcon;

        public PagoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCliente = itemView.findViewById(R.id.nombreCompleto);
            cantidad = itemView.findViewById(R.id.cantidad);
            fecha = itemView.findViewById(R.id.fechaPago);
            metodoPago = itemView.findViewById(R.id.metodoPago);
            metodoPagoIcon = itemView.findViewById(R.id.iconoMetodoPago);
        }

        public void bind(Pago pago) {
            if (pago.getReserva() != null && pago.getReserva().getCliente() != null) {
                nombreCliente.setText(pago.getReserva().getCliente().getNombreCompleto());
            } else {
                nombreCliente.setText("Cliente desconocido");  // O algún valor predeterminado
            }
            cantidad.setText("€" + pago.getCantidad());
            fecha.setText(pago.getFechaPago().toString());
            metodoPago.setText(pago.getMetodoPago());

            if ("Efectivo".equals(pago.getMetodoPago())) {
                metodoPagoIcon.setImageResource(R.drawable.efectivo32);
            } else {
                metodoPagoIcon.setImageResource(R.drawable.tarjeta32);
            }
        }
    }

    public void addPagos(List<Pago> nuevosPagos) {
        int positionStart = pagoList.size();
        pagoList.addAll(nuevosPagos);
        notifyItemRangeInserted(positionStart, nuevosPagos.size());
    }


    public interface PagoAdapterCallback {

    }
}
