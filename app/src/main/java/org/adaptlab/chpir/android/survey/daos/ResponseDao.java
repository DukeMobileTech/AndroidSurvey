package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Response;

@Dao
public abstract class ResponseDao extends BaseDao<Response> {
    @Query("SELECT * FROM Responses WHERE UUID=:uuid LIMIT 1")
    public abstract LiveData<Response> findByUUID(String uuid);

    @Query("SELECT * FROM Responses WHERE SurveyUUID=:surveyUUID AND QuestionIdentifier=:questionIdentifier LIMIT 1")
    public abstract LiveData<Response> find(String surveyUUID, String questionIdentifier);

}
