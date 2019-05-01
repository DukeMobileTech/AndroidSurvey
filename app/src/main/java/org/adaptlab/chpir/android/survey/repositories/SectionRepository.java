package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class SectionRepository implements Downloadable {
    private SectionDao mSectionDao;
    private SectionTranslationDao mSectionTranslationDao;

    public SectionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSectionDao = db.sectionDao();
        mSectionTranslationDao = db.sectionTranslationDao();
    }

    @Override
    public void download() {
        new TranslatableEntityDownloadTask(mSectionDao, mSectionTranslationDao, getRemoteTableName(),
                Section.class, SectionTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "sections";
    }
}
