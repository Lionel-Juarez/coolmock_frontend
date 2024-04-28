package com.example.hamacav1.entidades.hamacas;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.hamacav1.entidades.reservas.Reserva;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hamaca implements Parcelable {

    private long idHamaca;
    private double precio;
    private boolean reservada;
    private boolean ocupada;
    private int planoId;
    private Long reservaId;
    private String numeroHamaca; // Añade este campo


    protected Hamaca(Parcel in) {
        idHamaca = in.readLong();
        precio = in.readDouble();
        reservada = in.readByte() != 0;
        ocupada = in.readByte() != 0;
        planoId = in.readInt();
        reservaId = (Long) in.readValue(Long.class.getClassLoader());
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
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(idHamaca);
        parcel.writeDouble(precio);
        parcel.writeByte((byte) (reservada ? 1 : 0));
        parcel.writeByte((byte) (ocupada ? 1 : 0));
        parcel.writeInt(planoId);
        parcel.writeValue(reservaId);  // Cambio aquí para manejar el ID como Long
        parcel.writeString(numeroHamaca); // Escribir el nuevo campo al parcel
    }

    public void fromJSON(JSONObject json) throws JSONException {
        idHamaca = json.optLong("idHamaca", -1);
        precio = json.optDouble("precio", 0.0);
        reservada = json.optBoolean("reservada", false);
        ocupada = json.optBoolean("ocupada", false);
        planoId = json.optInt("planoId", 0);
        reservaId = json.optLong("reservaId", -1);  // Asume que esto es un número, no un objeto
        numeroHamaca = json.optString("numeroHamaca", ""); // Añadir manejo para el nuevo campo

    }

}