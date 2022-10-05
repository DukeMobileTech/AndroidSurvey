package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Collage;

import java.util.List;

@Dao
public abstract class CollageDao extends BaseDao<Collage> {
    @Query("SELECT * FROM Collages")
    public abstract LiveData<List<Collage>> getAllCollages();

}
