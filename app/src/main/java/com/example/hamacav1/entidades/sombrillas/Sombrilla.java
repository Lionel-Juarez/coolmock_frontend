package com.example.hamacav1.entidades.sombrillas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sombrilla implements Parcelable {

    private long idSombrilla;
    private double precio;
    private boolean ocupada;
    private String numeroSombrilla;
    private List<Long> reservaIds;
    private String cantidadHamacas;


    protected Sombrilla(Parcel in) {
        idSombrilla = in.readLong();
        precio = in.readDouble();
        ocupada = in.readByte() != 0;
        numeroSombrilla = in.readString();
        cantidadHamacas = in.readString();
        reservaIds = new ArrayList<>();
        in.readList(reservaIds, Long.class.getClassLoader());
    }

    public static final Creator<Sombrilla> CREATOR = new Creator<Sombrilla>() {
        @Override
        public Sombrilla createFromParcel(Parcel in) {
            return new Sombrilla(in);
        }

        @Override
        public Sombrilla[] newArray(int size) {
            return new Sombrilla[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(idSombrilla);
        parcel.writeDouble(precio);
        parcel.writeByte((byte) (ocupada ? 1 : 0));
        parcel.writeString(numeroSombrilla);
        parcel.writeString(cantidadHamacas);
        parcel.writeList(reservaIds);
    }

    public static Sombrilla fromJSON(JSONObject jsonObject) throws JSONException {
        Sombrilla sombrilla = new Sombrilla();
        sombrilla.setIdSombrilla(jsonObject.optLong("idSombrilla"));
        sombrilla.setNumeroSombrilla(jsonObject.optString("numeroSombrilla"));
        sombrilla.setCantidadHamacas(jsonObject.optString("cantidadHamacas"));
        sombrilla.setPrecio(jsonObject.optDouble("precio"));
        sombrilla.setOcupada(jsonObject.optBoolean("ocupada"));

        JSONArray reservaArray = jsonObject.optJSONArray("reservaIds");
        if (reservaArray != null) {
            List<Long> reservaIds = new ArrayList<>();
            for (int i = 0; i < reservaArray.length(); i++) {
                try {
                    reservaIds.add(reservaArray.getLong(i));
                } catch (JSONException e) {
                    Log.e("Sombrilla", "Error al leer el ID de reserva en la posición " + i + ": " + e.getMessage());
                }
            }
            sombrilla.setReservaIds(reservaIds);
        }

        return sombrilla;
    }

    public boolean isReservada() {
        return reservaIds != null && !reservaIds.isEmpty();
    }

    public Long getReservaId() {
        if (!reservaIds.isEmpty()) {
            return reservaIds.get(0);
        } else {
            return null;
        }
    }

    public void setReservada(boolean reservada) {
        if (reservada) {
            ocupada = false;
            if (reservaIds == null) {
                reservaIds = new ArrayList<>();
            }
            reservaIds.add(0L);
        } else {
            if (reservaIds != null) {
                reservaIds.clear();
            }
        }
    }
}