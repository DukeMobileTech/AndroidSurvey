package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.ConditionSkip;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Survey;

@Dao
public abstract class SurveyDao extends BaseDao<Survey> {
    @Query("SELECT * FROM Surveys WHERE UUID=:uuid LIMIT 1")
    public abstract LiveData<Survey> findByUUID(String uuid);

}
