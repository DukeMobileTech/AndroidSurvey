package org.adaptlab.chpir.android.survey.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.MultipleSkip;

import java.util.List;

@Dao
public abstract class MultipleSkipDao extends BaseDao<MultipleSkip> {
    @Query("SELECT * FROM MultipleSkips WHERE QuestionIdentifier=:identifier AND InstrumentRemoteId=:instrumentId AND Deleted=0")
    public abstract List<MultipleSkip> questionMultipleSkipsSync(String identifier, Long instrumentId);

    @Query("SELECT * FROM MultipleSkips WHERE SkipQuestionIdentifier=:identifier AND InstrumentRemoteId=:instrumentId AND Deleted=0")
    public abstract List<MultipleSkip> skipsQuestionMultipleSkipsSync(String identifier, Long instrumentId);

    @Query("SELECT * FROM MultipleSkips WHERE QuestionIdentifier=:qIdentifier AND OptionIdentifier=:oIdentifier AND SkipQuestionIdentifier=:sqIdentifier AND Value=:value LIMIT 1")
    public abstract MultipleSkip findByAttributesSync(String qIdentifier, String oIdentifier, String sqIdentifier, String value);

}
