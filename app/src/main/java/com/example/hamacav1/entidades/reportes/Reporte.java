package com.example.hamacav1.entidades.reportes;

import org.json.JSONException;
import org.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {
    private long idReporte;
    private String titulo;
    private String estado;
    private String comentarioCompleto;
    private String fechaCreacion;
    private String creadoPor;


    public void fromJSON(JSONObject fcjson) throws JSONException {
        this.idReporte = fcjson.optLong("idReporte", -1);
        this.titulo = fcjson.optString("titulo", "");
        this.estado = fcjson.optString("estado", "");
        this.comentarioCompleto = fcjson.optString("comentarioCompleto", "");
        this.fechaCreacion = fcjson.optString("fechaCreacion", "");

        // Actualización para manejar el objeto anidado 'creadoPor'
        JSONObject creador = fcjson.optJSONObject("creadoPor");
        if (creador != null) {
            this.creadoPor = creador.optString("nombreUsuario", "");
        } else {
            this.creadoPor = ""; // Proporciona un valor predeterminado si 'creadoPor' es null o no está presente.
        }
    }
}
