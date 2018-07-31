package com.example.tomee.simpleauth;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendYoHttp extends AsyncTask<String, Void, Boolean> {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String LEGACY_SERVER_KEY = "AIzaSyC6HNkvwEvY-WcAx-_mmRbOl51L-qPJlIE";
    private static final String stringUrl = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String targetToken = params[0];
        String selfName = params[1];
        String selfUid = params[2];
        OutputStream out = null;

        try {
            OkHttpClient client = new OkHttpClient();

            JSONObject json=new JSONObject();
            JSONObject dataJson=new JSONObject();
            dataJson.put("senderName", selfName);
            dataJson.put("senderUid", selfUid);
            json.put("to", targetToken);
            json.put("data", dataJson);
            RequestBody body = RequestBody.create(JSON, json.toString());

            Request request = new Request.Builder()
                    .header("Authorization","key="+LEGACY_SERVER_KEY)
                    .url(stringUrl)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String finalResponse = response.body().string();
            System.out.println(finalResponse);

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
