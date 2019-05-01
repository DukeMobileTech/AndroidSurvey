package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.OptionSetDao;
import org.adaptlab.chpir.android.survey.daos.OptionSetTranslationDao;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.OptionSetTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class OptionSetRepository implements Downloadable {
    private OptionSetDao mOptionSetDao;
    private OptionSetTranslationDao mOptionSetTranslationDao;

    public OptionSetRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionSetDao = db.optionSetDao();
        mOptionSetTranslationDao = db.optionSetTranslationDao();
    }

    @Override
    public void download() {
        new TranslatableEntityDownloadTask(mOptionSetDao, mOptionSetTranslationDao, getRemoteTableName(),
                OptionSet.class, OptionSetTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "option_sets";
    }

}
