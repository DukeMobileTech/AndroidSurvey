package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;

import java.util.List;

@Dao
public abstract class DisplayTranslationDao extends BaseDao<DisplayTranslation> {
    @Query("SELECT * from DisplayTranslations ORDER BY Text ASC")
    public abstract LiveData<List<DisplayTranslation>> getAllDisplayTranslations();

}
