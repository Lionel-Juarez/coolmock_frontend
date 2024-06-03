package com.example.hamacav1.entidades.pagos;

import com.example.hamacav1.entidades.reservas.Reserva;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    private Long idPago;
    private Reserva reserva;
    private double cantidad;
    private String metodoPago;
    private boolean pagado;
    private LocalDateTime fechaPago;
    private String detallesPago;
    private String tipoHamaca;

    public void fromJSON(JSONObject jsonObject) {
        try {
            this.idPago = jsonObject.optLong("idPago");
            this.cantidad = jsonObject.optDouble("cantidad");
            this.metodoPago = jsonObject.optString("metodoPago");
            this.pagado = jsonObject.optBoolean("pagado");
            this.fechaPago = parseDateTime(jsonObject.optString("fechaPago"));
            this.detallesPago = jsonObject.optString("detallesPago");
            this.tipoHamaca = jsonObject.optString("tipoHamaca");

            JSONObject reservaJson = jsonObject.optJSONObject("reserva");
            if (reservaJson != null) {
                this.reserva = new Reserva();
                this.reserva.fromJSON(reservaJson);
            }
        } catch (JSONException ignored) {
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr != null && !dateTimeStr.isEmpty() && !dateTimeStr.equals("null")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                return LocalDateTime.parse(dateTimeStr, formatter);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
