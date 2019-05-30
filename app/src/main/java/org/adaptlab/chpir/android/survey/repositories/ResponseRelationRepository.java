package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.ResponseDao;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;

import java.util.List;

public class ResponseRelationRepository {
    public final String TAG = this.getClass().getName();
    private LiveData<List<ResponseRelation>> responses;

    public ResponseRelationRepository(Application application, Long instrumentId, Long displayId, final String surveyUUID) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "initialize: " + instrumentId + " " + displayId + " " + surveyUUID);
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        ResponseDao responseDao = db.responseDao();
        responses = responseDao.displayResponses(instrumentId, displayId, surveyUUID);
    }

    public LiveData<List<ResponseRelation>> getResponses() {
        return responses;
    }

}
