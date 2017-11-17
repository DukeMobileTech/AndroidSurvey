package org.adaptlab.chpir.android.activerecordcloudsync;

import android.content.Context;
import android.util.Log;

import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.AppUtil;
import org.apache.commons.codec.CharEncoding;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpPushr {
    private static final String TAG = "HttpPushr";
    private Class<? extends SendModel> mSendTableClass;
    private String mRemoteTableName;
    private Context mContext;
    private static final int TIMEOUT = 10000;

    public HttpPushr(String remoteTableName,
                     Class<? extends SendModel> sendTableClass,
                     Context context) {
        mSendTableClass = sendTableClass;
        mRemoteTableName = remoteTableName;
        mContext = context;
    }

    public void push() {
        if (ActiveRecordCloudSync.getEndPoint() == null) {
            if (AppUtil.DEBUG) Log.i(TAG, "ActiveRecordCloudSync end point is not set!");
            return;
        }

        List<? extends SendModel> allElements = getElements();

        try {
            if (isPersistent()) {
                for (SendModel element : allElements)
                    sendData(element);
            } else {
                sendData(mSendTableClass.newInstance());
            }
        } catch (InstantiationException ie) {
            if (AppUtil.DEBUG) Log.e(TAG, "InstantiationException: " + ie);
        } catch (IllegalAccessException ie) {
            if (AppUtil.DEBUG) Log.e(TAG, "IllegalAccessException: " + ie);
        }
    }

    private List<? extends SendModel> getElements() {
        try {
            return new Select().from(mSendTableClass).orderBy(String.format("%s", mSendTableClass.newInstance().getPrimaryKey()) + " ASC").execute();
        } catch (InstantiationException er) {
            if (AppUtil.DEBUG) Log.e(TAG, "InstantiationException " + er);
        } catch (IllegalAccessException er) {
            if (AppUtil.DEBUG) Log.e(TAG, "IllegalAccessException " + er);
        }
        return new Select().from(mSendTableClass).execute();
    }

    private void sendData(SendModel element) {
        if (!element.isSent() && element.readyToSend()) {
            HttpURLConnection connection = null;
            String endPoint;
            if (element.belongsToRoster() && AppUtil.getAdminSettingsInstance().useEndpoint2()) {
                endPoint = ActiveRecordCloudSync.getEndPoint2() + mRemoteTableName + ActiveRecordCloudSync.getParams2();
            } else {
                endPoint = ActiveRecordCloudSync.getEndPoint() + mRemoteTableName + ActiveRecordCloudSync.getParams();
            }
            try {
                connection = (HttpURLConnection) new URL(endPoint).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                connection.setDoOutput(true);

                byte[] outputInBytes = element.toJSON().toString().getBytes(CharEncoding.UTF_8);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(outputInBytes);
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    if (AppUtil.DEBUG) Log.i(TAG, "Received OK HTTP code for " + element.toJSON());
                    element.setAsSent(mContext);
                } else {
                    if (AppUtil.DEBUG) Log.e(TAG, "Received BAD HTTP code " + responseCode + " for " + element.toJSON());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    private boolean isPersistent() throws InstantiationException, IllegalAccessException {
        SendModel sendModel = mSendTableClass.newInstance();
        return sendModel.isPersistent();
    }
}
