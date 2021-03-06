package com.example.ishita.assigntasks.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * This class is to parse the received SMS message and extract the OTP for verification.
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            SmsMessage currentMessage;
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = bundle.getString("format");
                        currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj, format);
                    } else
                        currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    // if the SMS is not from our gateway, ignore the message
                    if (!senderAddress.toLowerCase().contains(Config.SMS_ORIGIN.toLowerCase())) {
                        return;
                    }

                    // verification code from sms
                    String verificationCode = getVerificationCode(message);

                    Intent httpIntent = new Intent(context, HttpService.class);
                    httpIntent.putExtra("otp", verificationCode);
                    context.startService(httpIntent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message The sms message received from the server
     * @return The OTP obtained from the message
     */
    @SuppressWarnings("ConstantConditions")
    private String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(Config.OTP_DELIMITER);

        if (index != -1) {
            int start = index + 2;
            int length = 6;
            code = message.substring(start, start + length);
            return code;
        }

        return code;
    }
}
