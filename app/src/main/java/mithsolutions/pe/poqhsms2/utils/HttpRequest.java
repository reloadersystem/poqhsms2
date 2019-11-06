package mithsolutions.pe.poqhsms2.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by MITH on 19/10/2019.
 */

public final class HttpRequest {
    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    /*Ruta para la solicitud HTTTP , Verbo HTTP (Get,Post,Put,...),Objeto json para enviar al servidor destino*/
        public static void getResponse(JSONObject body, final Callback.Void doOnResponse)throws Exception {
            AsyncThread thread = new AsyncThread() {
                    @Override
                    public JSONObject action() {
                        return getResponse(body);
                    }

                    @Override
                    public void onReponse(JSONObject result) {
                        try {
                            doOnResponse.call(result);
                        }catch (Exception ex){
                            System.out.println(ex);
                        }
                    }
                };
            thread.execute();
        }

        public static JSONObject getResponse(JSONObject body){
            JSONObject salida = null;
            try {
                String respuesta = "";
                String requestMethod = body.has("requestMethod") ? body.getString("requestMethod") : "POST";
                String contentType = body.has("contentType") ? body.getString("contentType") : "application/json; charset=UTF-8";
                String authorization = body.has("authorization") ? body.getString("authorization") : null;
                boolean hasOutPut = requestMethod.equalsIgnoreCase(Http.POST) || requestMethod.equalsIgnoreCase(Http.PUT);
                boolean hasParameters = body.has("requestParams");
                String requestUrl = body.getString("url");
                int connectTimeOut = body.has("connectTimeOut") ? body.getInt("connectTimeOut") : 5000;

                if (!hasOutPut && hasParameters) {
                    requestUrl += "?body=" + body.getJSONObject("requestParams").toString();
                }
                URL url = new URL(requestUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", contentType);
                conn.setConnectTimeout(connectTimeOut);
                conn.setDoOutput(hasOutPut);
                conn.setDoInput(true);
                conn.setRequestMethod(requestMethod);
                conn.setRequestProperty("Authorization", authorization);
                if (hasOutPut && hasParameters) {
                    try (OutputStream os = conn.getOutputStream();) {
                        os.write(body.getJSONObject("requestParams").toString().getBytes("UTF-8"));
                    }
                }
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));) {
                    String linea;
                    //procesamos al salida
                    while ((linea = rd.readLine()) != null) {
                        respuesta += linea;
                    }
                }
                conn.disconnect();
                salida = (respuesta.isEmpty()) ? new JSONObject() : new JSONObject(respuesta);
            } catch (Exception ex) {
                try {
                    salida = new JSONObject().put("status", false).put("message", ex.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return salida;
        }
    }
