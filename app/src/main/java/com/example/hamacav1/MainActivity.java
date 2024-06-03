package com.example.hamacav1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.hamacav1.entidades.gestionCuentas.CuentasFragment;
import com.example.hamacav1.entidades.pagos.PagoFragment;
import com.example.hamacav1.entidades.sombrillas.SombrillaFragment;
import com.example.hamacav1.entidades.reportes.ReportsFragment;
import com.example.hamacav1.databinding.ActivityMainBinding;
import com.example.hamacav1.entidades.reservas.ReservaFragment;
import com.example.hamacav1.initialmenus.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        renovarToken();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new ReservaFragment());

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new ReservaFragment());
            } else if (itemId == R.id.sunbed) {
                Log.d("Main", "El boton sunbed se ha pulsado");
                replaceFragment(new SombrillaFragment());
            } else if (itemId == R.id.reportes) {
                replaceFragment(new ReportsFragment());
            } else if (itemId == R.id.calcs) {
                replaceFragment(new PagoFragment());
            } else if (itemId == R.id.cuentas) {
                replaceFragment(new CuentasFragment());
            }
            return true;
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
