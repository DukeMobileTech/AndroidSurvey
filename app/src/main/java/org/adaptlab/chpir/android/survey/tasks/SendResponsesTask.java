package org.adaptlab.chpir.android.survey.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.NetworkNotificationUtils;

public class SendResponsesTask extends AsyncTask<Void, Void, Void> {
    
    private Context mContext;
    
    public SendResponsesTask (Context context){
        mContext = context;
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        if (NetworkNotificationUtils.checkForNetworkErrors(mContext)) {
            ActiveRecordCloudSync.syncSendTables(mContext);
        }
        
        return null;
    }      
}
