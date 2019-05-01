package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.DisplayTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class DisplayRepository implements Downloadable {
    private DisplayDao mDisplayDao;
    private DisplayTranslationDao mDisplayTranslationDao;

    public DisplayRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDisplayDao = db.displayDao();
        mDisplayTranslationDao = db.displayTranslationDao();
    }

    public void download() {
        new TranslatableEntityDownloadTask(mDisplayDao, mDisplayTranslationDao, getRemoteTableName(),
                Display.class, DisplayTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "displays";
    }
}
