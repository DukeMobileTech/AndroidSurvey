package org.adaptlab.chpir.android.survey.repositories;

import com.google.gson.Gson;

import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;

public abstract class Repository {
    public abstract void download();

    public abstract String getRemoteTableName();

    public abstract BaseDao getDao();

    public abstract SurveyEntity getEntity();

    public Gson getGson() {
        return new Gson();
    }

    public BaseDao getTranslationDao() {
        return null;
    }

    public SurveyEntity getTranslationEntity() {
        return null;
    }

}
