package com.example.hamacav1.entidades.usuarios;

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

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final List<Usuario> usuarioList;
    private final Context context;
    private final UsuarioAdapterCallback callback; // Asegúrate de tener esta interfaz definida y de establecer el callback

    public UsuarioAdapter(List<Usuario> UsuariosList, Context context, UsuarioAdapterCallback callback) {
        this.usuarioList = UsuariosList;
        this.context = context;
        this.callback = callback;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);
        holder.nombre.setText(usuario.getUsername());
        holder.rol.setText(usuario.getRol());
        holder.edit.setOnClickListener(v -> callback.editPressed(position)); // Edita
        holder.delete.setOnClickListener(v -> callback.deletePressed(position)); // Elimina
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, rol;
        ImageButton edit;
        ImageView delete;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombreCompleto);
            rol = itemView.findViewById(R.id.rol);

            delete = itemView.findViewById(R.id.eliminarUsuario);
            edit = itemView.findViewById(R.id.modificarUsuario);

            //expandableView.setVisibility(View.GONE);
        }
    }

    public interface UsuarioAdapterCallback {
        void deletePressed(int position);
        void editPressed(int position);
    }

}
