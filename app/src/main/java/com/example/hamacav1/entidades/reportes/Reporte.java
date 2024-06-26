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

    public void fromJSON(JSONObject fcjson) throws JSONException {
        this.idReporte = fcjson.optLong("idReporte", -1);
        this.titulo = fcjson.optString("titulo", "");
        this.estado = fcjson.optString("estado", "");
        this.comentarioCompleto = fcjson.optString("comentarioCompleto", "");
        this.fechaCreacion = fcjson.optString("fechaCreacion", "");
    }
}
