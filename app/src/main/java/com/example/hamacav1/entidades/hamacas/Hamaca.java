package com.example.hamacav1.entidades.hamacas;

import org.json.JSONException;
import org.json.JSONObject;

public class Hamaca {

    private long idHamaca;
    private double precio;
    private boolean reservada;
    private boolean ocupada;
    private int planoId; // Añade este campo

    public Hamaca() {
    }

    public Hamaca(long idHamaca, double precio, boolean reservada, boolean ocupada, int planoId) {
        this.idHamaca = idHamaca;
        this.precio = precio;
        this.reservada = reservada;
        this.ocupada = ocupada;
        this.planoId = planoId;
    }

    public int getPlanoId() {
        return planoId;
    }

    public void setPlanoId(int planoId) {
        this.planoId = planoId;
    }

    public long getIdHamaca() {
        return idHamaca;
    }

    public void setIdHamaca(long idHamaca) {
        this.idHamaca = idHamaca;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isReservada() {
        return reservada;
    }

    public void setReservada(boolean reservada) {
        this.reservada = reservada;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public void fromJSON(JSONObject fcjson) throws JSONException {
        this.idHamaca = fcjson.optLong("idHamaca", -1);
        this.precio = fcjson.optDouble("precio", 0);
        this.reservada = fcjson.optBoolean("reservada", false);
        this.ocupada = fcjson.optBoolean("ocupada", false);
    }

}
