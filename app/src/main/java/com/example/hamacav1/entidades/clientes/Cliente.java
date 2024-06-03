package com.example.hamacav1.entidades.clientes;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements Serializable {
    private long idCliente;
    private String nombreCompleto;
    private String numeroTelefono;
    private String email;
    private String rol;
    private String uid;

    @NonNull
    @Override
    public String toString() {
        return this.getNombreCompleto();
    }


    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.idCliente = jsonObject.optLong("idCliente", -1);
        this.nombreCompleto = jsonObject.optString("nombreCompleto", "");
        this.numeroTelefono = jsonObject.optString("numeroTelefono", "");
        this.email = jsonObject.optString("email", "");
        this.rol = jsonObject.optString("rol", "");
        this.uid = jsonObject.optString("uid", "");
    }
}
