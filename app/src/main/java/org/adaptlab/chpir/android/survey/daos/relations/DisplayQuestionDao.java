package org.adaptlab.chpir.android.survey.daos.relations;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.relations.DisplayQuestion;

@Dao
public abstract class DisplayQuestionDao {
    @Transaction
    @Query("SELECT * FROM Displays WHERE RemoteId=:id AND InstrumentRemoteId=:instrumentId LIMIT 1")
    public abstract LiveData<DisplayQuestion> findById(Long instrumentId, Long id);

    @Transaction
    @Query("SELECT * FROM Displays WHERE Position=:position AND InstrumentRemoteId=:instrumentId LIMIT 1")
    public abstract LiveData<DisplayQuestion> findByPosition(Long instrumentId, int position);

}
