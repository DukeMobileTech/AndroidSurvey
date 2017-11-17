package org.adaptlab.chpir.android.activerecordcloudsync;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFetchr {
    private static final String TAG = "HttpFetchr";
    private Class<? extends ReceiveModel> mReceiveTableClass;
    private String mRemoteTableName;

    public HttpFetchr(String remoteTableName, Class<? extends ReceiveModel> receiveTableClass) {
        mReceiveTableClass = receiveTableClass;
        mRemoteTableName = remoteTableName;
    }

    public void fetch() {
        if (TextUtils.isEmpty(ActiveRecordCloudSync.getEndPoint())) {
            if (BuildConfig.DEBUG) Log.i(TAG, "ActiveRecordCloudSync end point is not set!");
            return;
        }

        try {
            String url;
            if (mRemoteTableName.equals("projects")) {
                url = ActiveRecordCloudSync.getProjectsEndPoint() + ActiveRecordCloudSync.getParams();
            } else {
                url = ActiveRecordCloudSync.getEndPoint() + mRemoteTableName + ActiveRecordCloudSync.getParams();
            }
            String jsonString = getUrl(url);
            JSONArray jsonArray = new JSONArray(jsonString);

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    ReceiveModel tableInstance = mReceiveTableClass.newInstance();
                    tableInstance.createObjectFromJSON(jsonArray.getJSONObject(i));
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }

        } catch (ConnectException cre) {
            Log.e(TAG, "Connection was refused", cre);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse items", je);
        } catch (InstantiationException ie) {
            Log.e(TAG, "Failed to instantiate receive table", ie);
        } catch (IllegalAccessException iae) {
            Log.e(TAG, "Failed to access receive table", iae);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Url is null", npe);
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

}
