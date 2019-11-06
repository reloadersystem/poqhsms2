package mithsolutions.pe.poqhsms2.utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by MITH on 18/10/2019.
 */

public final class SmsUtil {
    private SmsUtil(){

    }
    //
//                String strMessage = "LoremnIpsum";
//
//                SmsManager sms = SmsManager.getDefault();
//
//                sms.sendTextMessage(strPhone, null, strMessage, null, null);
//
//                Toast.makeText(this, "Sent.", Toast.LENGTH_SHORT).show();
    public static void sendSms(JSONObject body) throws Exception{
        String numeroCelular= body.getString("numeroCelular");

        String mensaje = body.getString("mensaje");

        SmsManager sms = SmsManager.getDefault();

        ArrayList messageParts = sms.divideMessage(mensaje);

        sms.sendMultipartTextMessage(numeroCelular, null, messageParts , null, null);

    }

}
