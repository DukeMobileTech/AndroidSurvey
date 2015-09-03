package org.adaptlab.chpir.android.activerecordcloudsync;

import android.util.Log;

import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.Models.AdminSettings;
import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Tasks.GetReceiveTablesTask;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpFetchr {
    private static final String TAG = "HttpFetchr";
    private Class<? extends ReceiveModel> mReceiveTableClass;
    private String mRemoteTableName;

    public HttpFetchr(String remoteTableName, Class<? extends ReceiveModel> receiveTableClass) {
        mReceiveTableClass = receiveTableClass;
        mRemoteTableName = remoteTableName;
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public void fetch() {
        if (ActiveRecordCloudSync.getEndPoint() == null) {
            Log.i(TAG, "ActiveRecordCloudSync end point is not set!");
            return;
        }

        ActiveRecordCloudSync.setFetchCount(ActiveRecordCloudSync.getFetchCount() + 1);
        try {
            String url = ActiveRecordCloudSync.getEndPoint() + mRemoteTableName + ActiveRecordCloudSync.getParams();
            String jsonString = getUrl(url);
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                ReceiveModel tableInstance = mReceiveTableClass.newInstance();
                tableInstance.createObjectFromJSON(jsonArray.getJSONObject(i));
            }
            recordLastSyncTime();

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

    private void recordLastSyncTime() {
        if (ActiveRecordCloudSync.getFetchCount() == ActiveRecordCloudSync.getReceiveTables().size()) {
            String latestSyncTime = ActiveRecordCloudSync.getLastSyncTime();
            AdminSettings adminSettings = AppUtil.getAdminSettingsInstance();
            String projectId = adminSettings.getProjectId();
            List<Instrument> instruments = Instrument.getAllProjectInstruments(Long.valueOf(projectId));
            for (Instrument instrument : instruments) {
                if (!instrument.loaded()) {
                    latestSyncTime = "";
                }
            }
            adminSettings.setLastSyncTime(latestSyncTime);
            if (latestSyncTime.equals("")) {
                new GetReceiveTablesTask(AppUtil.getContext()).execute();
            }
        }
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
