package com.example.hamacav1.util;

import org.json.JSONObject;

public class Parametro {

    String llave;
    String valor;

    public Parametro(String creadoPor, JSONObject id) {

    }

    public Parametro(String llave, String valor) {
        this.llave = llave;
        this.valor = valor;
    }

    public String getLlave() {
        return llave;
    }

    public void setLlave(String llave) {
        this.llave = llave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}