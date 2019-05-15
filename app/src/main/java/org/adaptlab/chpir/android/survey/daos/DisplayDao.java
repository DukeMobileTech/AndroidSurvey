package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Display;

import java.util.List;

@Dao
public abstract class DisplayDao extends BaseDao<Display> {
    @Query("SELECT * FROM Displays WHERE InstrumentRemoteId=:instrumentId AND Deleted=0 ORDER BY Position ASC")
    public abstract List<Display> instrumentDisplaysSync(Long instrumentId);

    @Query("SELECT * FROM Displays WHERE InstrumentRemoteId=:instrumentId AND Deleted=0 ORDER BY Position ASC")
    public abstract LiveData<List<Display>> instrumentDisplays(Long instrumentId);

    @Query("SELECT * FROM Displays WHERE RemoteId=:id LIMIT 1")
    public abstract Display findByIdSync(Long id);

    @Query("SELECT * FROM Displays WHERE Title=:title AND InstrumentRemoteId=:instrumentId LIMIT 1")
    public abstract Display findByTitleAndInstrumentIdSync(String title, Long instrumentId);

}
