package com.example.hamacav1.entidades.usuarios;

import org.json.JSONException;
import org.json.JSONObject;

public class Usuario {

    private long id;
    private String nombreUsuario;
    private String password;
    private String rol;

    public Usuario() {
    }

    public Usuario(long id, String nombreUsuario, String password, String rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.rol = rol;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.optLong("id", -1);
        this.nombreUsuario = jsonObject.optString("nombreUsuario", "");
        this.password = jsonObject.optString("password", "");
        this.rol = jsonObject.optString("rol", "");
    }
}
