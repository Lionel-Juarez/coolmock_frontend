package com.example.hamacav1.entidades.reservas;

import android.util.Log;

import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.hamacas.Hamaca;
import com.example.hamacav1.entidades.usuarios.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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
    private List<Hamaca> hamacas;  // Cambiado de un objeto singular a una lista
    private Cliente cliente;
    private Usuario creadaPor;
    private String estado;
    private boolean pagada;
    private String metodoPago;
    private String fechaPago;
    private String fechaReserva;


    // Método fromJSON en la clase que contiene reservas
    public void fromJSON(JSONObject json) throws JSONException {
        Log.d("Reserva", "JSON recibido: " + json.toString());
        this.idReserva = json.optLong("idReserva", -1);
        this.estado = json.optString("estado", "");
        this.pagada = json.optBoolean("pagada", false);
        this.metodoPago = json.optString("metodoPago", "");
        this.fechaPago = json.optString("fechaPago", "");
        this.fechaReserva = json.optString("fechaReserva", "");

        try {
            JSONArray hamacasJson = json.optJSONArray("hamacas");
            if (hamacasJson != null) {
                this.hamacas = new ArrayList<>();
                for (int i = 0; i < hamacasJson.length(); i++) {
                    JSONObject hamacaObj = hamacasJson.getJSONObject(i);
                    Hamaca hamaca = new Hamaca();
                    hamaca.fromJSON(hamacaObj);
                    this.hamacas.add(hamaca);
                }
            }
        } catch (JSONException e) {
            Log.e("Reserva", "Error parsing hamacas array: " + e.getMessage());
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
            Log.d("Reserva", "Usuario creado correctamente");
        } else {
            Log.d("Reserva", "No se encontró el objeto 'creadoPor' en el JSON");
            this.creadaPor = null;
        }

    }
}
