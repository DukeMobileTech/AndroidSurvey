package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.entities.relations.SurveyRelation;

import java.util.List;

@Dao
public abstract class SurveyDao extends BaseDao<Survey> {
    @Transaction
    @Query("SELECT * FROM Surveys WHERE UUID=:uuid LIMIT 1")
    public abstract LiveData<SurveyRelation> findByUUID(String uuid);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId")
    public abstract LiveData<List<SurveyRelation>> projectSurveys(Long projectId);
}
