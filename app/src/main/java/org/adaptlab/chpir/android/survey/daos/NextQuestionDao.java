package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.NextQuestion;

import java.util.List;

@Dao
public abstract class NextQuestionDao extends BaseDao<NextQuestion> {
    @Query("SELECT * FROM NextQuestions WHERE QuestionIdentifier=:identifier AND InstrumentRemoteId=:instrumentId")
    public abstract List<NextQuestion> questionNextQuestionsSync(String identifier, Long instrumentId);

    @Query("SELECT * FROM NextQuestions WHERE QuestionIdentifier=:qIdentifier AND OptionIdentifier=:oIdentifier AND NextQuestionIdentifier=:nqIdentifier AND Value=:value LIMIT 1")
    public abstract NextQuestion findByAttributesSync(String qIdentifier, String oIdentifier, String nqIdentifier, String value);

}
