package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;

import org.adaptlab.chpir.android.survey.daos.DeviceUserDao;
import org.adaptlab.chpir.android.survey.entities.DeviceUser;

public class GetDeviceUserTask extends AsyncTask<DeviceUserDao, Void, DeviceUser> {
    private final String mUserName;
    private AsyncTaskListener mListener;

    public GetDeviceUserTask(String userName) {
        mUserName = userName;
    }

    public void setListener(AsyncTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected DeviceUser doInBackground(DeviceUserDao... params) {
        return params[0].findByUserName(mUserName);
    }

    @Override
    protected void onPostExecute(DeviceUser deviceUser) {
        super.onPostExecute(deviceUser);
        mListener.onAsyncTaskFinished(deviceUser);
    }

    public interface AsyncTaskListener {
        void onAsyncTaskFinished(DeviceUser deviceUser);
    }
}
