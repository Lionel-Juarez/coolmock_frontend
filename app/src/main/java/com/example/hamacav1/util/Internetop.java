package com.example.hamacav1.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Internetop {

    private static Internetop me=null;
    private Internetop(){

    }
    public static Internetop getInstance(){
        if (me == null) {
            synchronized(Internetop.class){
                if(me==null){
                    me = new Internetop();
                }
            }
        }
        return me;
    }

    public String postText(String urlo, List<Parametro> params){//Usaremos POST para insertar nueva información
        int cont=0;/*Como puede haber muchos errores a la hora de realizar la conexión a Internet, conviene
        intentar establecer la conexión al menos un par de veces*/
        String res=okPostText(urlo, params);
        while((cont<5)&&(res.equals("error.PIPE"))){
            ++cont;
            res=okPostText(urlo,params);
        }
        return res;
    }

    private String okPostText(String urlo, List<Parametro> params){
        try {
            OkHttpClient client = new OkHttpClient();//Creamos el cliente OKHttp
            JSONObject jsonObject=new JSONObject();/*La información será enviada en formato JSON por lo que
            la lista de parámetros que el método tiene como argumento se transforma en objetos JSON*/
            for (Parametro pair : params) {
                jsonObject.put(pair.getLlave(),pair.getValor());
            }
            RequestBody body = RequestBody.create(jsonObject.toString(),
                    MediaType.parse("application/json"));//Creamos un requestBody de tipo JSON
            Request request = new Request.Builder()
                    .url(urlo)
                    .post(body)
                    .build();/*Construimos el requestBody con la url y la información en formato JSON que
                    queremos enviar*/
            Response response = client.newCall(request).execute();//Ejecutamos la conexión con nuestro request
            if (!response.isSuccessful()) {//Si la conexión ha tenido problemas enviamos un error de vuelta
                return "error.OKHttp";
            } else {//En caso contrario enviamos la información devuelta en el cuerpo de la petición
                return response.body().string();
            }
        } catch (IOException e) {//Manejo de otras posibles excepciones
            e.printStackTrace();
            return "error.PIPE";
        } catch (JSONException e) {
            e.printStackTrace();
            return "error.JSONException";
        }
    }

    public String putText(String urlo, List<Parametro> params){//Usaremos PUT para actualizar la información
        int cont=0;
        String res=okPutText(urlo, params);
        while((cont<5)&&(res.equals("error.PIPE"))){
            ++cont;
            res=okPutText(urlo,params);
        }
        return res;
    }

    private String okPutText(String urlo, List<Parametro> params){
        try {
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonObject=new JSONObject();
            for (Parametro pair : params) {
                jsonObject.put(pair.getLlave(),pair.getValor());
            }
            RequestBody body = RequestBody.create(jsonObject.toString(),
                    MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(urlo)
                    .put(body)//En lugar de POST, usamos PUT
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return "error.OKHttp";
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "error.PIPE";
        } catch (JSONException e) {
            e.printStackTrace();
            return "error.JSONException";
        }
    }

    public String getString(String myurl){//Los usaremos para obtener información desde el servidor
        int cont=0;
        String res=okGetString(myurl);
        while((cont<5)&&(res.equals("error.IOException"))){
            ++cont;
            res=okGetString(myurl);
        }
        return res;
    }

    public String okGetString(String myurl){
        try {
            OkHttpClient client = new OkHttpClient();//Creamos el cliente OKHttp
            Request request = new Request.Builder()
                    .url(myurl)
                    .build();//La petición sólo tendrá la url con algunos parámetros
            Response response = client.newCall(request).execute();//Ejecutamos la conexión
            if (!response.isSuccessful()){//Si la conexión tiene fallos devolvemos un error
                return "error.OKHttp";
            }
            else{//Si la conexión es exitosa devolvemos el cuerpo de la respuesta
                return response.body().string();
            }
        }
        catch(IOException e){//Manejo de excepciones
            e.printStackTrace();
            return "error.IOException";
        }
    }

    public String deleteTask(String myurl){//Usaremos delete para eliminar información del servidor
        int cont=0;
        String res=okDeleteTask(myurl);
        while((cont<5)&&(res.equals("error.IOException"))){
            ++cont;
            res=okDeleteTask(myurl);
        }
        return res;
    }

    public String okDeleteTask(String myurl){
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .delete()
                    .url(myurl)
                    .build();/*En la petición indicamos que la operación que se va a ejecutar es un delete.
                    No habrá cuerpo en la petición, únicamente la url con algunos parámetros*/
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()){//Manejo de la respuesta
                return "error.OKHttp";
            }
            else{
                return response.body().string();
            }
        }
        catch(IOException e){//Manejo de excepciones
            e.printStackTrace();
            return "error.IOException";
        }
    }
}
