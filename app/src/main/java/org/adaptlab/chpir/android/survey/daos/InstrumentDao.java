package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;

import java.util.List;

@Dao
public abstract class InstrumentDao extends BaseDao<Instrument> {
    @Query("SELECT * FROM Instruments WHERE ProjectId=:projectId AND Published=1 AND Deleted=0 ORDER BY Title ASC")
    public abstract List<Instrument> projectInstrumentsSync(Long projectId);

    @Query("SELECT * FROM Instruments WHERE ProjectId=:projectId AND Published=1 AND Deleted=0 ORDER BY Title ASC")
    public abstract LiveData<List<Instrument>> projectInstruments(Long projectId);

    @Query("SELECT * FROM Questions WHERE InstrumentRemoteId=:instrumentId AND Deleted=0")
    public abstract LiveData<List<Question>> questions(Long instrumentId);

    @Transaction
    @Query("SELECT * FROM Instruments WHERE RemoteId=:id")
    public abstract LiveData<InstrumentRelation> findInstrumentRelationById(Long id);

    @Transaction
    @Query("SELECT * FROM Instruments WHERE RemoteId=:id")
    public abstract InstrumentRelation findInstrumentRelationByIdSync(Long id);

}
