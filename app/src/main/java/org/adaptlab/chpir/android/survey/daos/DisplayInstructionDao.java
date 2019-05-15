package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;

import java.util.List;

@Dao
public abstract class DisplayInstructionDao extends BaseDao<DisplayInstruction> {

    @Query("SELECT DisplayInstructions.* FROM DisplayInstructions INNER JOIN Instruments ON Instruments.RemoteId=:instrumentId WHERE DisplayRemoteId=:displayId AND DisplayInstructions.Deleted=0 ORDER BY Position ASC")
    public abstract LiveData<List<DisplayInstruction>> displayInstructions(long instrumentId, long displayId);

}
