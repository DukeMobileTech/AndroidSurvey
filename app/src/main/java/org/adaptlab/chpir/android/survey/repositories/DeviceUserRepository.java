package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DeviceUserDao;
import org.adaptlab.chpir.android.survey.entities.DeviceUser;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class DeviceUserRepository implements Downloadable {
    private DeviceUserDao deviceUserDao;

    public DeviceUserRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        deviceUserDao = db.deviceUserDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(deviceUserDao, getRemoteTableName(), DeviceUser.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "device_users";
    }
}
