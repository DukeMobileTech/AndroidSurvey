package org.adaptlab.chpir.android.activerecordcloudsync;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.models.DeviceSyncEntry;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActiveRecordCloudSync {
    private static final String TAG = "ActiveRecordCloudSync";
    private static Map<String, Class<? extends ReceiveModel>> mReceiveTables =
            new LinkedHashMap<String, Class<? extends ReceiveModel>>();
    private static Map<String, Class<? extends SendModel>> mSendTables =
            new LinkedHashMap<String, Class<? extends SendModel>>();

    private static String mEndPoint;        // The remote API endpoint url
    private static String mAccessToken;     // API Access Key
    private static int mVersionCode;        // App version code from Manifest
    private static String mLastSyncTime;

    /**
     * Add a ReceiveTable.  A ReceiveTable is an active record model class that extends the
     * ReceiveModel abstract class.
     *
     * @param tableName
     * @param receiveTable
     */
    public static void addReceiveTable(String tableName, Class<? extends ReceiveModel> receiveTable) {
        mReceiveTables.put(tableName, receiveTable);
    }

    public static Map<String, Class<? extends ReceiveModel>> getReceiveTables() {
        return mReceiveTables;
    }

    public static void addSendTable(String tableName, Class<? extends SendModel> sendTable) {
        mSendTables.put(tableName, sendTable);
    }

    public static void setEndPoint(String endPoint) {
        if (BuildConfig.DEBUG) Log.i(TAG, "Api End point is: " + endPoint);

        char lastChar = endPoint.charAt(endPoint.length() - 1);
        if (lastChar != '/') endPoint = endPoint + "/";

        mEndPoint = endPoint;
    }

    public static String getEndPoint() {
        return mEndPoint;
    }

    static String getProjectsEndPoint() {
        String domainName = AppUtil.getSettings().getApiUrl();
        if (TextUtils.isEmpty(domainName)) return null;
        char lastChar = domainName.charAt(domainName.length() - 1);
        if (lastChar != '/') domainName = domainName + "/";

        return domainName + "api/" + AppUtil.getSettings().getApiVersion() + "/projects/";
    }

    public static void syncReceiveTables(Context context) {
        downloadNotification(context, android.R.drawable.stat_sys_download, R.string.sync_notification_text);
        Date currentTime = new Date();
        ActiveRecordCloudSync.setLastSyncTime(Long.toString(currentTime.getTime()));
        DeviceSyncEntry deviceSyncEntry = new DeviceSyncEntry();
        for (Map.Entry<String, Class<? extends ReceiveModel>> entry : mReceiveTables.entrySet()) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Syncing " + entry.getValue() + " from remote table " + entry.getKey());
            }
            HttpFetchr httpFetchr = new HttpFetchr(entry.getKey(), entry.getValue());
            httpFetchr.fetch();
        }
        deviceSyncEntry.pushRemote();
        downloadNotification(context, android.R.drawable.stat_sys_download_done, R.string.sync_notification_complete_text);
    }

    public static void downloadNotification(Context context, int title, int text) {
        NotificationUtils.showNotification(context, title,
                R.string.app_name, context.getString(text),
                Notification.BADGE_ICON_SMALL, NotificationCompat.PRIORITY_DEFAULT,
                NotificationManager.IMPORTANCE_DEFAULT);
    }

    public static boolean isApiAvailable() {
        if (getPingAddress() == null) return false;
        int responseCode = ping(getPingAddress(), 10000);
        return responseCode == 426 || (200 <= responseCode && responseCode < 300);
    }

    /*
     * Check to see if this version of the application meets the
     * minimum standard to interact with API.
     */
    public static boolean isVersionAcceptable() {
        int responseCode = ping(getPingAddress(), 10000);
        return responseCode != 426;  // Http Status Code 426 = upgrade required     
    }

    public static void setAccessToken(String token) {
        mAccessToken = token;
    }

    public static String getAccessToken() {
        return mAccessToken;
    }

    public static void setVersionCode(int code) {
        mVersionCode = code;
    }

    /*
     * Version code from AndroidManifest
     */
    public static int getVersionCode() {
        return mVersionCode;
    }

    /*
     * Append to all api calls.
     * Ensure that the access token is valid and the version code is up to date
     * before allowing an update.
     */
    public static String getParams() {
        return "?access_token=" + getAccessToken() + "&version_code=" + getVersionCode() +
                "&last_sync_time=" + AppUtil.getSettings().getLastSyncTime();
    }

    public static String getEndPoint2() {
        return AppUtil.getSettings().getApi2Url();
    }

    public static String getParams2() {
        return "?access_token=" + AppUtil.getSettings().getApi2Key() +
                "&version_code=" + getVersionCode();
    }

    public static String getLastSyncTime() {
        return mLastSyncTime;
    }

    private static void setLastSyncTime(String time) {
        mLastSyncTime = time;
    }

    private static String getPingAddress() {
        return getProjectsEndPoint();
    }

    private static int ping(String url, int timeout) {
        if (url == null) return -1;
        url = url + getParams();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if (BuildConfig.DEBUG)
                Log.i(TAG, "Received response code " + responseCode + " for api endpoint");
            return responseCode;
        } catch (IOException exception) {
            return -1;
        }
    }
}