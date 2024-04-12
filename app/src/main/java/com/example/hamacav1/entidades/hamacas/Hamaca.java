package com.example.hamacav1.entidades.hamacas;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Hamaca implements Parcelable {

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

    protected Hamaca(Parcel in) {
        idHamaca = in.readLong();
        precio = in.readDouble();
        reservada = in.readByte() != 0;
        ocupada = in.readByte() != 0;
        planoId = in.readInt();
    }

    public static final Creator<Hamaca> CREATOR = new Creator<Hamaca>() {
        @Override
        public Hamaca createFromParcel(Parcel in) {
            return new Hamaca(in);
        }

        @Override
        public Hamaca[] newArray(int size) {
            return new Hamaca[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(idHamaca);
        parcel.writeDouble(precio);
        parcel.writeByte((byte) (reservada ? 1 : 0));
        parcel.writeByte((byte) (ocupada ? 1 : 0));
        parcel.writeInt(planoId);
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
