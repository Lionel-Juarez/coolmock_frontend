package com.example.hamacav1.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

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

    private static Internetop me = null;
    private Context context;

    private Internetop(Context context) {
        this.context = context.getApplicationContext();
    }

    public static Internetop getInstance(Context context) {
        if (me == null) {
            synchronized (Internetop.class) {
                if (me == null) {
                    me = new Internetop(context);
                }
            }
        }
        return me;
    }

    private String okPostText(String urlo, List<Parametro> params) {
        try {
            OkHttpClient client = OkHttpProvider.getInstance(context);
            JSONObject jsonObject = new JSONObject();
            for (Parametro pair : params) {
                jsonObject.put(pair.getLlave(), pair.getValor());
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
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) {
                return false;
            } else {
                NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
                return (actNw != null) && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            }
        } else {
            @SuppressWarnings("deprecation")
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }
    private String okPutText(String urlo, List<Parametro> params) {
        try {
            OkHttpClient client = OkHttpProvider.getInstance(context);
            JSONObject jsonObject = new JSONObject();
            for (Parametro pair : params) {
                jsonObject.put(pair.getLlave(), pair.getValor());
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

    public String getString(String myurl) {
        int cont = 0;
        String res = okGetString(myurl);
        while ((cont < 5) && (res.equals("error.IOException"))) {
            ++cont;
            res = okGetString(myurl);
        }
        return res;
    }

    public String okGetString(String myurl) {
        try {
            OkHttpClient client = OkHttpProvider.getInstance(context);
            Request request = new Request.Builder()
                    .url(myurl)
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return "error.OKHttp";
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "error.IOException";
        }
    }

    public String deleteTask(String myurl) {
        int cont = 0;
        String res = okDeleteTask(myurl);
        while ((cont < 5) && (res.equals("error.IOException"))) {
            ++cont;
            res = okDeleteTask(myurl);
        }
        return res;
    }

    public String okDeleteTask(String myurl) {
        try {
            OkHttpClient client = OkHttpProvider.getInstance(context);
            Request request = new Request.Builder()
                    .delete()
                    .url(myurl)
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return "error.OKHttp";
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "error.IOException";
        }
    }

    public String sendPostRequest(String url, JSONObject json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "error.OKHttp";
            } else {
                return response.body().string();
            }
        }
    }

    public String sendPutRequest(String url, JSONObject json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "error.OKHttp";
            } else {
                return response.body().string();
            }
        }
    }


}
