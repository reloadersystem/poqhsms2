package mithsolutions.pe.poqhsms2.view.activities;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mithsolutions.pe.poqhsms2.R;
import mithsolutions.pe.poqhsms2.utils.AppNetwork;
import mithsolutions.pe.poqhsms2.utils.AppWiFiNetwork;
import mithsolutions.pe.poqhsms2.utils.Callback;
import mithsolutions.pe.poqhsms2.utils.Http;
import mithsolutions.pe.poqhsms2.utils.HttpRequest;

public class Test extends Activity {
    private static final String SMS_SENT_ACTION = "com.mycompany.myapp.SMS_SENT";
    private static final String SMS_DELIVERED_ACTION = "com.mycompany.myapp.SMS_DELIVERED";
    private static final String EXTRA_NUMBER = "number";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_ID = "sms_id";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private List<String> numberList = new ArrayList<String>();
    private List<String> messageList = new ArrayList<String>();
    private List<Integer> messageIdList = new ArrayList<Integer>();

    private Button btnBucle;
    private SmsManager smsManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver resultsReceiver;

    private static final int WAIT = 60000 * 5;
    private static final int WAIT_FOR_RECONNECT = 10000 * 1;
    private Context context;
    EditText txtLogger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        txtLogger = (EditText)findViewById(R.id.txtLogger);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            }
        }
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        btnBucle = findViewById(R.id.btnBucle);
        btnBucle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
        smsManager = SmsManager.getDefault();
        resultsReceiver = new SmsResultReceiver();

        intentFilter = new IntentFilter(SMS_SENT_ACTION);
        intentFilter.addAction(SMS_DELIVERED_ACTION);

        boolean repetir = false;
        do{
            Toast.makeText(context, "RECONECTANDO ", Toast.LENGTH_SHORT).show();
            repetir = AppNetwork.isConnected();//AppNetwork.checkInternetConnection(context);//AppNetwork.haveNetworkConnection(this);
            if(repetir){
                getData();
            }else{
                try {
                    boolean seReconecto = setConnectionToInternet();
                    Thread.sleep(WAIT_FOR_RECONNECT);
//                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }while(!repetir);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resultsReceiver != null) {
            registerReceiver(resultsReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (resultsReceiver != null) {
            unregisterReceiver(resultsReceiver);
        }
    }

    private boolean setConnectionToInternet(){
        return AppWiFiNetwork.connectToNetworkWPA("TABLET_SISTEMAS","S1st3m4$2019",context);
    }

    public void toastOnThread(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(Test.this, toast, Toast.LENGTH_SHORT).show());
    }
    public void runOnThread(Callback.Void accion)
    {
            runOnUiThread(() -> {
                try {
                    accion.call(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
    //No se llega a pintar tod porque nunca llega a terminar el flujo por eso que no llega a aparecer el renderizado de la pantalla. se recomienda buscar un flujo donde si lo permita
    public void getData() {
        try {
            final String DOMAIN = getString(R.string.domain);
            JSONObject httpParams = new JSONObject();
            httpParams.put("url", DOMAIN + getString(R.string.smspendiente));
            httpParams.put("requestMethod", Http.GET);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject respuesta = new JSONObject().put("status",false);
                        boolean isConnected = false;
                        do {
                            toastOnThread("Empezando el getData() -->");
                            isConnected = AppNetwork.isConnected();//AppNetwork.checkInternetConnection(context);//AppNetwork.haveNetworkConnection(this);
                            if(isConnected){
                                //colocar bucle y repetir do while
                                List<String> xnumberList = new ArrayList<String>();
                                List<String> xmessageList = new ArrayList<String>();
                                List<Integer> xmessageIdList = new ArrayList<Integer>();
                                respuesta = HttpRequest.getResponse(httpParams);
                                if (respuesta.getBoolean("status")) {
                                    JSONArray data = respuesta.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject obj = data.getJSONObject(i);
                                        xnumberList.add(obj.getString("sms_destinatario"));
                                        xmessageList.add(obj.getString("sms_mensaje"));
                                        xmessageIdList.add(obj.getInt("sms_id"));
                                    }
                                }
                                messageList = xmessageList;
                                numberList = xnumberList;
                                messageIdList = xmessageIdList;
                                if(!respuesta.getBoolean("status")){
                                    runOnThread(new Callback.Void(){
                                        @Override
                                        public void call(Object o) throws Exception {
                                            txtLogger.setText("SIN DATOS.", TextView.BufferType.EDITABLE);
                                        }
                                    });
                                    toastOnThread("SIN DATOS -->");
                                    Thread.sleep(WAIT);
                                    toastOnThread("REINICIAR -->");
                                }
                            }else{
                                try {
                                    boolean seReconecto = setConnectionToInternet();
                                    toastOnThread("Se está reconectando -->");
                                    Thread.sleep(WAIT_FOR_RECONNECT);
//                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                } catch (InterruptedException e) {
                                    toastOnThread("Se está reconectando catch -->"+e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                        } while (!isConnected || !respuesta.getBoolean("status"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
//                        Toast.makeText(context, " ERROR AL OBTENER LOS DATOS -> " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        getData();
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                toastOnThread("ERROR AL ESPERAR EL THREAD  -->");
                Toast.makeText(context, " ERROR AL ESPERAR EL THREAD -> " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            toastOnThread("Enviar primer mensaje   -->");
            sendNextMessage();

        } catch (Exception ex) {
            ex.printStackTrace();
            toastOnThread("ERROR AL OBTENER LOS DATOS    -->");
        }

    }

    private void sendNextMessage() {
        // We're going to remove numbers and messages from
        // the lists as we send, so if the lists are empty, we're done.
        if (numberList.size() == 0) {

            try {
                Thread.sleep(WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
                toastOnThread("AL ESPERAR PARA EJECUTAR EL SIGUIENTE HILO -->");
            }
//            getData();
            toastOnThread("REINICIAR getData()  -->");
            btnBucle.performClick();
            return;
        }

        // The list size is a sufficiently unique request code,
        // for the PendingIntent since it decrements for each send.
        int requestCode = numberList.size();

        String number = numberList.get(0);
        String message = messageList.get(0);
        String sms_id = messageIdList.get(0) + "";

        // The Intents must be implicit for this example,
        // as we're registering our Receiver dynamically.
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);

        // We attach the recipient's number and message to
        // the Intents for easy retrieval in the Receiver.
        sentIntent.putExtra(EXTRA_NUMBER, number);
        sentIntent.putExtra(EXTRA_MESSAGE, message);
        sentIntent.putExtra(EXTRA_ID, sms_id);

        deliveredIntent.putExtra(EXTRA_NUMBER, number);
        deliveredIntent.putExtra(EXTRA_MESSAGE, message);
        deliveredIntent.putExtra(EXTRA_ID, sms_id);

        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<String> mSMSMessage = smsManager.divideMessage(message);
        // Construct the PendingIntents for the results.
        // FLAG_ONE_SHOT cancels the PendingIntent after use so we
        // can safely reuse the request codes in subsequent runs.
        PendingIntent sentPI = PendingIntent.getBroadcast(this,
                requestCode,
                sentIntent,
                PendingIntent.FLAG_ONE_SHOT);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this,
                requestCode,
                deliveredIntent,
                PendingIntent.FLAG_ONE_SHOT);
        for (int j = 0; j < mSMSMessage.size(); j++) {
            sentPendingIntents.add(j, sentPI);

            deliveredPendingIntents.add(j, deliveredPI);
        }
        // Send our message.
//            smsManager.sendTextMessage(number, null, message, sentPI, deliveredPI);
        smsManager.sendMultipartTextMessage(number, null, mSMSMessage,
                sentPendingIntents, deliveredPendingIntents);
        // Remove the number and message we just sent to from the lists.
        numberList.remove(0);
        messageList.remove(0);
        messageIdList.remove(0);
    }

private class SmsResultReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // A simple result Toast text.
        String result = null;

        // Get the result action.
        String action = intent.getAction();

        // Retrieve the recipient's number and message.
        String number = intent.getStringExtra(EXTRA_NUMBER);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String sms_id = intent.getStringExtra(EXTRA_ID);
        // This is the result for a send.
        if (SMS_SENT_ACTION.equals(action)) {
            int resultCode = getResultCode();
            try {
                JSONObject body = new JSONObject();
                body.put(EXTRA_NUMBER, number);
                body.put(EXTRA_MESSAGE, message);
                body.put(EXTRA_ID, sms_id);
                body.put("resultCode", resultCode);
                result = "Send result : " + translateSentResult(body);
                Toast.makeText(context, number+" - "+message, Toast.LENGTH_SHORT).show();
                // The current send is complete. Send the next one.
                sendNextMessage();
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, " BROADCAST DE ENVIO -> " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        // This is the result for a delivery.
        else if (SMS_DELIVERED_ACTION.equals(action)) {
            SmsMessage sms = null;

            // A delivery result comes from the service
            // center as a simple SMS in a single PDU.
            byte[] pdu = intent.getByteArrayExtra("pdu");
            String format = intent.getStringExtra("format");

            // Construct the SmsMessage from the PDU.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && format != null) {
//                        sms = SmsMessage.createFromPdu(pdu, format);
                sms = SmsMessage.createFromPdu(pdu);
            } else {
                sms = SmsMessage.createFromPdu(pdu);
            }

            // getResultCode() is not reliable for delivery results.
            // We need to get the status from the SmsMessage.
            result = "Delivery result : " + translateDeliveryStatus(sms.getStatus());
        }

        result = number + ", " + message + "\n" + result;
        toastOnThread(result);
    }

    String translateSentResult(JSONObject params) {
        String mensaje = null;
        System.out.println(params);
        try {
            switch (params.getInt("resultCode")) {
                case Activity.RESULT_OK:
//                            mensaje = "Activity.RESULT_OK";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    mensaje = "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    mensaje = "SmsManager.RESULT_ERROR_RADIO_OFF";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    mensaje = "SmsManager.RESULT_ERROR_NULL_PDU";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    mensaje = "SmsManager.RESULT_ERROR_NO_SERVICE";
                    break;
                default:
                    mensaje = "Unknown error code";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            toastOnThread(" NO OBTUVO EL CÓDIGO DE RESULTADO -> " + ex.getMessage());
        }
        final String error = mensaje;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject respuesta =  new JSONObject().put("status", false);
                    boolean isConnected = false;
                    do {
                        isConnected = AppNetwork.isConnected();//AppNetwork.checkInternetConnection(context);//AppNetwork.haveNetworkConnection(this);
                        if (isConnected) {
                            try {
                                final String DOMAIN = getString(R.string.domain);
                                JSONObject body = new JSONObject();
                                JSONObject requestParams = new JSONObject();

                                requestParams.put(EXTRA_ID, params.getString(EXTRA_ID));
                                if (error != null) {
                                    requestParams.put("error", error);
                                }
                                body.put("url", DOMAIN + getString(R.string.smsactualizar));
                                body.put("requestParams", requestParams);
                                HttpRequest.getResponse(body);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                toastOnThread(" HILO QUE ACTUALIZA -> " + ex.getMessage());
                            }
                        } else {
                            try {
                                boolean seReconecto = setConnectionToInternet();
                                toastOnThread("Se está reconectando --> ");
                                Thread.sleep(WAIT_FOR_RECONNECT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                toastOnThread("FAIL EN RECONECTAR  --> "+e.getMessage());
                            }
                        }
                    } while (!isConnected ) ;
                }catch (Exception ex) {
                    ex.printStackTrace();
                    toastOnThread("--> "+ex.getMessage());
//                    Toast.makeText(context, " --> " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            toastOnThread("HILO QUE ACTUALIZA NO ESPERA QUE TERMINE SU EJECUCIÓN -> "+e.getMessage());
        }
        return mensaje;
    }

    String translateDeliveryStatus(int status) {
        switch (status) {
            case Telephony.Sms.STATUS_COMPLETE:
                return "Sms.STATUS_COMPLETE";
            case Telephony.Sms.STATUS_FAILED:
                return "Sms.STATUS_FAILED";
            case Telephony.Sms.STATUS_PENDING:
                return "Sms.STATUS_PENDING";
            case Telephony.Sms.STATUS_NONE:
                return "Sms.STATUS_NONE";
            default:
                return "Unknown status code";
        }
    }
}
}
