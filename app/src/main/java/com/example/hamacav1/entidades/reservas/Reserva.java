package com.example.hamacav1.entidades.reservas;

import android.util.Log;

import com.example.hamacav1.entidades.clientes.Cliente;
import com.example.hamacav1.entidades.hamacas.Hamaca;
import com.example.hamacav1.entidades.usuarios.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva implements Serializable {
    private Long idReserva;
    private Hamaca hamaca;
    private Cliente cliente;
    private Usuario creadoPor;
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

        JSONObject hamacaJson = json.optJSONObject("hamaca");
        if (hamacaJson != null) {
            this.hamaca = new Hamaca();
            this.hamaca.fromJSON(hamacaJson);
        }

        JSONObject clienteJson = json.optJSONObject("cliente");
        if (clienteJson != null) {
            this.cliente = new Cliente();
            this.cliente.fromJSON(clienteJson);
        }

        JSONObject usuarioJson = json.optJSONObject("creadaPor");
        if (usuarioJson != null) {
            this.creadoPor = new Usuario();
            this.creadoPor.fromJSON(usuarioJson);
            Log.d("Reserva", "Usuario creado correctamente");
        } else {
            Log.d("Reserva", "No se encontró el objeto 'creadoPor' en el JSON");
            this.creadoPor = null;
        }

    }


}
