package com.example.hamacav1.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Internetop {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private static Internetop me=null;
    private Internetop() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
    public static Internetop getInstance(){
        if (me == null) {
            synchronized(Internetop.class){
                if(me == null){
                    me = new Internetop();
                }
            }
        }
        return me;
    }

    public String postText(String urlo, List<Parametro> params){
        int cont=0;
        String res=okPostText(urlo, params);
        while((cont<5)&&(res.equals("error.PIPE"))){
            ++cont;
            res=okPostText(urlo,params);
        }
        return res;
    }

    private String okPostText(String urlo, List<Parametro> params){
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
                    .post(body)
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

    public String putText(String urlo, List<Parametro> params){
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
                    .put(body)
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

    public String getString(String myurl){
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
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(myurl)
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()){
                return "error.OKHttp";
            }
            else{
                return response.body().string();
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return "error.IOException";
        }
    }

    public String deleteTask(String myurl){
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
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()){
                return "error.OKHttp";
            }
            else{
                return response.body().string();
            }
        }
        catch(IOException e){
            e.printStackTrace();
            return "error.IOException";
        }
    }
}
