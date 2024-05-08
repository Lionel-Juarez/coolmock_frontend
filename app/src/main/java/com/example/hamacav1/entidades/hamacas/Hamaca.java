package com.example.hamacav1.entidades.hamacas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.hamacav1.entidades.reservas.Reserva;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hamaca implements Parcelable {

    private long idHamaca;
    private double precio;
    private boolean ocupada;
    private int planoId;
    private String numeroHamaca;
    private List<Long> reservaIds;
    private String lado;


    protected Hamaca(Parcel in) {
        idHamaca = in.readLong();
        precio = in.readDouble();
        ocupada = in.readByte() != 0;
        planoId = in.readInt();
        numeroHamaca = in.readString();
        lado = in.readString();
        reservaIds = new ArrayList<>();
        in.readList(reservaIds, Long.class.getClassLoader());
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
        parcel.writeByte((byte) (ocupada ? 1 : 0));
        parcel.writeInt(planoId);
        parcel.writeString(numeroHamaca);
        parcel.writeString(lado);
        parcel.writeList(reservaIds);
    }

    public static Hamaca fromJSON(JSONObject jsonObject) throws JSONException {
        Hamaca hamaca = new Hamaca();
        hamaca.setIdHamaca(jsonObject.optLong("idHamaca"));
        hamaca.setNumeroHamaca(jsonObject.optString("numeroHamaca"));
        hamaca.setPrecio(jsonObject.optDouble("precio"));
        hamaca.setOcupada(jsonObject.optBoolean("ocupada"));
        hamaca.setPlanoId(jsonObject.optInt("planoId"));
        hamaca.setLado(jsonObject.optString("lado"));

        JSONArray reservaArray = jsonObject.optJSONArray("reservaIds");
        if (reservaArray != null) {
            List<Long> reservaIds = new ArrayList<>();
            for (int i = 0; i < reservaArray.length(); i++) {
                try {
                    reservaIds.add(reservaArray.getLong(i));
                } catch (JSONException e) {
                    Log.e("Hamaca", "Error al leer el ID de reserva en la posición " + i + ": " + e.getMessage());
                }
            }
            hamaca.setReservaIds(reservaIds);
        }

        return hamaca;
    }

    // Método para determinar si la hamaca está reservada
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