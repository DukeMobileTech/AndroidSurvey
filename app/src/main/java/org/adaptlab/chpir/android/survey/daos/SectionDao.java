package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.relations.SectionRelation;

import java.util.List;

@Dao
public abstract class SectionDao extends BaseDao<Section> {
    @Query("SELECT * FROM Sections WHERE InstrumentRemoteId=:instrumentId AND Deleted=0 ORDER BY Position ASC")
    public abstract LiveData<List<SectionRelation>> instrumentSections(Long instrumentId);

}
