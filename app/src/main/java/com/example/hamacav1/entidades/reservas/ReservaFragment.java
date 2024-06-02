package com.example.hamacav1.entidades.reservas;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReservaFragment extends Fragment implements ReservaAdapter.ReservasAdapterCallback {
    public static final String EXTRA_RESERVA_ID = "EXTRA_RESERVA_ID";
    private RecyclerView reservasRecyclerView;
    private ReservaAdapter reservasAdapter;
    private List<Reserva> reservasList;
    private ReservasViewModel viewModel;
    private TextView tvNoReservas;
    private Long reservaId;
    private ProgressBar progressBar;

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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas, container, false);
        reservasRecyclerView = view.findViewById(R.id.reservasRecyclerView);
        tvNoReservas = view.findViewById(R.id.tvNoReservas);
        progressBar = view.findViewById(R.id.progressBar);

        reservasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservasAdapter = new ReservaAdapter(new ArrayList<>(), getContext(), this, reservaId);
        reservasRecyclerView.setAdapter(reservasAdapter);

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ReservasViewModel.class)) {
                    return (T) new ReservasViewModel(getActivity().getApplicationContext());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(ReservasViewModel.class);

        Date today = Calendar.getInstance().getTime();
        viewModel.loadReservasByDateAndState(today, "Pendiente");

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessages().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getReservas().observe(getViewLifecycleOwner(), nuevasReservas -> {
            if (nuevasReservas == null || nuevasReservas.isEmpty()) {
                tvNoReservas.setVisibility(View.VISIBLE);
                reservasRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoReservas.setVisibility(View.GONE);
                reservasRecyclerView.setVisibility(View.VISIBLE);
                reservasList = nuevasReservas;
                reservasAdapter.setReservas(nuevasReservas);
                reservasAdapter.notifyDataSetChanged();
                if (reservaId != null) {
                    scrollToItem(reservaId);
                }
            }
        });

        view.findViewById(R.id.btnNew).setOnClickListener(v -> irSombrillas());
        setupFilterMenu(view);
        return view;
    }



    private void irSombrillas() {
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
    private void setupFilterMenu(View view) {
        ImageView filterButton = view.findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(this::showFilterPopup);
    }

    private void showFilterPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.popup_menu_layout, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view.getRootView(), Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.action_filter_name).setOnClickListener(v -> {
            showNameSearchDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_filter_state).setOnClickListener(v -> {
            showStateSelectionDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_select_date).setOnClickListener(v -> {
            showDatePickerDialog();
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.action_show_all).setOnClickListener(v -> {
            viewModel.loadAllReservas();
            popupWindow.dismiss();
        });
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    viewModel.loadReservasByDate(selectedDate.getTime());
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private void showNameSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filtrar por nombre");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                Log.d("DialogInterface", "Filtrando por nombre: " + name);
                viewModel.filterReservasByName(name);
            } else {
                Log.d("DialogInterface", "Intento de filtrado sin nombre");
                Toast.makeText(getContext(), "Por favor, introduce un nombre.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Log.d("DialogInterface", "Filtrado por nombre cancecantidadHamacas");
            dialog.cancel();
        });

        builder.show();
    }

    private void showStateSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Estado");
        String[] states = {"Pendiente", "Ha llegado", "Cancelada"};
        builder.setItems(states, (dialog, which) -> {
            Log.d("DialogInterface", "Filtrando por nombre: " + states[which]);
            viewModel.filterReservasByState(states[which]);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void detailExpanded(int position) {

    }
    @Override
    public void detailCollapsed(int position) {

    }

    @Override
    public void onPagarClicked(Reserva reserva) {
        showPaymentMethodDialog(reserva);
    }

    private void showPaymentMethodDialog(Reserva reserva) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar Método de Pago");

        String[] paymentMethods = {"Tarjeta", "Efectivo"};
        builder.setItems(paymentMethods, (dialog, which) -> {
            String selectedMethod = paymentMethods[which];
            processPayment(reserva, selectedMethod);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void processPayment(Reserva reserva, String metodoPago) {
        reserva.setPagada(true);
        reserva.setEstado("Ha llegado");
        reserva.setMetodoPago(metodoPago);
        reserva.setFechaPago(LocalDateTime.now());

        // Actualizar las sombrillas asociadas
        for (Sombrilla sombrilla : reserva.getSombrillas()) {
            sombrilla.setReservada(false);
            sombrilla.setOcupada(true);
        }

        // Llamar al backend para actualizar la reserva y las sombrillas
        viewModel.updateReserva(reserva, (success) -> {
            if (success) {
                // Crear el reporte solo si la actualización fue exitosa
                String titulo = "Pago de Reserva";
                String descripcion = "La reserva con ID " + reserva.getIdReserva() + " ha sido pagada utilizando " + metodoPago + ".";
                NuevoReporte.crearReporte(getContext(), titulo, descripcion);
            } else {
                Toast.makeText(getContext(), "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onCambiarEstadoClicked(Reserva reserva) {

    }

    @Override
    public void onModificarClicked(Reserva reserva) {

    }
}