package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Section;

import java.util.List;

@Dao
public abstract class SectionDao extends BaseDao<Section> {
    @Query("SELECT * FROM Sections WHERE InstrumentRemoteId=:instrumentId AND Deleted=0")
    public abstract LiveData<List<Section>> instrumentSections(Long instrumentId);

}
