package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.OptionSetOption;

import java.util.List;

@Dao
public abstract class OptionSetOptionDao extends BaseDao<OptionSetOption> {
    @Query("SELECT * FROM OptionSetOptions")
    public abstract LiveData<List<OptionSetOption>> getAllOptionSetOptions();
}
