package com.example.hamacav1.entidades.reservas;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
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
import java.util.Calendar;
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
//        Spinner spinnerFiltro = view.findViewById(R.id.spinnerFiltro);
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

//        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String seleccion = parent.getItemAtPosition(position).toString();
//                viewModel.ordenarReservas(seleccion);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });

//        Button btnSelectDate = view.findViewById(R.id.btnSelectDate);
//        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        view.findViewById(R.id.fab_add_reserva).setOnClickListener(v -> irHamacas());
        setupFilterMenu(view);
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

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    viewModel.loadReservasByDate(selectedDate.getTime());
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private void setupFilterMenu(View view) {
        ImageButton filterButton = view.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(v -> showFilterPopup(v));
    }


    private void showFilterPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.filter_options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_filter_name) {
                showNameSearchDialog();
                return true;
            } else if (id == R.id.action_filter_state) {
                showStateSelectionDialog();
                return true;
            } else if (id == R.id.action_select_date) {
                showDatePickerDialog();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showNameSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Buscar por nombre");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString();
            viewModel.filterReservasByName(name);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void showStateSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar Estado");
        String[] states = {"Pendiente", "Ha llegado", "Cancelada"};
        builder.setItems(states, (dialog, which) -> {
            viewModel.filterReservasByState(states[which]);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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