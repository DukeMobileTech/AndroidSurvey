package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.List;

@Dao
public abstract class InstrumentDao extends BaseDao<Instrument> {
    @Query("SELECT * FROM Instruments WHERE RemoteId=:id LIMIT 1")
    public abstract LiveData<Instrument> findById(Long id);

    @Query("SELECT * FROM Instruments WHERE RemoteId=:id LIMIT 1")
    public abstract Instrument findByIdSync(Long id);

    @Query("SELECT * FROM Instruments WHERE ProjectId=:projectId AND Published=1 ORDER BY Title ASC")
    public abstract List<Instrument> projectInstrumentsSync(Long projectId);

    @Query("SELECT * FROM Instruments WHERE ProjectId=:projectId AND Published=1 ORDER BY Title ASC")
    public abstract LiveData<List<Instrument>> projectInstruments(Long projectId);

    @Query("SELECT * FROM Instruments ORDER BY Title ASC")
    public abstract LiveData<List<Instrument>> getAllInstruments();

    @Query("SELECT * FROM InstrumentTranslations WHERE InstrumentRemoteId=:instrumentId")
    public abstract LiveData<List<InstrumentTranslation>> instrumentTranslations(Long instrumentId);

    @Query("SELECT * FROM Questions WHERE InstrumentRemoteId=:instrumentId")
    public abstract LiveData<List<Question>> questions(Long instrumentId);

}
