package com.example.hamacav1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.example.hamacav1.entidades.gestionCuentas.CuentasFragment;
import com.example.hamacav1.entidades.pagos.PagoFragment;
import com.example.hamacav1.entidades.sombrillas.SombrillaFragment;
import com.example.hamacav1.entidades.reportes.ReportsFragment;
import com.example.hamacav1.databinding.ActivityMainBinding;
import com.example.hamacav1.entidades.reservas.ReservaFragment;
import com.example.hamacav1.initialmenus.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    public static String rol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String idToken = sharedPreferences.getString("idToken", null);

        if (userId == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        verificarRolCliente(userId, idToken);

        renovarToken();

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new ReservaFragment());
            } else if (itemId == R.id.sunbed) {
                Log.d("Main", "El boton sunbed se ha pulsado");
                replaceFragment(new SombrillaFragment());
            } else if (itemId == R.id.reportes && rol != null && !rol.equals("CLIENTE")) {
                replaceFragment(new ReportsFragment());
            } else if (itemId == R.id.calcs && rol != null && !rol.equals("CLIENTE")) {
                replaceFragment(new PagoFragment());
            } else if (itemId == R.id.cuentas) {
                replaceFragment(new CuentasFragment());
            }
            return true;
        });
    }

    private void ajustarNavegacionPorRol() {
        Menu menu = binding.bottomNavigationView.getMenu();
        if (rol != null && rol.equals("CLIENTE")) {
            menu.findItem(R.id.reportes).setVisible(false);
            menu.findItem(R.id.calcs).setVisible(false);
        }
    }


    private void verificarRolCliente(String uid, String idToken) {
        String url = getResources().getString(R.string.url_clientes) + "uid/" + uid;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + idToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull @NotNull Call call, @NonNull @NotNull IOException e) {
                Log.e("MainActivity", "Error al obtener el cliente", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al obtener los datos del cliente", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull @NotNull Call call, @NonNull @NotNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.d("MainActivity", "Cliente: " + jsonObject.toString(4)); // Formato bonito para JSON
                        rol = jsonObject.optString("rol", "CLIENTE"); // Si el rol es nulo, establece como "CLIENTE" por defecto

                        runOnUiThread(() -> {
                            ajustarNavegacionPorRol();
                            replaceFragment(new ReservaFragment()); // Asegurar que se reemplace el fragmento después de ajustar la navegación
                        });
                    } catch (JSONException e) {
                        Log.e("MainActivity", "Error al procesar los datos del cliente", e);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al procesar los datos del cliente", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MainActivity", "Respuesta no exitosa del servidor");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al obtener los datos del cliente", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void renovarToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    // Guardar el token en SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("idToken", idToken);
                    editor.apply();
                    Log.d("TokenRenovado", "Firebase ID Token: " + idToken);
                } else {
                    // Manejar error de renovación del token
                    Log.e("TokenRenovacionError", "Error al renovar el token: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }
    }
    public void selectSunbed() {
        binding.bottomNavigationView.setSelectedItemId(R.id.sunbed);
    }
    public void setSelectedItemId(int itemId) {
        binding.bottomNavigationView.setSelectedItemId(itemId);
    }
}
