package org.adaptlab.chpir.android.survey.daos.relations;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Response;

import java.util.List;

@Dao
public abstract class DisplayResponseDao {
    @Query("SELECT Responses.* FROM Responses INNER JOIN Questions ON Questions.RemoteId=Responses.QuestionRemoteId AND Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId WHERE Responses.SurveyUUID=:surveyUUID")
    public abstract LiveData<List<Response>> displayResponses(String surveyUUID, long instrumentId, long displayId);

}
