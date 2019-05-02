package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetOptionDao;
import org.adaptlab.chpir.android.survey.entities.Entity;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class OptionSetOptionRepository extends Repository {
    private OptionSetOptionDao mOptionSetOptionDao;

    public OptionSetOptionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionSetOptionDao = db.optionSetOptionDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "option_in_option_sets";
    }

    @Override
    public BaseDao getDao() {
        return mOptionSetOptionDao;
    }

    @Override
    public Entity getEntity() {
        return new OptionSetOption();
    }

}
