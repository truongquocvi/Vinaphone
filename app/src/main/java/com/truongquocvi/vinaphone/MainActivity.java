package com.truongquocvi.vinaphone;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class MainActivity extends AppCompatActivity {
    private static MainActivity ins;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private int dataLeft = 0;
    private String resp = "";
    public static int logged = 1;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int logged = 1;
        login();
        setContentView(R.layout.activity_main);
        Button btnCheck = (Button)(findViewById(R.id.btnCheck));
        Button btnRenew = (Button)(findViewById(R.id.btnRenew));
        Button btnExit= (Button)(findViewById(R.id.btnExit));
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS();
            }
        });
        btnRenew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renew();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });
    }

    private void afterLogin() {
        JSONObject obj = null;
        try {
            obj = new JSONObject(resp);
            Log.e("loggin", resp);
            logged = Integer.parseInt(obj.get("Code").toString());
        } catch (Exception e) {
            logged = -1;
        }
        if (logged == -1) {
            update("Dich vu hien khong co san. Vui long thu lai sau");
        } else if (logged == 1) {
            update("Vui long dung sim 3G va khong ket noi VPN, proxy de tu nhan dien so dien thoai");
        } else if (logged == 0) {
            try {
                String json = obj.get("Result").toString();
                json = json.substring(1, json.length() - 1);
                obj = new JSONObject(json);
                update("Dang nhap thanh cong! So dien thoai cua ban la: " + obj.get("ma_tb").toString());
                ins = this;
                start();
            } catch (Exception e) {
                update("Co loi xay ra");
                Log.e("Login", "Fail");
            }
        } else
            update("Co loi xay ra");
        Log.d("Logged", Integer.toString(logged));
    }
    
    static private Handler handler = new Handler();
    Runnable task = new Runnable() {
        public void run() {
            Log.d("SMS", "Gui");
            sendSMS();
            while(true) {
                handler.postDelayed(this, 60*60000);
            }
        }
    };

    public void start() {
        handler.postDelayed(task, 60*60000);
    }

    private void sendSMS()
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("888", null, "data", null, null);
        update("Dang kiem tra dung luong con lai");
    }

    public static MainActivity  getInstace(){
        return ins;
    }

    public void update(String message) {
        String now = sdf.format(Calendar.getInstance().getTime());
        TextView textView = (TextView)(findViewById(R.id.textView));
        textView.setText("- " + now + ": " + message + "\n" + textView.getText().toString());
    }

    public void processSms(int data) {
        if (data < 512)
            renew();
        else
            update("Ban con lai " + String.valueOf(data) + "MB");
        dataLeft = data;
    }

    private void login() {
        post("http://app.my.vinaphone.com.vn/myvnp/acc_check3g", null);
        //post("http://api.truongquocvi.com/json.php", null);
    }

    private void renew() {
        HashMap<String, String> data = new HashMap<>();
        data.put("package_code", "MI_BIG_FB2GBN7D");
        data.put("service", "MI_BIG_FB2GBN7D");
        data.put("type", "3");
        post("http://app.my.vinaphone.com.vn/myvnp/pac_unregister_normal", data);
        post("http://app.my.vinaphone.com.vn/myvnp/pac_register_normal", data);
        update("Da dang ky lai goi");
    }

    private void post(String url, HashMap<String, String> data) {
        Request.Builder builder = new Request.Builder().url(url).header("User-Agent", "MyVinaPhone/2.6.4 (iPhone; iOS 10.2; Scale/2.00").header("content-type", "application/json; charset=utf-8");
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
                if (logged != 0)
                    setResp("-1");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                if (logged != 0)
                    setResp(body);
            }
        });
    }

    private void setResp(String resp) {
        this.resp = resp;
        afterLogin();
    }
}
