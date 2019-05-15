package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.OptionSet;

import java.util.List;

@Dao
public abstract class OptionSetDao extends BaseDao<OptionSet> {
    @Query("SELECT * FROM OptionSets")
    public abstract LiveData<List<OptionSet>> getAllOptionSets();

}
