package com.example.hamacav1.entidades.reservas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.entidades.hamacas.HamacaFragment;
import com.example.hamacav1.util.Internetop;
import com.example.hamacav1.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class ReservaFragment extends Fragment implements ReservaAdapter.ReservasAdapterCallback {
    public static final String EXTRA_RESERVA_ID = "EXTRA_RESERVA_ID";
    private RecyclerView reservasRecyclerView;
    private ReservaAdapter reservasAdapter;
    private List<Reserva> reservasList;
    private ReservasViewModel viewModel;
    private TextView tvNoReservas;
    private Long reservaId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reservaId = getArguments().getLong(EXTRA_RESERVA_ID, -1);
            if (reservaId != -1) {
                Log.d("ReservaFragment", "ID de Reserva recibido: " + reservaId);
            } else {
                Log.d("ReservaFragment", "No se recibió ID de Reserva válido.");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas, container, false);
        reservasRecyclerView = view.findViewById(R.id.reservasRecyclerView);
        tvNoReservas = view.findViewById(R.id.tvNoReservas);
        reservasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Asegúrate de que 'this' y 'reservaId' estén en el orden correcto
        reservasAdapter = new ReservaAdapter(new ArrayList<>(), getContext(), this, reservaId);

        reservasRecyclerView.setAdapter(reservasAdapter);
        viewModel = new ViewModelProvider(this).get(ReservasViewModel.class);
        viewModel.getReservas().observe(getViewLifecycleOwner(), nuevasReservas -> {
            if (nuevasReservas == null || nuevasReservas.isEmpty()) {
                tvNoReservas.setVisibility(View.VISIBLE);
                reservasRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoReservas.setVisibility(View.GONE);
                reservasRecyclerView.setVisibility(View.VISIBLE);
                reservasList = nuevasReservas; // Actualiza reservasList aquí
                reservasAdapter.setReservas(nuevasReservas);
                reservasAdapter.notifyDataSetChanged();
                if (reservaId != null) {
                    scrollToItem(reservaId);
                }
            }
        });


        view.findViewById(R.id.fab_add_reserva).setOnClickListener(v -> irHamacas());

        return view;
    }


    private void irHamacas() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).selectSunbed();
        }
    }


    private void scrollToItem(Long reservaId) {
        int position = findReservaPositionById(reservaId);
        if (position >= 0) {
            Log.d("ReservaFragment", "Desplazando a la posición de Reserva: " + position);
            reservasRecyclerView.scrollToPosition(position);
            reservasAdapter.expandItem(position);
        } else {
            Log.d("ReservaFragment", "Reserva ID " + reservaId + " no encontrada.");
        }
    }
    private int findReservaPositionById(Long id) {
        if (reservasList == null) return -1;  // Retorna -1 si la lista es nula
        for (int i = 0; i < reservasList.size(); i++) {
            if (reservasList.get(i).getIdReserva().equals(id)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void deletePressed(int position) {

    }

    @Override
    public void editPressed(int position) {

    }

    @Override
    public void detailExpanded(int position) {

    }

    @Override
    public void detailCollapsed(int position) {

    }
}