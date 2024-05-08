package com.example.hamacav1.entidades.reservas;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.hamacas.Hamaca;
import com.example.hamacav1.entidades.usuarios.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva implements Serializable {
    private Long idReserva;
    private List<Hamaca> hamacas;
    private Cliente cliente;
    private Usuario creadaPor;
    private String estado;
    private boolean pagada;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaReserva;
    private String lado;

    public void fromJSON(JSONObject json) throws JSONException {
        this.idReserva = json.optLong("idReserva");
        this.estado = json.optString("estado");
        this.pagada = json.optBoolean("pagada");
        this.metodoPago = json.optString("metodoPago");
        this.fechaPago = parseDateTime(json.optString("fechaPago"));
        this.fechaReserva = parseDateTime(json.optString("fechaReserva"));
        this.lado = json.optString("lado");

        JSONArray hamacasJson = json.optJSONArray("hamacas");
        if (hamacasJson != null) {
            this.hamacas = new ArrayList<>();
            for (int i = 0; i < hamacasJson.length(); i++) {
                JSONObject hamacaObj = hamacasJson.getJSONObject(i);
                Hamaca hamaca = Hamaca.fromJSON(hamacaObj);
                this.hamacas.add(hamaca);
            }
        }

        JSONObject clienteJson = json.optJSONObject("cliente");
        if (clienteJson != null) {
            this.cliente = new Cliente();
            this.cliente.fromJSON(clienteJson);
        }

        JSONObject usuarioJson = json.optJSONObject("creadaPor");
        if (usuarioJson != null) {
            this.creadaPor = new Usuario();
            this.creadaPor.fromJSON(usuarioJson);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr != null && !dateTimeStr.isEmpty() && !dateTimeStr.equals("null")) {
                Log.d("parseDateTime", "Fecha a analizar: " + dateTimeStr);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                return LocalDateTime.parse(dateTimeStr, formatter);
            }
        } catch (DateTimeParseException e) {
            Log.e("parseDateTime", "Error parsing date: " + dateTimeStr, e);
        }
        return null;
    }

}
