package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Diagram;

import java.util.List;

@Dao
public abstract class DiagramDao extends BaseDao<Diagram> {
    @Query("SELECT * FROM Diagrams")
    public abstract LiveData<List<Diagram>> getAllDiagrams();

}
