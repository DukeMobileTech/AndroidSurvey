package org.adaptlab.chpir.android.activerecordcloudsync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;

public class NotificationUtils {
    private static final String TAG = "NetworkNotificationUtil";
    private static final String CHANNEL = "NOTIFICATION_CHANNEL";

    /*
     * Place a notification in the notification tray
     */
    public static void showNotification(Context context, int iconId, int titleId, String message,
                                        int defaults, int priority, int importance) {
        if (context == null) return;
        int CHANNEL_ID = 100;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(iconId)
                .setContentTitle(context.getString(titleId))
                .setContentText(message)
                .setDefaults(defaults)
                .setPriority(priority);

        NotificationManager notificationManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = context.getSystemService(NotificationManager.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL, CHANNEL, importance);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(CHANNEL_ID, builder.build());
        } else {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(CHANNEL_ID, builder.build());
        }
    }

    /*
     * Check for various network errors and display error message in notification tray.
     *
     * Return false if network errors, return true if okay to proceed.
     *
     * This WILL throw an exception if executed on UI thread since it pings a URL.
     */
    public static boolean checkForNetworkErrors(Context context) {
        if (!isNetworkAvailable(context.getApplicationContext())) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Network is not available");
            showNotification(context, android.R.drawable.ic_dialog_alert, R.string.app_name,
                    context.getString(R.string.network_unavailable), Notification.BADGE_ICON_SMALL,
                    NotificationCompat.PRIORITY_DEFAULT, NotificationManager.IMPORTANCE_DEFAULT);
        } else if (!ActiveRecordCloudSync.isApiAvailable()) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Api endpoint is not available");
            showNotification(context, android.R.drawable.ic_dialog_alert, R.string.app_name,
                    context.getString(R.string.api_unavailable), Notification.BADGE_ICON_SMALL,
                    NotificationCompat.PRIORITY_DEFAULT, NotificationManager.IMPORTANCE_DEFAULT);
        } else if (!ActiveRecordCloudSync.isVersionAcceptable()) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Android version code is not acceptable");
            showNotification(context, android.R.drawable.ic_dialog_alert, R.string.app_name,
                    context.getString(R.string.unacceptable_version_code), Notification.BADGE_ICON_SMALL,
                    NotificationCompat.PRIORITY_DEFAULT, NotificationManager.IMPORTANCE_DEFAULT);
        } else {
            return true;
        }

        return false;
    }

    /*
     * Check if Network is available on device.
     */
    @SuppressWarnings("deprecation")
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
    }
}