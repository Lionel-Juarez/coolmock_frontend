package com.example.hamacav1.entidades.usuarios;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.R;
import com.example.hamacav1.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UsuarioFragment extends Fragment implements UsuarioAdapter.UsuarioAdapterCallback {

    private RecyclerView usuarioRecyclerView;
    private UsuarioAdapter usuario;
    private List<Usuario> usuarioList;
    ActivityResultLauncher<Intent> nuevoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarUsuarios();
                }
            });

    @Override
    public void editPressed(int position) {
        if (usuarioList != null) {
            if (usuarioList.size() > position) {
                Usuario usuario = usuarioList.get(position);
                Intent myIntent = new Intent(getActivity(), NuevoUsuario.class);
                myIntent.putExtra("id", usuario.getId());
                nuevoResultLauncher.launch(myIntent);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);
        usuarioRecyclerView = view.findViewById(R.id.usuariosRecyclerView);
        usuarioRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usuarioList = new ArrayList<>();
        usuario = new UsuarioAdapter(usuarioList, getContext(), this);
        usuarioRecyclerView.setAdapter(usuario);

        loadUsuariosFromBackend();

        view.findViewById(R.id.fab_add_usuario).setOnClickListener(v -> newUsuario());

        return view;
    }

    private void newUsuario() {
        Intent intent = new Intent(getContext(), NuevoUsuario.class);
        nuevoResultLauncher.launch(intent);
    }

    private void loadUsuariosFromBackend() {
        String url = getResources().getString(R.string.url_usuarios) ;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Log.d("UsuariosFragment", "Iniciando carga de Usuarios desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("UsuariosFragment", "Error al cargar Usuarios: ", e);
                // Aquí puedes añadir un mensaje de UI para informar al usuario
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("UsuariosFragment", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                assert response.body() != null;
                final String responseData = response.body().string();
                Log.d("UsuariosFragment", "Usuarios cargados correctamente: " + responseData);

                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        usuarioList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Usuario Usuario = new Usuario();
                            Usuario.fromJSON(jsonObject);
                            usuarioList.add(Usuario);
                        }
                        usuario.notifyDataSetChanged();
                        Log.d("UsuariosFragment", "Usuarios actualizados en la interfaz de usuario.");
                    } catch (JSONException e) {
                        Log.e("UsuariosFragment", "Error al parsear Usuarios: ", e);
                    }
                });
            }
        });
    }


    @Override
    public void deletePressed(int position) {
        AlertDialog diaBox = AskOption(position);
        diaBox.show();//Mostramos un diálogo de confirmación
    }
    private AlertDialog AskOption(final int position) {
        return new AlertDialog.Builder(requireActivity())

                .setTitle(R.string.eliminar_Usuario)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                    eliminarUsuario(position);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create();
    }

    private void eliminarUsuario(int position){
        if(usuarioList !=null && usuarioList.size() > position) {
            Usuario Usuario = usuarioList.get(position);
            Log.d("UsuariosFragment", "Eliminando Usuario: " + Usuario.getId());

            if (Internetop.getInstance(requireContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_usuarios) + "eliminarUsuario/" + Usuario.getId();
                eliminarTask(url);
            } else {
                Log.e("UsuariosFragment", "Conexión de red no disponible para eliminar Usuario.");
                Utils.showError(requireContext(),"error.IOException");
            }
        } else {
            Log.e("UsuariosFragment", "Posición de Usuario no válida o lista de Usuarios vacía.");
            Utils.showError(requireContext(),"error.desconocido");
        }
    }

    private void eliminarTask(String url){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {

            Internetop internetop = Internetop.getInstance(getContext());
            String result = internetop.deleteTask(url);
            handler.post(() -> {
                if(result.equalsIgnoreCase("error.IOException")||
                        result.equals("error.OKHttp")) {//Controlamos los posibles errores
                    Utils.showError(requireContext(),result);
                }
                else if(result.equalsIgnoreCase("null")){
                    Utils.showError(requireContext(),"error.desconocido");
                }
                else{
                    cargarUsuarios();
                }
            });
        });
    }

    private void cargarUsuarios() {
        Log.d("UsuariosFragment", "Intentando cargar Usuarios...");
        if (Internetop.getInstance(requireContext()).isNetworkAvailable()) {
            Log.d("UsuariosFragment", "Conexión de red disponible. Cargando Usuarios...");

            Resources res = getResources();
            String url = res.getString(R.string.url_usuarios);
            Log.d("UsuariosFragment", "URL de carga de Usuarios: " + url);

            getListaTask(url);
        } else {
            Log.e("UsuariosFragment", "Conexión de red no disponible.");
            Utils.showError(requireContext(),"error.IOException");
        }
    }


    private void getListaTask(String url) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            Internetop internetop = Internetop.getInstance(getContext());
            String result = internetop.getString(url);
            handler.post(() -> {
                if(result.equalsIgnoreCase("error.IOException")||
                        result.equals("error.OKHttp")) {

                    Utils.showError(requireContext(),result);
                }
                else if(result.equalsIgnoreCase("null")){
                    Utils.showError(requireContext(),"error.desconocido");
                }
                else{
                    resetLista(result);
                }
            });
        });
    }
    @SuppressLint("NotifyDataSetChanged")
    private void resetLista(String result){
        try {
            JSONArray listaUsuariosJson = new JSONArray(result);
            if (usuarioList == null) {
                usuarioList = new ArrayList<>();
            } else {
                usuarioList.clear();
            }
            for (int i = 0; i < listaUsuariosJson.length(); ++i) {
                JSONObject jsonUser = listaUsuariosJson.getJSONObject(i);
                Usuario Usuario = new Usuario();
                Usuario.fromJSON(jsonUser);
                usuarioList.add(Usuario);
            }
            if (usuario == null) {
                usuario = new UsuarioAdapter(usuarioList, getContext(), this);
                usuarioRecyclerView.setAdapter(usuario);
            } else {
                usuario.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            Utils.showError(requireContext(),e.getMessage());
        }
    }
}