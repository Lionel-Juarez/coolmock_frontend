package com.example.hamacav1.entidades.clientes;

import org.json.JSONException;
import org.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private long idCliente;
    private String nombreCompleto;
    private String numeroTelefono;

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.idCliente = jsonObject.optLong("idCliente", -1);
        this.nombreCompleto = jsonObject.optString("nombreCompleto", "");
        this.numeroTelefono = jsonObject.optString("numeroTelefono", "");
    }
}
