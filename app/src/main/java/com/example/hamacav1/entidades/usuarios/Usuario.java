package com.example.hamacav1.entidades.usuarios;

import org.json.JSONException;
import org.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private long id;
    private String uid;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String rol;

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.optLong("id", -1);
        this.uid = jsonObject.optString("uid", "");
        this.nombreCompleto = jsonObject.optString("nombreCompleto", "");
        this.email = jsonObject.optString("email", "");
        this.telefono = jsonObject.optString("telefono", "");
        this.rol = jsonObject.optString("rol", "");
    }
}
