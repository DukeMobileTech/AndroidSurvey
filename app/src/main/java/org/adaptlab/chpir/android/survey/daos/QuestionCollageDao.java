package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.QuestionCollage;

import java.util.List;

@Dao
public abstract class QuestionCollageDao extends BaseDao<QuestionCollage> {
    @Query("SELECT * FROM QuestionCollages")
    public abstract LiveData<List<QuestionCollage>> getQuestionCollages();
}
