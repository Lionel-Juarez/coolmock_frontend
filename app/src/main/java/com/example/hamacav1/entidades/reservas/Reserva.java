package com.example.hamacav1.entidades.reservas;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.sombrillas.Sombrilla;
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
    private List<Sombrilla> sombrillas;
    private Cliente cliente;
    private Usuario creadaPor;
    private String estado;
    private boolean pagada;
    private String metodoPago;
    private String horaLlegada;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaReservaRealizada;
    private String cantidadHamacas;

    public void fromJSON(JSONObject json) throws JSONException {
        this.idReserva = json.optLong("idReserva");
        this.estado = json.optString("estado");
        this.pagada = json.optBoolean("pagada");
        this.metodoPago = json.optString("metodoPago");
        this.horaLlegada = json.optString("horaLlegada");
        this.cantidadHamacas = json.optString("cantidadHamacas");
        this.fechaPago = parseDateTime(json.optString("fechaPago"));
        this.fechaReserva = parseDateTime(json.optString("fechaReserva"));
        this.fechaReservaRealizada = parseDateTime(json.optString("fechaReservaRealizada"));

        JSONArray sombrillasJson = json.optJSONArray("sombrillas");
        if (sombrillasJson != null) {
            this.sombrillas = new ArrayList<>();
            for (int i = 0; i < sombrillasJson.length(); i++) {
                JSONObject sombrillaObj = sombrillasJson.getJSONObject(i);
                Sombrilla sombrilla = Sombrilla.fromJSON(sombrillaObj);
                this.sombrillas.add(sombrilla);
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
