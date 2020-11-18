package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.converters.SurveyEntityDeserializer;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.SubdomainDao;
import org.adaptlab.chpir.android.survey.entities.Subdomain;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class SubdomainRepository extends Repository {
    private SubdomainDao mSubdomainDao;

    public SubdomainRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mSubdomainDao = db.subdomainDao();
    }

    @Override
    public EntityDownloadTask download() {
        EntityDownloadTask task = new EntityDownloadTask(this);
        task.execute();
        return task;
    }

    @Override
    public String getRemoteTableName() {
        return "subdomains";
    }

    @Override
    public BaseDao getDao() {
        return mSubdomainDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Subdomain();
    }

    @Override
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Subdomain.class, new SurveyEntityDeserializer<>(Subdomain.class));
        return gsonBuilder.create();
    }
}
