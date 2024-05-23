package com.example.hamacav1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hamacav1.entidades.calculos.CalcsFragment;
import com.example.hamacav1.entidades.clientes.ClienteFragment;
import com.example.hamacav1.entidades.sombrillas.SombrillaFragment;
import com.example.hamacav1.entidades.reportes.ReportsFragment;
import com.example.hamacav1.databinding.ActivityMainBinding;
import com.example.hamacav1.entidades.reservas.ReservaFragment;
import com.example.hamacav1.entidades.usuarios.UsuarioFragment;
import com.example.hamacav1.initialmenus.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Verificar sesión
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            // No hay sesión activa, redirigir a la pantalla de login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return; // Salir del método para evitar ejecutar el resto del código
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
                replaceFragment(new CalcsFragment());
            }

            return true;
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

    }
    public  void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        TextView usuarioLayout = dialog.findViewById(R.id.layoutUsuario);
        TextView clienteLayout = dialog.findViewById(R.id.layoutCliente);
        TextView logoutLayout = dialog.findViewById(R.id.layoutLogout);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        clienteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ClienteFragment());
                dialog.dismiss();
            }
        });

        usuarioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new UsuarioFragment());
                dialog.dismiss();
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    // Método para renovar el token
    private void renovarToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
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
                        Log.e("TokenRenovacionError", "Error al renovar el token: " + task.getException().getMessage());
                    }
                }
            });
        }
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut(); // Cerrar sesión de Firebase
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Limpiar SharedPreferences
        editor.apply();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finalizar MainActivity
    }
    public void selectSunbed() {
        binding.bottomNavigationView.setSelectedItemId(R.id.sunbed); // Establece el elemento seleccionado programáticamente
    }
    public void setSelectedItemId(int itemId) {
        binding.bottomNavigationView.setSelectedItemId(itemId);
    }

}