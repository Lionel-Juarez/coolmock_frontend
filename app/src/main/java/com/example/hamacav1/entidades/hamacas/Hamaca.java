package com.example.hamacav1.entidades.hamacas;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.hamacav1.entidades.reservas.Reserva;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hamaca {

    private long idHamaca;
    private double precio;
    private boolean reservada;
    private boolean ocupada;
    private int planoId;
    private Reserva idReserva;

    // Método fromJSON modificado
    public void fromJSON(JSONObject json) throws JSONException {
        idHamaca = json.optLong("idHamaca", -1);
        precio = json.optDouble("precio", 0.0);
        reservada = json.optBoolean("reservada", false);
        ocupada = json.optBoolean("ocupada", false);
        planoId = json.optInt("planoId", 0);

        JSONObject reservaJson = json.optJSONObject("idReserva");
        if (reservaJson != null) {
            this.idReserva = new Reserva();
            this.idReserva.fromJSON(reservaJson);
        }
    }
}
