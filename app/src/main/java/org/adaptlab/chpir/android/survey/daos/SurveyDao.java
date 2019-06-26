package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.relations.SurveyRelation;

import java.util.List;

@Dao
public abstract class SurveyDao extends BaseDao<Survey> {
    @Query("SELECT * FROM Surveys WHERE UUID=:uuid")
    public abstract LiveData<Survey> findByUUID(String uuid);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE UUID=:uuid")
    public abstract LiveData<SurveyRelation> findSurveyRelationByUUID(String uuid);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId ORDER BY LastUpdated DESC")
    public abstract LiveData<List<ProjectSurveyRelation>> projectSurveys(Long projectId);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId")
    public abstract List<ProjectSurveyRelation> projectSurveysSync(Long projectId);

    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId AND Complete=1")
    public abstract List<Survey> projectCompletedSurveys(Long projectId);

    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId AND Complete=0")
    public abstract List<Survey> projectIncompleteSurveys(Long projectId);

}
