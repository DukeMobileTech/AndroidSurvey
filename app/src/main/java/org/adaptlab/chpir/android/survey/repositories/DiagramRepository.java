package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.DiagramDao;
import org.adaptlab.chpir.android.survey.entities.Diagram;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class DiagramRepository extends Repository {
    private final DiagramDao mDiagramDao;

    public DiagramRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDiagramDao = db.diagramDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "diagrams";
    }

    @Override
    public BaseDao getDao() {
        return mDiagramDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return null;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Diagram();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return null;
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Diagram.class, new SurveyEntityDeserializer<>(Diagram.class));
        return gsonBuilder.create();
    }
}
