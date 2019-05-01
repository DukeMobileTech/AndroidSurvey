package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.OptionDao;
import org.adaptlab.chpir.android.survey.daos.OptionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class OptionRepository implements Downloadable {
    private OptionDao mOptionDao;
    private OptionTranslationDao mOptionTranslationDao;

    public OptionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mOptionDao = db.optionDao();
        mOptionTranslationDao = db.optionTranslationDao();
    }

    @Override
    public void download() {
        new TranslatableEntityDownloadTask(mOptionDao, mOptionTranslationDao, getRemoteTableName(),
                Option.class, OptionTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "options";
    }

}
