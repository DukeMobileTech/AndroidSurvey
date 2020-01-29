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
    @Query("SELECT * FROM Surveys WHERE (ProjectId=:projectId AND SentToRemote=0 AND Queued=0) ORDER BY LastUpdated DESC")
    public abstract LiveData<List<ProjectSurveyRelation>> onGoingProjectSurveys(Long projectId);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE (ProjectId=:projectId AND SentToRemote=1) OR " +
            "(ProjectId=:projectId AND Queued=1)  ORDER BY LastUpdated DESC")
    public abstract LiveData<List<ProjectSurveyRelation>> submittedProjectSurveys(Long projectId);

    @Transaction
    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId AND Queued=1")
    public abstract List<ProjectSurveyRelation> queuedProjectSurveys(Long projectId);

    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId AND Complete=1")
    public abstract List<Survey> projectCompletedSurveys(Long projectId);

    @Query("SELECT * FROM Surveys WHERE ProjectId=:projectId AND Complete=0")
    public abstract List<Survey> projectIncompleteSurveys(Long projectId);

}
