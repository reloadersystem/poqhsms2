package mithsolutions.pe.poqhsms2.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONObject;

import mithsolutions.pe.poqhsms2.R;

/**
 * Created by MITH on 20/10/2019.
 */

public class SmsSentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context,
                            "SMS Sent " + intent.getStringExtra("sms_id"),
                            Toast.LENGTH_SHORT).show();
                    JSONObject params = new JSONObject(intent.getAction());
                    JSONObject obj = new JSONObject();
                    JSONObject httpParams = new JSONObject();
                    httpParams.put("url", params.getString("url"));
                    obj.put("sms_id", params.getString("sms_id"));
                    httpParams.put("requestParams", obj);
                    HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                        @Override
                        public void call(JSONObject respuesta) throws Exception {
                            System.out.println(respuesta);
                        }
                    });
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
