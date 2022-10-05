package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.ScoreSchemeDao;
import org.adaptlab.chpir.android.survey.entities.ScoreScheme;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class ScoreSchemeRepository extends Repository {
    private final ScoreSchemeDao mScoreSchemeDao;

    public ScoreSchemeRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mScoreSchemeDao = db.scoreSchemeDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "score_schemes";
    }

    @Override
    public BaseDao getDao() {
        return mScoreSchemeDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new ScoreScheme();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ScoreScheme.class, new SurveyEntityDeserializer<>(ScoreScheme.class));
        return gsonBuilder.create();
    }
}
