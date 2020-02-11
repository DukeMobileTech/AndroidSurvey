package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.relations.InstructionRelation;

@Dao
public abstract class InstructionDao extends BaseDao<Instruction> {
    @Transaction
    @Query("SELECT * FROM Instructions WHERE RemoteId=:instructionId")
    public abstract InstructionRelation instructionSync(Long instructionId);

}
