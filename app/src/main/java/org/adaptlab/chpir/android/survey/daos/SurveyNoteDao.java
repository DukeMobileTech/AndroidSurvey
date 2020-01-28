package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.SurveyNote;

import java.util.List;

@Dao
public abstract class SurveyNoteDao extends BaseDao<SurveyNote> {

    @Transaction
    @Query("SELECT SurveyNotes.* FROM SurveyNotes WHERE SurveyNotes.SurveyUUID=:uuid")
    public abstract LiveData<List<SurveyNote>> surveyNotes(String uuid);

}
