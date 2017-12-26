package com.truongquocvi.vinaphone;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Truong Quoc Vi on 12/25/2017.
 */

public class API implements Runnable {

    public static String url = "", resp = "";
    public static HashMap<String, String> data = new HashMap<>();
    private static OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url, cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url);
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    }).build();

    @Override
    public void run() {
        Request.Builder builder = new Request.Builder().url(url).header("User-Agent", "MyVinaPhone/2.6.4 (iPhone; iOS 10.2; Scale/2.00");
        FormBody.Builder postData = new FormBody.Builder();
        if (data != null) {
            for (HashMap.Entry<String, String> part : data.entrySet()) {
                postData.add(part.getKey(), part.getValue());
            }
        }
        builder.post(postData.build());
        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API", Log.getStackTraceString(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.e("API", body);
            }
        });
    }

    public static void post() {
        Thread t1 = new Thread(new API());
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
        }

    }
}

