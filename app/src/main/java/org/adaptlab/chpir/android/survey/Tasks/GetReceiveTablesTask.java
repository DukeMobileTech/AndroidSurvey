package org.adaptlab.chpir.android.survey.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;

public class GetReceiveTablesTask extends AsyncTask<Void, Void, Void> {
    Context mContext;

    public GetReceiveTablesTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
            ActiveRecordCloudSync.syncReceiveTables(mContext);
        }
        return null;
    }

}