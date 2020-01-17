package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.SectionDao;
import org.adaptlab.chpir.android.survey.daos.SectionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.SectionTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
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
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
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
    public SurveyEntity getEntity() {
        return new Section();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new SectionTranslation();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Section.class, new SurveyEntityDeserializer<>(Section.class));
        return gsonBuilder.create();
    }
}
