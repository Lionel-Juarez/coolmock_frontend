package com.example.hamacav1.entidades.reports;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Report {

    private long idReporte;
    private String title;
    private String state;
    private String fullComment;
    private String creationDate;
    private String createdBy; //idUsuario deberia ir aqui


    public Report() {
    }

    public Report(Long idReporte, String title, String state, String fullComment, String creationDate, String createdBy) {
        this.idReporte = idReporte;
        this.title = title;
        this.state = state;
        this.fullComment = fullComment;
        this.creationDate = creationDate;
        this.createdBy = createdBy;
    }

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFullComment() {
        return fullComment;
    }

    public void setFullComment(String fullComment) {
        this.fullComment = fullComment;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public void fromJSON(JSONObject fcjson) throws JSONException {
        this.idReporte = fcjson.optLong("idReporte", -1);
        this.title = fcjson.optString("titulo", "");
        this.state = fcjson.optString("estado", "");
        this.fullComment = fcjson.optString("comentarioCompleto", "");
        this.creationDate = fcjson.optString("fechaCreacion", "");

        // Actualización para manejar el objeto anidado 'creadoPor'
        JSONObject creador = fcjson.optJSONObject("creadoPor");
        if (creador != null) {
            // Aquí asignamos el nombre de usuario del objeto 'creadoPor' al campo 'createdBy'.
            // Esto asume que 'createdBy' es una cadena. Si 'createdBy' debe ser un objeto más complejo,
            // necesitarás ajustar esta parte.
            this.createdBy = creador.optString("nombreUsuario", "");
        } else {
            this.createdBy = ""; // Proporciona un valor predeterminado si 'creadoPor' es null o no está presente.
        }
    }

}
