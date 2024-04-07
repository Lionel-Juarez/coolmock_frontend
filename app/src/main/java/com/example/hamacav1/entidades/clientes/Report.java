package com.example.hamacav1.entidades.clientes;

import org.json.JSONException;
import org.json.JSONObject;

public class Report {

    private long idReporte;
    private String titulo;
    private String estado;
    private String comentarioCompleto;
    private String fechaCreacion;
    private String creadoPor; //idUsuario deberia ir aqui


    public Report() {
    }

    public Report(Long idReporte, String title, String estado, String comentarioCompleto, String fechaCreacion, String creadoPor) {
        this.idReporte = idReporte;
        this.titulo = title;
        this.estado = estado;
        this.comentarioCompleto = comentarioCompleto;
        this.fechaCreacion = fechaCreacion;
        this.creadoPor = creadoPor;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getComentarioCompleto() {
        return comentarioCompleto;
    }

    public void setComentarioCompleto(String comentarioCompleto) {
        this.comentarioCompleto = comentarioCompleto;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }


    public void fromJSON(JSONObject fcjson) throws JSONException {
        this.idReporte = fcjson.optLong("idReporte", -1);
        this.titulo = fcjson.optString("titulo", "");
        this.estado = fcjson.optString("estado", "");
        this.comentarioCompleto = fcjson.optString("comentarioCompleto", "");
        this.fechaCreacion = fcjson.optString("fechaCreacion", "");

        // Actualización para manejar el objeto anidado 'creadoPor'
        JSONObject creador = fcjson.optJSONObject("creadoPor");
        if (creador != null) {
            // Aquí asignamos el nombre de usuario del objeto 'creadoPor' al campo 'createdBy'.
            // Esto asume que 'createdBy' es una cadena. Si 'createdBy' debe ser un objeto más complejo,
            // necesitarás ajustar esta parte.
            this.creadoPor = creador.optString("nombreUsuario", "");
        } else {
            this.creadoPor = ""; // Proporciona un valor predeterminado si 'creadoPor' es null o no está presente.
        }
    }

}
