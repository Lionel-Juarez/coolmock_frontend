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
    private String nombreUsuario;
    private String password;
    private String rol;

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.optLong("id", -1);
        this.nombreUsuario = jsonObject.optString("nombreUsuario", "");
        this.password = jsonObject.optString("password", "");
        this.rol = jsonObject.optString("rol", "");
    }
}
