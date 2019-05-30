package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.relations.ResponseRelation;

import java.util.List;

@Dao
public abstract class ResponseDao extends BaseDao<Response> {
    @Query("SELECT * FROM Responses WHERE UUID=:uuid LIMIT 1")
    public abstract LiveData<Response> findByUUID(String uuid);

    @Transaction
    @Query("SELECT Responses.* FROM Responses " +
            "INNER JOIN Questions ON Questions.QuestionIdentifier=Responses.QuestionIdentifier AND " +
            "Questions.InstrumentRemoteId=:instrumentId AND Questions.DisplayId=:displayId " +
            "WHERE Responses.SurveyUUID=:uuid")
    public abstract LiveData<List<ResponseRelation>> displayResponses(Long instrumentId, Long displayId, String uuid);

}
