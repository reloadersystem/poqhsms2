package mithsolutions.pe.poqhsms2.utils;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MITH on 20/10/2019.
 */

public abstract class AsyncThread extends AsyncTask<Object, Void, JSONObject> {
    public abstract JSONObject action();
    public abstract void onReponse(JSONObject result);
    @Override
    protected JSONObject doInBackground(Object... params) {
        return action();
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            onReponse(result);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
