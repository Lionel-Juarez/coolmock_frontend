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

    private final List<Cliente> clienteList;
    private final Context context;
    private final ClienteAdapterCallback callback;

    public ClienteAdapter(List<Cliente> ClientesList, Context context, ClienteAdapterCallback callback) {
        this.clienteList = ClientesList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_cliente, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = clienteList.get(position);
        holder.nombreCompleto.setText(cliente.getNombreCompleto());
        holder.numeroTelefono.setText(cliente.getNumeroTelefono());
        holder.email.setText(cliente.getEmail());
        holder.edit.setOnClickListener(v -> callback.editPressed(position));
        holder.delete.setOnClickListener(v -> callback.deletePressed(position));
    }

    @Override
    public int getItemCount() {
        return clienteList.size();
    }

    public static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView nombreCompleto, numeroTelefono, email;
        ImageButton edit;
        ImageView delete;
        View expandableView;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCompleto = itemView.findViewById(R.id.nombreCompleto);
            numeroTelefono = itemView.findViewById(R.id.numeroTelefono);
            email = itemView.findViewById(R.id.email);

            delete = itemView.findViewById(R.id.eliminarCliente);
            edit = itemView.findViewById(R.id.modificarCliente);
            expandableView = itemView.findViewById(R.id.expandable_view);

            //expandableView.setVisibility(View.GONE);
        }
    }

    public interface ClienteAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }

}
