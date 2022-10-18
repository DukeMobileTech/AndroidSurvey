package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.OptionCollage;

import java.util.List;

@Dao
public abstract class OptionCollageDao extends BaseDao<OptionCollage> {
    @Query("SELECT * FROM OptionCollages")
    public abstract LiveData<List<OptionCollage>> getOptionCollages();
}
