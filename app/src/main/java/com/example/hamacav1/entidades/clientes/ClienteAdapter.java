package com.example.hamacav1.entidades.clientes;

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

import java.util.List;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {

    private List<Cliente> clienteList;
    private Context context;
    private ClienteAdapterCallback callback; // Asegúrate de tener esta interfaz definida y de establecer el callback

    public ClienteAdapter(List<Cliente> ClientesList, Context context, ClienteAdapterCallback callback) {
        this.clienteList = ClientesList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cliente_card, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);
        holder.nombreCompleto.setText(cliente.getNombreCompleto());
        holder.numeroTelefono.setText(cliente.getNumeroTelefono());
        holder.edit.setOnClickListener(v -> callback.editPressed(position)); // Edita
        holder.delete.setOnClickListener(v -> callback.deletePressed(position)); // Elimina
    }

    @Override
    public int getItemCount() {
        return clienteList.size();
    }

    public static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView nombreCompleto, numeroTelefono;
        ImageButton edit;
        ImageView delete; // Cambiado a ImageView para reflejar tu XML
        View expandableView; // Este es el contenedor que quieres expandir/colapsar

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCompleto = itemView.findViewById(R.id.nombreCompleto);
            numeroTelefono = itemView.findViewById(R.id.numeroTelefono);

            delete = itemView.findViewById(R.id.eliminarCliente); // Asegúrate de que esto coincida con tu XML
            edit = itemView.findViewById(R.id.modificarCliente); // Asegúrate de que esto coincida con tu XML
            expandableView = itemView.findViewById(R.id.expandable_view); // Asume que tienes una sección llamada así

            //expandableView.setVisibility(View.GONE);
        }
    }

    public interface ClienteAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }

}
