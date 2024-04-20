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

    private RecyclerView reservasRecyclerView;
    private ReservaAdapter reservasAdapter;
    private List<Reserva> reservasList;
    private ReservasViewModel viewModel;
    private TextView tvNoReservas;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas, container, false);
        reservasRecyclerView = view.findViewById(R.id.reservasRecyclerView);
        tvNoReservas = view.findViewById(R.id.tvNoReservas);  // Referencia al TextView
        reservasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservasAdapter = new ReservaAdapter(new ArrayList<>(), getContext(), this);
        reservasRecyclerView.setAdapter(reservasAdapter);

        viewModel = new ViewModelProvider(this).get(ReservasViewModel.class);
        viewModel.getReservas().observe(getViewLifecycleOwner(), nuevasReservas -> {
            if (nuevasReservas == null || nuevasReservas.isEmpty()) {
                tvNoReservas.setVisibility(View.VISIBLE);
                reservasRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoReservas.setVisibility(View.GONE);
                reservasRecyclerView.setVisibility(View.VISIBLE);
                reservasAdapter.setReservas(nuevasReservas);
                reservasAdapter.notifyDataSetChanged();
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

    @Override
    public void deletePressed(int position) {

    }

    @Override
    public void editPressed(int position) {

    }
}