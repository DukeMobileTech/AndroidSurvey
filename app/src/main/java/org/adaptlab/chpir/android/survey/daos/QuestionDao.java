package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.List;

@Dao
public abstract class QuestionDao extends BaseDao<Question> {
    @Query("SELECT * from Questions ORDER BY NumberInInstrument ASC")
    public abstract LiveData<List<Question>> getAllQuestions();

}
