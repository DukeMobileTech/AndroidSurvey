package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.LoopQuestion;

import java.util.List;

@Dao
public abstract class LoopQuestionDao extends BaseDao<LoopQuestion> {
    @Query("SELECT * FROM LoopQuestions WHERE QuestionRemoteId=:questionId")
    public abstract List<LoopQuestion> allLoopQuestionsSync(Long questionId);

    @Query("SELECT * FROM LoopQuestions WHERE QuestionRemoteId=:questionId AND Deleted=0")
    public abstract List<LoopQuestion> loopQuestionsSync(Long questionId);

}
