package com.example.hamacav1.entidades.gestionCuentas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.clientes.ClienteFragment;
import com.example.hamacav1.entidades.clientes.NuevoCliente;
import com.example.hamacav1.entidades.usuarios.UsuarioFragment;
import com.example.hamacav1.initialmenus.LoginActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CuentasFragment extends Fragment {
    private CardView usuarioCard;
    private TextView tituloClienteCard;
    private TextView tituloFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuentas, container, false);

        CardView clienteCard = view.findViewById(R.id.clienteCard);
        usuarioCard = view.findViewById(R.id.usuarioCard);
        CardView logoutCard = view.findViewById(R.id.logoutCard);
        tituloClienteCard = view.findViewById(R.id.tvTitleCliente);
        tituloFragment = view.findViewById(R.id.tvTitleCuentas);

        clienteCard.setOnClickListener(v -> {
            if ("CLIENTE".equals(MainActivity.rol)) {
                abrirModoEdicionCliente();
            } else {
                replaceFragment(new ClienteFragment());
            }
        });

        usuarioCard.setOnClickListener(v -> replaceFragment(new UsuarioFragment()));
        logoutCard.setOnClickListener(v -> showLogoutConfirmation());

        ajustarVisibilidadPorRol();

        return view;
    }

    private void ajustarVisibilidadPorRol() {
        String rol = MainActivity.rol;
        Log.d("CuentasFragment", "Rol del usuario: " + rol);

        if ("CLIENTE".equals(rol)) {
            usuarioCard.setVisibility(View.GONE);
            tituloClienteCard.setText(getString(R.string.cuenta_cliente));
            tituloFragment.setText(getString(R.string.ajuste_clientes));
            Log.d("CuentasFragment", "El usuario es un cliente, ocultando usuarioCard y cambiando título a 'Tus datos'");
        } else {
            usuarioCard.setVisibility(View.VISIBLE);
            Log.d("CuentasFragment", "El usuario no es un cliente, mostrando usuarioCard");
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_logout_confirmation, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void abrirModoEdicionCliente() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId != null) {
            String url = getResources().getString(R.string.url_clientes) + "uid/" + userId;
            OkHttpClient client = new OkHttpClient();

            String idToken = sharedPreferences.getString("idToken", null);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + idToken)
                    .build();

            Log.d("CuentasFragment", "Cargando datos del cliente actual desde: " + url);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                    Log.e("CuentasFragment", "Error al cargar datos del cliente actual: ", e);
                }

                @Override
                public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("CuentasFragment", "Respuesta no exitosa del servidor: " + response);
                        throw new IOException("Código inesperado " + response);
                    }

                    assert response.body() != null;
                    final String responseData = response.body().string();
                    Log.d("CuentasFragment", "Datos del cliente cargados correctamente: " + responseData);

                    requireActivity().runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            Cliente cliente = new Cliente();
                            cliente.fromJSON(jsonObject);
                            Intent intent = new Intent(getContext(), NuevoCliente.class);
                            intent.putExtra("cliente", cliente);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.e("CuentasFragment", "Error al parsear datos del cliente: ", e);
                        }
                    });
                }
            });
        }
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}



