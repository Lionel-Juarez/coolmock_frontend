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
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
    ActivityResultLauncher<Intent> nuevoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d("ReservaFragment", "Una nueva reserva fue añadida. Recargando reservas...");
                    cargarReservas(); // Asumiendo que este método recarga la lista desde el backend
                }
            });


    @Override
    public void editPressed(int position) {
        if (reservasList != null) {
            if (reservasList.size() > position) {
                Reserva reserva = reservasList.get(position);
                Intent myIntent = new Intent(getActivity(), NuevaReserva.class);
                //myIntent.putExtra("idReserva", reserva.getCreadoPor());
                nuevoResultLauncher.launch(myIntent);
            }
        }
    }

    public interface OnReservasReceivedListener {
        void onReceived(List<Reserva> reservas);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservas, container, false);
        reservasRecyclerView = view.findViewById(R.id.reservasRecyclerView);
        reservasRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reservasList = new ArrayList<>();
        reservasAdapter = new ReservaAdapter(reservasList, getContext(), this);
        reservasRecyclerView.setAdapter(reservasAdapter);

        loadReservasFromBackend();

        view.findViewById(R.id.fab_add_reserva).setOnClickListener(v -> irHamacas());

        return view;
    }

    private void irHamacas() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).selectSunbed();
        }
    }


    private void loadReservasFromBackend() {
        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), "No hay conexión a Internet", Toast.LENGTH_LONG).show();
            return;
        }
        String url = getResources().getString(R.string.url_reservas);
        Log.d("ReservaFragment", "Cargando reservas desde el backend: " + url);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ReservaFragment", "Error al cargar reservas: ", e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.e("ReservaFragment", "Respuesta no exitosa del servidor: " + response);
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en respuesta del servidor: Código " + response.code(), Toast.LENGTH_LONG).show());
                        return;
                    }

                    final String responseData = response.body().string();
                    Log.d("ReservaFragment", "Reservas cargadas correctamente: " + responseData);

                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) { // Verifica que el fragmento esté aún adjunto a la actividad
                            Toast.makeText(getContext(), "Mensaje", Toast.LENGTH_LONG).show();
                            try {
                                JSONArray jsonArray = new JSONArray(responseData);
                                reservasList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Reserva reserva = new Reserva();
                                    reserva.fromJSON(jsonObject);
                                    reservasList.add(reserva);
                                }
                                reservasAdapter.notifyDataSetChanged();
                                Log.d("ReservaFragment", "Reservas actualizadas en la interfaz de usuario.");
                                Toast.makeText(getContext(), "Reservas actualizadas.", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Log.e("ReservaFragment", "Error al parsear reservas: ", e);
                                Toast.makeText(getContext(), "Error al procesar las reservas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }                        }
                    });
                } catch (IOException e) {
                    Log.e("ReservaFragment", "Error al leer la respuesta: ", e);
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al leer respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void procesarRespuestaReservas(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            reservasList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Reserva reserva = new Reserva();
                reserva.fromJSON(jsonObject);
                reservasList.add(reserva);
            }
            reservasAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Reservas actualizadas.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error al procesar las reservas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void scrollToNewestReserva() {
        if (!reservasList.isEmpty()) {
            reservasRecyclerView.scrollToPosition(reservasList.size() - 1);
        }
    }

    @Override
    public void deletePressed(int position) {
        AlertDialog diaBox = AskOption(position);
        diaBox.show();//Mostramos un diálogo de confirmación
    }
    private AlertDialog AskOption(final int position) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getActivity())

                .setTitle(R.string.eliminar_reserva)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        eliminarReserva(position);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private void eliminarReserva(int position){
        if(reservasList !=null && reservasList.size() > position) {
            Reserva reserva = reservasList.get(position);
            Log.d("ReservaFragment", "Eliminando reserva: " + reserva.getIdReserva());

            if (isNetworkAvailable()) {
                String url = getResources().getString(R.string.url_reservas) + "eliminarReserva/" + reserva.getIdReserva();
                eliminarTask(url);
            } else {
                Log.e("ReservaFragment", "Conexión de red no disponible para eliminar reserva.");
                showError("error.IOException");
            }
        } else {
            Log.e("ReservaFragment", "Posición de reserva no válida o lista de reservas vacía.");
            showError("error.desconocido");
        }
    }


    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) {
                return false;
            } else {
                NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
                return (actNw != null) && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }


    private void eliminarTask(String url){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(new Runnable() {
            @Override
            public void run() {

                Internetop interopera= Internetop.getInstance();
                String result = interopera.deleteTask(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(result.equalsIgnoreCase("error.IOException")||
                                result.equals("error.OKHttp")) {
                            showError(result);
                        }
                        else if(result.equalsIgnoreCase("null")){
                            showError("error.desconocido");
                        }
                        else{
                            cargarReservas();
                        }
                    }
                });
            }
        });
    }


    private void cargarReservas() {
        Log.d("ReservaFragment", "Cargando reservas...");
        loadReservasFromBackend(); // Este método debe manejar la carga de reservas desde el backend
    }
    private void resetLista(String result){
        try {
            JSONArray listaReservasJson = new JSONArray(result);
            if (reservasList == null) {
                reservasList = new ArrayList<>();
            } else {
                reservasList.clear();
            }
            for (int i = 0; i < listaReservasJson.length(); ++i) {
                JSONObject jsonUser = listaReservasJson.getJSONObject(i);
                Reserva reserva = new Reserva();
                reserva.fromJSON(jsonUser);
                reservasList.add(reserva);
            }
            if (reservasAdapter == null) {
                reservasAdapter = new ReservaAdapter(reservasList, getContext(), this);
                reservasRecyclerView.setAdapter(reservasAdapter);
            } else {
                reservasAdapter.notifyDataSetChanged();
            }
            // Si estás utilizando una ProgressBar, aquí iría el código para ocultarla
            // Por ejemplo:
            // ProgressBar pbMain = findViewById(R.id.pb_main);
            // pbMain.setVisibility(View.GONE);
        } catch (JSONException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String error) {
        String message;
        Resources res = getResources();
        int duration;
        if (error.equals("error.IOException")||error.equals("error.OKHttp")) {
            message = res.getString(R.string.error_connection);
            duration = Toast.LENGTH_SHORT;
        }
        else if(error.equals("error.undelivered")){
            message = res.getString(R.string.error_undelivered);
            duration = Toast.LENGTH_LONG;
        }
        else {
            message = res.getString(R.string.error_unknown);
            duration = Toast.LENGTH_SHORT;
        }
        Toast toast = Toast.makeText(getActivity(), message, duration);
        toast.show();
        Log.d("ReservaFragment", "Mostrando error: " + message);
    }
}