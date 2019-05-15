package org.adaptlab.chpir.android.survey.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import org.adaptlab.chpir.android.survey.entities.Instruction;

import java.util.List;

@Dao
public abstract class InstructionDao extends BaseDao<Instruction> {

    @Query("SELECT * FROM Instructions")
    public abstract LiveData<List<Instruction>> getAllInstructions();

}
