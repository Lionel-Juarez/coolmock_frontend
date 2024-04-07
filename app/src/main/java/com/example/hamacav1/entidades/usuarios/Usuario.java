package com.example.hamacav1.entidades.usuarios;

import org.json.JSONException;
import org.json.JSONObject;

public class Usuario {

    private long id;
    private String nombreUsuario;
    private String password;
    private boolean esAdministrador;

    public Usuario() {
    }

    public Usuario(long id, String nombreUsuario, String password, boolean esAdministrador) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.esAdministrador = esAdministrador;
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

    public boolean isEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(boolean esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    public void fromJSON(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.optLong("id", -1);
        this.nombreUsuario = jsonObject.optString("nombreUsuario", "");
        this.password = jsonObject.optString("password", "");
        this.esAdministrador = jsonObject.optBoolean("esAdministrador", false);
    }
}
