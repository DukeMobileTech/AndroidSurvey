package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.daos.SettingsDao;
import org.adaptlab.chpir.android.survey.entities.Settings;

public class GetSettingsTask extends AsyncTask<SettingsDao, Void, Settings> {
    private AsyncTaskListener mListener;

    public void setListener(AsyncTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected Settings doInBackground(SettingsDao... params) {
        return params[0].getInstanceSync();
    }

    @Override
    protected void onPostExecute(Settings settings) {
        super.onPostExecute(settings);
        if (settings != null) {
            mListener.onAsyncTaskFinished(settings);
        }
    }

    public interface AsyncTaskListener {
        void onAsyncTaskFinished(Settings settings);
    }
}
