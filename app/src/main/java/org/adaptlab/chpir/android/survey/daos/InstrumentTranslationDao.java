package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;

import java.util.List;

@Dao
public abstract class InstrumentTranslationDao extends BaseDao<InstrumentTranslation> {
    @Query("SELECT * from InstrumentTranslations ORDER BY Title ASC")
    public abstract LiveData<List<InstrumentTranslation>> getAllInstrumentTranslations();

}
