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
    private List<Long> reservaIds;  // Lista de IDs de reservas asociadas


    protected Hamaca(Parcel in) {
        idHamaca = in.readLong();
        precio = in.readDouble();
        ocupada = in.readByte() != 0;
        planoId = in.readInt();
        numeroHamaca = in.readString();
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
        parcel.writeList(reservaIds);
    }

    public static Hamaca fromJSON(JSONObject jsonObject) throws JSONException {
        Hamaca hamaca = new Hamaca();
        hamaca.setIdHamaca(jsonObject.optLong("idHamaca"));
        hamaca.setNumeroHamaca(jsonObject.optString("numeroHamaca"));
        hamaca.setPrecio(jsonObject.optDouble("precio"));
        hamaca.setOcupada(jsonObject.optBoolean("ocupada"));
        hamaca.setPlanoId(jsonObject.optInt("planoId"));

        JSONArray reservaArray = jsonObject.optJSONArray("reservaIds");
        if (reservaArray != null) {
            List<Long> reservaIds = new ArrayList<>();
            for (int i = 0; i < reservaArray.length(); i++) {
                try {
                    // Asegúrate de que cada elemento del array es un objeto JSON antes de intentar convertirlo
                    reservaIds.add(reservaArray.getLong(i)); // Cambiado para manejar directamente valores long en lugar de JSONObject
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
            return reservaIds.get(0); // Devuelve el primer ID de reserva si hay al menos una asociada
        } else {
            return null; // Devuelve null si no hay reservas asociadas
        }
    }

    public void setReservada(boolean reservada) {
        // Lógica para establecer el estado de reservada
        if (reservada) {
            // Si la hamaca está siendo marcada como reservada, se asume que no está ocupada
            ocupada = false;
            // Si no hay ninguna reserva asociada, se crea una lista vacía
            if (reservaIds == null) {
                reservaIds = new ArrayList<>();
            }
            // Se agrega una reserva ficticia a la lista de IDs de reserva
            reservaIds.add(0L); // Se podría utilizar cualquier valor no nulo
        } else {
            // Si la hamaca está siendo marcada como no reservada, se limpia la lista de IDs de reserva
            if (reservaIds != null) {
                reservaIds.clear();
            }
        }
    }
}