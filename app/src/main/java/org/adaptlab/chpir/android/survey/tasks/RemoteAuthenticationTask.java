package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RemoteAuthenticationTask extends AsyncTask<String, Void, String> {
    private final String TAG = "RemoteAuthTask";

    private AsyncTaskListener mListener;

    public void setListener(AsyncTaskListener listener) {
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String uri = params[0];
        String userName = params[1];
        String password = params[2];

        final String urlString;
        if (uri.contains("device_users/")) {
            urlString = uri;
        } else {
            urlString = uri + "device_users/";
        }
        JSONObject json = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", userName);
            jsonObject.put("password", password);
            json.put("device_user", jsonObject);
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }

        HttpURLConnection urlConnection = null;
        String apiKey = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(30000);
            urlConnection.setDoOutput(true);

            byte[] outputInBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(outputInBytes);
            outputStream.close();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                InputStream in = urlConnection.getInputStream();

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0) {
                    output.write(buffer, 0, bytesRead);
                }
                output.close();
                String jsonString = new String(output.toByteArray());
                JSONObject jsonObject = new JSONObject(jsonString);
                apiKey = jsonObject.optString("access_token", null);
            } else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                apiKey = HttpURLConnection.HTTP_UNAUTHORIZED + "";
            }

        } catch (IOException | JSONException e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Exception: " + e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return apiKey;
    }

    @Override
    protected void onPostExecute(String param) {
        super.onPostExecute(param);
        mListener.onAsyncTaskFinished(param);
    }

    public interface AsyncTaskListener {
        void onAsyncTaskFinished(String param);
    }

}