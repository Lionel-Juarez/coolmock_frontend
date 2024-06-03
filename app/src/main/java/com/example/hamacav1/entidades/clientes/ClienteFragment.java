package com.example.hamacav1.entidades.clientes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.hamacav1.R;
import com.example.hamacav1.util.Internetop;
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

public class ClienteFragment extends Fragment implements ClienteAdapter.ClienteAdapterCallback {

    private RecyclerView clienteRecyclerView;
    private ClienteAdapter cliente;
    private List<Cliente> clienteList;
    ActivityResultLauncher<Intent> nuevoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarClientes();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        clienteRecyclerView = view.findViewById(R.id.clientesRecyclerView);
        clienteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clienteList = new ArrayList<>();
        cliente = new ClienteAdapter(clienteList, getContext(), this);
        clienteRecyclerView.setAdapter(cliente);

        loadClientesFromBackend();

        view.findViewById(R.id.fab_add_cliente).setOnClickListener(v -> newCliente());

        return view;
    }

    private void newCliente() {
        Intent intent = new Intent(getContext(), NuevoCliente.class);
        nuevoResultLauncher.launch(intent);
    }

    private void loadClientesFromBackend() {
        String url = getResources().getString(R.string.url_clientes);
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String idToken = sharedPreferences.getString("idToken", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken) // Añadir el token aquí
                .build();

        Log.d("ClientesFragment", "Iniciando carga de Clientes desde el backend: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ClientesFragment", "Error al cargar Clientes: ", e);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("ClientesFragment", "Respuesta no exitosa del servidor: " + response);
                    throw new IOException("Código inesperado " + response);
                }

                assert response.body() != null;
                final String responseData = response.body().string();
                Log.d("ClientesFragment", "Clientes cargados correctamente: " + responseData);

                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        clienteList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Cliente Cliente = new Cliente();
                            Cliente.fromJSON(jsonObject);
                            clienteList.add(Cliente);
                        }
                        cliente.notifyDataSetChanged();
                        Log.d("ClientesFragment", "Clientes actualizados en la interfaz de cliente.");
                    } catch (JSONException e) {
                        Log.e("ClientesFragment", "Error al parsear Clientes: ", e);
                    }
                });
            }
        });
    }

    @Override
    public void deletePressed(int position) {
        AlertDialog diaBox = AskOption(position);
        diaBox.show();
    }
    private AlertDialog AskOption(final int position) {
        return new AlertDialog.Builder(requireActivity())

                .setTitle(R.string.eliminar_cliente)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                    eliminarCliente(position);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create();
    }

    private void eliminarCliente(int position){
        if(clienteList !=null && clienteList.size() > position) {
            Cliente Cliente = clienteList.get(position);
            Log.d("ClientesFragment", "Eliminando Cliente: " + Cliente.getIdCliente());

            if (Internetop.getInstance(getContext()).isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_clientes) + "eliminarCliente/" + Cliente.getIdCliente();
                eliminarTask(url);
            } else {
                Log.e("ClientesFragment", "Conexión de red no disponible para eliminar Cliente.");
                Utils.showError(requireContext(),"error.IOException");
            }
        } else {
            Log.e("ClientesFragment", "Posición de Cliente no válida o lista de Clientes vacía.");
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
                        result.equals("error.OKHttp")) {
                    Utils.showError(requireContext(), result);
                }
                else if(result.equalsIgnoreCase("null")){
                    Utils.showError(requireContext(), "error.desconocido");
                }
                else{
                    cargarClientes();
                }
            });
        });
    }
    @Override
    public void editPressed(int position) {
        if (clienteList != null && clienteList.size() > position) {
            Cliente cliente = clienteList.get(position);
            Intent myIntent = new Intent(getActivity(), NuevoCliente.class);
            myIntent.putExtra("cliente", cliente);
            nuevoResultLauncher.launch(myIntent);
        }
    }

    private void cargarClientes() {
        Log.d("ClientesFragment", "Intentando cargar Clientes...");
        if (Internetop.getInstance(getContext()).isNetworkAvailable()) {
            Log.d("ClientesFragment", "Conexión de red disponible. Cargando Clientes...");

            Resources res = getResources();
            String url = res.getString(R.string.url_clientes);
            Log.d("ClientesFragment", "URL de carga de Clientes: " + url);

            getListaTask(url);
        } else {
            Log.e("ClientesFragment", "Conexión de red no disponible.");
            Utils.showError(requireContext(), "error.IOException");


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

                    Utils.showError(requireContext(), result);
                }
                else if(result.equalsIgnoreCase("null")){
                    Utils.showError(requireContext(), "error.desconocido");
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
            JSONArray listaClientesJson = new JSONArray(result);
            if (clienteList == null) {
                clienteList = new ArrayList<>();
            } else {
                clienteList.clear();
            }
            for (int i = 0; i < listaClientesJson.length(); ++i) {
                JSONObject jsonUser = listaClientesJson.getJSONObject(i);
                Cliente Cliente = new Cliente();
                Cliente.fromJSON(jsonUser);
                clienteList.add(Cliente);
            }
            if (cliente == null) {
                cliente = new ClienteAdapter(clienteList, getContext(), this);
                clienteRecyclerView.setAdapter(cliente);
            } else {
                cliente.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Utils.showError(requireContext(), e.getMessage());
        }
    }
}