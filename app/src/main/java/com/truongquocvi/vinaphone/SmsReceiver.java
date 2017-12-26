package com.truongquocvi.vinaphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Truong Quoc Vi on 12/25/2017.
 */

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";
    public static final String BROADCAST_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private final Handler handler = new Handler();
    private Context context = null;
    private Intent intent = null;
    private int data = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.getInstace().logged == 0) {
            this.context = context;
            this.intent = new Intent(BROADCAST_ACTION);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
                for (int i = 0; i < smsExtra.length; ++i) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                    String address = sms.getOriginatingAddress();
                    if (address.equals("888")) {
                        abortBroadcast();
                        String body = sms.getMessageBody().toString();
                        if (body.matches("(.*?)Goi cuoc Data cua quy khach da het dung luong(.*?)"))
                            data = 0;
                        else {
                            Pattern pattern = Pattern.compile("\\d+ MB");
                            Matcher matcher = pattern.matcher(body);
                            if (matcher.find()) {
                                try {
                                    data = Integer.parseInt(matcher.group().replace(" MB", ""));
                                } catch (Exception e) {
                                    data = 0;
                                }
                            } else {
                                data = 0;
                            }
                        }
                    }
                }
                MainActivity.getInstace().processSms(data);
            }
        }
    }
}
