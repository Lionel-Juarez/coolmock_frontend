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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;
import com.example.hamacav1.entidades.pagos.Pago;
import com.example.hamacav1.entidades.pagos.PagoViewModel;
import com.example.hamacav1.entidades.reportes.NuevoReporte;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
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
    private PagoViewModel pagoViewModel;

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

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ReservasViewModel.class)) {
                    return (T) new ReservasViewModel(getActivity().getApplicationContext());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(ReservasViewModel.class);

        pagoViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(PagoViewModel.class)) {
                    return (T) new PagoViewModel(getActivity().getApplication());
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }).get(PagoViewModel.class);



        TextView tvFechaReserva = view.findViewById(R.id.tvFechaReserva);
        @SuppressLint("SimpleDateFormat") String fechaActual = new SimpleDateFormat("dd/MM/yy").format(new Date());
        tvFechaReserva.setText(fechaActual);
        tvFechaReserva.setTextColor(ContextCompat.getColor(requireContext(), R.color.principalColor));

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
            reservasList = nuevasReservas;
            if (reservasList == null || reservasList.isEmpty()) {
                tvNoReservas.setVisibility(View.VISIBLE);
                reservasRecyclerView.setVisibility(View.GONE);
            } else {
                tvNoReservas.setVisibility(View.GONE);
                reservasRecyclerView.setVisibility(View.VISIBLE);
                reservasAdapter.setReservas(reservasList);
                reservasAdapter.notifyDataSetChanged();
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
                viewModel.filterReservasByName(name);
            } else {
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
        reserva.setEstado("Pendiente");
        reserva.setMetodoPago(metodoPago);
        reserva.setFechaPago(LocalDateTime.now());

        viewModel.updatePagoReserva(reserva, (success) -> {
            if (success) {
                Pago pago = new Pago();
                pago.setReserva(reserva);
                pago.setCantidad(reserva.getTotal());
                pago.setMetodoPago(metodoPago);
                pago.setPagado(true);
                pago.setFechaPago(LocalDateTime.now());
                pago.setDetallesPago("Pago realizado para la reserva con ID " + reserva.getIdReserva());
                pago.setTipoHamaca("Standard"); // Modificar según sea necesario

                pagoViewModel.createPago(pago, (pagoSuccess) -> {
                    if (pagoSuccess) {
                        String titulo = "Pago de Reserva";
                        String descripcion = "La reserva con ID " + reserva.getIdReserva() + " ha sido pagada utilizando " + metodoPago + ".";
                        NuevoReporte.crearReporte(getContext(), titulo, descripcion);
                    } else {
                        Toast.makeText(getContext(), "Error al crear el pago", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onCambiarEstadoClicked(Reserva reserva) {
        showChangeStateDialog(reserva);
    }

    private void showChangeStateDialog(Reserva reserva) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Cambiar Estado de la Reserva");

        String haLlegado = requireContext().getResources().getString(R.string.ha_llegado);
        String cancelar = requireContext().getResources().getString(R.string.cancelar_reserva);
        String[] estados = {haLlegado, cancelar};
        builder.setItems(estados, (dialog, which) -> {
            String selectedState = estados[which];
            if (selectedState.equals(haLlegado)) {
                reserva.setEstado("Ha llegado");
                for (Sombrilla sombrilla : reserva.getSombrillas()) {
                    sombrilla.setReservada(false);
                    sombrilla.setOcupada(true);
                }
                viewModel.updateLlegadaReserva(reserva, success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Reserva actualizada con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error al actualizar la reserva", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (selectedState.equals(cancelar)) {
                AlertDialog.Builder cancelBuilder = new AlertDialog.Builder(requireContext());
                cancelBuilder.setTitle("Cancelar Reserva");

                final EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setHint("Razón de la cancelación");

                cancelBuilder.setView(input);

                cancelBuilder.setPositiveButton("Cancelar Reserva", (cancelDialog, whichCancel) -> {
                    String cancelacionDescripcion = input.getText().toString();
                    reserva.setEstado("Cancelada");
                    for (Sombrilla sombrilla : reserva.getSombrillas()) {
                        sombrilla.setReservada(false);
                        sombrilla.setOcupada(false);
                    }
                    viewModel.updateCancelacionReserva(reserva, cancelacionDescripcion, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al cancelar la reserva", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                cancelBuilder.setNegativeButton("Cancelar", (cancelDialog, whichCancel) -> cancelDialog.cancel());

                AlertDialog cancelDialog = cancelBuilder.create();
                cancelDialog.show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}