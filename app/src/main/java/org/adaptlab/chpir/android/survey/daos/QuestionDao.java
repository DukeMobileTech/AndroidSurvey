package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;

import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.List;

@Dao
public abstract class QuestionDao extends BaseDao<Question> {
    @Query("SELECT * FROM Questions WHERE InstrumentRemoteId=:instrumentId AND Deleted=0 ORDER BY NumberInInstrument ASC")
    public abstract List<Question> instrumentQuestionsSync(Long instrumentId);

    @Query("SELECT * FROM Questions WHERE QuestionIdentifier=:identifier LIMIT 1")
    public abstract Question findByQuestionIdentifierSync(String identifier);

    @Query("SELECT * FROM Questions WHERE RemoteId=:id LIMIT 1")
    public abstract Question findByIdSync(Long id);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM Questions INNER JOIN Displays ON Displays.RemoteId=Questions.DisplayId " +
            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
    public abstract LiveData<List<Question>> displayQuestions(Long instrumentId, Long displayId);

}
