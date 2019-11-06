package mithsolutions.pe.poqhsms2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mithsolutions.pe.poqhsms2.utils.Http;
import mithsolutions.pe.poqhsms2.utils.SmsUtil;
//import android.widget.Toast;

//import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private AsyncTask<?, ?, ?> runningTask;
    private Context context;
    private LockScreenReceiver lockScreenReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = this.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.SEND_SMS};

                requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            } else {
                context = this;
                if (runningTask != null) runningTask.cancel(true);
                runningTask = new LongOperation();
                runningTask.execute();
//                lockScreenReceiver = new LockScreenReceiver((LongOperation) runningTask);
//                IntentFilter lockFilter = new IntentFilter();
//                lockFilter.addAction(Intent.ACTION_SCREEN_ON);
//                lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
//                lockFilter.addAction(Intent.ACTION_USER_PRESENT);
//                registerReceiver(lockScreenReceiver, lockFilter);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancel running task(s) to avoid memory leaks
        if (runningTask != null) runningTask.cancel(true);
    }

    private final class LongOperation extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... params) {
            JSONObject salida = new JSONObject();
            try {
                JSONObject body = new JSONObject();
                JSONObject httpParams = new JSONObject();
                httpParams.put("url",getString(R.string.domain)+getString(R.string.smspendiente));
                httpParams.put("requestMethod",Http.GET);

                while(true) {
                    try {
                        JSONObject respuesta = Http.getResponse(httpParams);
                        if(respuesta.getBoolean("status")) {
                            JSONArray data = respuesta.getJSONArray("data");
                            for (int i=0;i<data.length();i++){
                                JSONObject obj = data.getJSONObject(i);
                                obj.getString("sms_destinatario");
                            }

                            body.put("mensaje", "");
                            System.out.println(body.getString("mensaje"));
                            SmsUtil.sendSms(body);
                            System.out.println(respuesta);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    Thread.sleep(10000);
                }
//                salida.put("status",true).put("message","Ejecución completa.");
            } catch (Exception e) {
                try {
                    e.printStackTrace();
                     salida.put("status", false).put("message", e.getMessage());
                }catch (JSONException ex){
                }
            }
            return salida;
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
    public class LockScreenReceiver extends BroadcastReceiver
     {
        private LongOperation runningTask;
        public LockScreenReceiver (LongOperation runningTask){
            this.runningTask = runningTask;
        }
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null && intent.getAction() != null)
            {
                runningTask.execute();
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                {
//                    runningTask.execute();
                    // Screen is on but not unlocked (if any locking mechanism present)
                }
                else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                {
                    // Screen is locked
//                    runningTask.execute();
                }
                else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
                {
//                    runningTask.execute();
                }
            }
        }
    }
}
