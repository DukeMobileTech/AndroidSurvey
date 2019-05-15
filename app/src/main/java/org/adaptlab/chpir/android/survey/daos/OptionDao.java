package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Option;

import java.util.List;

@Dao
public abstract class OptionDao extends BaseDao<Option> {
    @Query("SELECT * FROM Options WHERE Deleted=0")
    public abstract LiveData<List<Option>> getAllOptions();

}
