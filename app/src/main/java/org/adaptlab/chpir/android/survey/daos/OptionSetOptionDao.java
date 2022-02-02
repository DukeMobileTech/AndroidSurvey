package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.OptionSetOption;

import java.util.List;

@Dao
public abstract class OptionSetOptionDao extends BaseDao<OptionSetOption> {
    @Query("SELECT * FROM OptionSetOptions")
    public abstract LiveData<List<OptionSetOption>> getAllOptionSetOptions();

    @Query("SELECT * FROM OptionSetOptions WHERE HasImage=1")
    public abstract List<OptionSetOption> withImages();
}
