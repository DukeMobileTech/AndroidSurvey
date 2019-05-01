package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Display;

import java.util.List;

@Dao
public abstract class DisplayDao extends BaseDao<Display> {
    @Query("SELECT * from Displays ORDER BY Title ASC")
    public abstract LiveData<List<Display>> getAllDisplays();

}
