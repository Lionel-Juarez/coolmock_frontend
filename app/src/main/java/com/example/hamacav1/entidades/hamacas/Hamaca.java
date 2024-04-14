package com.example.hamacav1.entidades.hamacas;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

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

    // Parcelable interface methods and creator
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

    public void fromJSON(JSONObject json) throws JSONException {
        idHamaca = json.optLong("idHamaca", -1);
        precio = json.optDouble("precio", 0.0);
        reservada = json.optBoolean("reservada", false);
        ocupada = json.optBoolean("ocupada", false);
        planoId = json.optInt("planoId", 0);
    }
}
