package org.adaptlab.chpir.android.activerecordcloudsync;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class NotificationService extends IntentService {
    private static final String TAG = "NotificationService";
    private static final String EXTRA_ID = "notification_id";

    public NotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getIntExtra(EXTRA_ID, 0));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setServiceAlarm(Context context, int id) {
        Intent i = new Intent(context, NotificationService.class);
        i.putExtra(EXTRA_ID, id);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 15000, pi);
    }

}