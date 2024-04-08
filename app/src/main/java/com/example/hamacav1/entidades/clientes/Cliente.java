package com.example.hamacav1.entidades.clientes;

import org.json.JSONException;
import org.json.JSONObject;

public class Cliente {

    private long idCLiente;
    private String nombreCompleto;
    private String numeroTelefono;

    public Cliente() {
    }

    public Cliente(long idCliente, String nombreCompleto, String numeroTelefono) {
        this.idCLiente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.numeroTelefono = numeroTelefono;
    }

    public long getIdCLiente() {
        return idCLiente;
    }

    public void setIdCLiente(long idCLiente) {
        this.idCLiente = idCLiente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.idCLiente = jsonObject.optLong("idCliente", -1);
        this.nombreCompleto = jsonObject.optString("nombreCompleto", "");
        this.numeroTelefono = jsonObject.optString("numeroTelefono", "");
    }
}
