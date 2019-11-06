package mithsolutions.pe.poqhsms2.view.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import mithsolutions.pe.poqhsms2.R;
import mithsolutions.pe.poqhsms2.utils.Callback;
import mithsolutions.pe.poqhsms2.utils.Http;
import mithsolutions.pe.poqhsms2.utils.HttpRequest;
import mithsolutions.pe.poqhsms2.utils.Promise;
import mithsolutions.pe.poqhsms2.utils.Promises;
import mithsolutions.pe.poqhsms2.utils.SmsDeliveredReceiver;
import mithsolutions.pe.poqhsms2.utils.SmsSentReceiver;
import mithsolutions.pe.poqhsms2.utils.SmsUtil;

public class Sms extends Activity {
    private AsyncTask<?, ?, ?> runningTask;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        context = this;
        if (runningTask != null) runningTask.cancel(true);
        runningTask = new LongOperation();
        runningTask.execute();
    }

    private void sendSMS(String phoneNumber, String message) {

        try {

            JSONObject body = new JSONObject();
            JSONObject httpParams = new JSONObject();
            httpParams.put("url",getString(R.string.domain)+getString(R.string.smspendiente));
            httpParams.put("requestMethod",Http.GET);

            try {
                JSONObject respuesta = Http.getResponse(httpParams);
                System.out.println(respuesta);
//                if(respuesta.getBoolean("status")) {
//                    JSONArray data = respuesta.getJSONArray("data");
//                    for (int i=0;i<data.length();i++){
//                        JSONObject obj = data.getJSONObject(i);
//
//                        SmsManager sms = SmsManager.getDefault();
//
//                        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
//                        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
//                        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
//                                new Intent(this, SmsSentReceiver.class), 0);
//
//                        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
//                                new Intent(this, SmsDeliveredReceiver.class), 0);
//
//                        ArrayList<String> mSMSMessage = sms.divideMessage(obj.getString("sms_mensaje"));
//
//                        for (int j = 0; j < mSMSMessage.size(); j++) {
//                            sentPendingIntents.add(j, sentPI);
//
//                            deliveredPendingIntents.add(j, deliveredPI);
//                        }
//                        sms.sendMultipartTextMessage(obj.getString("sms_destinatario"), null, mSMSMessage ,
//                                sentPendingIntents, deliveredPendingIntents);
//
//                    }

//                }
            }catch (Exception ex){
                ex.printStackTrace();
            }



        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(this, "SMS sending failed...",
                    Toast.LENGTH_SHORT).show();
        }


    }

    private final class LongOperation extends AsyncTask<Object, Void, JSONObject> {
        public synchronized void doSomeTask(){
                JSONObject salida = new JSONObject();
//            while(true) {
                try {
                    try {
                        final String domain = getString(R.string.domain);
                        JSONObject body = new JSONObject();
                        JSONObject httpParams = new JSONObject();
                        httpParams.put("url", domain + getString(R.string.smspendiente));
                        httpParams.put("requestMethod", Http.GET);
                        String SENT = "SMS_SENT";
                        String DELIVERED = "SMS_DELIVERED";
                        try {

                            JSONObject respuesta = Http.getResponse(httpParams);
                            System.out.println(respuesta);
                            if (respuesta.getBoolean("status")) {
                                JSONArray data = respuesta.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    final JSONObject obj = data.getJSONObject(i);

                                    SmsManager sms = SmsManager.getDefault();

                                    ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
                                    ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();

                                    //                                Intent intentSent = new Intent(context, SmsSentReceiver.class);
                                    //                                intentSent.putExtra("sms_id",obj.getInt("sms_id")+"");
                                    //                                intentSent.putExtra("endpoint",domain+getString(R.string.smsactualizar));
                                    //                                PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                                    //                                        intentSent, 0);
                                    //
                                    //                                Intent intentDelivered = new Intent(context, SmsDeliveredReceiver.class);
                                    //                                intentDelivered.putExtra("sms_id",obj.getInt("sms_id")+"");
                                    //                                PendingIntent deliveredPI =  PendingIntent.getBroadcast(context, 0,intentDelivered, 0);//PendingIntent.getBroadcast(context, 0, intentDelivered,PendingIntent.FLAG_CANCEL_CURRENT);

                                    PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                                            new Intent(obj.getInt("sms_id") + ""), 0);

                                    PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                                            new Intent(DELIVERED), 0);

                                    registerReceiver(new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context arg0, Intent intent) {
                                            try {
                                                JSONObject obj = new JSONObject();
                                                JSONObject httpParams = new JSONObject();
                                                httpParams.put("url", domain + getString(R.string.smsactualizar));
                                                obj.put("sms_id", intent.getAction());
                                                switch (getResultCode()) {
                                                    case Activity.RESULT_OK:
                                                        httpParams.put("requestParams", obj);
                                                        HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                                                            @Override
                                                            public void call(JSONObject respuesta) throws Exception {
                                                                System.out.println(respuesta);
                                                            }
                                                        });
                                                        Toast.makeText(getBaseContext(), "SMS sent " + obj.getInt("sms_id"),
                                                                Toast.LENGTH_SHORT).show();

                                                        break;
                                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                        obj.put("error", "FALLO GENÉRICO.");
                                                        httpParams.put("requestParams", obj);
                                                        HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                                                            @Override
                                                            public void call(JSONObject respuesta) throws Exception {
                                                                System.out.println(respuesta);
                                                            }
                                                        });
                                                        Toast.makeText(getBaseContext(), "Generic failure",
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                        obj.put("error", "EL SERVICIO NO ESTA DISPONIBLE.");
                                                        httpParams.put("requestParams", obj);
                                                        HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                                                            @Override
                                                            public void call(JSONObject respuesta) throws Exception {
                                                                System.out.println(respuesta);
                                                            }
                                                        });
                                                        Toast.makeText(getBaseContext(), "No service",
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case SmsManager.RESULT_ERROR_NULL_PDU:
                                                        obj.put("error", "UNIDAD DE PROTOCOLO DE DATOS NO ENCONTRADA.");
                                                        httpParams.put("requestParams", obj);
                                                        HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                                                            @Override
                                                            public void call(JSONObject respuesta) throws Exception {
                                                                System.out.println(respuesta);
                                                            }
                                                        });
                                                        Toast.makeText(getBaseContext(), "Null PDU",
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                        obj.put("error", "FALLÓ PORQUE LA RADIO SE APAGÓ EXPLICÍTAMENTE.");
                                                        httpParams.put("requestParams", obj);
                                                        HttpRequest.getResponse(httpParams, new Callback.Void<JSONObject>() {
                                                            @Override
                                                            public void call(JSONObject respuesta) throws Exception {
                                                                System.out.println(respuesta);
                                                            }
                                                        });
                                                        Toast.makeText(getBaseContext(), "Radio off",
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                }

                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }, new IntentFilter(obj.getInt("sms_id") + ""));

                                    //---when the SMS has been delivered---
                                    registerReceiver(new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context arg0, Intent arg1) {
                                            try {
                                                switch (getResultCode()) {
                                                    case Activity.RESULT_OK:
                                                        Toast.makeText(getBaseContext(), "SMS delivered " + obj.getInt("sms_id"),
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case Activity.RESULT_CANCELED:
                                                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                                                Toast.LENGTH_SHORT).show();
                                                        break;
                                                }
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }, new IntentFilter(DELIVERED));

                                    ArrayList<String> mSMSMessage = sms.divideMessage(obj.getString("sms_mensaje"));

                                    for (int j = 0; j < mSMSMessage.size(); j++) {
                                        sentPendingIntents.add(j, sentPI);

                                        deliveredPendingIntents.add(j, deliveredPI);
                                    }
                                    sms.sendMultipartTextMessage(obj.getString("sms_destinatario"), null, mSMSMessage,
                                            sentPendingIntents, deliveredPendingIntents);

                                }

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                    } catch (Exception e) {

                        e.printStackTrace();
                        Toast.makeText(context, "SMS sending failed...",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    try {
                        e.printStackTrace();
                        salida.put("status", false).put("message", e.getMessage());
                    } catch (JSONException ex) {
                    }
                }



        }

        @Override
        protected JSONObject doInBackground(Object... params) {
            while(true) {
                doSomeTask();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
            }catch (JSONException ex){
                ex.printStackTrace();
            }
        }

    }
}

