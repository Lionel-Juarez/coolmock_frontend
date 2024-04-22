package com.example.hamacav1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
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

import com.example.hamacav1.entidades.calculos.CalcsFragment;
import com.example.hamacav1.entidades.clientes.ClienteFragment;
import com.example.hamacav1.entidades.hamacas.HamacaFragment;
import com.example.hamacav1.entidades.reportes.ReportsFragment;
import com.example.hamacav1.databinding.ActivityMainBinding;
import com.example.hamacav1.entidades.reservas.ReservaFragment;
import com.example.hamacav1.entidades.usuarios.UsuarioFragment;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                replaceFragment(new HamacaFragment());
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

        LinearLayout usuarioLayout = dialog.findViewById(R.id.layoutUsuario);
        LinearLayout clienteLayout = dialog.findViewById(R.id.layoutCliente);
        LinearLayout liveLayout = dialog.findViewById(R.id.layoutLive);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        clienteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplazar el fragmento actual con ClienteFragment
                replaceFragment(new ClienteFragment());
                dialog.dismiss();
            }
        });

        usuarioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reemplazar el fragmento actual con UserFragment
                replaceFragment(new UsuarioFragment());
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
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }



    public void selectSunbed() {
        binding.bottomNavigationView.setSelectedItemId(R.id.sunbed); // Establece el elemento seleccionado programáticamente
    }



}