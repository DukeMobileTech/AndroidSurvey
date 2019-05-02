package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Entity;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class SectionRepository extends Repository {
    private SectionDao mSectionDao;
    private SectionTranslationDao mSectionTranslationDao;

    public SectionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSectionDao = db.sectionDao();
        mSectionTranslationDao = db.sectionTranslationDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "sections";
    }

    @Override
    public BaseDao getDao() {
        return mSectionDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return mSectionTranslationDao;
    }

    @Override
    public Entity getEntity() {
        return new Section();
    }

    @Override
    public Entity getTranslationEntity() {
        return new SectionTranslation();
    }
}
